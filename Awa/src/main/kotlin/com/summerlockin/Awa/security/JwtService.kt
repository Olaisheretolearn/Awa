package com.summerlockin.Awa.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date

@Service
class JwtService(
    @Value("\${JWT_SECRET}") private val jwtSecret: String
) {

    private val secretKey =
        Keys.hmacShaKeyFor(
            Base64.getDecoder().decode(jwtSecret)
        )

    private val accessTokenValidityMs =
        15L * 60L * 1000L

    val refreshTokenValidityMs =
        30L * 24L * 60L * 60L * 1000L

    private fun generateToken(
        userId: String,
        type: String,
        expiry: Long,
        jti: String? = null
    ): String {

        val now = Date()
        val expiryTime = Date(now.time + expiry)

        val builder = Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryTime)

        if (jti != null) {
            builder.id(jti)
        }

        return builder
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String): String =
        generateToken(
            userId = userId,
            type = "access",
            expiry = accessTokenValidityMs
        )

    fun generateRefreshToken(
        userId: String,
        jti: String
    ): String =
        generateToken(
            userId = userId,
            type = "refresh",
            expiry = refreshTokenValidityMs,
            jti = jti
        )

    fun validateToken(token: String): Boolean {
        return try {
            getSignedClaims(token)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getTokenType(token: String): String? {
        return try {
            getSignedClaims(token)
                .payload["type"] as? String
        } catch (_: Exception) {
            null
        }
    }

    fun getJtiFromToken(token: String): String? {
        return try {
            getSignedClaims(token).payload.id
        } catch (_: Exception) {
            null
        }
    }

    fun getUserIdFromToken(token: String): String {
        return getSignedClaims(token).payload.subject
    }

    fun getSignedClaims(token: String) =
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
}