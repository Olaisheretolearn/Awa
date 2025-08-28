package com.summerlockin.Awa.security

import com.summerlockin.Awa.DTO.AuthResponse
import com.summerlockin.Awa.repository.userRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userRepository: userRepository,
    private val encoder: PasswordEncoder
) {
    fun login(email: String, password: String): AuthResponse {
        val user = userRepository.findByEmailIgnoreCase(email)
            ?: throw RuntimeException("User not found")

        if (!encoder.matches(password, user.password)) {
            throw RuntimeException("Invalid credentials")
        }

        val accessToken = jwtService.generateAccessToken(user.id!!.toString())
        val refreshToken = jwtService.generateRefreshToken(user.id!!.toString())

        return AuthResponse(accessToken, refreshToken)
    }

    fun refresh(refreshToken: String): AuthResponse {
        val userId = jwtService.getUserIdFromToken(refreshToken)
        if (!jwtService.isRefreshTokenValid(refreshToken, userId)) {
            throw RuntimeException("Invalid or expired refresh token")
        }


        val newAccess  = jwtService.generateAccessToken(userId)
        val newRefresh = jwtService.generateRefreshToken(userId)

        return AuthResponse(newAccess, newRefresh)
    }

}
