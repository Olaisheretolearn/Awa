package com.summerlockin.Awa.repository

import com.summerlockin.Awa.model.Task
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface TaskRepository : MongoRepository<Task, ObjectId> {
    fun findByRoomId(roomId: ObjectId): List<Task>

    fun findByAssignedTo(userId: ObjectId): List<Task>

    fun findByRoomIdAndIsComplete(roomId: ObjectId, isComplete: Boolean): List<Task>

    fun findByAssignedToAndIsComplete(userId: ObjectId, isComplete: Boolean): List<Task>

}