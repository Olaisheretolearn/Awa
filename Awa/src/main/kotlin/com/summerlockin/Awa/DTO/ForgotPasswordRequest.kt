
package com.summerlockin.Awa.DTO

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ForgotPasswordRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Size(max = 254, message = "Email must be 254 characters or fewer")
    val email: String
)

