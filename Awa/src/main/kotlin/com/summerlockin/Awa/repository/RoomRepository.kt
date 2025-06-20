package com.summerlockin.Awa.repository
import com.summerlockin.Awa.model.Room
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface  RoomRepository : MongoRepository<Room, ObjectId> {
    fun findByCode( code :String) :Room?
}