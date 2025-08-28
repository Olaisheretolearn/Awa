package com.summerlockin.Awa.security

import com.summerlockin.Awa.repository.userRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: userRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmailIgnoreCase(email)
            ?: throw UsernameNotFoundException("User with email $email not found")

        return UserPrincipal(user)
    }
}
