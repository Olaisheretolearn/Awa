package com.summerlockin.Awa.DTO

data class MemberResponse(
    val id: String,
    val firstName: String,
    val email: String,
    val role: String,         // OWNER / MEMBER
    val avatarId: String?,
    val avatarImageUrl: String?
)