package com.summerlockin.Awa.DTO

import org.bson.types.ObjectId
import java.time.Instant

data class ShoppingItemCreateRequest(
    val roomId: String,
    val listName: String,
    val itemName: String,
    val addedByUserId: String,
    val addedByName: String,
    val isBought: Boolean = false,
    val boughtByUserId: String? = null,
    val boughtByName: String? = null,
    val createdAt: Instant = Instant.now()
)

