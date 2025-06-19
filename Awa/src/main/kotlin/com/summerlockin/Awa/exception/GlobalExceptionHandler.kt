package com.summerlockin.Awa.exception



import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Resource not found"),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(AlreadyExistsException::class)
    fun handleAlreadyExists(ex: AlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Already exists"),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("Something went wrong: ${ex.message}"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}

data class ErrorResponse(
    val message: String
)
