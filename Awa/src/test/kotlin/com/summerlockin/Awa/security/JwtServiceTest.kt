package com.summerlockin.Awa.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JwtServiceTest {

@Test
fun `access tokens should last 15 minutes`() {
    val secret = Base64.getEncoder().encodeToString(ByteArray(64))
    val service = JwtService(secret)

    val token = service.generateAccessToken("user-123")

    val claims = Jwts.parser()
        .verifyWith(
            Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(secret)
            )
        )
        .build()
        .parseSignedClaims(token)
        .payload

    val validityMs =
        claims.expiration.time - claims.issuedAt.time

    assertTrue(
        validityMs == 15 * 60 * 1000L,
        "Expected access token validity to be 15 minutes, but was $validityMs ms"
    )
}

@Test
fun `tokens carry the credentials version used for immediate session invalidation`() {
    val secret = Base64.getEncoder().encodeToString(ByteArray(64))
    val service = JwtService(secret)

    val accessToken = service.generateAccessToken("user-123", credentialsVersion = 7)
    val refreshToken = service.generateRefreshToken("user-123", "jti-123", credentialsVersion = 7)

    assertEquals(7, service.getCredentialsVersionFromToken(accessToken))
    assertEquals(7, service.getCredentialsVersionFromToken(refreshToken))
}
}
