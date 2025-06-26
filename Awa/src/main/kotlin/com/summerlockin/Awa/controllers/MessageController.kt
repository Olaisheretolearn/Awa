package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.MessageCreateRequest
import com.summerlockin.Awa.DTO.MessageResponse
import com.summerlockin.Awa.service.MessageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/message")
class MessageController(
    private val messageService: MessageService,
) {
    @PostMapping
    fun createMessage(@RequestBody request: MessageCreateRequest): ResponseEntity<MessageResponse> {
        val createdMessage = messageService.createMessage(request)
        return ResponseEntity.status(201).body(createdMessage)
    }

    @GetMapping("api/messages/{roomId}")
    fun getMessages(@PathVariable roomId: String): ResponseEntity<List<MessageResponse>> {
        val messages = messageService.getMessages(roomId)
        return ResponseEntity.ok().body(messages)
    }



}