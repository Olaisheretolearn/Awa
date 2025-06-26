package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.MessageCreateRequest
import com.summerlockin.Awa.DTO.MessageResponse
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.Message
import com.summerlockin.Awa.model.Reaction
import com.summerlockin.Awa.repository.MessageRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

@Service
class MessageService(
    private val messageRepository: MessageRepository
) {

    fun createMessage(request: MessageCreateRequest): MessageResponse {
        val message = Message(
            content = request.content,
            senderId = ObjectId(request.senderId),
            senderName = request.senderName,
            roomId = ObjectId(request.roomId),
            attachmentUrls = request.attachmentUrls
        )
        return messageRepository.save(message).toDTO()
    }

    fun getMessages (roomId: String): List<MessageResponse> {
        val messages = messageRepository.findAllByRoomIdOrderByTimestampAsc(ObjectId(roomId))
        return messages.map { it.toDTO() }
    }

    fun reactToMessage(messageId: String, userId: String, emoji: String): MessageResponse {
        val message = messageRepository.findById(ObjectId(messageId)).orElseThrow {
            NotFoundException("Message not found with id $messageId")
        }

        //check if they reacted already
        val existingReaction = message.reactions.find { it.userId == userId }

        if (existingReaction != null) { //IF they react with the same emoji , remove the emoji
            if (existingReaction.emoji == emoji) {
                message.reactions.removeIf { it.userId == userId }
            } else {
                //else if they have reacted and they react with a new emoji, replace
                message.reactions.removeIf { it.userId == userId }
                message.reactions.add(Reaction(userId = userId, emoji = emoji))
            }
        } else {
            message.reactions.add(Reaction(userId = userId, emoji = emoji))
        }

        val updatedMessage = messageRepository.save(message)
        return updatedMessage.toDTO()
    }






    private fun Message.toDTO(): MessageResponse {
        return MessageResponse(
            id = this.id.toString(),
            roomId = this.roomId.toString(),
            senderId = this.senderId.toString(),
            senderName = this.senderName,
            content = this.content,
            timestamp = DateTimeFormatter.ISO_INSTANT.format(this.timestamp.atOffset(ZoneOffset.UTC)),
            reactions = this.reactions,
            attachmentUrls = this.attachmentUrls
        )
    }
}
