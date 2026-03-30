package com.example.taskcatalog.mapper

import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.model.Task

fun Task.toResponse(): TaskResponse = TaskResponse(
    id = id,
    title = title,
    description = description,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)
