package com.summerlockin.Awa.DTO

import com.summerlockin.Awa.model.ShareStatus
import java.time.Instant

data class BillShareResponse(
    val userId: String,
    val amount: Double,
    val status: ShareStatus,
    val markedPaidAt: Instant?,
    val confirmedPaidAt: Instant?
)
