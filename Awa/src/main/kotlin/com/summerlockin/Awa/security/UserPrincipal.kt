package com.summerlockin.Awa.security

import com.summerlockin.Awa.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import kotlin.text.toHexString

class UserPrincipal(private val user : User) : UserDetails{

    fun getId(): String = user.id?.toHexString()
        ?: throw IllegalStateException("User ID is null")


    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    }



    override fun getPassword(): String = user.password
    override fun getUsername(): String = user.email

    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}