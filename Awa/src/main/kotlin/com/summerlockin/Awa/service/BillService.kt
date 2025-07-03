package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.*
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.Bill
import com.summerlockin.Awa.repository.BillsRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BillService(
    private val billsRepository: BillsRepository
) {

    fun createBill(request: BillCreateRequest): BillResponse {
        val paidById = request.paidByUserId?.let { ObjectId(it) }
            ?: throw IllegalArgumentException("paidByUserId cannot be null")

        val splitAmongIds = request.splitAmongUserIds.map { ObjectId(it) }

        val bill = Bill(
            roomId = ObjectId(request.roomId),
            name = request.name,
            amount = request.amount,
            description = request.description,
            dueDate = request.dueDate,
            paidBy = paidById,
            isPaid = request.isPaid,
            splitAmong = splitAmongIds,
            createdAt = Instant.now()
        )

        return billsRepository.save(bill).toDTO()
    }

    fun updateBill(id: String, request: BillUpdateRequest): BillResponse {
        val bill = billsRepository.findById(ObjectId(id))
            .orElseThrow { NotFoundException("Bill not found") }

        val updatedBill = bill.copy(
            name = request.name ?: bill.name,
            amount = request.amount ?: bill.amount,
            description = request.description ?: bill.description,
            dueDate = request.dueDate ?: bill.dueDate,
            isPaid = request.isPaid ?: bill.isPaid,
            paidBy = request.paidByUserId?.let { ObjectId(it) } ?: bill.paidBy,
            splitAmong = request.splitAmongUserIds?.map { ObjectId(it) } ?: bill.splitAmong
        )

        return billsRepository.save(updatedBill).toDTO()
    }

    fun deleteBill(id: String): Boolean {
        val bill = billsRepository.findById(ObjectId(id))
            .orElseThrow { NotFoundException("Bill not found") }

        billsRepository.delete(bill)
        return true
    }

    fun markAsPaid(id: String, paidByUserId: String): BillResponse {
        val bill = billsRepository.findById(ObjectId(id))
            .orElseThrow { NotFoundException("Bill not found") }

        val updated = bill.copy(
            isPaid = true,
            paidBy = ObjectId(paidByUserId)
        )

        return billsRepository.save(updated).toDTO()
    }

    fun getBillsByRoom(roomId: String): List<BillResponse> {
        return billsRepository.findAllByRoomId(ObjectId(roomId)).map { it.toDTO() }
    }

    fun getBillsByUser(userId: String): List<BillResponse> {
        return billsRepository.findAllByPaidBy(ObjectId(userId)).map { it.toDTO() }
    }

    private fun Bill.toDTO(): BillResponse {
        return BillResponse(
            id = this.id.toString(),
            roomId = this.roomId.toString(),
            name = this.name,
            description = this.description,
            amount = this.amount,
            dueDate = this.dueDate,
            paidByUserId = this.paidBy.toString(),
            isPaid = this.isPaid,
            splitAmongUserIds = this.splitAmong.map { it.toString() }
        )
    }
}
