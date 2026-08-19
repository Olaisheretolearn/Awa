package com.summerlockin.Awa.DTO

import com.summerlockin.Awa.validation.ValidPassword
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    @field:Size(max = 256, message = "Current password is invalid")
    val oldPassword: String,
    @field:NotBlank(message = "New password is required")
    @field:ValidPassword
    val newPassword: String
)
