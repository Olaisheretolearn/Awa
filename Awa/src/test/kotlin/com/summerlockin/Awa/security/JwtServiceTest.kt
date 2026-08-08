package com.summerlockin.Awa.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import java.util.Base64
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
}
