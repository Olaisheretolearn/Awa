package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.ChangePasswordRequest
import com.summerlockin.Awa.DTO.ForgotPasswordRequest
import com.summerlockin.Awa.DTO.ResetPasswordRequest
import com.summerlockin.Awa.exception.GlobalExceptionHandler
import com.summerlockin.Awa.model.User
import com.summerlockin.Awa.security.UserPrincipal
import com.summerlockin.Awa.service.PasswordResetService
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.PostMapping
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PasswordResetControllerTest {
    private val service = mock<PasswordResetService>()
    private val controller = PasswordResetController(service)
    private val mockMvc = MockMvcBuilders
        .standaloneSetup(controller)
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @Test
    fun `forgot password delegates and always returns the generic accepted response`() {
        val request = ForgotPasswordRequest("person@example.com")

        val response = controller.forgot(request)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertEquals(
            "If an account exists for that email, a password reset link has been sent.",
            response.body?.message
        )
        verify(service).requestReset(request)
    }

    @Test
    fun `reset password delegates and returns success`() {
        val request = ResetPasswordRequest("a".repeat(43), "a-different-secure-password")

        val response = controller.reset(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Password reset successfully.", response.body?.message)
        verify(service).resetPassword(request)
    }

    @Test
    fun `change password uses only the authenticated principal identity`() {
        val userId = ObjectId()
        val principal = UserPrincipal(
            User(
                id = userId,
                firstname = "Person",
                email = "person@example.com",
                password = "hash"
            )
        )
        val request = ChangePasswordRequest("current-password", "a-different-secure-password")

        val response = controller.changePassword(request, principal)

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(service).changePassword(userId.toHexString(), request)
    }

    @Test
    fun `password endpoints use the frontend contract paths`() {
        val forgot = PasswordResetController::class.java.getDeclaredMethod(
            "forgot",
            ForgotPasswordRequest::class.java
        )
        val reset = PasswordResetController::class.java.getDeclaredMethod(
            "reset",
            ResetPasswordRequest::class.java
        )
        val change = PasswordResetController::class.java.getDeclaredMethod(
            "changePassword",
            ChangePasswordRequest::class.java,
            UserPrincipal::class.java
        )

        assertContentEquals(arrayOf("/forgot-password"), forgot.getAnnotation(PostMapping::class.java).value)
        assertContentEquals(arrayOf("/reset-password"), reset.getAnnotation(PostMapping::class.java).value)
        assertContentEquals(arrayOf("/change-password"), change.getAnnotation(PostMapping::class.java).value)
    }

    @Test
    fun `forgot password rejects malformed email with structured validation error`() {
        mockMvc.post("/api/auth/forgot-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"not-an-email"}"""
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("VALIDATION_ERROR") }
                jsonPath("$.errors[0].field") { value("email") }
            }
    }

    @Test
    fun `reset password rejects a weak password before reaching service`() {
        mockMvc.post("/api/auth/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"${"a".repeat(43)}","newPassword":"short"}"""
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("VALIDATION_ERROR") }
                jsonPath("$.errors[0].field") { value("newPassword") }
            }
    }

    @Test
    fun `valid reset request reaches service`() {
        val token = "a".repeat(43)

        mockMvc.post("/api/auth/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"$token","newPassword":"a-secure-new-password"}"""
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("Password reset successfully.") }
            }

        verify(service).resetPassword(ResetPasswordRequest(token, "a-secure-new-password"))
    }
}
