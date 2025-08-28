package com.summerlockin.Awa.controllers


import com.summerlockin.Awa.security.AuthenticationService


import com.summerlockin.Awa.DTO.AuthResponse
import com.summerlockin.Awa.DTO.RefreshTokenRequest
import com.summerlockin.Awa.DTO.UserLoginRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthenticationService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: UserLoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request.email, request.password))
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        val tokens = authService.refresh(request.refreshToken)
        return ResponseEntity.ok(tokens)
    }

}