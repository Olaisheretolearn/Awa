package com.summerlockin.Awa.repository

import com.summerlockin.Awa.model.RefreshToken
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface RefreshTokenRepository : MongoRepository<RefreshToken, ObjectId> {
    fun findByTokenHash(tokenHash: String): RefreshToken?
    fun findByJti(jti: String): RefreshToken?
    fun findAllByUserIdAndRevokedFalse(userId: ObjectId): List<RefreshToken>
}