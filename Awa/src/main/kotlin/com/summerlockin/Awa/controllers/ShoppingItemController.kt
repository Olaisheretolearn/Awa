package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.MarkAsBoughtRequest
import com.summerlockin.Awa.DTO.ShoppingItemCreateRequest
import com.summerlockin.Awa.DTO.ShoppingItemResponse
import com.summerlockin.Awa.DTO.ShoppingItemUpdateRequest
import com.summerlockin.Awa.security.UserPrincipal
import com.summerlockin.Awa.service.ShoppingItemService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/room/{roomId}/shopping")
class ShoppingItemController(
    private val shoppingItemService: ShoppingItemService
) {

    @PostMapping
    fun createShoppingItem(
        @PathVariable roomId: String,
        @RequestBody request: ShoppingItemCreateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShoppingItemResponse> {
        val item = shoppingItemService.createShoppingItem(roomId, principal.getId(), request)
        return ResponseEntity.status(201).body(item)
    }



    @PatchMapping("/{itemId}")
    fun updateShoppingItem(
        @PathVariable itemId: String,
        @RequestBody request: ShoppingItemUpdateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShoppingItemResponse> {
        val updated = shoppingItemService.editShoppingItem(itemId, principal.getId(), request)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{itemId}")
    fun deleteShoppingItem(@PathVariable itemId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<String> {
        shoppingItemService.deleteShoppingItem(itemId, principal.getId())
        return ResponseEntity.ok("Item deleted successfully")
    }

    @GetMapping
    fun getAllItems(@PathVariable roomId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<ShoppingItemResponse>> {
        val items = shoppingItemService.getAllShoppingItems(roomId, principal.getId())
        return ResponseEntity.ok(items)
    }

    @GetMapping("/sorted")
    fun getSortedItems(@PathVariable roomId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<ShoppingItemResponse>> {
        val items = shoppingItemService.getSortedShoppingItems(roomId, principal.getId())
        return ResponseEntity.ok(items)
    }

    @PatchMapping("/{itemId}/bought")
    fun markItemAsBought(
        @PathVariable itemId: String,
        @RequestBody request: MarkAsBoughtRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShoppingItemResponse> {
        val item = shoppingItemService.markAsBought(itemId, principal.getId(), request)
        return ResponseEntity.ok(item)
    }

    @GetMapping("/{itemId}/status")
    fun getItemStatus(@PathVariable itemId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<Boolean> {
        val status = shoppingItemService.getItemStatus(itemId, principal.getId())
        return ResponseEntity.ok(status)
    }

    @GetMapping("/search")
    fun searchItems(
        @PathVariable roomId: String,
        @RequestParam keyword: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<ShoppingItemResponse>> {
        val items = shoppingItemService.searchItemsByName(roomId, principal.getId(), keyword)
        return ResponseEntity.ok(items)
    }

    @GetMapping("/added-by/{userId}")
    fun getItemsByUser(
        @PathVariable roomId: String,
        @PathVariable userId: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<ShoppingItemResponse>> {
        val items = shoppingItemService.getItemsByUser(userId, roomId, principal.getId())
        return ResponseEntity.ok(items)
    }

    @GetMapping("/bought-by/{userId}")
    fun getItemsBoughtByUser(
        @PathVariable roomId: String,
        @PathVariable userId: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<ShoppingItemResponse>> {
        val items = shoppingItemService.getItemsBoughtByUser(userId, roomId, principal.getId())
        return ResponseEntity.ok(items)
    }
}
