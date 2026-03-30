package com.example.taskcatalog.service

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.PageResponse
import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.mapper.toResponse
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.repository.TaskRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime

@Service
class TaskService(
    private val taskRepository: TaskRepository
) {

    fun createTask(request: CreateTaskRequest): Mono<TaskResponse> = Mono.fromCallable {
        val now = LocalDateTime.now()
        taskRepository.save(
            title = request.title.trim(),
            description = request.description?.trim()?.takeIf { it.isNotEmpty() },
            status = TaskStatus.NEW,
            createdAt = now,
            updatedAt = now
        ).toResponse()
    }.subscribeOn(Schedulers.boundedElastic())

    fun getTaskById(id: Long): Mono<TaskResponse> = Mono.fromCallable {
        taskRepository.findById(id)?.toResponse() ?: throw TaskNotFoundException(id)
    }.subscribeOn(Schedulers.boundedElastic())

    fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>> = Mono.fromCallable {
        val tasks = taskRepository.findAll(page, size, status).map { it.toResponse() }
        val totalElements = taskRepository.count(status)
        val totalPages = if (totalElements == 0L) 0 else ((totalElements + size - 1) / size).toInt()

        PageResponse(
            content = tasks,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages
        )
    }.subscribeOn(Schedulers.boundedElastic())

    fun updateStatus(id: Long, request: UpdateTaskStatusRequest): Mono<TaskResponse> = Mono.fromCallable {
        val updated = taskRepository.updateStatus(id, request.status, LocalDateTime.now())
        if (!updated) {
            throw TaskNotFoundException(id)
        }

        taskRepository.findById(id)?.toResponse() ?: throw TaskNotFoundException(id)
    }.subscribeOn(Schedulers.boundedElastic())

    fun deleteTask(id: Long): Mono<Void> = Mono.fromRunnable<Void> {
        val deleted = taskRepository.deleteById(id)
        if (!deleted) {
            throw TaskNotFoundException(id)
        }
    }.subscribeOn(Schedulers.boundedElastic()).then()
}
