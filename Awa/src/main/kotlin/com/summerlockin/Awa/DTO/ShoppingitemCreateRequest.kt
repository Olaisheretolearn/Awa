package com.summerlockin.Awa.DTO

data class ShoppingItemCreateRequest(
    val listName: String,
    val itemName: String,
    val isBought: Boolean = false,
)
