package com.summerlockin.Awa.exception

import com.mongodb.DuplicateKeyException
import com.mongodb.MongoSocketWriteException
import com.summerlockin.Awa.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.dao.DuplicateKeyException as SpringDuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import jakarta.servlet.http.HttpServletRequest
import javax.net.ssl.SSLException


@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)


    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException, request: HttpServletRequest) =
        err(HttpStatus.NOT_FOUND, code = "NOT_FOUND", msg = ex.message ?: "Resource not found", request, ex)

    @ExceptionHandler(AlreadyExistsException::class)
    fun handleAlreadyExists(ex: AlreadyExistsException, request: HttpServletRequest) =
        err(HttpStatus.CONFLICT, code = "CONFLICT", msg = ex.message ?: "Resource already exists", request, ex)

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException, request: HttpServletRequest) =
        err(HttpStatus.UNAUTHORIZED, code = "UNAUTHORIZED", msg = ex.message ?: "Authentication is required.", request, ex)

    // --- Access / method issues ---
    @ExceptionHandler(AccessDeniedException::class)
    fun handleForbidden(ex: AccessDeniedException, request: HttpServletRequest) =
        err(HttpStatus.FORBIDDEN, code = "FORBIDDEN", msg = "You do not have permission to access this resource.", request, ex)

    @ExceptionHandler(org.springframework.security.core.AuthenticationException::class)
    fun handleAuthenticationException(ex: org.springframework.security.core.AuthenticationException, request: HttpServletRequest) =
        err(HttpStatus.UNAUTHORIZED, code = "UNAUTHORIZED", msg = "Authentication is required.", request, ex)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException, request: HttpServletRequest) =
        err(HttpStatus.METHOD_NOT_ALLOWED, code = "METHOD_NOT_ALLOWED", msg = "Method not allowed", request, ex)

    // --- Validation / bad input ---
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBeanValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ApiErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map {
            FieldErrorResponse(field = it.field, message = it.defaultMessage ?: "Invalid value")
        }
        return err(
            HttpStatus.BAD_REQUEST,
            code = "VALIDATION_ERROR",
            msg = "Validation failed.",
            request = request,
            ex = ex,
            errors = errors
        )
    }


    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException, request: HttpServletRequest) =
        err(HttpStatus.BAD_REQUEST, code = "BAD_REQUEST", msg = "Missing parameter.", request, ex)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException, request: HttpServletRequest) =
        err(HttpStatus.BAD_REQUEST, code = "BAD_REQUEST", msg = "Malformed request body.", request, ex)

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: org.springframework.web.method.annotation.MethodArgumentTypeMismatchException, request: HttpServletRequest) =
        err(HttpStatus.BAD_REQUEST, code = "BAD_REQUEST", msg = "Invalid parameter.", request, ex)


    @ExceptionHandler(DuplicateKeyException::class, SpringDuplicateKeyException::class)
    fun handleDuplicateKey(ex: Exception, request: HttpServletRequest) =
        err(HttpStatus.CONFLICT, code = "CONFLICT", msg = "Resource already exists.", request, ex)

    @ExceptionHandler(MongoSocketWriteException::class, SSLException::class)
    fun handleDbNetwork(ex: Exception, request: HttpServletRequest) =
        err(HttpStatus.SERVICE_UNAVAILABLE, code = "SERVER_ERROR", msg = "An unexpected error occurred.", request, ex)

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception, request: HttpServletRequest) =
        err(HttpStatus.INTERNAL_SERVER_ERROR, code = "SERVER_ERROR", msg = "An unexpected error occurred.", request, ex)


    private fun err(
        status: HttpStatus,
        code: String,
        msg: String,
        request: HttpServletRequest,
        ex: Exception,
        errors: List<FieldErrorResponse> = emptyList()
    ): ResponseEntity<ApiErrorResponse> {
        val requestId = MDC.get("requestId") ?: request.getAttribute("requestId")?.toString()
        val userId = (SecurityContextHolder.getContext().authentication?.principal as? UserPrincipal)?.getId()
        logger.error(
            "requestId={} userId={} method={} uri={} status={} code={}",
            requestId,
            userId,
            request.method,
            request.requestURI,
            status.value(),
            code,
            ex
        )
        return ResponseEntity.status(status).body(
            ApiErrorResponse(
                status = status.value(),
                code = code,
                message = msg,
                requestId = requestId,
                errors = errors
            )
        )
    }
}
