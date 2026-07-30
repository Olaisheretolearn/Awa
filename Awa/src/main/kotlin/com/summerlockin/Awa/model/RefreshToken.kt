package com.summerlockin.Awa.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("refresh_tokens")
data class RefreshToken(
    @Id val id: ObjectId? = null,
    @Indexed
    val userId: ObjectId,
    @Indexed(unique = true)
    val tokenHash: String,
    @Indexed(unique = true)
    val jti: String,
    val issuedAt: Instant = Instant.now(),
    val expiresAt: Instant,
    val revoked: Boolean = false,
    val revokedAt: Instant? = null,
    val deviceId: String? = null,
    val createdIp: String? = null,
    val lastUsedAt: Instant? = null,
)