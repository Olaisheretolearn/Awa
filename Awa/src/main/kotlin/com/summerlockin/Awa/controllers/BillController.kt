// com.summerlockin.Awa.controllers.BillController
package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.*
import com.summerlockin.Awa.security.UserPrincipal
import com.summerlockin.Awa.service.BillService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/room/{roomId}/bill")
class BillController(
    private val billService: BillService
) {
    @PostMapping
    fun createBill(
        @PathVariable roomId: String,
        @RequestBody request: BillCreateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<BillResponse> {
        val bill = billService.createBill(request.copy(roomId = roomId), principal.getId())
        return ResponseEntity.status(201).body(bill)
    }

    @GetMapping
    fun getBillsByRoom(
        @PathVariable roomId: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<BillResponse>> {
        val bills = billService.getBillsByRoom(roomId, principal.getId())
        return ResponseEntity.ok(bills)
    }

    @DeleteMapping("/{billId}")
    fun deleteBill(
        @PathVariable billId: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<String> {
        billService.deleteBill(billId, principal.getId())
        return ResponseEntity.ok("Bill deleted successfully")
    }


    @PatchMapping("/{billId}/shares/{debtorUserId}/mark-paid")
    fun markSharePaid(
        @PathVariable billId: String,
        @PathVariable debtorUserId: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<BillResponse> {
        val updated = billService.markSharePaid(billId, debtorUserId, principal.getId())
        return ResponseEntity.ok(updated)
    }


    @PatchMapping("/{billId}/shares/{debtorUserId}/confirm")
    fun confirmShare(
        @PathVariable billId: String,
        @PathVariable debtorUserId: String,
        @RequestParam(defaultValue = "true") confirm: Boolean,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<BillResponse> {
        val updated = billService.confirmShare(billId, debtorUserId, principal.getId(), confirm)
        return ResponseEntity.ok(updated)
    }
}
