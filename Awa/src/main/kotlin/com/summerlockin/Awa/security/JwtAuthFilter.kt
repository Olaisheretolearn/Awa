package com.summerlockin.Awa.security

import com.summerlockin.Awa.repository.userRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.bson.types.ObjectId
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userRepository: userRepository
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val authHeader =
            request.getHeader("Authorization")

        if (
            authHeader == null ||
            !authHeader.startsWith("Bearer ")
        ) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        try {

            if (!jwtService.validateToken(token)) {
                filterChain.doFilter(request, response)
                return
            }

            if (jwtService.getTokenType(token) != "access") {
                filterChain.doFilter(request, response)
                return
            }

            val userId =
                jwtService.getUserIdFromToken(token)

            val credentialsVersion =
                jwtService.getCredentialsVersionFromToken(token)

            val objectId = try {
                ObjectId(userId)
            } catch (_: IllegalArgumentException) {
                filterChain.doFilter(request, response)
                return
            }

            val user =
                userRepository
                    .findById(objectId)
                    .orElse(null)

            if (
                user != null &&
                user.isActive &&
                user.credentialsVersion == credentialsVersion
            ) {

                val userDetails: UserDetails =
                    UserPrincipal(user)

                val authentication =
                    UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    ).apply {
                        details =
                            WebAuthenticationDetailsSource()
                                .buildDetails(request)
                    }

                SecurityContextHolder
                    .getContext()
                    .authentication = authentication
            }

        } catch (_: Exception) {
          
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}
