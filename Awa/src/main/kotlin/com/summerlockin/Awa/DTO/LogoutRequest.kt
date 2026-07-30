package com.summerlockin.Awa.DTO

data class LogoutRequest(
    val refreshToken: String,
    val revokeAllSessions: Boolean = false
)