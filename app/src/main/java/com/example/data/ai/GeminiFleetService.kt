package com.example.data.ai

import android.util.Log
import com.example.ui.util.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiFleetService"

data class ParsedSmartEntry(
    val vehicleReg: String = "",
    val grossIncome: Double = 0.0,
    val fuelExpense: Double = 0.0,
    val tollAndTaxExpense: Double = 0.0,
    val foodOrMiscExpense: Double = 0.0,
    val tripsOrRounds: Int = 1,
    val notes: String = ""
)

class GeminiFleetService {

    // Lazy initialization of models to avoid early evaluation before Firebase/AppCheck is ready
    private val generalAdvisorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val smartParserModel by lazy {
        val entrySchema = Schema.obj(
            properties = mapOf(
                "vehicleReg" to Schema.string(description = "Vehicle registration number if detected (e.g. Ba 2 Kha 1234), or empty string"),
                "grossIncome" to Schema.double(description = "Total fare or daily income collected in Nepali Rupees"),
                "fuelExpense" to Schema.double(description = "Diesel, petrol, or charging expense in Nepali Rupees"),
                "tollAndTaxExpense" to Schema.double(description = "Toll, counter tax, or road tax in Rupees"),
                "foodOrMiscExpense" to Schema.double(description = "Food, snacks, tea, or misc expenses in Rupees"),
                "tripsOrRounds" to Schema.integer(description = "Number of rounds, trips, or trips made"),
                "notes" to Schema.string(description = "Brief summary notes in Nepali or English")
            ),
            optionalProperties = listOf("vehicleReg", "fuelExpense", "tollAndTaxExpense", "foodOrMiscExpense", "tripsOrRounds", "notes")
        )

        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = entrySchema
        }

        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    /**
     * Parses spoken or typed natural language (Nepali or English) into daily khata numbers
     */
    suspend fun parseDailyKhataText(userInput: String, knownVehicles: List<String>): Result<ParsedSmartEntry> {
        return try {
            val vehiclesContext = if (knownVehicles.isNotEmpty()) {
                "Registered vehicles: ${knownVehicles.joinToString(", ")}."
            } else ""

            val prompt = """
                You are a smart accounting assistant for public vehicle owners in Nepal.
                Parse the user's spoken or written daily vehicle khata note into structured numeric fields.
                $vehiclesContext
                User note: "$userInput"
                Extract: gross fare income, fuel cost, toll/road tax, food/misc cost, trips, vehicle registration.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                smartParserModel.generateContent(prompt)
            }

            val jsonStr = response.text ?: throw IllegalStateException("Empty response from AI")
            val json = JSONObject(jsonStr)

            val parsed = ParsedSmartEntry(
                vehicleReg = json.optString("vehicleReg", ""),
                grossIncome = json.optDouble("grossIncome", 0.0),
                fuelExpense = json.optDouble("fuelExpense", 0.0),
                tollAndTaxExpense = json.optDouble("tollAndTaxExpense", 0.0),
                foodOrMiscExpense = json.optDouble("foodOrMiscExpense", 0.0),
                tripsOrRounds = json.optInt("tripsOrRounds", 1),
                notes = json.optString("notes", "")
            )
            Result.success(parsed)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Parsing timed out", e)
            Result.failure(Exception("AI समय समाप्त भयो (Request timed out). कृपया पुन: प्रयास गर्नुहोस्।"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse text with Gemini", e)
            Result.failure(e)
        }
    }

    /**
     * Streams fleet financial advice and answers in Nepali or English
     */
    fun askAdvisorStream(question: String, fleetSummary: String, language: AppLanguage): Flow<String> = flow {
        val langInstruction = if (language == AppLanguage.NEPALI) {
            "तपाईं 'सजिलो गाडी खाता' को अनुभवी नेपाली गाडी व्यवसायी सल्लाहकार हुनुहुन्छ। नेपाली भाषामा सरल, आदरार्थी, र स्पष्ट तरिकाले जवाफ दिनुहोस् (बुबा र छोराले बुझ्ने गरी)।"
        } else {
            "You are a friendly and expert fleet financial advisor for public vehicle owners in Nepal. Give clear, polite, and practical advice."
        }

        val prompt = """
            $langInstruction
            
            Current Fleet Status:
            $fleetSummary
            
            User's Question:
            $question
            
            Provide practical advice on increasing profit, reducing fuel wastage, managing driver commissions fairly, and planning bank installments.
        """.trimIndent()

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            generalAdvisorModel.generateContentStream(prompt).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                accumulated.append(chunkText)
                emit(accumulated.toString())
            }
        }
    }

    /**
     * Generates a monthly financial health report with recommendations
     */
    suspend fun generateMonthlyReportInsight(
        monthYear: String,
        totalIncome: Double,
        totalExpenses: Double,
        netProfit: Double,
        vehicleCount: Int,
        fuelTotal: Double,
        emiTotal: Double,
        driverPayoutTotal: Double,
        language: AppLanguage
    ): Result<String> {
        return try {
            val prompt = if (language == AppLanguage.NEPALI) {
                """
                तपाईं नेपाली यातायात व्यवसायी सल्लाहकार हुनुहुन्छ। तलको महिनाको खाता विवरण हेरेर छोटो, उत्साहजनक र उपयोगी विश्लेषण दिनुहोस्:
                - महिना: $monthYear
                - गाडी संख्या: $vehicleCount
                - कुल आम्दानी: रु $totalIncome
                - सञ्चालन खर्च (डिजेल, कर, खाजा): रु $totalExpenses (जसमा डिजेल: रु $fuelTotal)
                - चालक तलब/कमिसन: रु $driverPayoutTotal
                - बैंक किस्ता (EMI): रु $emiTotal
                - खुद नाफा (Net Savings): रु $netProfit

                ३-४ बुँदामा विश्लेषण दिनुहोस्:
                १. यो महिनाको नाफा स्थिति कस्तो रह्यो?
                २. खर्च घटाउने उपाय (विशेष गरी इन्धन वा मर्मत)
                ३. बैंक किस्ता र भविष्यको लागि सुझाव।
                """.trimIndent()
            } else {
                """
                You are a transport fleet financial advisor in Nepal. Analyze this monthly performance:
                - Month: $monthYear
                - Fleet size: $vehicleCount vehicles
                - Gross Revenue: NPR $totalIncome
                - Operating Expenses: NPR $totalExpenses (Fuel: NPR $fuelTotal)
                - Driver Wages: NPR $driverPayoutTotal
                - Bank Installments (EMI): NPR $emiTotal
                - Net Profit: NPR $netProfit

                Provide 3 concise actionable bullet points on financial health, fuel efficiency, and cash flow for bank EMIs.
                """.trimIndent()
            }

            val response = withTimeout(60_000L) {
                generalAdvisorModel.generateContent(prompt)
            }
            Result.success(response.text ?: "No insight generated")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Monthly insight timed out", e)
            Result.failure(Exception("AI analysis timed out. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed monthly analysis", e)
            Result.failure(e)
        }
    }
}
