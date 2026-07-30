// com.summerlockin.Awa.service.BillService
package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.*
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.*
import com.summerlockin.Awa.repository.BillsRepository
import org.bson.types.ObjectId
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Service
class BillService(
    private val billsRepository: BillsRepository,
    private val authorizationService: AuthorizationService,
) {

    fun createBill(req: BillCreateRequest, actingUserId: String): BillResponse {
        authorizationService.requireBillRoomAccess(req.roomId, actingUserId)
        val creator = ObjectId(actingUserId)

        // participants; ensure payer isn’t in shares
        val raw = (req.splitAmongUserIds ?: emptyList()).map(::ObjectId)
        authorizationService.requireUsersInRoom(req.roomId, raw.map { it.toHexString() })
        val participants = raw.filterNot { it == creator }
        require(participants.isNotEmpty()) { "No participants to split across (besides payer)" }

        // Split exactly to 2dp, fix rounding remainder by adding cents to first shares
        val total = BigDecimal.valueOf(req.amount).setScale(2, RoundingMode.HALF_UP)
        val n = BigDecimal.valueOf(participants.size.toLong())
        val per = total.divide(n, 2, RoundingMode.HALF_UP)

        val shares = participants.map { BillShare(userId = it, amount = per.toDouble()) }.toMutableList()

        // adjust remainder (if any) to the first share to keep sum precise
        val remainder = total.subtract(per.multiply(n)).toDouble()
        if (remainder != 0.0) {
            val first = shares.first()
            shares[0] = first.copy(amount = BigDecimal.valueOf(first.amount).add(BigDecimal.valueOf(remainder)).toDouble())
        }

        val bill = Bill(
            roomId = ObjectId(req.roomId),
            name = req.name,
            description = req.description,
            amount = total.toDouble(),
            dueDate = req.dueDate,
            paidBy = creator,
            splitAmong = raw,
            shares = shares,
            isPaid = false
        )

        return billsRepository.save(bill).toDTO()
    }

    fun getBillsByRoom(roomId: String, actingUserId: String): List<BillResponse> {
        authorizationService.requireBillRoomAccess(roomId, actingUserId)
        return billsRepository.findAllByRoomId(ObjectId(roomId)).map { it.toDTO() }
    }

    fun deleteBill(id: String, actingUserId: String): Boolean {
        val bill = find(id)
        authorizationService.requireBillRoomAccess(bill.roomId.toHexString(), actingUserId)
        if (bill.paidBy != ObjectId(actingUserId)) {
            authorizationService.requireRoomOwner(bill.roomId.toHexString(), actingUserId)
        }
        billsRepository.delete(bill)
        return true
    }

    /** Debtor taps "Mark as paid" */
    fun markSharePaid(billId: String, debtorUserId: String, actingUserId: String): BillResponse {
        val bill = find(billId)
        authorizationService.requireBillRoomAccess(bill.roomId.toHexString(), actingUserId)
        authorizationService.requireSelf(actingUserId, debtorUserId)
        val idx = bill.shares.indexOfFirst { it.userId == ObjectId(debtorUserId) }
        if (idx == -1) throw NotFoundException("Share not found for user")
        val s = bill.shares[idx]
        if (s.status == ShareStatus.CONFIRMED) return bill.toDTO()

        bill.shares[idx] = s.copy(status = ShareStatus.MARKED_PAID, markedPaidAt = Instant.now())
        return finalizeAndSave(bill).toDTO()
    }


    fun confirmShare(billId: String, debtorUserId: String, actingUserId: String, confirm: Boolean): BillResponse {
        val bill = find(billId)
        authorizationService.requireBillRoomAccess(bill.roomId.toHexString(), actingUserId)
        if (bill.paidBy != ObjectId(actingUserId)) {
            throw AccessDeniedException("Only creator can confirm")
        }

        val idx = bill.shares.indexOfFirst { it.userId == ObjectId(debtorUserId) }
        if (idx == -1) throw NotFoundException("Share not found for user")

        val s = bill.shares[idx]
        bill.shares[idx] = if (confirm) {
            s.copy(status = ShareStatus.CONFIRMED, confirmedPaidAt = Instant.now())
        } else {
            s.copy(status = ShareStatus.PENDING, markedPaidAt = null)
        }

        return finalizeAndSave(bill).toDTO()
    }



    private fun find(id: String) =
        billsRepository.findById(ObjectId(id)).orElseThrow { NotFoundException("Bill not found") }

    private fun finalizeAndSave(bill: Bill): Bill {
        val allConfirmed = bill.shares.all { it.status == ShareStatus.CONFIRMED }
        val updated = bill.copy(isPaid = allConfirmed)
        return billsRepository.save(updated)
    }

    private fun Bill.toDTO(): BillResponse {
        val totalOwed = shares.filter { it.status != ShareStatus.CONFIRMED }
            .map { it.amount }
            .fold(0.0) { a, b -> (BigDecimal.valueOf(a) + BigDecimal.valueOf(b)).toDouble() }

        return BillResponse(
            id = id.toString(),
            roomId = roomId.toString(),
            name = name,
            description = description,
            amount = amount,
            dueDate = dueDate,
            paidByUserId = paidBy.toString(),
            isPaid = isPaid,
            splitAmongUserIds = splitAmong.map { it.toString() },
            shares = shares.map {
                BillShareResponse(
                    userId = it.userId.toString(),
                    amount = it.amount,
                    status = it.status,
                    markedPaidAt = it.markedPaidAt,
                    confirmedPaidAt = it.confirmedPaidAt
                )
            },
            totalOwedToCreator = BigDecimal.valueOf(totalOwed).setScale(2, RoundingMode.HALF_UP).toDouble()
        )
    }
}
