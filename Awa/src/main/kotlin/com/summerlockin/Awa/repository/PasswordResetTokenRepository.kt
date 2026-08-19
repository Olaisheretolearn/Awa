
package com.summerlockin.Awa.repository

import com.summerlockin.Awa.model.PasswordResetToken
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface PasswordResetTokenRepository : MongoRepository<PasswordResetToken, ObjectId> {
    fun findByTokenHash(tokenHash: String): PasswordResetToken?
    fun findFirstByUserIdOrderByCreatedAtDesc(userId: ObjectId): PasswordResetToken?
    fun deleteByUserId(userId: ObjectId)
}
