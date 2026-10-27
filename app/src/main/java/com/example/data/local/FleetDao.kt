package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BankInstallment
import com.example.data.model.DailyLog
import com.example.data.model.DriverPayout
import com.example.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {
    // --- Vehicles ---
    @Query("SELECT * FROM vehicles ORDER BY id DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId")
    fun getVehicleById(vehicleId: Long): Flow<Vehicle?>

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId")
    suspend fun getVehicleByIdOnce(vehicleId: Long): Vehicle?

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getVehicleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)

    // --- Daily Logs ---
    @Query("SELECT * FROM daily_logs ORDER BY date DESC, id DESC")
    fun getAllDailyLogs(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE date = :date ORDER BY id DESC")
    fun getLogsForDate(date: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getLogsForMonth(monthPrefix: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE vehicleId = :vehicleId AND date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getLogsForVehicleAndMonth(vehicleId: Long, monthPrefix: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getLogsForVehicle(vehicleId: Long): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE vehicleId = :vehicleId AND date = :date LIMIT 1")
    suspend fun getLogForVehicleAndDate(vehicleId: Long, date: String): DailyLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyLog(log: DailyLog): Long

    @Delete
    suspend fun deleteDailyLog(log: DailyLog)

    // --- Driver Payouts ---
    @Query("SELECT * FROM driver_payouts ORDER BY date DESC, id DESC")
    fun getAllDriverPayouts(): Flow<List<DriverPayout>>

    @Query("SELECT * FROM driver_payouts WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getPayoutsForVehicle(vehicleId: Long): Flow<List<DriverPayout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriverPayout(payout: DriverPayout): Long

    @Delete
    suspend fun deleteDriverPayout(payout: DriverPayout)

    // --- Bank Installments ---
    @Query("SELECT * FROM bank_installments ORDER BY yearMonth DESC, dueDay ASC")
    fun getAllInstallments(): Flow<List<BankInstallment>>

    @Query("SELECT * FROM bank_installments WHERE yearMonth = :yearMonth")
    fun getInstallmentsForMonth(yearMonth: String): Flow<List<BankInstallment>>

    @Query("SELECT * FROM bank_installments WHERE vehicleId = :vehicleId ORDER BY yearMonth DESC")
    fun getInstallmentsForVehicle(vehicleId: Long): Flow<List<BankInstallment>>

    @Query("SELECT * FROM bank_installments WHERE vehicleId = :vehicleId AND yearMonth = :yearMonth LIMIT 1")
    suspend fun getInstallmentForVehicleAndMonth(vehicleId: Long, yearMonth: String): BankInstallment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInstallment(installment: BankInstallment): Long

    @Delete
    suspend fun deleteInstallment(installment: BankInstallment)
}
