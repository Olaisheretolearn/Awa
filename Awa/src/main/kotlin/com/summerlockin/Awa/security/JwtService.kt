package com.summerlockin.Awa.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date

@Service
class JwtService (
    @Value("\${JWT_SECRET}") private val jwtSecret: String
) {
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))
    private val accessTokenValidityMs = 15L * 60L * 1000L
    val refreshTokenValidityMs = 30L * 24 * 60 * 60 * 1000L

    private fun generateToken(userId: String, type: String, expiry: Long, jti: String? = null): String {
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
        generateToken(userId, "access", accessTokenValidityMs)
    fun generateRefreshToken(userId: String, jti: String = java.util.UUID.randomUUID().toString()): String =
        generateToken(userId, "refresh", refreshTokenValidityMs, jti)


    fun validateToken(token: String): Boolean {
        return try {
            getSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getTokenType(token: String): String? = try {
        getSignedClaims(token)
            .payload["type"] as? String
    } catch (_: Exception) { null }

    fun isRefreshTokenValid(token: String, expectedUserId: String, expectedJti: String? = null): Boolean {
        if (!validateToken(token)) return false
        if (getTokenType(token) != "refresh") return false
        if (getUserIdFromToken(token) != expectedUserId) return false
        if (expectedJti != null && getJtiFromToken(token) != expectedJti) return false
        return true
    }

    fun getJtiFromToken(token: String): String? = try {
        getSignedClaims(token).payload.id
    } catch (_: Exception) { null }

    fun getSignedClaims(token: String) = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)


    fun getUserIdFromToken(token: String): String {
        val claims = getSignedClaims(token).payload
        return claims.subject
    }
}