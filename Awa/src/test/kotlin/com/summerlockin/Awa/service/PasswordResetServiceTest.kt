package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.ChangePasswordRequest
import com.summerlockin.Awa.DTO.ForgotPasswordRequest
import com.summerlockin.Awa.DTO.ResetPasswordRequest
import com.summerlockin.Awa.config.PasswordResetProperties
import com.summerlockin.Awa.exception.InvalidCurrentPasswordException
import com.summerlockin.Awa.exception.InvalidPasswordResetTokenException
import com.summerlockin.Awa.exception.PasswordReuseException
import com.summerlockin.Awa.model.PasswordResetToken
import com.summerlockin.Awa.model.User
import com.summerlockin.Awa.repository.PasswordResetTokenRepository
import com.summerlockin.Awa.repository.userRepository
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PasswordResetServiceTest {
    private val users = mock<userRepository>()
    private val tokens = mock<PasswordResetTokenRepository>()
    private val encoder = mock<PasswordEncoder>()
    private val mailer = mock<PasswordResetMailer>()
    private val refreshTokens = mock<RefreshTokenService>()
    private val tokenStore = mock<PasswordResetTokenStore>()
    private val properties = PasswordResetProperties(
        frontendUrl = "https://app.awa.example/reset-password",
        tokenTtl = Duration.ofMinutes(30),
        requestCooldown = Duration.ofMinutes(1)
    )
    private val service = PasswordResetService(
        users,
        tokens,
        encoder,
        mailer,
        refreshTokens,
        tokenStore,
        properties
    )

    @Test
    fun `request reset stores only a hash and emails the raw token in the frontend link`() {
        val user = user()
        whenever(users.findByEmailIgnoreCase("person@example.com")).thenReturn(user)
        whenever(tokens.save(any<PasswordResetToken>())).thenAnswer { it.getArgument(0) }

        service.requestReset(ForgotPasswordRequest("  PERSON@example.com "))

        val saved = argumentCaptor<PasswordResetToken>()
        verify(tokens).save(saved.capture())
        val link = argumentCaptor<String>()
        verify(mailer).sendPasswordReset(
            eq(user.email),
            eq(user.firstname),
            link.capture(),
            eq(30),
            eq("password-reset-${saved.firstValue.tokenHash}")
        )

        val rawToken = UriComponentsBuilder.fromUriString(link.firstValue)
            .build()
            .queryParams
            .getFirst("token")!!
        assertEquals(43, rawToken.length)
        assertEquals(hash(rawToken), saved.firstValue.tokenHash)
        assertFalse(saved.firstValue.tokenHash.contains(rawToken))
        assertTrue(saved.firstValue.expiresAt.isAfter(saved.firstValue.createdAt))
    }

    @Test
    fun `request reset always does nothing externally for an unknown email`() {
        whenever(users.findByEmailIgnoreCase("missing@example.com")).thenReturn(null)

        service.requestReset(ForgotPasswordRequest("missing@example.com"))

        verifyNoInteractions(tokens, mailer)
    }

    @Test
    fun `request reset suppresses another email during the cooldown`() {
        val user = user()
        val recent = resetToken(user.id!!, createdAt = Instant.now())
        whenever(users.findByEmailIgnoreCase(user.email)).thenReturn(user)
        whenever(tokens.findFirstByUserIdOrderByCreatedAtDesc(user.id!!)).thenReturn(recent)

        service.requestReset(ForgotPasswordRequest(user.email))

        verify(tokens, never()).save(any<PasswordResetToken>())
        verifyNoInteractions(mailer)
    }

    @Test
    fun `reset password consumes token changes password increments version and revokes sessions`() {
        val rawToken = "reset-token-value"
        val user = user(credentialsVersion = 4)
        val record = resetToken(user.id!!, tokenHash = hash(rawToken))
        whenever(tokens.findByTokenHash(record.tokenHash)).thenReturn(record)
        whenever(users.findById(user.id!!)).thenReturn(Optional.of(user))
        whenever(encoder.matches("a-new-secure-password", user.password)).thenReturn(false)
        whenever(tokenStore.consumeValid(eq(record.tokenHash), any())).thenReturn(record.copy(used = true))
        whenever(encoder.encode("a-new-secure-password")).thenReturn("new-password-hash")

        service.resetPassword(ResetPasswordRequest(rawToken, "a-new-secure-password"))

        val updated = argumentCaptor<User>()
        verify(users).save(updated.capture())
        assertEquals("new-password-hash", updated.firstValue.password)
        assertEquals(5, updated.firstValue.credentialsVersion)
        verify(refreshTokens).revokeAllForUser(user.id!!.toHexString())
        verify(tokens).deleteByUserId(user.id!!)
    }

    @Test
    fun `reset password rejects expired token without consuming or writing`() {
        val rawToken = "expired-token"
        val record = resetToken(
            userId = ObjectId(),
            tokenHash = hash(rawToken),
            expiresAt = Instant.now().minusSeconds(1)
        )
        whenever(tokens.findByTokenHash(record.tokenHash)).thenReturn(record)

        assertFailsWith<InvalidPasswordResetTokenException> {
            service.resetPassword(ResetPasswordRequest(rawToken, "a-new-secure-password"))
        }

        verifyNoInteractions(tokenStore, encoder, refreshTokens)
        verify(users, never()).save(any<User>())
    }

    @Test
    fun `reset password rejects token lost in an atomic consumption race`() {
        val rawToken = "raced-token"
        val user = user()
        val record = resetToken(user.id!!, tokenHash = hash(rawToken))
        whenever(tokens.findByTokenHash(record.tokenHash)).thenReturn(record)
        whenever(users.findById(user.id!!)).thenReturn(Optional.of(user))
        whenever(encoder.matches(any(), eq(user.password))).thenReturn(false)
        whenever(tokenStore.consumeValid(eq(record.tokenHash), any())).thenReturn(null)

        assertFailsWith<InvalidPasswordResetTokenException> {
            service.resetPassword(ResetPasswordRequest(rawToken, "a-new-secure-password"))
        }

        verify(users, never()).save(any<User>())
        verifyNoInteractions(refreshTokens)
    }

    @Test
    fun `reset password rejects reuse without consuming the token`() {
        val rawToken = "valid-token"
        val user = user()
        val record = resetToken(user.id!!, tokenHash = hash(rawToken))
        whenever(tokens.findByTokenHash(record.tokenHash)).thenReturn(record)
        whenever(users.findById(user.id!!)).thenReturn(Optional.of(user))
        whenever(encoder.matches("same-password-value", user.password)).thenReturn(true)

        assertFailsWith<PasswordReuseException> {
            service.resetPassword(ResetPasswordRequest(rawToken, "same-password-value"))
        }

        verifyNoInteractions(tokenStore, refreshTokens)
        verify(users, never()).save(any<User>())
    }

    @Test
    fun `authenticated password change verifies current password and invalidates reset and session tokens`() {
        val user = user(credentialsVersion = 2)
        whenever(users.findById(user.id!!)).thenReturn(Optional.of(user))
        whenever(encoder.matches("current-password", user.password)).thenReturn(true)
        whenever(encoder.matches("different-secure-password", user.password)).thenReturn(false)
        whenever(encoder.encode("different-secure-password")).thenReturn("changed-hash")

        service.changePassword(
            user.id!!.toHexString(),
            ChangePasswordRequest("current-password", "different-secure-password")
        )

        val updated = argumentCaptor<User>()
        verify(users).save(updated.capture())
        assertEquals("changed-hash", updated.firstValue.password)
        assertEquals(3, updated.firstValue.credentialsVersion)
        verify(refreshTokens).revokeAllForUser(user.id!!.toHexString())
        verify(tokens).deleteByUserId(user.id!!)
    }

    @Test
    fun `authenticated password change rejects the wrong current password`() {
        val user = user()
        whenever(users.findById(user.id!!)).thenReturn(Optional.of(user))
        whenever(encoder.matches("wrong-password", user.password)).thenReturn(false)

        assertFailsWith<InvalidCurrentPasswordException> {
            service.changePassword(
                user.id!!.toHexString(),
                ChangePasswordRequest("wrong-password", "different-secure-password")
            )
        }

        verify(users, never()).save(any<User>())
        verifyNoInteractions(refreshTokens)
    }

    private fun user(credentialsVersion: Long = 0) = User(
        id = ObjectId(),
        firstname = "Awa User",
        email = "person@example.com",
        password = "existing-hash",
        credentialsVersion = credentialsVersion
    )

    private fun resetToken(
        userId: ObjectId,
        tokenHash: String = hash("raw-token"),
        createdAt: Instant = Instant.now().minusSeconds(120),
        expiresAt: Instant = Instant.now().plusSeconds(120)
    ) = PasswordResetToken(
        tokenHash = tokenHash,
        userId = userId,
        createdAt = createdAt,
        expiresAt = expiresAt
    )

    private fun hash(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray(StandardCharsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
