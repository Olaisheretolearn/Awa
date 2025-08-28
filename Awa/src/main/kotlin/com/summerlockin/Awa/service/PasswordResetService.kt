// com/summerlockin/Awa/service/PasswordResetService.kt
package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.ForgotPasswordRequest
import com.summerlockin.Awa.DTO.ResetPasswordRequest
import com.summerlockin.Awa.model.PasswordResetToken
import com.summerlockin.Awa.repository.PasswordResetTokenRepository
import com.summerlockin.Awa.repository.userRepository
import org.bson.types.ObjectId
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

interface Mailer {
    fun sendPasswordReset(email: String, link: String)
}

@Service
class ConsoleMailer : Mailer {
    override fun sendPasswordReset(email: String, link: String) {
        // DEV: just log. Swap with JavaMailSender / SendGrid / Mailgun later.
        println("Password reset for $email → $link")
    }
}

@Service
class PasswordResetService(
    private val users: userRepository,
    private val tokens: PasswordResetTokenRepository,
    private val encoder: PasswordEncoder,
    private val mailer: Mailer
) {
    private val expiry: Duration = Duration.ofMinutes(60)

    fun requestReset(req: ForgotPasswordRequest) {
        val email = req.email.trim().lowercase()
        val user = users.findByEmailIgnoreCase(email)

        // Always 200 to caller; only proceed if user exists
        if (user != null && user.id != null) {
            tokens.deleteByUserId(user.id)
            val token = UUID.randomUUID().toString().replace("-", "")
            val reset = PasswordResetToken(
                token = token,
                userId = user.id,
                expiresAt = Instant.now().plus(expiry),
            )
            tokens.save(reset)

            val link = "https://your-frontend/reset-password?token=$token"
            mailer.sendPasswordReset(user.email, link)
        }
    }

    fun resetPassword(req: ResetPasswordRequest) {
        val record = tokens.findByToken(req.token) ?: throw IllegalArgumentException("Invalid or expired token")
        if (record.used) throw IllegalArgumentException("Token already used")
        if (record.expiresAt.isBefore(Instant.now())) throw IllegalArgumentException("Token expired")

        // Optional attempt counter
        if (record.attempts >= 5) throw IllegalArgumentException("Token locked")
        tokens.save(record.copy(attempts = record.attempts + 1))

        val user = users.findById(record.userId).orElseThrow { IllegalStateException("User not found") }
        val updated = user.copy(password = encoder.encode(req.newPassword))
        users.save(updated)

        tokens.deleteByUserId(record.userId) // one-time use
    }

}
