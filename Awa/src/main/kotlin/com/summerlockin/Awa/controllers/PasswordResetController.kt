// com/summerlockin/Awa/controllers/PasswordResetController.kt
package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.ChangePasswordRequest
import com.summerlockin.Awa.DTO.ForgotPasswordRequest
import com.summerlockin.Awa.DTO.PasswordActionResponse
import com.summerlockin.Awa.DTO.ResetPasswordRequest
import com.summerlockin.Awa.security.UserPrincipal
import com.summerlockin.Awa.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class PasswordResetController(
    private val service: PasswordResetService
) {
    @PostMapping("/forgot-password")
    fun forgot(@Valid @RequestBody req: ForgotPasswordRequest): ResponseEntity<PasswordActionResponse> {
        service.requestReset(req)
        return ResponseEntity.accepted().body(
            PasswordActionResponse(
                "If an account exists for that email, a password reset link has been sent."
            )
        )
    }

    @PostMapping("/reset-password")
    fun reset(@Valid @RequestBody req: ResetPasswordRequest): ResponseEntity<PasswordActionResponse> {
        service.resetPassword(req)
        return ResponseEntity.ok(PasswordActionResponse("Password reset successfully."))
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody req: ChangePasswordRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<PasswordActionResponse> {
        service.changePassword(principal.getId(), req)
        return ResponseEntity.ok(PasswordActionResponse("Password changed successfully."))
    }
}
