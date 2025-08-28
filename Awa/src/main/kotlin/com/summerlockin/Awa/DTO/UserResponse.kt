package com.summerlockin.Awa.DTO

data class UserResponse(
    val id: String,
    val firstName: String,
    val email: String,
    val createdAt: String,
    val roomId: String? = null,
    val role: String,
    val avatarId: String?,
    val avatarImageUrl: String?
)

