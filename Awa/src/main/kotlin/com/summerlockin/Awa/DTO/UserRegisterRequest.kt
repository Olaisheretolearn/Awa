package com.summerlockin.Awa.DTO

import com.summerlockin.Awa.validation.ValidPassword
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRegisterRequest(
    @field:NotBlank(message = "First name is required")
    @field:Size(max = 100, message = "First name must be 100 characters or fewer")
    val firstName: String,
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Size(max = 254, message = "Email must be 254 characters or fewer")
    val email: String,
    @field:NotBlank(message = "Password is required")
    @field:ValidPassword
    val password: String
)
