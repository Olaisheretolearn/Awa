package com.summerlockin.Awa.DTO

import jakarta.validation.constraints.Email

data class UserUpdateRequest(
    val email: String?=null,
    val firstname : String?=null
)
