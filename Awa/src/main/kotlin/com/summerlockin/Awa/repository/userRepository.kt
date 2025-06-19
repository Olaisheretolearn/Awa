package com.summerlockin.Awa.repository

import com.summerlockin.Awa.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface userRepository : MongoRepository<User, ObjectId> {
    fun findByEmailIgnoreCase(user:String):User?
}