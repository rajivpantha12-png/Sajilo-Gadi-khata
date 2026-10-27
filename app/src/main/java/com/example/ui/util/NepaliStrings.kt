package com.example.ui.util

/**
 * Clean, culturally natural Nepali & English translations specially crafted
 * for transport fleet owners and elderly family members.
 */
enum class AppLanguage {
    NEPALI,
    ENGLISH
}

object NepaliStrings {

    // App Title
    fun appTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "सजिलो गाडी खाता"
        AppLanguage.ENGLISH -> "Sajilo Gadi Khata"
    }

    // Tabs
    fun tabDailyLedger(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "दैनिक खाता"
        AppLanguage.ENGLISH -> "Daily Ledger"
    }
    fun tabVehicles(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "गाडीहरू"
        AppLanguage.ENGLISH -> "Vehicles"
    }
    fun tabDrivers(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "चालक (ड्राइभर)"
        AppLanguage.ENGLISH -> "Drivers"
    }
    fun tabBank(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "गाडी किस्ता (EMI)"
        AppLanguage.ENGLISH -> "Bank EMI"
    }
    fun tabReports(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "मासिक रिपोर्ट"
        AppLanguage.ENGLISH -> "Monthly Report"
    }

    // Father Mode Labels
    fun fatherModeTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "बुबाको लागि सजिलो मोड"
        AppLanguage.ENGLISH -> "Senior / Easy Mode"
    }
    fun fatherModeSub(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "ठूला अक्षर र सजिलो बटनहरू"
        AppLanguage.ENGLISH -> "Large text & high-contrast big buttons"
    }

    // Dashboard Cards
    fun todayIncome(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "आजको कुल कमाइ"
        AppLanguage.ENGLISH -> "Today's Gross Income"
    }
    fun todayExpense(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "आजको कुल खर्च"
        AppLanguage.ENGLISH -> "Today's Expenses"
    }
    fun netSavings(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "आजको खुद बचत (नाफा)"
        AppLanguage.ENGLISH -> "Today's Net Profit"
    }
    fun totalTrips(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "कुल ट्रिप संख्या"
        AppLanguage.ENGLISH -> "Total Trips"
    }

    // Quick Actions for Father
    fun addIncomeQuick(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "कमाइ थप्नुहोस् (+)"
        AppLanguage.ENGLISH -> "Add Income (+)"
    }
    fun addExpenseQuick(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "खर्च थप्नुहोस् (-)"
        AppLanguage.ENGLISH -> "Add Expense (-)"
    }
    fun addFullEntry(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "विस्तृत दैनिक हिसाब लेख्नुहोस्"
        AppLanguage.ENGLISH -> "Full Trip Entry"
    }

    // Sync & Google Account
    fun loginBannerTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "गुगल खाता जोड्नुहोस् (Google Sign-In)"
        AppLanguage.ENGLISH -> "Connect Google Account"
    }
    fun loginBannerSub(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "एउटै गुगल आइडीबाट चलाउनुहोस् — छोरा र बुबा दुवैको फोनमा हिसाब तुरुन्तै अपडेट हुन्छ।"
        AppLanguage.ENGLISH -> "Use the same Google account on both phones to automatically sync every trip and expense."
    }
    fun loginBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "साइन-इन गर्नुहोस् (Google Login)"
        AppLanguage.ENGLISH -> "Sign In with Google"
    }
    fun loggedInAs(lang: AppLanguage, email: String) = when (lang) {
        AppLanguage.NEPALI -> "जोडिएको खाता: $email (स्वतः सिङ्क सक्रिय)"
        AppLanguage.ENGLISH -> "Connected: $email (Live Sync Active)"
    }

    // Update Notification
    fun updateAvailableTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "🚀 नयाँ अपडेट उपलब्ध छ!"
        AppLanguage.ENGLISH -> "🚀 New Update Available!"
    }
    fun updateAvailableDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "नयाँ संस्करण उपलब्ध छ। नयाँ सुविधा र सुधार पाउन तुरुन्तै अपडेट गर्नुहोस्।"
        AppLanguage.ENGLISH -> "A new version of Sajilo Gadi Khata is available with enhancements."
    }
    fun updateNowBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "तुरुन्तै अपडेट गर्नुहोस्"
        AppLanguage.ENGLISH -> "Update Now"
    }
    fun updateLaterBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "पछि गर्ने"
        AppLanguage.ENGLISH -> "Later"
    }

    // Download & Share
    fun downloadShareTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "एप डाउनलोड तथा सेयर गर्नुहोस्"
        AppLanguage.ENGLISH -> "Download & Share App"
    }
    fun scanQrToDownload(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "अर्को फोनको क्यामेराले यो QR कोड स्क्यान गर्नुहोस् र सिधै एप इन्स्टल गर्नुहोस्:"
        AppLanguage.ENGLISH -> "Scan this QR code with another phone's camera to install the app:"
    }
    fun copyLinkBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "लिङ्क कपी गर्नुहोस्"
        AppLanguage.ENGLISH -> "Copy Download Link"
    }
    fun shareViaAppsBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "व्हाट्सएप / म्यासेजमा पठाउनुहोस्"
        AppLanguage.ENGLISH -> "Share via WhatsApp / SMS"
    }
    fun directDownloadApk(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "सिधै APK डाउनलोड गर्नुहोस्"
        AppLanguage.ENGLISH -> "Direct APK Download"
    }
    fun downloadZipBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "प्रोजेक्ट ZIP डाउनलोड गर्नुहोस्"
        AppLanguage.ENGLISH -> "Download Project ZIP"
    }

    // Form Fields
    fun selectVehicle(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "गाडी रोज्नुहोस्"
        AppLanguage.ENGLISH -> "Select Vehicle"
    }
    fun amount(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "रकम (रु.)"
        AppLanguage.ENGLISH -> "Amount (Rs.)"
    }
    fun category(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "खर्चको शीर्षक"
        AppLanguage.ENGLISH -> "Expense Category"
    }
    fun notes(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "कैफियत (टिप्पणी)"
        AppLanguage.ENGLISH -> "Notes / Remarks"
    }
    fun saveBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "सेभ गर्नुहोस् (Save)"
        AppLanguage.ENGLISH -> "Save Entry"
    }
    fun cancelBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "रद्द गर्नुहोस् (Cancel)"
        AppLanguage.ENGLISH -> "Cancel"
    }

    // Categories
    fun catFuel(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "इन्धन (डिजेल/पेट्रोल)"
        AppLanguage.ENGLISH -> "Fuel (Diesel/Petrol)"
    }
    fun catEv(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "विद्युतीय चार्ज (EV)"
        AppLanguage.ENGLISH -> "EV Charging"
    }
    fun catRepair(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "गाडी मर्मत तथा सर्भिसिङ"
        AppLanguage.ENGLISH -> "Repair & Maintenance"
    }
    fun catToll(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "सडक कर / काउन्टर कमिसन"
        AppLanguage.ENGLISH -> "Road Toll / Commission"
    }
    fun catFood(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "चालक खाना / भत्ता"
        AppLanguage.ENGLISH -> "Driver Food Allowance"
    }
    fun catFine(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "ट्राफिक चिट / जरिवाना"
        AppLanguage.ENGLISH -> "Traffic Fine"
    }
    fun catOther(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "अन्य विविध खर्च"
        AppLanguage.ENGLISH -> "Other Expense"
    }

    // Gemini AI Strings
    fun aiAdvisorTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "गाडी सल्लाहकार (Gemini AI)"
        AppLanguage.ENGLISH -> "Vehicle Advisor (Gemini AI)"
    }
    fun aiAdvisorSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "डिजेल बचत, नाफा वृद्धि, र किस्ता व्यवस्थापनका सुझावहरू"
        AppLanguage.ENGLISH -> "Fuel saving, profit increase, & bank installment tips"
    }
    fun aiSmartVoiceTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "AI बोली/लेखाइ प्रविष्टि (Smart Entry)"
        AppLanguage.ENGLISH -> "Smart AI Voice/Text Entry"
    }
    fun aiSmartVoiceSub(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "बोलेर वा लेखेर हिसाब भन्नुहोस्, Gemini AI ले सोझै खाता तयार गर्छ।"
        AppLanguage.ENGLISH -> "Speak or type your trip notes, Gemini AI will auto-fill the ledger."
    }
    fun aiMonthlyInsightBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.NEPALI -> "Gemini AI महिनाको समीक्षा"
        AppLanguage.ENGLISH -> "Gemini AI Monthly Review"
    }
}
