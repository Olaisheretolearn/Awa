package com.summerlockin.Awa.DTO

import com.summerlockin.Awa.validation.ValidPassword
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
    @field:NotBlank(message = "Reset token is required")
    @field:Size(max = 256, message = "Reset token is invalid")
    val token: String,
    @field:NotBlank(message = "New password is required")
    @field:ValidPassword
    val newPassword: String
)
