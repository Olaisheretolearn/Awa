package com.summerlockin.Awa.service

import com.summerlockin.Awa.exception.UnauthorizedException
import com.summerlockin.Awa.model.RefreshToken
import com.summerlockin.Awa.repository.RefreshTokenRepository
import com.summerlockin.Awa.security.JwtService
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val mongoTemplate: MongoTemplate,
    private val jwtService: JwtService,
) {

    fun issueRefreshToken(
        userId: String,
        credentialsVersion: Long = 0,
        deviceId: String? = null,
        createdIp: String? = null
    ): String {

        val jti = UUID.randomUUID().toString()

        val token = jwtService.generateRefreshToken(
            userId = userId,
            jti = jti,
            credentialsVersion = credentialsVersion
        )

        persistRefreshToken(
            userId = userId,
            token = token,
            jti = jti,
            deviceId = deviceId,
            createdIp = createdIp
        )

        return token
    }

    fun rotate(
        refreshToken: String,
        deviceId: String? = null,
        createdIp: String? = null
    ): String {

        // Cryptographically validate the JWT first.
        val claims = try {
            jwtService.getSignedClaims(refreshToken).payload
        } catch (_: Exception) {
            throw UnauthorizedException("Invalid or expired refresh token")
        }

        val userId = claims.subject
            ?: throw UnauthorizedException("Invalid refresh token")

        val jti = claims.id
            ?: throw UnauthorizedException("Invalid refresh token")

        val credentialsVersion =
            (claims["credentialsVersion"] as? Number)?.toLong() ?: 0

        if (claims["type", String::class.java] != "refresh") {
            throw UnauthorizedException("Invalid refresh token")
        }

        // Make sure this is actually a valid Mongo ObjectId.
        val objectId = try {
            ObjectId(userId)
        } catch (_: IllegalArgumentException) {
            throw UnauthorizedException("Invalid refresh token")
        }

        val tokenHash = hashToken(refreshToken)

        val record = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw UnauthorizedException("Invalid refresh token")

        if (
            record.userId != objectId ||
            record.jti != jti
        ) {
            throw UnauthorizedException("Invalid refresh token")
        }

        /*
         * A revoked refresh token being presented again means somebody
         * attempted to reuse an already-consumed token.
         *
         * Revoke the entire user's refresh-token family.
         */
        if (record.revoked) {
            revokeAllForUser(userId)
            throw UnauthorizedException("Refresh token replay detected")
        }

        /*
         * Atomic MongoDB update.

         * If two requests attempt to rotate the same token simultaneously,
         * only one can successfully change revoked=false -> revoked=true.
         */
        val consumed = consumeToken(tokenHash)

        if (!consumed) {
            revokeAllForUser(userId)
            throw UnauthorizedException("Refresh token replay detected")
        }

        // Issue a completely new refresh token.
        val newJti = UUID.randomUUID().toString()

        val newToken = jwtService.generateRefreshToken(
            userId = userId,
            jti = newJti,
            credentialsVersion = credentialsVersion
        )

        persistRefreshToken(
            userId = userId,
            token = newToken,
            jti = newJti,
            deviceId = deviceId,
            createdIp = createdIp
        )

        return newToken
    }

    fun revoke(refreshToken: String) {
        val tokenHash = hashToken(refreshToken)

        val record =
            refreshTokenRepository.findByTokenHash(tokenHash)
                ?: return

        markRevoked(record.tokenHash)
    }

    fun revokeAllForUser(userId: String) {

        val objectId = try {
            ObjectId(userId)
        } catch (_: IllegalArgumentException) {
            return
        }

        val query = Query(
            Criteria
                .where("userId").`is`(objectId)
                .and("revoked").`is`(false)
        )

        val update = Update()
            .set("revoked", true)
            .set("revokedAt", Instant.now())

        mongoTemplate.updateMulti(
            query,
            update,
            RefreshToken::class.java
        )
    }

    fun revokeByJti(jti: String) {

        val token =
            refreshTokenRepository.findByJti(jti)
                ?: return

        markRevoked(token.tokenHash)
    }

    private fun persistRefreshToken(
        userId: String,
        token: String,
        jti: String,
        deviceId: String?,
        createdIp: String?
    ) {

        val now = Instant.now()

        val record = RefreshToken(
            userId = ObjectId(userId),
            tokenHash = hashToken(token),
            jti = jti,
            issuedAt = now,
            expiresAt = now.plusMillis(
                jwtService.refreshTokenValidityMs
            ),
            deviceId = deviceId,
            createdIp = createdIp
        )

        refreshTokenRepository.save(record)
    }

    private fun consumeToken(tokenHash: String): Boolean {

        val query = Query(
            Criteria
                .where("tokenHash").`is`(tokenHash)
                .and("revoked").`is`(false)
        )

        val now = Instant.now()

        val update = Update()
            .set("revoked", true)
            .set("revokedAt", now)
            .set("lastUsedAt", now)

        return mongoTemplate
            .updateFirst(
                query,
                update,
                RefreshToken::class.java
            )
            .modifiedCount == 1L
    }

    private fun markRevoked(tokenHash: String) {

        val query = Query(
            Criteria
                .where("tokenHash").`is`(tokenHash)
                .and("revoked").`is`(false)
        )

        val update = Update()
            .set("revoked", true)
            .set("revokedAt", Instant.now())

        mongoTemplate.updateFirst(
            query,
            update,
            RefreshToken::class.java
        )
    }

    private fun hashToken(token: String): String {

        val digest = MessageDigest
            .getInstance("SHA-256")
            .digest(
                token.toByteArray(StandardCharsets.UTF_8)
            )

        return Base64.getEncoder()
            .encodeToString(digest)
    }
}
