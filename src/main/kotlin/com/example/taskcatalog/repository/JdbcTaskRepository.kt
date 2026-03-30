package com.example.taskcatalog.repository

import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime

@Repository
class JdbcTaskRepository(
    private val jdbcClient: JdbcClient
) : TaskRepository {

    private val taskRowMapper = RowMapper<Task> { rs: ResultSet, _: Int ->
        Task(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            description = rs.getString("description"),
            status = TaskStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }

    override fun save(
        title: String,
        description: String?,
        status: TaskStatus,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime
    ): Task {
        val keyHolder = GeneratedKeyHolder()

        jdbcClient.sql(
            """
            insert into tasks (title, description, status, created_at, updated_at)
            values (:title, :description, :status, :createdAt, :updatedAt)
            """.trimIndent()
        )
            .param("title", title)
            .param("description", description)
            .param("status", status.name)
            .param("createdAt", createdAt)
            .param("updatedAt", updatedAt)
            .update(keyHolder, "id")

        val id = keyHolder.key?.toLong() ?: error("Failed to retrieve generated task id")
        return findById(id) ?: error("Task with generated id=$id was not found after insert")
    }

    override fun findById(id: Long): Task? = jdbcClient.sql(
        """
        select id, title, description, status, created_at, updated_at
        from tasks
        where id = :id
        """.trimIndent()
    )
        .param("id", id)
        .query(taskRowMapper)
        .optional()
        .orElse(null)

    override fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task> {
        val offset = page * size
        val hasStatusFilter = status != null
        val sql = if (hasStatusFilter) {
            """
            select id, title, description, status, created_at, updated_at
            from tasks
            where status = :status
            order by created_at desc
            limit :limit offset :offset
            """.trimIndent()
        } else {
            """
            select id, title, description, status, created_at, updated_at
            from tasks
            order by created_at desc
            limit :limit offset :offset
            """.trimIndent()
        }

        var statement = jdbcClient.sql(sql)
            .param("limit", size)
            .param("offset", offset)

        if (hasStatusFilter) {
            statement = statement.param("status", status!!.name)
        }

        return statement.query(taskRowMapper).list()
    }

    override fun count(status: TaskStatus?): Long {
        val hasStatusFilter = status != null
        val sql = if (hasStatusFilter) {
            "select count(*) from tasks where status = :status"
        } else {
            "select count(*) from tasks"
        }

        var statement = jdbcClient.sql(sql)
        if (hasStatusFilter) {
            statement = statement.param("status", status!!.name)
        }

        return statement.query(Long::class.java).single()
    }

    override fun updateStatus(id: Long, status: TaskStatus, updatedAt: LocalDateTime): Boolean {
        val updatedRows = jdbcClient.sql(
            """
            update tasks
            set status = :status,
                updated_at = :updatedAt
            where id = :id
            """.trimIndent()
        )
            .param("status", status.name)
            .param("updatedAt", updatedAt)
            .param("id", id)
            .update()

        return updatedRows > 0
    }

    override fun deleteById(id: Long): Boolean {
        val deletedRows = jdbcClient.sql("delete from tasks where id = :id")
            .param("id", id)
            .update()
        return deletedRows > 0
    }
}
