package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BankInstallment
import com.example.data.model.DailyLog
import com.example.data.model.DriverPayout
import com.example.data.model.Vehicle

@Database(
    entities = [
        Vehicle::class,
        DailyLog::class,
        DriverPayout::class,
        BankInstallment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FleetDatabase : RoomDatabase() {
    abstract fun fleetDao(): FleetDao

    companion object {
        @Volatile
        private var INSTANCE: FleetDatabase? = null

        fun getDatabase(context: Context): FleetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FleetDatabase::class.java,
                    "fleet_ledger_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
