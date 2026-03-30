package com.example.taskcatalog.controller

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.PageResponse
import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.exception.GlobalExceptionHandler
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.service.TaskService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@WebFluxTest(TaskController::class)
@Import(GlobalExceptionHandler::class)
class TaskControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var taskService: TaskService

    @Test
    fun `should return 201 on create`() {
        val response = taskResponse()
        Mockito.`when`(taskService.createTask(CreateTaskRequest("Prepare report", "Monthly financial report")))
            .thenReturn(Mono.just(response))

        webTestClient.post()
            .uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "title": "Prepare report",
                  "description": "Monthly financial report"
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.status").isEqualTo("NEW")
    }

    @Test
    fun `should validate create request`() {
        webTestClient.post()
            .uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "title": ""
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Validation failed")
            .jsonPath("$.details[0]").exists()
    }

    @Test
    fun `should return page of tasks`() {
        val pageResponse = PageResponse(
            content = listOf(taskResponse()),
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1
        )
        Mockito.`when`(taskService.getTasks(0, 10, TaskStatus.NEW)).thenReturn(Mono.just(pageResponse))

        webTestClient.get()
            .uri("/api/tasks?page=0&size=10&status=NEW")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(1)
            .jsonPath("$.totalElements").isEqualTo(1)
    }

    @Test
    fun `should return 404 when task is missing`() {
        Mockito.`when`(taskService.getTaskById(99L)).thenReturn(Mono.error(TaskNotFoundException(99L)))

        webTestClient.get()
            .uri("/api/tasks/99")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.message").isEqualTo("Task with id=99 not found")
    }

    @Test
    fun `should return 204 on delete`() {
        Mockito.`when`(taskService.deleteTask(1L)).thenReturn(Mono.empty())

        webTestClient.delete()
            .uri("/api/tasks/1")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `should validate required pagination params`() {
        webTestClient.get()
            .uri("/api/tasks")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should patch status`() {
        val response = taskResponse(status = TaskStatus.DONE, updatedAt = LocalDateTime.of(2026, 3, 26, 13, 0))
        Mockito.`when`(taskService.updateStatus(1L, UpdateTaskStatusRequest(TaskStatus.DONE)))
            .thenReturn(Mono.just(response))

        webTestClient.patch()
            .uri("/api/tasks/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"status\":\"DONE\"}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("DONE")
    }

    private fun taskResponse(
        id: Long = 1L,
        title: String = "Prepare report",
        description: String? = "Monthly financial report",
        status: TaskStatus = TaskStatus.NEW,
        createdAt: LocalDateTime = LocalDateTime.of(2026, 3, 26, 12, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2026, 3, 26, 12, 0)
    ) = TaskResponse(
        id = id,
        title = title,
        description = description,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
