// com/summerlockin/Awa/controllers/PasswordResetController.kt
package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.ChangePasswordRequest
import com.summerlockin.Awa.DTO.ForgotPasswordRequest
import com.summerlockin.Awa.DTO.ResetPasswordRequest
import com.summerlockin.Awa.service.RefreshTokenService
import com.summerlockin.Awa.repository.userRepository
import com.summerlockin.Awa.security.UserPrincipal
import com.summerlockin.Awa.service.PasswordResetService
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class PasswordResetController(
    private val service: PasswordResetService,
    private val userRepository: userRepository,
    private val encoder: PasswordEncoder,
    private val refreshTokenService: RefreshTokenService
) {
    @PostMapping("/forgot-password")
    fun forgot(@RequestBody req: ForgotPasswordRequest): ResponseEntity<Unit> {
        service.requestReset(req)
        // Always 200 OK
        return ResponseEntity.ok().build()
    }

    @PostMapping("/reset-password")
    fun reset(@RequestBody req: ResetPasswordRequest): ResponseEntity<Unit> {
        service.resetPassword(req)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/change-password")
    fun changePassword(
        @RequestBody req: ChangePasswordRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Unit> {
        val userId = principal.getId()
        val user = userRepository.findById(ObjectId(userId)).orElseThrow { IllegalArgumentException("User not found") }
        if (!encoder.matches(req.oldPassword, user.password)) {
            return ResponseEntity.badRequest().build()
        }
        userRepository.save(user.copy(password = encoder.encode(req.newPassword)))
        refreshTokenService.revokeAllForUser(userId)
        return ResponseEntity.ok().build()
    }
}
