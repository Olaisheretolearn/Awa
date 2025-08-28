// com.summerlockin.Awa.service.BillService
package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.*
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.*
import com.summerlockin.Awa.repository.BillsRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Service
class BillService(
    private val billsRepository: BillsRepository,
) {

    fun createBill(req: BillCreateRequest): BillResponse {
        val creator = req.paidByUserId?.let(::ObjectId)
            ?: throw IllegalArgumentException("paidByUserId is required")

        // participants; ensure payer isn’t in shares
        val raw = (req.splitAmongUserIds ?: emptyList()).map(::ObjectId)
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

    fun getBillsByRoom(roomId: String): List<BillResponse> =
        billsRepository.findAllByRoomId(ObjectId(roomId)).map { it.toDTO() }

    fun deleteBill(id: String): Boolean {
        val bill = find(id)
        billsRepository.delete(bill)
        return true
    }

    /** Debtor taps "Mark as paid" */
    fun markSharePaid(billId: String, debtorUserId: String): BillResponse {
        val bill = find(billId)
        val idx = bill.shares.indexOfFirst { it.userId == ObjectId(debtorUserId) }
        if (idx == -1) throw NotFoundException("Share not found for user")
        val s = bill.shares[idx]
        if (s.status == ShareStatus.CONFIRMED) return bill.toDTO()

        bill.shares[idx] = s.copy(status = ShareStatus.MARKED_PAID, markedPaidAt = Instant.now())
        return finalizeAndSave(bill).toDTO()
    }


    fun confirmShare(billId: String, creatorUserId: String, debtorUserId: String, confirm: Boolean): BillResponse {
        val bill = find(billId)
        require(bill.paidBy == ObjectId(creatorUserId)) { "Only creator can confirm" }

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
