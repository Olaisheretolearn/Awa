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
    fun issueRefreshToken(userId: String, deviceId: String? = null, createdIp: String? = null): String {
        val jti = UUID.randomUUID().toString()
        val token = jwtService.generateRefreshToken(userId, jti)
        persistRefreshToken(userId = userId, token = token, jti = jti, deviceId = deviceId, createdIp = createdIp)
        return token
    }

    fun rotate(refreshToken: String, deviceId: String? = null, createdIp: String? = null): String {
        val claims = jwtService.getSignedClaims(refreshToken).payload
        val userId = claims.subject ?: throw UnauthorizedException("Invalid refresh token")
        val jti = claims.id ?: throw UnauthorizedException("Invalid refresh token")
        val expiresAt = claims.expiration?.toInstant() ?: throw UnauthorizedException("Invalid refresh token")

        if (claims.issuedAt?.toInstant()?.isAfter(Instant.now()) == true || expiresAt.isBefore(Instant.now())) {
            throw UnauthorizedException("Refresh token expired")
        }

        if (claims["type", String::class.java] != "refresh") {
            throw UnauthorizedException("Invalid refresh token")
        }

        val tokenHash = hashToken(refreshToken)
        val record = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw UnauthorizedException("Invalid refresh token")

        if (record.userId.toHexString() != userId || record.jti != jti) {
            throw UnauthorizedException("Invalid refresh token")
        }

        if (record.revoked) {
            revokeAllForUser(userId)
            throw UnauthorizedException("Refresh token replay detected")
        }

        val consumed = consumeToken(tokenHash)
        if (!consumed) {
            revokeAllForUser(userId)
            throw UnauthorizedException("Refresh token replay detected")
        }

        val newToken = jwtService.generateRefreshToken(userId, UUID.randomUUID().toString())
        persistRefreshToken(userId = userId, token = newToken, jti = jwtService.getJtiFromToken(newToken)!!, deviceId = deviceId, createdIp = createdIp)
        return newToken
    }

    fun revoke(refreshToken: String) {
        val tokenHash = hashToken(refreshToken)
        val record = refreshTokenRepository.findByTokenHash(tokenHash) ?: return
        markRevoked(record.tokenHash)
    }

    fun revokeAllForUser(userId: String) {
        val now = Instant.now()
        val query = Query(Criteria.where("userId").`is`(ObjectId(userId)).and("revoked").`is`(false))
        val update = Update().set("revoked", true).set("revokedAt", now)
        mongoTemplate.updateMulti(query, update, RefreshToken::class.java)
    }

    fun revokeByJti(jti: String) {
        val token = refreshTokenRepository.findByJti(jti) ?: return
        markRevoked(token.tokenHash)
    }

    private fun persistRefreshToken(userId: String, token: String, jti: String, deviceId: String?, createdIp: String?) {
        val record = RefreshToken(
            userId = ObjectId(userId),
            tokenHash = hashToken(token),
            jti = jti,
            issuedAt = Instant.now(),
            expiresAt = Instant.now().plusMillis(jwtService.refreshTokenValidityMs),
            deviceId = deviceId,
            createdIp = createdIp
        )
        refreshTokenRepository.save(record)
    }

    private fun consumeToken(tokenHash: String): Boolean {
        val query = Query(Criteria.where("tokenHash").`is`(tokenHash).and("revoked").`is`(false))
        val update = Update().set("revoked", true).set("revokedAt", Instant.now()).set("lastUsedAt", Instant.now())
        return mongoTemplate.updateFirst(query, update, RefreshToken::class.java).modifiedCount == 1L
    }

    private fun markRevoked(tokenHash: String) {
        val query = Query(Criteria.where("tokenHash").`is`(tokenHash).and("revoked").`is`(false))
        val update = Update().set("revoked", true).set("revokedAt", Instant.now())
        mongoTemplate.updateFirst(query, update, RefreshToken::class.java)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(digest)
    }
}