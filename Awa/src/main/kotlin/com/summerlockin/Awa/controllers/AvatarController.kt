package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.model.AvatarId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// controllers/AvatarController.kt
@RestController
@RequestMapping("/api/avatars")
class AvatarController {
    @GetMapping
    fun list(): List<Map<String, String>> =
        AvatarId.values().map {
            mapOf("id" to it.name, "title" to it.title, "imageUrl" to it.imageUrl)
        }
}
