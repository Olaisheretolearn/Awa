package com.summerlockin.Awa.service

import com.summerlockin.Awa.model.PasswordResetToken
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PasswordResetTokenStore(
    private val mongoTemplate: MongoTemplate
) {
    /** Atomically changes a valid token from unused to used. */
    fun consumeValid(tokenHash: String, now: Instant): PasswordResetToken? {
        val query = Query(
            Criteria.where("token").`is`(tokenHash)
                .and("used").`is`(false)
                .and("expiresAt").gt(now)
        )
        val update = Update()
            .set("used", true)
            .set("usedAt", now)

        return mongoTemplate.findAndModify(
            query,
            update,
            FindAndModifyOptions.options().returnNew(true),
            PasswordResetToken::class.java
        )
    }
}
