package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bank_installments",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["vehicleId"]),
        Index(value = ["yearMonth"]),
        Index(value = ["vehicleId", "yearMonth"], unique = true)
    ]
)
data class BankInstallment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val yearMonth: String, // Format: YYYY-MM
    val bankName: String,
    val amountDue: Double,
    val amountPaid: Double = 0.0,
    val paymentDate: String = "", // Format: YYYY-MM-DD
    val dueDay: Int = 10,
    val isPaid: Boolean = false,
    val referenceOrTxnNo: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
