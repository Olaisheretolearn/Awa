package com.summerlockin.Awa.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("users")
data class User(
    @Id val id : ObjectId?=null,
    val firstname : String,
    val email :String,
    val password : String,
    val roomId: ObjectId? = null,
    val role: Role = Role.MEMBER,
    val joinedAt : Instant =Instant.now(),
    val isActive: Boolean = true,
    val avatarId: AvatarId? = null

)

enum class Role {
    OWNER,
    MEMBER
}
