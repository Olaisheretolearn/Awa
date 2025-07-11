package com.summerlockin.Awa.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("ShoppingItem")
data class ShoppingItem(
    @Id val id: ObjectId? =null,
    val roomId : ObjectId,
    val listName: String = "General",
    val itemName: String,
    val addedByUserId: ObjectId,
    val addedByName:String, // this should autofilled by frontend
    val isBought : Boolean = false,
    val boughtByUserId: ObjectId? = null,
    val boughtByName:String? = null,
    val createdAt: Instant = Instant.now()
)
