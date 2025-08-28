// com/summerlockin/Awa/controllers/PasswordResetController.kt
package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.ChangePasswordRequest
import com.summerlockin.Awa.DTO.ForgotPasswordRequest
import com.summerlockin.Awa.DTO.ResetPasswordRequest
import com.summerlockin.Awa.repository.userRepository
import com.summerlockin.Awa.security.Encoder
import com.summerlockin.Awa.security.JwtService
import com.summerlockin.Awa.service.PasswordResetService
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class PasswordResetController(
    private val service: PasswordResetService,
    private val jwt: JwtService,
    private val userRepository: userRepository,
    private val encoder: PasswordEncoder
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
        @RequestHeader("Authorization") bearer: String,
        @RequestBody req: ChangePasswordRequest
    ): ResponseEntity<Unit> {
        val userId = jwt.getUserIdFromToken(bearer.removePrefix("Bearer ").trim())
        val user = userRepository.findById(ObjectId(userId)).orElseThrow { IllegalArgumentException("User not found") }
        if (!encoder.matches(req.oldPassword, user.password)) {
            return ResponseEntity.badRequest().build()
        }
        userRepository.save(user.copy(password = encoder.encode(req.newPassword)))
        return ResponseEntity.ok().build()
    }
}
