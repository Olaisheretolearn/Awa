package com.summerlockin.Awa.DTO

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import java.time.Instant

data class ShoppingItemUpdateRequest(
    val roomId: String? = null,
    val listName: String? = null,
    val itemName: String? = null,
    val addedByUserId: String? = null,
    val addedByName: String? = null,
    val isBought: Boolean? = null,
    val boughtByUserId: String? = null,
    val boughtByName: String? = null
)
