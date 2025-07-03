package com.summerlockin.Awa.repository

import com.summerlockin.Awa.model.Bill
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface BillsRepository : MongoRepository<Bill, ObjectId> {

    fun findAllByRoomId(roomId: ObjectId): List<Bill>

    fun findAllByRoomIdAndIsPaid(roomId: ObjectId, isPaid: Boolean): List<Bill>

    fun findAllByPaidBy(paidBy: ObjectId): List<Bill>

    fun findAllBySplitAmongContains(userId: ObjectId): List<Bill>

    fun findAllByDueDateBefore(date: java.time.Instant): List<Bill> // for overdue bills

    fun findAllByRoomIdOrderByDueDateAsc(roomId: ObjectId): List<Bill>
}
