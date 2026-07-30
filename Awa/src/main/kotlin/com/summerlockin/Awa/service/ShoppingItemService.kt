package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.MarkAsBoughtRequest
import com.summerlockin.Awa.DTO.ShoppingItemCreateRequest
import com.summerlockin.Awa.DTO.ShoppingItemResponse
import com.summerlockin.Awa.DTO.ShoppingItemUpdateRequest
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.ShoppingItem
import com.summerlockin.Awa.repository.ShoppingItemRepository
import com.summerlockin.Awa.repository.userRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ShoppingItemService(
    private val shoppingItemRepository: ShoppingItemRepository,
    private val userRepository: userRepository,
    private val authorizationService: AuthorizationService
) {


    fun createShoppingItem(roomId: String, actingUserId: String, request: ShoppingItemCreateRequest): ShoppingItemResponse {
        authorizationService.requireShoppingItemRoomAccess(roomId, actingUserId)
        val actor = userRepository.findById(ObjectId(actingUserId)).orElseThrow { NotFoundException("User not found") }
        val shoppingItem = ShoppingItem(
            roomId = ObjectId(roomId),
            listName = request.listName,
            itemName = request.itemName,
            addedByUserId = ObjectId(actingUserId),
            addedByName = actor.firstname,
            isBought = request.isBought,
            createdAt = Instant.now()
        )
        return shoppingItemRepository.save(shoppingItem).toDTO()
    }

    fun deleteShoppingItem(itemId: String, actingUserId: String): Boolean {
        val item = shoppingItemRepository.findById(ObjectId(itemId))
            .orElseThrow { NotFoundException("Shopping item not found") }

        authorizationService.requireShoppingItemRoomAccess(item.roomId.toHexString(), actingUserId)
        if (item.addedByUserId != ObjectId(actingUserId)) {
            authorizationService.requireRoomOwner(item.roomId.toHexString(), actingUserId)
        }

        shoppingItemRepository.delete(item)
        return true
    }

    //patch a shopping item
    fun editShoppingItem(itemId: String, actingUserId: String, request: ShoppingItemUpdateRequest): ShoppingItemResponse {
        val shoppingItem = shoppingItemRepository.findById(ObjectId(itemId))
            .orElseThrow {
                RuntimeException("Shopping item not found")
            }

        authorizationService.requireShoppingItemRoomAccess(shoppingItem.roomId.toHexString(), actingUserId)
        if (shoppingItem.addedByUserId != ObjectId(actingUserId)) {
            authorizationService.requireRoomOwner(shoppingItem.roomId.toHexString(), actingUserId)
        }

        val updated = shoppingItem.copy(
            roomId = request.roomId?.let { ObjectId(it) } ?: shoppingItem.roomId,
            listName = request.listName ?: shoppingItem.listName,
            itemName = request.itemName ?: shoppingItem.itemName,
            isBought = request.isBought ?: shoppingItem.isBought,
            addedByUserId = shoppingItem.addedByUserId,
            addedByName = shoppingItem.addedByName,
            boughtByUserId = shoppingItem.boughtByUserId,
            boughtByName = shoppingItem.boughtByName
        )

        return shoppingItemRepository.save(updated).toDTO()
    }


    //get all shopping items in a room
    fun getAllShoppingItems(roomId:String, actingUserId: String):List<ShoppingItemResponse>{
        authorizationService.requireShoppingItemRoomAccess(roomId, actingUserId)
        val shoppingItems = shoppingItemRepository.findAllByRoomId(ObjectId(roomId))
        return shoppingItems.map{it.toDTO()}
    }

    //get all shoppingItems  but sorted
    fun getSortedShoppingItems(roomId: String, actingUserId: String): List<ShoppingItemResponse> {
        authorizationService.requireShoppingItemRoomAccess(roomId, actingUserId)
        val items = shoppingItemRepository.findAllByRoomIdOrderByCreatedAtDesc(ObjectId(roomId))
        return items.map { it.toDTO() }
    }

    //mark as bought
    fun markAsBought(itemId: String, actingUserId: String, request: MarkAsBoughtRequest): ShoppingItemResponse {
        val item = shoppingItemRepository.findById(ObjectId(itemId))
            .orElseThrow { NotFoundException("Shopping item not found") }

        authorizationService.requireShoppingItemRoomAccess(item.roomId.toHexString(), actingUserId)
        val actor = userRepository.findById(ObjectId(actingUserId)).orElseThrow { NotFoundException("User not found") }

        val updated = item.copy(
            isBought = true,
            boughtByUserId = ObjectId(actingUserId),
            boughtByName = actor.firstname
        )

        return shoppingItemRepository.save(updated).toDTO()
    }



    // get items by either bough or unbought // essentially by status
    fun getItemStatus(itemId: String, actingUserId: String): Boolean {
        val item = shoppingItemRepository.findById(ObjectId(itemId))
            .orElseThrow {
                NotFoundException("Shopping item not found")
            }

        authorizationService.requireShoppingItemRoomAccess(item.roomId.toHexString(), actingUserId)

        return item.isBought
    }




    //search item by name
    fun searchItemsByName(roomId: String, actingUserId: String, keyword: String): List<ShoppingItemResponse> {
        authorizationService.requireShoppingItemRoomAccess(roomId, actingUserId)
        val items = shoppingItemRepository
            .findAllByRoomIdAndItemNameContainingIgnoreCase(ObjectId(roomId), keyword)

        return items.map { it.toDTO() }
    }



    // get all items added by a specific user
    fun getItemsByUser(addedByUserId: String, roomId: String, actingUserId: String): List<ShoppingItemResponse> {
        authorizationService.requireSelf(actingUserId, addedByUserId)
        authorizationService.requireShoppingItemRoomAccess(roomId, actingUserId)
        val items = shoppingItemRepository
            .findAllByRoomIdAndAddedByUserId(ObjectId(roomId), ObjectId(addedByUserId))

        return items.map { it.toDTO() }
    }


    //get items bought by a sopecific user
    fun getItemsBoughtByUser(boughtBy: String, roomId: String, actingUserId: String): List<ShoppingItemResponse> {
        authorizationService.requireSelf(actingUserId, boughtBy)
        authorizationService.requireShoppingItemRoomAccess(roomId, actingUserId)
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