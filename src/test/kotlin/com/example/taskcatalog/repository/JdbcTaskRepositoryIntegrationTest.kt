package com.example.taskcatalog.repository

import com.example.taskcatalog.TaskCatalogApplication
import com.example.taskcatalog.model.TaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import java.time.LocalDateTime

@SpringBootTest(classes = [TaskCatalogApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
class JdbcTaskRepositoryIntegrationTest {

    @Autowired
    private lateinit var repository: JdbcTaskRepository

    @Autowired
    private lateinit var jdbcClient: JdbcClient

    @BeforeEach
    fun cleanDb() {
        jdbcClient.sql("delete from tasks").update()
    }

    @Test
    fun `should save and find task`() {
        val now = LocalDateTime.of(2026, 3, 26, 12, 0)
        val created = repository.save("Prepare report", "Monthly financial report", TaskStatus.NEW, now, now)

        val found = repository.findById(created.id)

        assertNotNull(found)
        assertEquals("Prepare report", found?.title)
        assertEquals(TaskStatus.NEW, found?.status)
    }

    @Test
    fun `should filter and paginate`() {
        val now = LocalDateTime.of(2026, 3, 26, 12, 0)
        repository.save("Task 1", null, TaskStatus.NEW, now.minusHours(2), now.minusHours(2))
        repository.save("Task 2", null, TaskStatus.NEW, now.minusHours(1), now.minusHours(1))
        repository.save("Task 3", null, TaskStatus.DONE, now, now)

        val tasks = repository.findAll(page = 0, size = 2, status = TaskStatus.NEW)
        val total = repository.count(TaskStatus.NEW)

        assertEquals(2, tasks.size)
        assertEquals(2L, total)
        assertEquals("Task 2", tasks.first().title)
    }

    @Test
    fun `should update and delete task`() {
        val now = LocalDateTime.of(2026, 3, 26, 12, 0)
        val created = repository.save("Task", null, TaskStatus.NEW, now, now)

        val updated = repository.updateStatus(created.id, TaskStatus.DONE, now.plusHours(1))
        val found = repository.findById(created.id)
        val deleted = repository.deleteById(created.id)
        val afterDelete = repository.findById(created.id)

        assertTrue(updated)
        assertEquals(TaskStatus.DONE, found?.status)
        assertTrue(deleted)
        assertEquals(null, afterDelete)
    }
}
