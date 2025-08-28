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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/room/{roomId}/message")
class MessageController(
    private val messageService: MessageService,
) {
    @PostMapping
    fun createMessage(
        @PathVariable roomId: String,
        @RequestBody request: MessageCreateRequest
    ): ResponseEntity<MessageResponse> {
        val updatedRequest = request.copy(roomId = roomId)
        val createdMessage = messageService.createMessage(updatedRequest)
        return ResponseEntity.status(201).body(createdMessage)
    }

    @GetMapping
    fun getMessages(
        @PathVariable roomId: String,
        @RequestParam(required = false) after: String?
    ): ResponseEntity<List<MessageResponse>> {
        val msgs = if (after.isNullOrBlank()) {
            messageService.getMessages(roomId)
        } else {
            messageService.getMessagesAfter(roomId, after)
        }
        return ResponseEntity.ok(msgs)
    }




    @PostMapping("/{messageId}/react")
    fun reactToMessage(
        @PathVariable roomId: String,
        @PathVariable messageId: String,
        @RequestParam userId: String,
        @RequestParam emoji: String
    ): ResponseEntity<MessageResponse> {
        val updated = messageService.reactToMessage(messageId, userId, emoji)
        return ResponseEntity.ok(updated)
    }

}