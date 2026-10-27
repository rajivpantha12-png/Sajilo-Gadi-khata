package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.SalaryType
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreRepo"

        fun create(context: Context): FirestoreRepository {
            val dbId = context.getString(R.string.firestore_database_id)
            val instance = FirebaseFirestore.getInstance(dbId)
            return FirestoreRepository(instance)
        }
    }

    private val auth get() = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    val currentUserId: String? get() = auth.currentUser?.uid

    // --- REALTIME OBSERVATION FLOWS ---

    fun observeVehicles(): Flow<List<FirestoreVehicle>> = flow {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("vehicles")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        emitAll(
            ref.snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreVehicle::class.java) }
                .catch { e ->
                    Log.e(TAG, "Error observing vehicles: ${e.message}", e)
                    emit(emptyList())
                }
        )
    }

    fun observeDailyLogs(): Flow<List<FirestoreDailyLog>> = flow {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("daily_logs")
            .orderBy("date", Query.Direction.DESCENDING)

        emitAll(
            ref.snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreDailyLog::class.java) }
                .catch { e ->
                    Log.e(TAG, "Error observing daily logs: ${e.message}", e)
                    emit(emptyList())
                }
        )
    }

    fun observeDriverPayouts(): Flow<List<FirestoreDriverPayout>> = flow {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("driver_payouts")
            .orderBy("date", Query.Direction.DESCENDING)

        emitAll(
            ref.snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreDriverPayout::class.java) }
                .catch { e ->
                    Log.e(TAG, "Error observing driver payouts: ${e.message}", e)
                    emit(emptyList())
                }
        )
    }

    fun observeBankInstallments(): Flow<List<FirestoreBankInstallment>> = flow {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("bank_installments")
            .orderBy("yearMonth", Query.Direction.DESCENDING)

        emitAll(
            ref.snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreBankInstallment::class.java) }
                .catch { e ->
                    Log.e(TAG, "Error observing bank installments: ${e.message}", e)
                    emit(emptyList())
                }
        )
    }

    // --- CRUD OPERATIONS ---

    suspend fun saveVehicle(vehicle: FirestoreVehicle): String {
        val uid = requireUserId()
        val collection = db.collection("users").document(uid).collection("vehicles")
        return if (vehicle.id.isBlank()) {
            val docRef = collection.document()
            docRef.set(
                vehicle.copy(
                    createdAt = null,
                    updatedAt = null
                )
            ).await()
            docRef.update(
                mapOf(
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            docRef.id
        } else {
            val docRef = collection.document(vehicle.id)
            docRef.set(
                vehicle.copy(updatedAt = null)
            ).await()
            docRef.update("updatedAt", FieldValue.serverTimestamp()).await()
            vehicle.id
        }
    }

    suspend fun deleteVehicle(vehicleId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("vehicles").document(vehicleId).delete().await()
    }

    suspend fun saveDailyLog(log: FirestoreDailyLog): String {
        val uid = requireUserId()
        val collection = db.collection("users").document(uid).collection("daily_logs")
        return if (log.id.isBlank()) {
            val docRef = collection.document()
            docRef.set(log.copy(createdAt = null, updatedAt = null)).await()
            docRef.update(
                mapOf(
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            docRef.id
        } else {
            val docRef = collection.document(log.id)
            docRef.set(log.copy(updatedAt = null)).await()
            docRef.update("updatedAt", FieldValue.serverTimestamp()).await()
            log.id
        }
    }

    suspend fun deleteDailyLog(logId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("daily_logs").document(logId).delete().await()
    }

    suspend fun recordDriverPayout(payout: FirestoreDriverPayout): String {
        val uid = requireUserId()
        val collection = db.collection("users").document(uid).collection("driver_payouts")
        val docRef = if (payout.id.isBlank()) collection.document() else collection.document(payout.id)
        docRef.set(payout.copy(createdAt = null, updatedAt = null)).await()
        docRef.update(
            mapOf(
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return docRef.id
    }

    suspend fun deleteDriverPayout(payoutId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("driver_payouts").document(payoutId).delete().await()
    }

    suspend fun saveBankInstallment(installment: FirestoreBankInstallment): String {
        val uid = requireUserId()
        val collection = db.collection("users").document(uid).collection("bank_installments")
        val docRef = if (installment.id.isBlank()) collection.document() else collection.document(installment.id)
        docRef.set(installment.copy(createdAt = null, updatedAt = null)).await()
        docRef.update(
            mapOf(
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return docRef.id
    }

    suspend fun deleteBankInstallment(installmentId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("bank_installments").document(installmentId).delete().await()
    }

    /**
     * Seeds initial user fleet data in cloud on first Google login if empty.
     */
    suspend fun seedInitialUserDataIfEmpty() {
        val uid = requireUserId()
        val vehiclesRef = db.collection("users").document(uid).collection("vehicles")
        val snapshot = vehiclesRef.limit(1).get().await()
        if (!snapshot.isEmpty) return // already has data

        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = sdfDate.format(cal.time)
        val currentMonthStr = sdfMonth.format(cal.time)

        // Seed 1: Diesel City Express Bus
        val busRef = vehiclesRef.document()
        busRef.set(
            FirestoreVehicle(
                id = busRef.id,
                regNumber = "BA-2-KHA 5521",
                nameOrModel = "City Super Bus (36-Seater)",
                type = "BUS",
                isElectric = false,
                routeOrArea = "Downtown - Ring Road Line",
                driverName = "Rajesh Sharma",
                driverPhone = "+977-9841234567",
                salaryType = "FIXED_DAILY",
                salaryRate = 1400.0,
                bankName = "Nabil Commercial Bank",
                monthlyEmi = 28000.0,
                emiDueDay = 10L,
                totalLoanAmount = 1800000.0
            )
        ).await()

        // Seed 2: EV Microbus (King Long / Photon EV)
        val evMicroRef = vehiclesRef.document()
        evMicroRef.set(
            FirestoreVehicle(
                id = evMicroRef.id,
                regNumber = "BA-1-JA 3490",
                nameOrModel = "Photon EV Microbus (14-Seater)",
                type = "EV_VAN",
                isElectric = true,
                routeOrArea = "Ratnapark - Bhaktapur Fast Route",
                driverName = "Bikash Gurung",
                driverPhone = "+977-9807654321",
                salaryType = "PERCENT_OF_INCOME",
                salaryRate = 16.0,
                bankName = "Everest Bank Ltd",
                monthlyEmi = 22500.0,
                emiDueDay = 15L,
                totalLoanAmount = 1450000.0
            )
        ).await()

        // Seed 3: City Taxi
        val taxiRef = vehiclesRef.document()
        taxiRef.set(
            FirestoreVehicle(
                id = taxiRef.id,
                regNumber = "BA-2-PA 8812",
                nameOrModel = "Hyundai Aura City Taxi",
                type = "TAXI",
                isElectric = false,
                routeOrArea = "Thamel - Airport Tourist Stand",
                driverName = "Sunil Thapa",
                driverPhone = "+977-9812398765",
                salaryType = "PERCENT_OF_NET",
                salaryRate = 25.0,
                bankName = "Standard Chartered",
                monthlyEmi = 16000.0,
                emiDueDay = 5L,
                totalLoanAmount = 950000.0
            )
        ).await()

        // Seed logs for past 3 days with EV charging and Maintenance bill details
        val logsRef = db.collection("users").document(uid).collection("daily_logs")

        // Log 1: Bus with Maintenance bill details
        val busLog = logsRef.document()
        busLog.set(
            FirestoreDailyLog(
                id = busLog.id,
                vehicleId = busRef.id,
                vehicleReg = "BA-2-KHA 5521",
                date = todayStr,
                grossIncome = 9800.0,
                fuelExpense = 3400.0,
                tollAndTaxExpense = 350.0,
                maintenanceExpense = 1450.0,
                maintenanceWorkDetails = "Replaced front brake shoe, 4L Mobil 15W40 oil change, puncture repair",
                maintenanceBillNo = "BILL-8491",
                maintenanceVendor = "Balaju Auto Workshop",
                helperWageExpense = 600.0,
                foodOrMiscExpense = 200.0,
                totalOperatingExpenses = 3400.0 + 350.0 + 1450.0 + 600.0 + 200.0,
                driverWageEarned = 1400.0,
                driverPaidAmount = 1000.0, // partial, 400 pending
                isDriverFullyPaid = false,
                netDailyProfit = 9800.0 - (3400.0 + 350.0 + 1450.0 + 600.0 + 200.0) - 1400.0,
                tripsOrRounds = 6L,
                notes = "Heavy morning rush"
            )
        ).await()

        // Log 2: EV Microbus with EV Charging units
        val evLog = logsRef.document()
        evLog.set(
            FirestoreDailyLog(
                id = evLog.id,
                vehicleId = evMicroRef.id,
                vehicleReg = "BA-1-JA 3490",
                date = todayStr,
                grossIncome = 8200.0,
                fuelExpense = 0.0,
                isEvCharging = true,
                evUnitsKwh = 48.0,
                evRatePerUnit = 12.5,
                evChargingExpense = 48.0 * 12.5, // 600
                tollAndTaxExpense = 250.0,
                maintenanceExpense = 0.0,
                helperWageExpense = 500.0,
                foodOrMiscExpense = 150.0,
                totalOperatingExpenses = 600.0 + 250.0 + 500.0 + 150.0,
                driverWageEarned = 8200.0 * 0.16, // 1312
                driverPaidAmount = 1312.0,
                isDriverFullyPaid = true,
                netDailyProfit = 8200.0 - 1500.0 - 1312.0,
                tripsOrRounds = 8L,
                notes = "Fast DC charging at Sajha Charging Station"
            )
        ).await()

        // Bank Installments
        val emiRef = db.collection("users").document(uid).collection("bank_installments")
        val busEmi = emiRef.document()
        busEmi.set(
            FirestoreBankInstallment(
                id = busEmi.id,
                vehicleId = busRef.id,
                vehicleReg = "BA-2-KHA 5521",
                yearMonth = currentMonthStr,
                bankName = "Nabil Commercial Bank",
                amountDue = 28000.0,
                amountPaid = 28000.0,
                paymentDate = "$currentMonthStr-08",
                dueDay = 10L,
                isPaid = true,
                referenceOrTxnNo = "TXN-984210"
            )
        ).await()

        val evEmi = emiRef.document()
        evEmi.set(
            FirestoreBankInstallment(
                id = evEmi.id,
                vehicleId = evMicroRef.id,
                vehicleReg = "BA-1-JA 3490",
                yearMonth = currentMonthStr,
                bankName = "Everest Bank Ltd",
                amountDue = 22500.0,
                amountPaid = 0.0,
                dueDay = 15L,
                isPaid = false
            )
        ).await()
    }

    /**
     * Builds a single consolidated JSON backup containing all vehicles, daily logs,
     * EV charging and repair bill details, driver payouts, and bank EMIs in one file.
     */
    fun createConsolidatedBackup(
        vehicles: List<FirestoreVehicle>,
        logs: List<FirestoreDailyLog>,
        payouts: List<FirestoreDriverPayout>,
        installments: List<FirestoreBankInstallment>,
        userEmail: String?
    ): String {
        val root = JSONObject()
        root.put("appName", "FleetLedger")
        root.put("backupDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        root.put("userEmail", userEmail ?: "Unknown")

        val vehiclesArray = JSONArray()
        vehicles.forEach { v ->
            vehiclesArray.put(
                JSONObject().apply {
                    put("id", v.id)
                    put("regNumber", v.regNumber)
                    put("nameOrModel", v.nameOrModel)
                    put("type", v.type)
                    put("isElectric", v.isElectric)
                    put("routeOrArea", v.routeOrArea)
                    put("driverName", v.driverName)
                    put("driverPhone", v.driverPhone)
                    put("salaryType", v.salaryType)
                    put("salaryRate", v.salaryRate)
                    put("bankName", v.bankName)
                    put("monthlyEmi", v.monthlyEmi)
                    put("emiDueDay", v.emiDueDay)
                }
            )
        }
        root.put("vehicles", vehiclesArray)

        val logsArray = JSONArray()
        logs.forEach { l ->
            logsArray.put(
                JSONObject().apply {
                    put("id", l.id)
                    put("vehicleId", l.vehicleId)
                    put("vehicleReg", l.vehicleReg)
                    put("date", l.date)
                    put("grossIncome", l.grossIncome)
                    put("fuelExpense", l.fuelExpense)
                    put("isEvCharging", l.isEvCharging)
                    put("evUnitsKwh", l.evUnitsKwh)
                    put("evRatePerUnit", l.evRatePerUnit)
                    put("evChargingExpense", l.evChargingExpense)
                    put("tollAndTaxExpense", l.tollAndTaxExpense)
                    put("maintenanceExpense", l.maintenanceExpense)
                    put("maintenanceWorkDetails", l.maintenanceWorkDetails)
                    put("maintenanceBillNo", l.maintenanceBillNo)
                    put("maintenanceVendor", l.maintenanceVendor)
                    put("challanOrFineExpense", l.challanOrFineExpense)
                    put("helperWageExpense", l.helperWageExpense)
                    put("foodOrMiscExpense", l.foodOrMiscExpense)
                    put("totalOperatingExpenses", l.totalOperatingExpenses)
                    put("driverWageEarned", l.driverWageEarned)
                    put("driverPaidAmount", l.driverPaidAmount)
                    put("isDriverFullyPaid", l.isDriverFullyPaid)
                    put("netDailyProfit", l.netDailyProfit)
                    put("tripsOrRounds", l.tripsOrRounds)
                    put("notes", l.notes)
                }
            )
        }
        root.put("dailyLogs", logsArray)

        val payoutsArray = JSONArray()
        payouts.forEach { p ->
            payoutsArray.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("vehicleId", p.vehicleId)
                    put("driverName", p.driverName)
                    put("amount", p.amount)
                    put("date", p.date)
                    put("paymentMode", p.paymentMode)
                    put("notes", p.notes)
                }
            )
        }
        root.put("driverPayouts", payoutsArray)

        val installmentsArray = JSONArray()
        installments.forEach { i ->
            installmentsArray.put(
                JSONObject().apply {
                    put("id", i.id)
                    put("vehicleId", i.vehicleId)
                    put("yearMonth", i.yearMonth)
                    put("bankName", i.bankName)
                    put("amountDue", i.amountDue)
                    put("amountPaid", i.amountPaid)
                    put("paymentDate", i.paymentDate)
                    put("dueDay", i.dueDay)
                    put("isPaid", i.isPaid)
                    put("referenceOrTxnNo", i.referenceOrTxnNo)
                }
            )
        }
        root.put("bankInstallments", installmentsArray)

        return root.toString(2)
    }
}
