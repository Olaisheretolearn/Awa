package com.summerlockin.Awa.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {
    private val bcrypt = BCryptPasswordEncoder()


    //takes raw passwprds and encodes them / shoulda used delegate instead , but later
    fun encode (raw:String):String {
        return bcrypt.encode(raw)
    }


    //since its one way , we take the newe String , has that as well , and see if it matches
    //the hashed one
    fun matches(raw:String, hashed:String ):Boolean{
        return bcrypt.matches(raw,hashed)
    }
}