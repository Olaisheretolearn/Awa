package com.summerlockin.Awa.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.summerlockin.Awa.exception.ApiErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class RestAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/json"
        objectMapper.writeValue(
            response.outputStream,
            ApiErrorResponse(
                status = HttpStatus.UNAUTHORIZED.value(),
                code = "UNAUTHORIZED",
                message = "Authentication is required.",
                requestId = request.getAttribute("requestId")?.toString()
            )
        )
    }
}