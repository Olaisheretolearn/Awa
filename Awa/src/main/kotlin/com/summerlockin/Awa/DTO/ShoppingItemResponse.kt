package com.summerlockin.Awa.DTO

import java.time.Instant

data class ShoppingItemResponse(
    val id : String,
    val roomId: String,
    val listName: String,
    val itemName: String,
    val addedByUserId: String,
    val addedByName: String,
    val isBought: Boolean = false,
    val boughtByUserId: String? = null,
    val boughtByName: String? = null,
    val createdAt: Instant
)
