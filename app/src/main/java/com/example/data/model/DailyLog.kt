package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_logs",
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
        Index(value = ["date"]),
        Index(value = ["vehicleId", "date"], unique = true)
    ]
)
data class DailyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val date: String, // Format: YYYY-MM-DD
    val grossIncome: Double = 0.0,
    
    // Breakdown of daily operating expenses
    val fuelExpense: Double = 0.0,
    val tollAndTaxExpense: Double = 0.0,
    val maintenanceExpense: Double = 0.0,
    val challanOrFineExpense: Double = 0.0,
    val helperWageExpense: Double = 0.0,
    val foodOrMiscExpense: Double = 0.0,
    
    // Total operating expenses excluding driver
    val totalOperatingExpenses: Double = 0.0,
    
    // Driver salary calculation
    val driverWageEarned: Double = 0.0,
    val driverPaidAmount: Double = 0.0,
    val isDriverFullyPaid: Boolean = true,
    
    // Net profit for the day: Gross Income - Total Expenses - Driver Wage
    val netDailyProfit: Double = 0.0,
    
    val odometerReading: Double = 0.0,
    val tripsOrRounds: Int = 1,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
