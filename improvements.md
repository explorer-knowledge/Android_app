# improvements.md — Full Backlog of Gaps & Improvements (#1–24)

Triaged from a full-repo audit. Each item carries a **tag** (ponytail-review + risk convention) and a **priority**. Working copy of the gaps section in `CODEBASE_MAP.md` §5 — this file is the canonical backlog; update it (and the `CODEBASE_MAP.md` view) when an item is worked.

Legend:
- **[RISK]** — will bite before production (data loss / crash / financial correctness).
- **[FIX]** — correctness or spec gap; should be fixed.
- **[GAP]** — spec feature never built.
- **[IMPROVE]** — quality/refactor, optional.
- `delete:` — ponytail over-engineering finding (dead/duplicated code).
- `stdlib:` — stdlib/native feature replaces hand-rolled code.
- `yagni:` — speculative abstraction; skip unless it earns its keep.

Status key: `▢` open · `🟡` in progress · `✅` done.

---

## A. Correctness / acceptance gaps

### #1 ✅ DONE — Bills list date-range filter (commit `7143b71`)
`BillDao.getFilteredBills(query, startMillis?, endExclusiveMillis?)` — one SQL query combining search + optional start/end, `@Transaction`, joins persons. `BillsViewModel` holds `Pair<Long?, Long?>` (`null to null` = no filter), `combine` + `flatMapLatest`. From/To pickers + Clear button in `BillsListScreen.kt`. CI green; installed on device.

### #2 🔴 [RISK]/[FIX] — Bill number: timestamps, not `INV-0001`
`generateBillNumber` (`BillFormViewModel.kt:138`) = `prefix + yyyyMMdd-HHmmss`. Spec (§3.3) wants incrementing `INV-0001`. **Same-second collision risk.**
- Fix: per-prefix counter. Simplest: a `bill_sequence` table (prefix PK, lastNumber), or `SELECT COALESCE(MAX(...))` on bills; generate + insert inside a single transaction to stay race-free. DataStore alone can't be transactional here.
- Note: B5 made the prefix configurable (`SettingsRepository.invoicePrefix`, default `BILL-`); the scheme itself is still timestamp-based.

### #3 ✅ DONE — Hardcoded "Paid" status on Home (commit `e667d70`)
`RecentBillCard`/list cards now read the real `bill.paymentStatus` (DB v3 column, default `"PENDING"`). Badges color by status in `HomeScreen.kt:233-307` and `BillsListScreen.kt:193-209`.

### #4 🟡 [FIX] — Inconsistent currency formatting (7+ call sites, 3 systems)
`NumberFormat.getCurrencyInstance` (Home `:57`, Reports `:40`) vs raw `"₹%.2f".format(...)` (`BillsListScreen:221`, `BillDetailScreen:206-219`, `BillFormScreen:394-424`) vs `String.format("$%.2f", ...)` — note the **literal `$`** in `ProductsListScreen.kt:186` and `PersonDetailScreen.kt:146` — vs `String.format(Locale.getDefault(), "%.2f", ...)` with **no currency symbol** in `PdfGenerator.kt:156-175` — vs raw interpolation `"₹$revenueThisMonth"` at `HomeScreen.kt:163` (bypasses the formatter defined 6 lines up).
- Fix: one `CurrencyFormatter` util (or top-level `formatMoney(Double): String` using `NumberFormat.getCurrencyInstance(Locale.getDefault())`) in `util/`, replace every call site, delete the `$`-bug sites. `delete:` the dup formatters.

### #5 🔴 [RISK] — `fallbackToDestructiveMigration()` still live (`DatabaseModule.kt:34`)
DB is at **v3 with `MIGRATION_2_3`**, but any un-migrated schema bump **wipes all user data**. Context7: Room ≥2.4 supports `@Database(autoMigrations=...)` + `AutoMigrationSpec`; requires `room { schemaDirectory(...) }` Gradle plugin + `exportSchema = true`.
- Fix path: enable `exportSchema`, commit the generated schema JSON, switch v2→v3 to an `@AutoMigration`, remove the fallback (keep `MIGRATION_2_3` until autoMigration verified). Do before anything that ships to a real user.

### #6 🟡 [RISK] — `BillItem.unit` dropped at bill time
Snapshots store `productName`/`unitPrice`/`taxPercent` but **not `unit`**; edit mode rebuilds `Product(unit = "")` (`BillFormViewModel.kt:100`). Historical bills lose unit info (cosmetic today, wrong invoices tomorrow).
- Fix: add `unitSnapshot: String` column (DB v4 + migration/autoMigration), populate at save, render in PDF/detail.

### #7 🟡 [GAP] — Quick-add inline person in Bill form never built
PROJECT.md §3.3 "or quick-add inline". `PersonDropdown` only picks existing persons.
- Fix (lazy): a "+ New" row in the dropdown that opens a minimal inline name+phone field; on save, insert person and select it. Reuse `PersonFormViewModel`'s insert path.

### #8 ✅ DONE — Dead `MonthlyRevenue` model removed
Entity no longer exists; Reports uses DAO aggregations.

