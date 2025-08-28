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

    private fun generateToken(userId: String, type: String, expiry: Long): String {
        val now = Date()
        val expiryTime = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryTime)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String): String =
        generateToken(userId, "access", accessTokenValidityMs)
    fun generateRefreshToken(userId: String): String =
        generateToken(userId, "refresh", refreshTokenValidityMs)


    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getTokenType(token: String): String? = try {
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload["type"] as? String
    } catch (_: Exception) { null }

    fun isRefreshTokenValid(token: String, expectedUserId: String): Boolean {
        if (!validateToken(token)) return false
        if (getTokenType(token) != "refresh") return false
        if (getUserIdFromToken(token) != expectedUserId) return false
        return true
    }



    fun getUserIdFromToken(token: String): String {
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
        return claims.subject
    }
}