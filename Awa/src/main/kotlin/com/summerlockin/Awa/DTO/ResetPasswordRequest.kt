package com.summerlockin.Awa.DTO

data class ResetPasswordRequest(val token: String, val newPassword: String)
