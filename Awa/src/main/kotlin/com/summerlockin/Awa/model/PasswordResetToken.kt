package com.summerlockin.Awa.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document("password_reset_tokens")
@CompoundIndex(name = "password_reset_user_unique_v2", def = "{'userId': 1}", unique = true)
data class PasswordResetToken(
    @Id val id: ObjectId? = null,
    // Keep the existing Mongo field/index name while storing only a SHA-256 hash in it.
    @Field("token")
    @Indexed(unique = true)
    val tokenHash: String,
    @Indexed
    val userId: ObjectId,
    val createdAt: Instant = Instant.now(),
    @Indexed(expireAfter = "0s")
    val expiresAt: Instant,
    val used: Boolean = false,
    val usedAt: Instant? = null
)

