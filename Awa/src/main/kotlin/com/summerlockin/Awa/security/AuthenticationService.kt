package com.summerlockin.Awa.security

import com.summerlockin.Awa.DTO.AuthResponse
import com.summerlockin.Awa.exception.UnauthorizedException
import com.summerlockin.Awa.repository.userRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val jwtService: JwtService,
    private val refreshTokenService: com.summerlockin.Awa.service.RefreshTokenService,
    private val userRepository: userRepository,
    private val encoder: PasswordEncoder
) {
    fun login(email: String, password: String): AuthResponse {
        val user = userRepository.findByEmailIgnoreCase(email)
            ?: throw RuntimeException("User not found")

        if (!encoder.matches(password, user.password)) {
throw UnauthorizedException("Authentication failed")

        }

        val accessToken = jwtService.generateAccessToken(user.id!!.toString())
        val refreshToken = refreshTokenService.issueRefreshToken(user.id!!.toString())

        return AuthResponse(accessToken, refreshToken)
    }

    fun refresh(refreshToken: String): AuthResponse {
        if (!jwtService.validateToken(refreshToken) || jwtService.getTokenType(refreshToken) != "refresh") {
            throw UnauthorizedException("Invalid or expired refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val newRefreshToken = refreshTokenService.rotate(refreshToken)

        val newAccess  = jwtService.generateAccessToken(userId)
        return AuthResponse(newAccess, newRefreshToken)
    }

    fun logout(refreshToken: String, revokeAllSessions: Boolean = false) {
        if (revokeAllSessions) {
            val userId = jwtService.getUserIdFromToken(refreshToken)
            refreshTokenService.revokeAllForUser(userId)
            return
        }

        refreshTokenService.revoke(refreshToken)
    }

}
