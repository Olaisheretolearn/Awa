package com.summerlockin.Awa.DTO

data class ShoppingItemUpdateRequest(
    val roomId: String? = null,
    val listName: String? = null,
    val itemName: String? = null,
    val isBought: Boolean? = null,
)
