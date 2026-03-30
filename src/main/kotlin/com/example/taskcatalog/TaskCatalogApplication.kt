package com.example.taskcatalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaskCatalogApplication

fun main(args: Array<String>) {
    runApplication<TaskCatalogApplication>(*args)
}
