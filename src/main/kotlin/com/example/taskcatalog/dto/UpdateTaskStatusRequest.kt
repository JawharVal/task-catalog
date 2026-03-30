package com.example.taskcatalog.dto

import com.example.taskcatalog.model.TaskStatus
import jakarta.validation.constraints.NotNull

data class UpdateTaskStatusRequest(
    @field:NotNull(message = "status must not be null")
    val status: TaskStatus
)
