package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "driver_payouts",
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
        Index(value = ["date"])
    ]
)
data class DriverPayout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val driverName: String,
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val paymentMode: String = "Cash", // Cash, Mobile Wallet, Bank Transfer
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
