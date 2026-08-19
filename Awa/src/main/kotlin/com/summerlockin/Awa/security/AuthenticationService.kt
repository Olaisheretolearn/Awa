package com.summerlockin.Awa.security

import com.summerlockin.Awa.DTO.AuthResponse
import com.summerlockin.Awa.exception.UnauthorizedException
import com.summerlockin.Awa.repository.userRepository
import com.summerlockin.Awa.service.RefreshTokenService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val userRepository: userRepository,
    private val encoder: PasswordEncoder
) {

    fun login(
        email: String,
        password: String
    ): AuthResponse {

        val user = userRepository
            .findByEmailIgnoreCase(email)
            ?: throw UnauthorizedException("Invalid credentials")

        if (!user.isActive || !encoder.matches(password, user.password)) {
            throw UnauthorizedException("Invalid credentials")
        }

        val userId = user.id?.toString()
            ?: throw UnauthorizedException("Invalid user")

        val accessToken =
            jwtService.generateAccessToken(userId, user.credentialsVersion)

        val refreshToken =
            refreshTokenService.issueRefreshToken(userId, user.credentialsVersion)

        return AuthResponse(
            accessToken,
            refreshToken
        )
    }

    fun refresh(refreshToken: String): AuthResponse {

        /*
         * rotate() validates:
         * - signature
         * - expiration
         * - token type
         * - user ID
         * - JTI
         * - MongoDB record
         * - revocation state
         * - replay
         */
        val newRefreshToken =
            refreshTokenService.rotate(refreshToken)

        val userId =
            jwtService.getUserIdFromToken(newRefreshToken)

        val tokenCredentialsVersion =
            jwtService.getCredentialsVersionFromToken(newRefreshToken)

        val user = try {
            userRepository.findById(org.bson.types.ObjectId(userId)).orElse(null)
        } catch (_: IllegalArgumentException) {
            null
        }

        if (
            user == null ||
            !user.isActive ||
            user.credentialsVersion != tokenCredentialsVersion
        ) {
            refreshTokenService.revoke(newRefreshToken)
            throw UnauthorizedException("Invalid or expired refresh token")
        }

        val newAccessToken =
            jwtService.generateAccessToken(userId, user.credentialsVersion)

        return AuthResponse(
            newAccessToken,
            newRefreshToken
        )
    }

    fun logout(
        refreshToken: String,
        revokeAllSessions: Boolean = false
    ) {

        if (revokeAllSessions) {

            val userId = try {
                jwtService.getUserIdFromToken(refreshToken)
            } catch (_: Exception) {
                return
            }

            refreshTokenService.revokeAllForUser(userId)
            return
        }

        refreshTokenService.revoke(refreshToken)
    }
}
