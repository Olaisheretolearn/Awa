package com.summerlockin.Awa.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.summerlockin.Awa.exception.ApiErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class RestAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {
    override fun handle(request: HttpServletRequest, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = "application/json"
        objectMapper.writeValue(
            response.outputStream,
            ApiErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                code = "FORBIDDEN",
                message = "You do not have permission to access this resource.",
                requestId = request.getAttribute("requestId")?.toString()
            )
        )
    }
}