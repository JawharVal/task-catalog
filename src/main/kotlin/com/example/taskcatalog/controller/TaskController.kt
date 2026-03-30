package com.example.taskcatalog.controller

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.PageResponse
import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.service.TaskService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Validated
@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    fun createTask(@Valid @RequestBody request: CreateTaskRequest): Mono<ResponseEntity<TaskResponse>> =
        taskService.createTask(request)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    fun getTasks(
        @RequestParam page: @Min(0, message = "page must be greater than or equal to 0") Int,
        @RequestParam size: @Min(1, message = "size must be greater than or equal to 1") Int,
        @RequestParam(required = false) status: TaskStatus?
    ): Mono<PageResponse<TaskResponse>> = taskService.getTasks(page, size, status)

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: Long): Mono<TaskResponse> = taskService.getTaskById(id)

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTaskStatusRequest
    ): Mono<TaskResponse> = taskService.updateStatus(id, request)

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): Mono<ResponseEntity<Void>> = taskService.deleteTask(id)
        .thenReturn(ResponseEntity.noContent().build())
}
