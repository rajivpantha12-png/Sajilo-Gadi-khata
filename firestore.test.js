const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-fleetled-cfe2887c-631a-4c27-ba0a-986428fea718";

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("unauthenticated user cannot read or write vehicles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore({ databaseId: DATABASE_ID });
  const vehicleRef = unauthDb.doc("users/alice/vehicles/v1");
  await assertFails(vehicleRef.get());
  await assertFails(vehicleRef.set({ regNumber: "BA-1-JA 1234", type: "BUS", salaryType: "FIXED_DAILY" }));
});

test("authenticated user can create and read their own vehicle", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore({ databaseId: DATABASE_ID });
  const vehicleRef = aliceDb.doc("users/alice/vehicles/v1");
  await assertSucceeds(vehicleRef.set({
    regNumber: "BA-1-JA 1234",
    nameOrModel: "City Bus",
    type: "BUS",
    salaryType: "FIXED_DAILY",
    salaryRate: 1400.0,
    isElectric: false
  }));
  await assertSucceeds(vehicleRef.get());
});

test("user cannot read or write another user's vehicles", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore({ databaseId: DATABASE_ID });
  const bobDb = testEnv.authenticatedContext("bob").firestore({ databaseId: DATABASE_ID });

  await assertSucceeds(aliceDb.doc("users/alice/vehicles/v1").set({
    regNumber: "BA-1-JA 1234",
    type: "BUS",
    salaryType: "FIXED_DAILY"
  }));

  await assertFails(bobDb.doc("users/alice/vehicles/v1").get());
  await assertFails(bobDb.doc("users/alice/vehicles/v1").delete());
});

test("authenticated user can create daily log with EV charging and maintenance bill details", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore({ databaseId: DATABASE_ID });
  const logRef = aliceDb.doc("users/alice/daily_logs/log1");
  await assertSucceeds(logRef.set({
    vehicleId: "v1",
    vehicleReg: "BA-1-JA 1234",
    date: "2026-10-26",
    grossIncome: 9500.0,
    fuelExpense: 0.0,
    isEvCharging: true,
    evUnitsKwh: 45.0,
    evRatePerUnit: 12.0,
    evChargingExpense: 540.0,
    maintenanceExpense: 1200.0,
    maintenanceWorkDetails: "Brake shoe replacement and wheel greasing",
    maintenanceBillNo: "BILL-9081",
    maintenanceVendor: "City Auto Garage",
    totalOperatingExpenses: 1740.0,
    driverWageEarned: 1400.0,
    driverPaidAmount: 1400.0,
    netDailyProfit: 6360.0
  }));
});

test("authenticated user can record driver payout with date and bank installment", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore({ databaseId: DATABASE_ID });
  const payoutRef = aliceDb.doc("users/alice/driver_payouts/p1");
  await assertSucceeds(payoutRef.set({
    vehicleId: "v1",
    driverName: "Rajesh Sharma",
    amount: 2500.0,
    date: "2026-10-26",
    paymentMode: "Cash",
    notes: "Weekly driver advance"
  }));

  const emiRef = aliceDb.doc("users/alice/bank_installments/emi1");
  await assertSucceeds(emiRef.set({
    vehicleId: "v1",
    yearMonth: "2026-10",
    bankName: "Nabil Bank",
    amountDue: 28000.0,
    amountPaid: 28000.0,
    isPaid: true
  }));
});
