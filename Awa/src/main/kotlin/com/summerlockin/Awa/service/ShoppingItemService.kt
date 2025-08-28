package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.MarkAsBoughtRequest
import com.summerlockin.Awa.DTO.ShoppingItemCreateRequest
import com.summerlockin.Awa.DTO.ShoppingItemResponse
import com.summerlockin.Awa.DTO.ShoppingItemUpdateRequest
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


    fun createShoppingItem(roomId: String, request: ShoppingItemCreateRequest): ShoppingItemResponse {
        val shoppingItem = ShoppingItem(
            roomId = ObjectId(roomId),
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
    fun editShoppingItem(itemId: String, request: ShoppingItemUpdateRequest): ShoppingItemResponse {
        val shoppingItem = shoppingItemRepository.findById(ObjectId(itemId))
            .orElseThrow {
                RuntimeException("Shopping item not found")
            }

        val updated = shoppingItem.copy(
            roomId = request.roomId?.let { ObjectId(it) } ?: shoppingItem.roomId,
            listName = request.listName ?: shoppingItem.listName,
            itemName = request.itemName ?: shoppingItem.itemName,
            addedByUserId = request.addedByUserId?.let { ObjectId(it) } ?: shoppingItem.addedByUserId,
            addedByName = request.addedByName ?: shoppingItem.addedByName,
            isBought = request.isBought ?: shoppingItem.isBought,
            boughtByUserId = request.boughtByUserId?.let { ObjectId(it) } ?: shoppingItem.boughtByUserId,
            boughtByName = request.boughtByName ?: shoppingItem.boughtByName
        )

        return shoppingItemRepository.save(updated).toDTO()
    }


    //get all shopping items in a room
    fun getAllShoppingItems(roomId:String):List<ShoppingItemResponse>{
        val shoppingItems = shoppingItemRepository.findAllByRoomId(ObjectId(roomId))
        return shoppingItems.map{it.toDTO()}
    }

    //get all shoppingItems  but sorted
    fun getSortedShoppingItems(roomId: String): List<ShoppingItemResponse> {
        val items = shoppingItemRepository.findAllByRoomIdOrderByCreatedAtDesc(ObjectId(roomId))
        return items.map { it.toDTO() }
    }

    //mark as bought
    fun markAsBought(itemId: String, request: MarkAsBoughtRequest): ShoppingItemResponse {
        val item = shoppingItemRepository.findById(ObjectId(itemId))
            .orElseThrow { NotFoundException("Shopping item not found") }

        val updated = item.copy(
            isBought = true,
            boughtByUserId = ObjectId(request.boughtByUserId),
            boughtByName = request.boughtByName
        )

        return shoppingItemRepository.save(updated).toDTO()
    }



    // get items by either bough or unbought // essentially by status
    fun getItemStatus(itemId: String): Boolean {
        val item = shoppingItemRepository.findById(ObjectId(itemId))
            .orElseThrow {
                NotFoundException("Shopping item not found")
            }

        return item.isBought
    }




    //search item by name
    fun searchItemsByName(roomId: String, keyword: String): List<ShoppingItemResponse> {
        val items = shoppingItemRepository
            .findAllByRoomIdAndItemNameContainingIgnoreCase(ObjectId(roomId), keyword)

        return items.map { it.toDTO() }
    }



    // get all items added by a specific user
    fun getItemsByUser(addedByUserId: String, roomId: String): List<ShoppingItemResponse> {
        val items = shoppingItemRepository
            .findAllByRoomIdAndAddedByUserId(ObjectId(roomId), ObjectId(addedByUserId))

        return items.map { it.toDTO() }
    }


    //get items bought by a sopecific user
    fun getItemsBoughtByUser(boughtBy: String, roomId: String): List<ShoppingItemResponse> {
        val items = shoppingItemRepository
            .findAllByRoomIdAndBoughtByUserId(ObjectId(roomId), ObjectId(boughtBy))

        return items.map { it.toDTO() }
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