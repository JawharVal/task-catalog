package com.example.taskcatalog.service

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.repository.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import reactor.test.StepVerifier
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    private lateinit var taskService: TaskService

    private val now: LocalDateTime = LocalDateTime.of(2026, 3, 26, 12, 0)

    @BeforeEach
    fun setUp() {
        taskService = TaskService(taskRepository)
    }

    @Test
    fun `should create task successfully`() {
        val savedTask = sampleTask(
            title = "Prepare report",
            description = null,
            status = TaskStatus.NEW
        )

        given(
            taskRepository.save(
                eq("Prepare report"),
                isNull(),
                eq(TaskStatus.NEW),
                any(),
                any()
            )
        ).willReturn(savedTask)

        StepVerifier.create(
            taskService.createTask(
                CreateTaskRequest(
                    title = "Prepare report",
                    description = null
                )
            )
        )
            .assertNext {
                assertEquals(1L, it.id)
                assertEquals("Prepare report", it.title)
                assertNull(it.description)
                assertEquals(TaskStatus.NEW, it.status)
            }
            .verifyComplete()
    }

    @Test
    fun `should get task by id`() {
        given(taskRepository.findById(1L)).willReturn(sampleTask())

        StepVerifier.create(taskService.getTaskById(1L))
            .assertNext {
                assertEquals(1L, it.id)
                assertEquals("Prepare report", it.title)
                assertEquals("Monthly financial report", it.description)
                assertEquals(TaskStatus.NEW, it.status)
            }
            .verifyComplete()
    }

    @Test
    fun `should return error when task does not exist`() {
        given(taskRepository.findById(99L)).willReturn(null)

        StepVerifier.create(taskService.getTaskById(99L))
            .expectErrorSatisfies { throwable ->
                val ex = throwable as TaskNotFoundException
                assertEquals("Task with id=99 not found", ex.message)
            }
            .verify()
    }

    @Test
    fun `should update status`() {
        val updatedTask = sampleTask(
            status = TaskStatus.DONE,
            updatedAt = now.plusHours(1)
        )

        given(taskRepository.updateStatus(eq(1L), eq(TaskStatus.DONE), any()))
            .willReturn(true)
        given(taskRepository.findById(1L)).willReturn(updatedTask)

        StepVerifier.create(taskService.updateStatus(1L, UpdateTaskStatusRequest(TaskStatus.DONE)))
            .assertNext {
                assertEquals(1L, it.id)
                assertEquals(TaskStatus.DONE, it.status)
                assertEquals(now.plusHours(1), it.updatedAt)
            }
            .verifyComplete()
    }

    @Test
    fun `should delete task`() {
        given(taskRepository.deleteById(1L)).willReturn(true)

        StepVerifier.create(taskService.deleteTask(1L))
            .verifyComplete()
    }

    @Test
    fun `should get paged tasks with filter`() {
        val tasks = listOf(
            sampleTask(id = 2L, title = "Task 2", status = TaskStatus.NEW),
            sampleTask(id = 1L, title = "Task 1", status = TaskStatus.NEW)
        )

        given(taskRepository.findAll(0, 10, TaskStatus.NEW)).willReturn(tasks)
        given(taskRepository.count(TaskStatus.NEW)).willReturn(2L)

        StepVerifier.create(taskService.getTasks(0, 10, TaskStatus.NEW))
            .assertNext { page ->
                assertEquals(0, page.page)
                assertEquals(10, page.size)
                assertEquals(2L, page.totalElements)
                assertEquals(1, page.totalPages)
                assertEquals(2, page.content.size)
                for (task in page.content) {
                    assertEquals(TaskStatus.NEW, task.status)
                }
            }
            .verifyComplete()
    }

    private fun sampleTask(
        id: Long = 1L,
        title: String = "Prepare report",
        description: String? = "Monthly financial report",
        status: TaskStatus = TaskStatus.NEW,
        createdAt: LocalDateTime = now,
        updatedAt: LocalDateTime = now
    ): Task = Task(
        id = id,
        title = title,
        description = description,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}