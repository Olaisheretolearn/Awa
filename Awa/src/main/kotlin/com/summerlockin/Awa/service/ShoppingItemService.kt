package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.ShoppingItemCreateRequest
import com.summerlockin.Awa.DTO.ShoppingItemResponse
import com.summerlockin.Awa.model.ShoppingItem
import com.summerlockin.Awa.repository.ShoppingItemRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ShoppingItemService(
    private val shoppingItemRepository: ShoppingItemRepository
) {

    fun createShoppingItem(request : ShoppingItemCreateRequest): ShoppingItemResponse {
        val shoppingItem = ShoppingItem(
            roomId = ObjectId(request.roomId),
            listName = request.listName,
            itemName = request.itemName,
            addedByUserId = ObjectId(request.addedByUserId),
            addedByName = request.addedByName,
            isBought = request.isBought,
            boughtByUserId = ObjectId(request.boughtByUserId),
            boughtByName =  request.boughtByName,
            createdAt = Instant.now()
        )
        return shoppingItemRepository.save(shoppingItem).toDTO()
    }

    fun ShoppingItem.toDTO(): ShoppingItemResponse {
        return ShoppingItemResponse(
            id = this.id.toString(),
            roomId = this.roomId.toString(),
            listName = this.listName,
            itemName = this.itemName,
            addedByUserId = this.addedByUserId.toString(),
            addedByName = this.addedByName,
            isBought = this.isBought,
            boughtByUserId = this.boughtByUserId?.toString(),
            boughtByName = this.boughtByName,
            createdAt = this.createdAt
        )
    }



}