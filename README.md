# सजिलो गाडी खाता (Sajilo Gadi Khata) - Fleet Ledger & Trip Management

A complete, production-grade Fleet Ledger & Income/Expense management system tailored for public transport and commercial vehicles (buses, microbuses, electric vans, taxis, trucks).

## Key Features
- **Daily Trip Ledger**: Record Gross Ticket Income, Fuel (Diesel/Petrol) or EV Charging kWh & costs, Route Tolls & Taxes, Maintenance/Repairs, Driver Allowances, and Traffic Fines.
- **Trip Counter & Trip Metrics**: Track daily trips per vehicle, kilometers run, and instant net balance.
- **Dual-Pane Desktop & Responsive Mobile Layout**:
  - Automatically switches between handheld mobile and widescreen desktop/tablet interfaces.
  - Interactive Preview Mode toggle in top-bar to switch between Mobile and Desktop views on any device.
- **Driver Settlement Ledger**:
  - Driver wage calculation by percentage of gross income or fixed daily wage.
  - Advance payment tracking and pending payout balances.
- **Bank EMI / Installment Tracker**:
  - Monthly vehicle loan EMI due dates, principal & interest breakdown, payment statuses, and remaining balances.
- **Monthly Net Profit & Loss Analytics**:
  - Vehicle-by-vehicle and fleet-wide financial breakdowns.
  - Export monthly summary to CSV.
- **Dual Mode Offline & Cloud Ready**:
  - Immediate out-of-the-box local offline mode populated with realistic vehicle fleet data (Buses, EV Vans, City Taxis).
  - Optional Google Sign-In with live Cloud Firestore synchronization across devices.

## Pre-Built APK
A ready-to-install Android APK is available in:
`prebuilt_apk/SajiloGadiKhata.apk`

To install on any Android phone (Android 7.0 / API 24 or newer):
1. Copy `SajiloGadiKhata.apk` to your phone.
2. Open your file manager and tap `SajiloGadiKhata.apk` to install.
3. Enable "Install from unknown sources" if prompted.

## Opening in Android Studio
1. Open Android Studio (Ladybug or Hedgehog or newer recommended).
2. Select **File > Open** and choose the extracted `sajilo-gadi-khata` directory.
3. Allow Gradle to sync.
4. Run on an Android Emulator or connected physical device.