### #9 🟡 [GAP] — Reports screen is a 4-card placeholder
PROJECT.md §5 implies fuller reporting. Currently 4 stat cards (`ReportsScreen.kt`), no per-month/product/person breakdowns, no date-range selector (the bills filter infra from #1 is reusable).
- `yagni:` don't build charts — a monthly breakdown list + per-product totals table covers a shopkeeper's actual needs.

---

## B. Architecture / quality improvements

### #10 🟡 [IMPROVE] — `BillingRepository` is a god-object (~26 edges)
Split into `PersonRepository`/`ProductRepository`/`BillRepository` (or per-module repos); VMs inject the narrow repo. Medium-sized refactor across all VMs — do as one batch with the local check cycle.

### #11 🟡 [IMPROVE] — `AppNavigation.kt` is a 248-line god composable
Extract route constants, bottom bar, and tab items. `delete:` the invisible `enabled=false` spacer `NavigationBarItem` (`AppNavigation.kt:97-103`) — `NavigationBar` already spaces evenly; that hack exists only to fake 4th-tab layout and hides a real "missing a 4th tab" smell.

### #12 🟡 [IMPROVE] — `getMonthBounds()` duplicated (`HomeViewModel.kt:82` + `ReportsViewModel.kt:41`)
Identical Calendar math, twice. Extract to `util/TimeRange.kt`; both call it. ~38 lines saved.

### #13 🟡 [IMPROVE] — `ui/components/` empty; list screens duplicate scaffolding
5 screens duplicate: search field, empty state, confirm-delete dialog, snackbar host. Extract `SearchField`, `EmptyState`, `ConfirmDeleteDialog` under `ui/components/` and refactor each list screen. Highest-leverage UI refactor — also fixes repeated lint suppressions.

### #14 🟡 [IMPROVE] — Dead/duplicated formatter code
- `delete:` raw `"₹$revenueThisMonth in sales • ..."` interpolation at `HomeScreen.kt:163` — uses the line-57 formatter instead.
- Part of #4's unified formatter.

### #15 🟡 [FIX] — `paymentStatus` is free-text `String`
`"PENDING"/"PAID"/"OVERDUE"` hardcoded in the picker (`BillFormScreen`), `when (statusText.uppercase())` in 2 list screens. Typo/branch drift risk.
- Fix (lazy): a `BillStatus` enum + a `@TypeConverter`; keep the column TEXT. No schema change. `paymentStatus` field added in DB v3.

### #16 🟡 [IMPROVE] — Only one unit test exists; no UI/ViewModel tests
`BillCalculatorTest.kt` is the entire suite; no `androidTest/` directory at all. Highest-value adds: `BillFormViewModel` validation + recalc tests, `BillCalculator` edge cases (negative discount, NaN, 100% discount), and a Room **migration test** (v2→v3) given #5.

### #17 🟡 [IMPROVE] — `ProductDao`/`PersonDao` byte-identical in shape
`yagni:` a base-interface abstraction would be speculative for 2 identical DAOs — skip unless a 3rd DAO appears.

### #18 🟡 [IMPROVE] — Naming: "Customers" (nav `AppNavigation.kt:124`) vs "Persons" (screens + spec)
Pick one; fix the nav label. One-word change.

### #19 🟡 [IMPROVE] — `product.unit` free-text
No suggestions/validation for pcs/kg/box. `native:` a simple `DropdownMenu` of common units in `ProductFormScreen` with free-text fallback — no library.

### #20 🟡 [IMPROVE] — Hand-rolled dark colors ignore Material theme
`HomeScreen.kt:60-62` gradient (`0xFF0F172A`/`0xFF020617`) + ~15 more literal hex colors (`:90-282`) bypass `colorScheme`. Breaks light-mode/dynamic-theme consistency. Replace with `MaterialTheme.colorScheme` tokens (or a `HomeGradient` in `theme/`).

### #21 🟡 [IMPROVE] — detekt baseline is stale + masking real signal
`detekt-baseline.xml` still references removed `QuickActionCard`, `StatCard`, `onNavigateToNewBill` symbols (5 matches), and masks 90+ issues. Run `./gradlew detektBaseline` to regenerate (drops dead entries), then treat newly-surfaced items honestly.

### #22 🟡 [IMPROVE] — No backup rules despite `allowBackup=true` (`AndroidManifest.xml:7`)
Room DB + DataStore + logo file unbacked-up/unclassified. Add `dataExtractionRules` + `backup_rules.xml` (exclude nothing sensitive; ensure DB+DataStore get backed up). Out of MVP scope but cheap to add.

### #23 🟡 [IMPROVE] — `copyUriToInternalStorage` (`SettingsScreen.kt:159`) overwrites logo
Always writes `business_logo.jpg`, never cleans old files. Consider `logos/` dir + delete-on-remove. `yagni:` only if logo churn actually happens.

### #24 🟡 [IMPROVE] — Heavy `@Suppress` reducing ktlint/detekt signal
`LongMethod`/`MagicNumber` suppressions sprinkled across screens mask real issues. Resolve by extracting composables (feeds #11/#13), then drop suppressions. Ties into #21.

---

## Ponytail net

```text
delete:  ~7 dead/dup sites (spacer hack, $ formatter, dup formatter, stale baseline, dead model)
stdlib:  NumberFormat / Calendar / Room AutoMigration / Material colorScheme
yagni:   skipped DAO base-interface, charts, quick-add repo abstraction
net:     ~80-100 lines removable, 0 dependencies removable (none over-engineered)
```

## Suggested order of attack

1. **#5** destructive migration (data-loss risk) → #16 migration test
2. **#2** invoice numbering (collision risk, spec gap)
3. **#4/#14** currency formatter sweep (visible inconsistency + `$`-bug)
4. **#6** unit snapshot (needs v4 schema — combine with #5 if not yet shipped)
5. **#7, #9** spec gaps (quick-add person, reports depth)
6. **#12, #18, #21** quick wins (dup logic, naming, baseline regen)
7. **#10, #11, #13, #24** refactor batch (repo split, nav, shared components, suppress-cleanup)
8. **#15, #19, #20, #22, #23** polish (enum status, unit suggestions, theming, backup, logo)
