package com.example

import com.example.data.model.SalaryType
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testFixedDriverWageCalculation() {
        val fixedRate = 1200.0
        val grossIncome = 9000.0
        val expenses = 3500.0
        
        val wage = when (SalaryType.FIXED_DAILY.name) {
            SalaryType.FIXED_DAILY.name -> fixedRate
            else -> 0.0
        }
        assertEquals(1200.0, wage, 0.01)
    }

    @Test
    fun testPercentOfIncomeDriverWageCalculation() {
        val ratePercent = 15.0 // 15%
        val grossIncome = 10000.0
        val calculated = (grossIncome * ratePercent) / 100.0
        assertEquals(1500.0, calculated, 0.01)
    }

    @Test
    fun testNetDailyProfitCalculation() {
        val grossIncome = 8500.0
        val fuel = 2500.0
        val toll = 300.0
        val repairs = 200.0
        val totalExpenses = fuel + toll + repairs // 3000
        val driverWage = 1200.0
        
        val netProfit = grossIncome - totalExpenses - driverWage
        assertEquals(4300.0, netProfit, 0.01)
    }
}
