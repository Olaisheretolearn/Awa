// com.summerlockin.Awa.controllers.BillController
package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.*
import com.summerlockin.Awa.service.BillService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/room/{roomId}/bill")
class BillController(
    private val billService: BillService
) {
    @PostMapping
    fun createBill(
        @PathVariable roomId: String,
        @RequestBody request: BillCreateRequest
    ): ResponseEntity<BillResponse> {
        val bill = billService.createBill(request.copy(roomId = roomId))
        return ResponseEntity.status(201).body(bill)
    }

    @GetMapping
    fun getBillsByRoom(
        @PathVariable roomId: String
    ): ResponseEntity<List<BillResponse>> {
        val bills = billService.getBillsByRoom(roomId)
        return ResponseEntity.ok(bills)
    }

    @DeleteMapping("/{billId}")
    fun deleteBill(
        @PathVariable billId: String
    ): ResponseEntity<String> {
        billService.deleteBill(billId)
        return ResponseEntity.ok("Bill deleted successfully")
    }


    @PatchMapping("/{billId}/shares/{debtorUserId}/mark-paid")
    fun markSharePaid(
        @PathVariable billId: String,
        @PathVariable debtorUserId: String
    ): ResponseEntity<BillResponse> {
        val updated = billService.markSharePaid(billId, debtorUserId)
        return ResponseEntity.ok(updated)
    }


    @PatchMapping("/{billId}/shares/{debtorUserId}/confirm")
    fun confirmShare(
        @PathVariable billId: String,
        @PathVariable debtorUserId: String,
        @RequestParam creatorUserId: String,
        @RequestParam(defaultValue = "true") confirm: Boolean
    ): ResponseEntity<BillResponse> {
        val updated = billService.confirmShare(billId, creatorUserId, debtorUserId, confirm)
        return ResponseEntity.ok(updated)
    }
}
