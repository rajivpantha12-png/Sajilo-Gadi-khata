package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VehicleType(val displayName: String, val iconName: String) {
    BUS("Bus", "DirectionsBus"),
    MICROBUS("Micro / Van", "AirportShuttle"),
    TAXI("Taxi / Cab", "LocalTaxi"),
    AUTO("Auto / Tuk-Tuk", "ElectricRickshaw"),
    TRUCK("Truck / Pickup", "LocalShipping"),
    OTHER("Other Vehicle", "Commute")
}

enum class SalaryType(val displayName: String) {
    FIXED_DAILY("Fixed Daily Wage"),
    PERCENT_OF_INCOME("% of Daily Gross Income"),
    PERCENT_OF_NET("% of Daily Net Fares")
}

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val regNumber: String,
    val nameOrModel: String,
    val type: String = VehicleType.BUS.name,
    val routeOrArea: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val salaryType: String = SalaryType.FIXED_DAILY.name,
    val salaryRate: Double = 1000.0, // Fixed daily amount or percentage (e.g. 15.0 for 15%)
    val bankName: String = "",
    val monthlyEmi: Double = 0.0,
    val emiDueDay: Int = 10, // Day of month (1-31)
    val totalLoanAmount: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
