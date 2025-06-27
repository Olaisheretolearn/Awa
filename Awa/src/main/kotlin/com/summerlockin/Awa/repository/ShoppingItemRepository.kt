package com.summerlockin.Awa.repository

import com.summerlockin.Awa.model.ShoppingItem
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ShoppingItemRepository: MongoRepository<ShoppingItem, ObjectId> {
    fun findAllByRoomId(roomId: ObjectId): List<ShoppingItem>

    fun findAllByRoomIdAndListName(roomId: ObjectId, listName: String): List<ShoppingItem>

    fun findAllByRoomIdAndIsBought(roomId: ObjectId, isBought: Boolean): List<ShoppingItem>

    fun findAllByRoomIdAndAddedBy(roomId: ObjectId, addedBy: ObjectId): List<ShoppingItem>

    fun findAllByRoomIdAndBoughtBy(roomId: ObjectId, boughtBy: ObjectId): List<ShoppingItem>



    //just incase
    fun findAllByRoomIdAndItemNameContainingIgnoreCase(roomId: ObjectId, keyword: String): List<ShoppingItem>
    fun findAllByRoomIdOrderByCreatedAtDesc(roomId: ObjectId): List<ShoppingItem>

}