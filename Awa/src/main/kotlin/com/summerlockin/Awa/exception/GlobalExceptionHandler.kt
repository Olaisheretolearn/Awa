package com.summerlockin.Awa.exception

import com.mongodb.DuplicateKeyException
import com.mongodb.MongoSocketWriteException
import org.springframework.dao.DuplicateKeyException as SpringDuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.net.ssl.SSLException


@ControllerAdvice
class GlobalExceptionHandler {


    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException) =
        err(HttpStatus.NOT_FOUND, code = "NOT_FOUND", msg = ex.message ?: "Resource not found")

    @ExceptionHandler(AlreadyExistsException::class)
    fun handleAlreadyExists(ex: AlreadyExistsException) =
        err(HttpStatus.CONFLICT, code = "ALREADY_EXISTS", msg = ex.message ?: "Already exists")

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException) =
        err(HttpStatus.UNAUTHORIZED, code = "UNAUTHORIZED", msg = ex.message ?: "Unauthorized")

    // --- Access / method issues ---
    @ExceptionHandler(AccessDeniedException::class)
    fun handleForbidden(ex: AccessDeniedException) =
        err(HttpStatus.FORBIDDEN, code = "FORBIDDEN", msg = "You don’t have permission.")

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException) =
        err(HttpStatus.METHOD_NOT_ALLOWED, code = "METHOD_NOT_ALLOWED", msg = "Method not allowed")

    // --- Validation / bad input ---
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBeanValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val first = ex.bindingResult.fieldErrors.firstOrNull()
        val human = first?.let { "${it.field}: ${it.defaultMessage}" } ?: "Validation failed"
        return err(HttpStatus.UNPROCESSABLE_ENTITY, code = "VALIDATION_ERROR", msg = human)
    }


    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException) =
        err(HttpStatus.BAD_REQUEST, code = "BAD_REQUEST", msg = "Missing parameter: ${ex.parameterName}")

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException) =
        err(HttpStatus.BAD_REQUEST, code = "BAD_REQUEST", msg = "Malformed request body")


    @ExceptionHandler(DuplicateKeyException::class, SpringDuplicateKeyException::class)
    fun handleDuplicateKey(@Suppress("UNUSED_PARAMETER") ex: Exception) =
        err(HttpStatus.CONFLICT, code = "DUPLICATE_KEY", msg = "Duplicate key")

    @ExceptionHandler(MongoSocketWriteException::class, SSLException::class)
    fun handleDbNetwork(@Suppress("UNUSED_PARAMETER") ex: Exception) =
        err(HttpStatus.SERVICE_UNAVAILABLE, code = "DB_UNAVAILABLE", msg = "Database is unavailable.")


    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception) =
        err(HttpStatus.INTERNAL_SERVER_ERROR, code = "SERVER_ERROR", msg = ex.message ?: "Something went wrong")


    private fun err(status: HttpStatus, code: String, msg: String): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status).body(ErrorResponse(code = code, message = msg))
}


data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: String = java.time.Instant.now().toString()
)
