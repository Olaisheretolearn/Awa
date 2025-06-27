package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.ShoppingItemCreateRequest
import com.summerlockin.Awa.DTO.ShoppingItemResponse
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.ShoppingItem
import com.summerlockin.Awa.repository.ShoppingItemRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ShoppingItemService(
    private val shoppingItemRepository: ShoppingItemRepository
) {

    fun createShoppingItem(request: ShoppingItemCreateRequest): ShoppingItemResponse {
        val shoppingItem = ShoppingItem(
            roomId = ObjectId(request.roomId),
            listName = request.listName,
            itemName = request.itemName,
            addedByUserId = ObjectId(request.addedByUserId),
            addedByName = request.addedByName,
            isBought = request.isBought,
            boughtByUserId = request.boughtByUserId?.let { ObjectId(it) },
            boughtByName = request.boughtByName,
            createdAt = Instant.now()
        )
        return shoppingItemRepository.save(shoppingItem).toDTO()
    }


    fun deleteShoppingItem(itemId: String): Boolean {
        val item = shoppingItemRepository.findById(ObjectId(itemId))
            .orElseThrow { NotFoundException("Shopping item not found") }

        shoppingItemRepository.delete(item)
        return true
    }

    //patch a shopping item

    //get all shopping items in a room

    //mark as bought

    // get items by either bough or unbought // essentially by status


    //search item by name


    // get all items added by a specific user


    //get items bought by a sopecific user



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