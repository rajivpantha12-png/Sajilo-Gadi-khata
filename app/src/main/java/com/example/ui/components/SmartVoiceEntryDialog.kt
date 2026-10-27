package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.ParsedSmartEntry
import com.example.data.firestore.FirestoreVehicle
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.util.AppLanguage
import com.example.ui.util.NepaliStrings

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SmartVoiceEntryDialog(
    vehicles: List<FirestoreVehicle>,
    isParsing: Boolean,
    parsedEntry: ParsedSmartEntry?,
    errorMessage: String?,
    language: AppLanguage,
    onParseText: (String) -> Unit,
    onConfirmSave: (ParsedSmartEntry, String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    var selectedVehicleId by remember(parsedEntry, vehicles) {
        val matched = vehicles.find { v ->
            parsedEntry != null && parsedEntry.vehicleReg.isNotBlank() &&
                    (v.regNumber.contains(parsedEntry.vehicleReg, ignoreCase = true) ||
                     parsedEntry.vehicleReg.contains(v.regNumber, ignoreCase = true))
        }
        mutableStateOf(matched?.id ?: vehicles.firstOrNull()?.id ?: "")
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val sampleNotes = if (language == AppLanguage.NEPALI) {
        listOf(
            "आज बा २ ख ५५२१ मा ५००० भाडा, १५०० डिजेल, २०० कर",
            "बा १ ज ३४९० मा ६००० आम्दानी, ५०० चार्ज, ३०० खाजा",
            "बा २ प ८८१२ मा ३५०० उठ्यो, १००० पेट्रोल"
        )
    } else {
        listOf(
            "BA-2-KHA 5521 made 5000 fare, 1500 fuel, 200 toll",
            "BA-1-JA 3490 made 6000 gross, 500 charge, 300 snacks"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("smart_voice_entry_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ProfitGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = ProfitGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = NepaliStrings.aiSmartVoiceTitle(language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = NepaliStrings.aiSmartVoiceSub(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_smart_voice_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Suggestion chips
                Text(
                    text = if (language == AppLanguage.NEPALI) "🎙️ नमुना वाक्यहरू (थिचेर जाँच्नुहोस्):" else "🎙️ Sample voice/text sentences:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sampleNotes.forEach { note ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                inputText = note
                                onParseText(note)
                            }
                        ) {
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text Input area
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = {
                        Text(
                            if (language == AppLanguage.NEPALI) "हिसाब लेख्नुहोस् वा भन्नुहोस्" else "Enter daily trip log",
                            fontSize = 13.sp
                        )
                    },
                    placeholder = {
                        Text(
                            if (language == AppLanguage.NEPALI)
                                "उदा: आज बा २ ख ५५२१ मा ५००० आम्दानी, १५०० को तेल, २०० कर..."
                            else
                                "e.g., Ba 2 Kha 5521 made 5000 fare, 1500 fuel, 200 toll...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smart_voice_input"),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Analyze with Gemini button
                Button(
                    onClick = { onParseText(inputText) },
                    enabled = inputText.isNotBlank() && !isParsing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("analyze_smart_entry_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                ) {
                    if (isParsing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (language == AppLanguage.NEPALI) "Gemini AI ले हिसाब छुट्याउँदै..." else "Gemini AI Analyzing...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (language == AppLanguage.NEPALI) "Gemini AI ले हिसाब छुट्याउनुहोस्" else "Extract Details with Gemini AI",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ $errorMessage",
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // If parsed successfully, show structured summary
                if (parsedEntry != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ProfitGreenContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (language == AppLanguage.NEPALI) "✅ AI द्वारा छुट्याइएको हिसाब विवरण:" else "✅ AI Extracted Khata Details:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ProfitGreen
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Vehicle Selection
                            Text(
                                text = if (language == AppLanguage.NEPALI) "गाडी छान्नुहोस्:" else "Select Vehicle:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = dropdownExpanded,
                                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                            ) {
                                val currentVeh = vehicles.find { it.id == selectedVehicleId }
                                OutlinedTextField(
                                    value = currentVeh?.let { "${it.regNumber} (${it.nameOrModel})" } ?: "गाडी छान्नुहोस्",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    vehicles.forEach { v ->
                                        DropdownMenuItem(
                                            text = { Text("${v.regNumber} - ${v.nameOrModel}") },
                                            onClick = {
                                                selectedVehicleId = v.id
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Grid of parsed figures
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "कमाइ (आम्दानी):" else "Income:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "रु. ${parsedEntry.grossIncome.toInt()}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProfitGreen
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "डिजेल/इन्धन:" else "Fuel Cost:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "रु. ${parsedEntry.fuelExpense.toInt()}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "सडक कर / अन्य:" else "Toll / Misc:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val other = parsedEntry.tollAndTaxExpense + parsedEntry.foodOrMiscExpense
                                    Text(
                                        text = "रु. ${other.toInt()}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            val calculatedNet = parsedEntry.grossIncome - (parsedEntry.fuelExpense + parsedEntry.tollAndTaxExpense + parsedEntry.foodOrMiscExpense)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "आजको खुद बचत: " else "Net Daily Profit: ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "रु. ${calculatedNet.toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (calculatedNet >= 0) ProfitGreen else ExpenseRed
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Confirm & Save button
                            Button(
                                onClick = { onConfirmSave(parsedEntry, selectedVehicleId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("confirm_smart_save_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "खातामा सुरक्षित गर्नुहोस् (Save)" else "Save to Daily Ledger",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
