package com.example.taskcatalog.exception

class TaskNotFoundException(id: Long) : RuntimeException("Task with id=$id not found")
