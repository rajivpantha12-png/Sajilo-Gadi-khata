package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreVehicle(
    @DocumentId val id: String = "",
    val regNumber: String = "",
    val nameOrModel: String = "",
    val type: String = "BUS", // BUS, EV_BUS, MICROBUS, EV_VAN, TAXI, AUTO, EV_AUTO, TRUCK, OTHER
    val isElectric: Boolean = false,
    val routeOrArea: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val salaryType: String = "FIXED_DAILY", // FIXED_DAILY, PERCENT_OF_INCOME, PERCENT_OF_NET
    val salaryRate: Double = 1200.0,
    val bankName: String = "",
    val monthlyEmi: Double = 0.0,
    val emiDueDay: Long = 10L,
    val totalLoanAmount: Double = 0.0,
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreDailyLog(
    @DocumentId val id: String = "",
    val vehicleId: String = "",
    val vehicleReg: String = "",
    val date: String = "", // YYYY-MM-DD
    val grossIncome: Double = 0.0,
    
    // Fuel & EV Charging
    val fuelExpense: Double = 0.0,
    val isEvCharging: Boolean = false,
    val evUnitsKwh: Double = 0.0,
    val evRatePerUnit: Double = 0.0,
    val evChargingExpense: Double = 0.0,
    
    // Operating expenses
    val tollAndTaxExpense: Double = 0.0,
    val maintenanceExpense: Double = 0.0,
    val maintenanceWorkDetails: String = "", // e.g. "Brake shoe replaced, Mobil/Engine oil changed, Air filter"
    val maintenanceBillNo: String = "", // e.g. "BILL-9041"
    val maintenanceVendor: String = "", // Workshop name
    val challanOrFineExpense: Double = 0.0,
    val helperWageExpense: Double = 0.0,
    val foodOrMiscExpense: Double = 0.0,
    val totalOperatingExpenses: Double = 0.0,
    
    // Driver daily wage calculation
    val driverWageEarned: Double = 0.0,
    val driverPaidAmount: Double = 0.0,
    val isDriverFullyPaid: Boolean = true,
    
    // Net profit = Gross - Operating - Driver
    val netDailyProfit: Double = 0.0,
    val tripsOrRounds: Long = 1L,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreDriverPayout(
    @DocumentId val id: String = "",
    val vehicleId: String = "",
    val vehicleReg: String = "",
    val driverName: String = "",
    val amount: Double = 0.0,
    val date: String = "", // YYYY-MM-DD
    val paymentMode: String = "Cash", // Cash, eSewa/Mobile Wallet, Bank Transfer
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreBankInstallment(
    @DocumentId val id: String = "",
    val vehicleId: String = "",
    val vehicleReg: String = "",
    val yearMonth: String = "", // YYYY-MM
    val bankName: String = "",
    val amountDue: Double = 0.0,
    val amountPaid: Double = 0.0,
    val paymentDate: String = "", // YYYY-MM-DD
    val dueDay: Long = 10L,
    val isPaid: Boolean = false,
    val referenceOrTxnNo: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
