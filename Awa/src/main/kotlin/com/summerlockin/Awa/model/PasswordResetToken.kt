package com.summerlockin.Awa.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

enum class TokenPurpose { PASSWORD_RESET }

@Document("password_reset_tokens")
data class PasswordResetToken(
    @Id val id: ObjectId? = null,
    @Indexed(unique = true)
    val token: String,
    @Indexed
    val userId: ObjectId,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant,
    val used: Boolean = false ,
    val attempts: Int = 0,
)

