package com.example.taskcatalog.exception

import com.example.taskcatalog.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFound(ex: TaskNotFoundException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> =
        buildErrorResponse(
            status = HttpStatus.NOT_FOUND,
            message = ex.message ?: "Task not found",
            path = exchange.request.path.value()
        )

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(
        ex: WebExchangeBindException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.allErrors.mapNotNull { error ->
            when (error) {
                is FieldError -> "${error.field}: ${error.defaultMessage ?: "invalid value"}"
                else -> error.defaultMessage
            }
        }

        return buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            path = exchange.request.path.value(),
            details = details
        )
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerMethodValidationException(
        ex: HandlerMethodValidationException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        val details = ex.parameterValidationResults.map { it.toString() }

        return buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            path = exchange.request.path.value(),
            details = details
        )
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInputException(
        ex: ServerWebInputException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> = buildErrorResponse(
        status = HttpStatus.BAD_REQUEST,
        message = ex.reason ?: "Invalid request",
        path = exchange.request.path.value()
    )

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> =
        buildErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = ex.message ?: "Unexpected error",
            path = exchange.request.path.value()
        )

    private fun buildErrorResponse(
        status: HttpStatusCode,
        message: String,
        path: String,
        details: List<String> = emptyList()
    ): ResponseEntity<ErrorResponse> = ResponseEntity.status(status).body(
        ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = HttpStatus.valueOf(status.value()).reasonPhrase,
            message = message,
            path = path,
            details = details
        )
    )
}
