package com.example.data.repository

import com.example.data.local.FleetDao
import com.example.data.model.BankInstallment
import com.example.data.model.DailyLog
import com.example.data.model.DriverPayout
import com.example.data.model.SalaryType
import com.example.data.model.Vehicle
import com.example.data.model.VehicleType
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FleetRepository(private val fleetDao: FleetDao) {

    val allVehicles: Flow<List<Vehicle>> = fleetDao.getAllVehicles()
    val allDailyLogs: Flow<List<DailyLog>> = fleetDao.getAllDailyLogs()
    val allDriverPayouts: Flow<List<DriverPayout>> = fleetDao.getAllDriverPayouts()
    val allInstallments: Flow<List<BankInstallment>> = fleetDao.getAllInstallments()

    fun getLogsForDate(date: String): Flow<List<DailyLog>> = fleetDao.getLogsForDate(date)
    fun getLogsForMonth(monthPrefix: String): Flow<List<DailyLog>> = fleetDao.getLogsForMonth(monthPrefix)
    fun getLogsForVehicleAndMonth(vehicleId: Long, monthPrefix: String): Flow<List<DailyLog>> =
        fleetDao.getLogsForVehicleAndMonth(vehicleId, monthPrefix)
    fun getLogsForVehicle(vehicleId: Long): Flow<List<DailyLog>> = fleetDao.getLogsForVehicle(vehicleId)

    fun getInstallmentsForMonth(yearMonth: String): Flow<List<BankInstallment>> =
        fleetDao.getInstallmentsForMonth(yearMonth)
    fun getInstallmentsForVehicle(vehicleId: Long): Flow<List<BankInstallment>> =
        fleetDao.getInstallmentsForVehicle(vehicleId)

    fun getPayoutsForVehicle(vehicleId: Long): Flow<List<DriverPayout>> =
        fleetDao.getPayoutsForVehicle(vehicleId)

    suspend fun getVehicleByIdOnce(id: Long): Vehicle? = fleetDao.getVehicleByIdOnce(id)
    suspend fun getLogForVehicleAndDate(vehicleId: Long, date: String): DailyLog? =
        fleetDao.getLogForVehicleAndDate(vehicleId, date)

    suspend fun saveVehicle(vehicle: Vehicle): Long {
        return if (vehicle.id == 0L) {
            fleetDao.insertVehicle(vehicle)
        } else {
            fleetDao.updateVehicle(vehicle)
            vehicle.id
        }
    }

    suspend fun deleteVehicle(vehicle: Vehicle) = fleetDao.deleteVehicle(vehicle)

    suspend fun saveDailyLog(log: DailyLog): Long {
        return fleetDao.insertOrUpdateDailyLog(log)
    }

    suspend fun deleteDailyLog(log: DailyLog) = fleetDao.deleteDailyLog(log)

    suspend fun recordDriverPayout(payout: DriverPayout): Long =
        fleetDao.insertDriverPayout(payout)

    suspend fun deleteDriverPayout(payout: DriverPayout) =
        fleetDao.deleteDriverPayout(payout)

    suspend fun saveInstallment(installment: BankInstallment): Long =
        fleetDao.insertOrUpdateInstallment(installment)

    suspend fun deleteInstallment(installment: BankInstallment) =
        fleetDao.deleteInstallment(installment)

    /**
     * Automatic calculation of Driver Daily Wage based on vehicle salary structure.
     */
    fun calculateDriverWage(
        salaryTypeStr: String,
        salaryRate: Double,
        grossIncome: Double,
        totalExpenses: Double
    ): Double {
        return when (salaryTypeStr) {
            SalaryType.FIXED_DAILY.name -> salaryRate
            SalaryType.PERCENT_OF_INCOME.name -> (grossIncome * salaryRate / 100.0)
            SalaryType.PERCENT_OF_NET.name -> {
                val netBeforeDriver = (grossIncome - totalExpenses).coerceAtLeast(0.0)
                (netBeforeDriver * salaryRate / 100.0)
            }
            else -> salaryRate
        }
    }

    /**
     * Seeds initial realistic public vehicle data if database is brand new.
     */
    suspend fun seedInitialDataIfEmpty() {
        if (fleetDao.getVehicleCount() > 0) return

        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = sdfDate.format(cal.time)
        val currentMonthStr = sdfMonth.format(cal.time)

        // Seed Vehicles
        val busId = fleetDao.insertVehicle(
            Vehicle(
                regNumber = "BA-2-KHA 5521",
                nameOrModel = "City Super Bus (36 Seater)",
                type = VehicleType.BUS.name,
                routeOrArea = "Downtown - Ring Road Express",
                driverName = "Rajesh Sharma",
                driverPhone = "+977-9841234567",
                salaryType = SalaryType.FIXED_DAILY.name,
                salaryRate = 1400.0,
                bankName = "Nabil Commercial Bank",
                monthlyEmi = 28000.0,
                emiDueDay = 10,
                totalLoanAmount = 1800000.0
            )
        )

        val microId = fleetDao.insertVehicle(
            Vehicle(
                regNumber = "BA-1-JA 3490",
                nameOrModel = "Toyota HiAce Microbus",
                type = VehicleType.MICROBUS.name,
                routeOrArea = "Airport - Central Route 4",
                driverName = "Bikash Gurung",
                driverPhone = "+977-9807654321",
                salaryType = SalaryType.PERCENT_OF_INCOME.name,
                salaryRate = 16.0, // 16% of gross fares
                bankName = "Everest Bank Ltd",
                monthlyEmi = 22500.0,
                emiDueDay = 15,
                totalLoanAmount = 1450000.0
            )
        )

        val taxiId = fleetDao.insertVehicle(
            Vehicle(
                regNumber = "BA-2-PA 8812",
                nameOrModel = "Hyundai Aura City Taxi",
                type = VehicleType.TAXI.name,
                routeOrArea = "Thamel - Airport Tourist Stand",
                driverName = "Sunil Thapa",
                driverPhone = "+977-9812398765",
                salaryType = SalaryType.PERCENT_OF_NET.name,
                salaryRate = 25.0, // 25% of net after fuel & toll
                bankName = "Standard Chartered",
                monthlyEmi = 16000.0,
                emiDueDay = 5,
                totalLoanAmount = 950000.0
            )
        )

        // Seed logs for the past 5 days including today
        val vehiclesList = listOf(
            Triple(busId, "Rajesh Sharma", 1400.0),
            Triple(microId, "Bikash Gurung", 0.0), // commission based
            Triple(taxiId, "Sunil Thapa", 0.0)
        )

        for (daysAgo in 4 downTo 0) {
            val dateCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
            val logDate = sdfDate.format(dateCal.time)

            // Bus logs
            val busIncome = 9200.0 + (daysAgo * 350.0)
            val busFuel = 3400.0
            val busToll = 350.0
            val busRepairs = if (daysAgo == 2) 800.0 else 100.0
            val busChallan = 0.0
            val busHelper = 600.0
            val busMisc = 200.0
            val busTotalExp = busFuel + busToll + busRepairs + busChallan + busHelper + busMisc
            val busDriverWage = 1400.0
            val busPaid = if (daysAgo == 0) 1000.0 else 1400.0 // partial on today

            fleetDao.insertOrUpdateDailyLog(
                DailyLog(
                    vehicleId = busId,
                    date = logDate,
                    grossIncome = busIncome,
                    fuelExpense = busFuel,
                    tollAndTaxExpense = busToll,
                    maintenanceExpense = busRepairs,
                    challanOrFineExpense = busChallan,
                    helperWageExpense = busHelper,
                    foodOrMiscExpense = busMisc,
                    totalOperatingExpenses = busTotalExp,
                    driverWageEarned = busDriverWage,
                    driverPaidAmount = busPaid,
                    isDriverFullyPaid = busPaid >= busDriverWage,
                    netDailyProfit = busIncome - busTotalExp - busDriverWage,
                    tripsOrRounds = 6,
                    notes = if (daysAgo == 2) "Replaced front brake shoe" else "Normal 6 trips"
                )
            )

            // Microbus logs (16% commission)
            val microIncome = 7800.0 + (daysAgo * 200.0)
            val microFuel = 2600.0
            val microToll = 250.0
            val microRepairs = 0.0
            val microHelper = 500.0
            val microMisc = 150.0
            val microTotalExp = microFuel + microToll + microRepairs + microHelper + microMisc
            val microWage = (microIncome * 0.16)
            val microPaid = microWage

            fleetDao.insertOrUpdateDailyLog(
                DailyLog(
                    vehicleId = microId,
                    date = logDate,
                    grossIncome = microIncome,
                    fuelExpense = microFuel,
                    tollAndTaxExpense = microToll,
                    maintenanceExpense = microRepairs,
                    challanOrFineExpense = 0.0,
                    helperWageExpense = microHelper,
                    foodOrMiscExpense = microMisc,
                    totalOperatingExpenses = microTotalExp,
                    driverWageEarned = microWage,
                    driverPaidAmount = microPaid,
                    isDriverFullyPaid = true,
                    netDailyProfit = microIncome - microTotalExp - microWage,
                    tripsOrRounds = 8,
                    notes = "Highway express trips"
                )
            )

            // Taxi logs (25% of net)
            val taxiIncome = 5600.0 - (daysAgo * 150.0)
            val taxiFuel = 1900.0
            val taxiToll = 120.0
            val taxiRepairs = 0.0
            val taxiTotalExp = taxiFuel + taxiToll + taxiRepairs
            val taxiNetBeforeDriver = (taxiIncome - taxiTotalExp).coerceAtLeast(0.0)
            val taxiWage = taxiNetBeforeDriver * 0.25
            val taxiPaid = taxiWage

            fleetDao.insertOrUpdateDailyLog(
                DailyLog(
                    vehicleId = taxiId,
                    date = logDate,
                    grossIncome = taxiIncome,
                    fuelExpense = taxiFuel,
                    tollAndTaxExpense = taxiToll,
                    maintenanceExpense = taxiRepairs,
                    challanOrFineExpense = 0.0,
                    helperWageExpense = 0.0,
                    foodOrMiscExpense = 100.0,
                    totalOperatingExpenses = taxiTotalExp + 100.0,
                    driverWageEarned = taxiWage,
                    driverPaidAmount = taxiPaid,
                    isDriverFullyPaid = true,
                    netDailyProfit = taxiIncome - (taxiTotalExp + 100.0) - taxiWage,
                    tripsOrRounds = 11,
                    notes = "Airport pickup runs"
                )
            )
        }

        // Seed bank installments for current month
        fleetDao.insertOrUpdateInstallment(
            BankInstallment(
                vehicleId = busId,
                yearMonth = currentMonthStr,
                bankName = "Nabil Commercial Bank",
                amountDue = 28000.0,
                amountPaid = 28000.0,
                paymentDate = "$currentMonthStr-08",
                dueDay = 10,
                isPaid = true,
                referenceOrTxnNo = "TXN-984210",
                notes = "Auto-debited from current account"
            )
        )

        fleetDao.insertOrUpdateInstallment(
            BankInstallment(
                vehicleId = microId,
                yearMonth = currentMonthStr,
                bankName = "Everest Bank Ltd",
                amountDue = 22500.0,
                amountPaid = 0.0,
                paymentDate = "",
                dueDay = 15,
                isPaid = false,
                referenceOrTxnNo = "",
                notes = "Payment pending for current month"
            )
        )

        fleetDao.insertOrUpdateInstallment(
            BankInstallment(
                vehicleId = taxiId,
                yearMonth = currentMonthStr,
                bankName = "Standard Chartered",
                amountDue = 16000.0,
                amountPaid = 16000.0,
                paymentDate = "$currentMonthStr-04",
                dueDay = 5,
                isPaid = true,
                referenceOrTxnNo = "SCB-77189",
                notes = "Paid via Mobile Banking"
            )
        )

        // Seed a sample driver payout
        fleetDao.insertDriverPayout(
            DriverPayout(
                vehicleId = busId,
                driverName = "Rajesh Sharma",
                amount = 2000.0,
                date = todayStr,
                paymentMode = "Cash",
                notes = "Advance payment for Dashain festival"
            )
        )
    }
}
