package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.ChangePasswordRequest
import com.summerlockin.Awa.DTO.ForgotPasswordRequest
import com.summerlockin.Awa.DTO.ResetPasswordRequest
import com.summerlockin.Awa.config.PasswordResetProperties
import com.summerlockin.Awa.exception.InvalidCurrentPasswordException
import com.summerlockin.Awa.exception.InvalidPasswordResetTokenException
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.exception.PasswordReuseException
import com.summerlockin.Awa.model.PasswordResetToken
import com.summerlockin.Awa.repository.PasswordResetTokenRepository
import com.summerlockin.Awa.repository.userRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

@Service
class PasswordResetService(
    private val users: userRepository,
    private val tokens: PasswordResetTokenRepository,
    private val encoder: PasswordEncoder,
    private val mailer: PasswordResetMailer,
    private val refreshTokenService: RefreshTokenService,
    private val tokenStore: PasswordResetTokenStore,
    private val properties: PasswordResetProperties
) {
    private val logger = LoggerFactory.getLogger(PasswordResetService::class.java)
    private val secureRandom = SecureRandom()

    init {
        properties.validate()
    }

    /**
     * Deliberately has no return value that reveals whether the email belongs to an account.
     */
    fun requestReset(req: ForgotPasswordRequest) {
        val email = req.email.trim().lowercase()
        val user = users.findByEmailIgnoreCase(email)

        if (user?.id == null || !user.isActive) return

        val now = Instant.now()
        val mostRecent = tokens.findFirstByUserIdOrderByCreatedAtDesc(user.id)
        if (mostRecent != null && mostRecent.createdAt.isAfter(now.minus(properties.requestCooldown))) {
            return
        }

        val rawToken = generateToken()
        val tokenHash = hashToken(rawToken)
        val record = PasswordResetToken(
            tokenHash = tokenHash,
            userId = user.id,
            createdAt = now,
            expiresAt = now.plus(properties.tokenTtl)
        )

        try {
            tokens.deleteByUserId(user.id)
            tokens.save(record)
        } catch (ex: DuplicateKeyException) {
            // A concurrent request already created the user's one active token.
            logger.info("Suppressed concurrent password-reset token creation")
            return
        }

        val resetLink = UriComponentsBuilder
            .fromUriString(properties.frontendUrl)
            .queryParam("token", rawToken)
            .build()
            .encode()
            .toUriString()

        try {
            mailer.sendPasswordReset(
                email = user.email,
                firstName = user.firstname,
                resetLink = resetLink,
                expiresInMinutes = properties.tokenTtl.toMinutes(),
                idempotencyKey = "password-reset-$tokenHash"
            )
        } catch (ex: RuntimeException) {
            // Preserve the generic API response so an email address cannot be enumerated.
            logger.error("Could not schedule password-reset email delivery", ex)
        }
    }

    fun resetPassword(req: ResetPasswordRequest) {
        val tokenHash = hashToken(req.token.trim())
        val now = Instant.now()
        val record = tokens.findByTokenHash(tokenHash)
            ?: throw InvalidPasswordResetTokenException()

        if (record.used || !record.expiresAt.isAfter(now)) {
            throw InvalidPasswordResetTokenException()
        }

        val user = users.findById(record.userId).orElse(null)
        if (user == null || !user.isActive) {
            tokens.deleteByUserId(record.userId)
            throw InvalidPasswordResetTokenException()
        }

        if (encoder.matches(req.newPassword, user.password)) {
            throw PasswordReuseException()
        }

        tokenStore.consumeValid(tokenHash, now)
            ?: throw InvalidPasswordResetTokenException()

        users.save(
            user.copy(
                password = encoder.encode(req.newPassword),
                credentialsVersion = user.credentialsVersion + 1
            )
        )
        refreshTokenService.revokeAllForUser(record.userId.toHexString())
        tokens.deleteByUserId(record.userId)
    }

    fun changePassword(actingUserId: String, req: ChangePasswordRequest) {
        val objectId = try {
            ObjectId(actingUserId)
        } catch (_: IllegalArgumentException) {
            throw NotFoundException("User not found")
        }
        val user = users.findById(objectId).orElseThrow { NotFoundException("User not found") }

        if (!encoder.matches(req.oldPassword, user.password)) {
            throw InvalidCurrentPasswordException()
        }
        if (encoder.matches(req.newPassword, user.password)) {
            throw PasswordReuseException()
        }

        users.save(
            user.copy(
                password = encoder.encode(req.newPassword),
                credentialsVersion = user.credentialsVersion + 1
            )
        )
        refreshTokenService.revokeAllForUser(actingUserId)
        tokens.deleteByUserId(objectId)
    }

    private fun generateToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(StandardCharsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
