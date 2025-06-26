package com.summerlockin.Awa.repository

import com.summerlockin.Awa.model.Message
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant
import java.util.Optional

interface MessageRepository : MongoRepository<Message, ObjectId> {
    fun findAllByIdIn(messageIds: List<ObjectId>): List<Message>

    fun findAllByRoomIdOrderByTimestampAsc(roomId: ObjectId): List<Message>

    fun findAllByRoomIdAndTimestampAfter(roomId: ObjectId, after: Instant): List<Message>

}
