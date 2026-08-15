# CODEBASE_MAP.md — BillEase Codebase Context Map

Read this before starting any major change. It maps every piece of functionality to its file/location and lists known gaps + improvement candidates. Keep it in sync when you refactor — if a path below no longer matches, update this file in the same commit.

---

## 1. Project Snapshot

**BillEase** — offline Kotlin Android billing app. MVVM + Repository, Room v3 (`MIGRATION_2_3` in `DatabaseModule.kt`; destructive-migration fallback still present at `DatabaseModule.kt:34`), Hilt, Compose M3, Navigation Compose, DataStore for settings, native `PdfDocument` + FileProvider for PDF share. CI = GitHub Actions (ktlint + detekt + unit tests + assembleDebug, APK artifact).

- App DB version: `3` (see `AppDatabase.kt`)
- Min SDK 24 / target 34, Kotlin JVM 17
- All code under `app/src/main/java/com/example/billease/`

---

## 2. Package Layout

```
com.example.billease/
  BillEaseApplication.kt    # @HiltAndroidApp
  MainActivity.kt           # setContent -> AppNavigation(); leftover Greeting() (dead)
  data/                     # Room entities, DAOs, repository, settings
  di/                       # Hilt modules
  domain/                   # pure business logic (BillCalculator) — no Android imports
  navigation/               # AppNavigation() — all routes + bottom nav
  ui/
    home/        bills/        persons/      products/
    reports/     settings/     theme/        components/   # components/ is EMPTY
  util/                     # PdfGenerator
```

---

## 3. Function Map — Where Everything Lives

### 3.1 Entry points & wiring

| Piece | Location |
|---|---|
| Application class (`@HiltAndroidApp`) | `BillEaseApplication.kt:7` |
| `MainActivity.onCreate` → theme + `AppNavigation()` | `MainActivity.kt:17` |
| Dead `Greeting()` composable (unused) | `MainActivity.kt:33` |
| All nav routes + bottom bar (5 tabs + center FAB → `bill_form/-1`) | `navigation/AppNavigation.kt:43` |
| Route table | `AppNavigation.kt:161`–`:245` (home, reports, settings, persons_list/form/detail, products_list/form, bills_list/form/detail) |

### 3.2 Data layer (`data/`)

| Piece | Location |
|---|---|
| `Person` entity | `Person.kt:7` |
| `Product` entity | `Product.kt:7` |
| `Bill` entity (FK→persons, `RESTRICT`) | `Bill.kt:20` |
| `BillItem` entity (FK→bills & products `RESTRICT`, snapshot fields) | `BillItem.kt:26` |
| `PersonDao` — getAll/search/getById/insert/update/delete | `PersonDao.kt:13`–`29` |
| `ProductDao` — same CRUD | `ProductDao.kt:13`–`29` |
| `BillDao` relations: `BillWithPerson`, `BillWithItemsAndPerson` | `BillDao.kt:14`, `:23` |
| `BillDao` stats: counts, revenue, date-range sums; `getFilteredBills` (search + date range) | `BillDao.kt:46`–`:90` |
| `BillDao` transactional ops: `insertBillWithItems`, `updateBillWithItems`, `deleteBill` | `BillDao.kt:114`, `:125`, `:108` |
| `BillingRepository` — single facade over all 3 DAOs (god-object, ~26 edges) | `BillingRepository.kt:8` |
| `AppSettings` data class | `SettingsRepository.kt:17` |
| `SettingsRepository` — DataStore prefs (`settings`) | `SettingsRepository.kt:26` |
| Room database (v3, `exportSchema=false`, `MIGRATION_2_3` in DatabaseModule) | `AppDatabase.kt:6`, `DatabaseModule.kt:49` |

### 3.3 DI (`di/`)

| Piece | Location |
|---|---|
| `DatabaseModule` — provides DB + 3 DAOs | `DatabaseModule.kt:18` |
| `fallbackToDestructiveMigration()` (flagged pre-release) | `DatabaseModule.kt:31` |

### 3.4 Domain (pure logic)

| Piece | Location |
|---|---|
| `BillItemInput` (snapshot-style input) | `domain/BillCalculator.kt:9` |
| `BillItemInput.fromProduct()` snapshot builder | `BillCalculator.kt:20` |
| Per-line math: `lineSubtotal`, `lineTax`, `lineTotal` | `BillCalculator.kt:33`–`:39` |
| `BillCalculationResult` | `BillCalculator.kt:42` |
| `BillCalculator.calculate()` — discount post-tax, floor at 0 | `BillCalculator.kt:55` |
| Unit tests (7 cases) | `app/src/test/.../domain/BillCalculatorTest.kt` |

### 3.5 Utilities

| Piece | Location |
|---|---|
| `PdfGenerator.generatePdf()` — logo, business header, item table, totals, notes, pagination | `util/PdfGenerator.kt:29` |
| PDF layout constants | `PdfGenerator.kt:22`–`:27` |
| Page-break handling | `PdfGenerator.kt:48` |
| FileProvider URI for share | `PdfGenerator.kt:222` |

### 3.6 UI — screens & ViewModels (one VM per screen)

| Screen | Composable | ViewModel / state |
|---|---|---|
| Home dashboard | `ui/home/HomeScreen.kt:46` | `HomeViewModel.kt:21` |
| Home search field | `HomeScreen.kt:103` | search query `HomeViewModel.kt:32` |
| Home hero stats (this-month revenue/bills) | `HomeScreen.kt:138` | `billsThisMonth:36`, `revenueThisMonth:44` |
| Home recent bills list | `HomeScreen.kt:197` (`RecentBillCard:226`) | `recentBills:56` (search + take 5) |
| Home avatar initial | `HomeScreen.kt:85` | `businessNameInitial:71` |
| Bills list | `ui/bills/BillsListScreen.kt:50` | `BillsViewModel.kt:19` |
| Bills search + delete dialog + list item | `BillsListScreen.kt:83`, `:131`, `:152` | `bills:28`, `deleteBill:43` |
| Bill detail | `ui/bills/BillDetailScreen.kt:55` | `BillDetailViewModel.kt:23` |
| Bill detail share→PDF | `BillDetailScreen.kt:111` | `generatePdf:36` |
| Bill detail delete + totals + line rows | `BillDetailScreen.kt:66`, `:200`, `:239` | `deleteBill:45` |
| Bill form | `ui/bills/BillFormScreen.kt:60` | `BillFormViewModel.kt:63` |
| Discard-on-back (dirty guard) | `BillFormScreen.kt:71` | `isDirty:77` |
| Date picker | `BillFormScreen.kt:240` (`BillDateField`) | `updateBillDate:157` |
| Person picker dropdown | `BillFormScreen.kt:282` | `selectPerson:141` |
| Dynamic line-item rows | `BillFormScreen.kt:320` (`LineItemRow`) | `updateLineItemProduct:162`, `updateLineItemQuantity:175`, `addLineItem:188`, `removeLineItem:193` |
| Live totals card | `BillFormScreen.kt:395` | `recalculate:202` |
| Bill number generation (timestamp-based) | — | `generateBillNumber:136` |
| Save bill (create/edit, validation) | `BillFormScreen.kt:224` | `saveBill:232` |
| Persons list | `ui/persons/PersonsListScreen.kt:48` | `PersonsViewModel.kt:18` |
| Person delete w/ bill-count guard | `PersonsListScreen.kt:137` | `deletePerson:45` |
| Person form | `ui/persons/PersonFormScreen.kt:29` | `PersonFormViewModel.kt:28`, `savePerson:78` |
| Person detail + bill history | `ui/persons/PersonDetailScreen.kt:39` (`BillHistoryItem:118`) | `PersonDetailViewModel.kt:16` |
| Products list | `ui/products/ProductsListScreen.kt:48` | `ProductsViewModel.kt:18` |
| Product delete w/ bill-item guard | `ProductsListScreen.kt:136` | `deleteProduct:45` |
| Product form | `ui/products/ProductFormScreen.kt:30` | `ProductFormViewModel.kt:29`, `saveProduct:80` |
| Reports (stats placeholder) | `ui/reports/ReportsScreen.kt:34` | `ReportsViewModel.kt:16` |
| Settings | `ui/settings/SettingsScreen.kt:44` | `SettingsViewModel.kt:15` |
| Settings logo picker + copy to internal storage | `SettingsScreen.kt:63`, `:159` | `updateSettings:28` |
| Shared components | `ui/components/` — **EMPTY** (planned, never created) | — |
| Theme | `ui/theme/Theme.kt:34` (`BillEaseTheme`), dynamic color `:42` | — |

### 3.7 Tests, build, CI

| Piece | Location |
|---|---|
| Unit tests | `app/src/test/.../BillCalculatorTest.kt` |
| App build script (deps, detekt baseline) | `app/build.gradle.kts` |
| CI pipeline (ktlint + detekt + test + assembleDebug + artifact) | `.github/workflows/android-build.yml` |
| Manifest (FileProvider authority `${applicationId}.fileprovider`) | `app/src/main/AndroidManifest.xml` |

---

## 4. Data Flow (tracing any feature)

```
Composable ──collectAsState──▶ ViewModel (Hilt) ──▶ BillingRepository ──▶ Room DAO ──▶ SQLite
     ▲                          ▲                     ▲
     └── events (onX) ──────────┘  BillCalculator (domain)  SettingsRepository (DataStore)

PDF flow: BillDetailViewModel.generatePdf() -> PdfGenerator -> cacheDir/pdfs -> FileProvider -> ACTION_SEND
```

Invariants:
- Delete protection is double-enforced: ViewModel count check **and** FK `RESTRICT` at DB level.
- Bill items store snapshots (`productNameSnapshot`, `unitPriceSnapshot`, `taxPercentSnapshot`) so editing/deleting a product never rewrites bill history.
- No business logic inside Composables — all math in `BillCalculator` / ViewModels.

---

## 5. Known Gaps & Improvement Candidates

> Each item: **[FIX]** = correctness/acceptance gap (should fix), **[IMPROVE]** = quality/refactor (optional), **[RISK]** = will bite before production.
> **The full 1–24 triaged list lives in `improvements.md`.** This section is the short status view.

### A. Correctness / acceptance gaps

1. ~~[FIX] Bills list has no date-range filter — PROJECT.md §3.3 requires "filter by date range". Only search exists.~~ **DONE (commit 7143b71)**: `BillDao.getFilteredBills()` combines search + optional start/end; `BillsViewModel` holds `Pair<Long?, Long?>`; From/To pickers in `BillsListScreen.kt`.
2. **[FIX]** Bill number is `PREFIX + yyyyMMdd-HHmmss` timestamp (`BillFormViewModel.kt:138`), not the specced incrementing `INV-0001` pattern. Collisions possible if two bills created in the same second.
3. ~~[FIX] Hardcoded mock logic: `RecentBillCard` always shows `statusText = "Paid"` (`HomeScreen.kt:235`) — either implement real status or remove the badge.~~ **DONE (commit e667d70)**: `paymentStatus` is a real `bills` column (DB v3, `MIGRATION_2_3`), picker in `BillFormScreen`, badges in `HomeScreen`/`BillsListScreen`.
4. **[FIX]** Currency is inconsistent: hardcoded `₹`/`$%.2f` strings in `BillsListScreen.kt:221`, `BillDetailScreen.kt:206-219`, `PersonDetailScreen.kt:146`, `ProductsListScreen.kt:186`, `BillFormScreen.kt:394-424`, raw `"₹$revenueThisMonth"` interpolation at `HomeScreen.kt:163`, and `String.format(Locale.getDefault(), "%.2f", ...)` without symbol in `PdfGenerator.kt:156-175` — all vs `NumberFormat.getCurrencyInstance` on Home/Reports. Unify via one formatter util.
5. **[RISK]** `fallbackToDestructiveMigration()` (`DatabaseModule.kt:34`) still wipes user data on any un-migrated schema bump. DB is at **v3 with `MIGRATION_2_3`**; best practice (Room ≥2.4) is `exportSchema=true` + `@Database(autoMigrations=...)` + Room Gradle plugin `schemaDirectory`. Must be replaced before any shipped release.
6. **[RISK]** `BillItem.unit` unit field is dropped at bill time — edit-mode line items reconstruct `Product(unit = "")` (`BillFormViewModel.kt:100`) and snapshots don't store unit. Cosmetic today but means unit info is lost from historical bills.
7. **[GAP]** Quick-add inline person in Bill form (PROJECT.md §3.3 "or quick-add inline") was never built — `PersonDropdown` only picks from existing persons.
8. ~~[GAP] `MonthlyRevenue` (`BillDao.kt:37`) is defined but **never queried/used** — dead model. Either wire it into Reports or delete it.~~ **DONE**: removed; the entity no longer exists.
9. **[GAP]** Reports screen is a 4-card stats placeholder, not the fuller reporting PROJECT.md §5 implies (no per-month breakdown, no product/person breakdowns).

### B. Architecture / quality improvements

10. **[IMPROVE]** `BillingRepository` is a god-object (~26 edges) — split into `PersonRepository`, `ProductRepository`, `BillRepository` (or per-module repos). Update VMs to inject the narrow repo.
11. **[IMPROVE]** `AppNavigation.kt` is a 248-line god composable — extract route constants, the bottom-bar, and tab items into separate composables/functions.
12. **[IMPROVE]** Duplicated month-bounds `Calendar` logic in `HomeViewModel.kt:82` and `ReportsViewModel.kt:41` — extract to a shared util (e.g. `util/TimeRange.kt`).
13. **[IMPROVE]** `ui/components/` is empty. Extract repeated patterns: search field (5 screens duplicate it), confirm-delete dialog, empty state, snackbar host, currency formatter.
14. **[IMPROVE]** Dead code cleanup: `"₹$revenueThisMonth in sales"` raw interpolation on `HomeScreen.kt:163` bypasses the formatter defined on line 57.
15. **[IMPROVE]** Search is Room `LIKE '%q%'` (server-side) on every keystroke via `flatMapLatest` — fine for MVP scale, but note it as a perf lever if the DB grows (consider `FTS4/5`).
16. **[IMPROVE]** No UI tests / ViewModel tests beyond `BillCalculatorTest`. Highest-value additions: `BillFormViewModel` validation + recalculate tests, `BillCalculator` edge cases (negative discount, NaN input).
17. **[IMPROVE]** `ProductDao`/`PersonDao` are byte-for-byte identical in shape — could share a base interface pattern, but low priority.
18. **[IMPROVE]** Naming/UX: person labels mix "Persons"/"Customers" (nav says Customers, screen says Persons, spec says Persons). Pick one.
19. **[IMPROVE]** `product.unit` (pcs/kg/box) is a free-text field — no suggestion list/validation. Could add a dropdown of common units.
20. **[IMPROVE]** `HomeScreen`/theme use hand-rolled dark color values (`0xFF0F172A` etc. at `HomeScreen.kt:60-132`) that ignore the Material theme — makes dark/light theming inconsistent.
21. **[IMPROVE]** detekt baseline (`detekt-baseline.xml`) still references removed `QuickActionCard`/`StatCard`/`onNavigateToNewBill` symbols — prune it (`./gradlew detektBaseline`) to restore real signal.
22. **[IMPROVE]** No `androidx.work`/backup strategy; `allowBackup=true` but no backup rules — device data loss risk noted as out of MVP scope.
23. **[IMPROVE]** `copyUriToInternalStorage` (`SettingsScreen.kt:159`) always overwrites `business_logo.jpg` and doesn't clean up old files; consider a `logos/` dir + delete-on-remove.
24. **[IMPROVE]** Import-formatting / wildcard-import rules are CI-enforced but some files carry heavy `@Suppress("LongMethod", "MagicNumber")` — these suppress ktlint/detekt, reducing real signal.

---

## 6. Playbook — Where to Make Major Changes

### Add a field to an existing entity (e.g. `Bill.paymentStatus`)
1. `data/Bill.kt` — add field with a default (Room can backfill defaults).
2. Bump `AppDatabase.kt:7` to `version = 3` and add a real `Migration` in `DatabaseModule.kt` (remove `fallbackToDestructiveMigration`).
3. `BillDao.kt` — extend queries if filtering/aggregating on it.
4. `BillFormViewModel` — read in `init:87`, write in `saveBill:232`; add widget in `BillFormScreen`.
5. Display: `BillDetailScreen`, `PdfGenerator`, list cards (`BillsListScreen`/`HomeScreen.RecentBillCard`).
6. If it affects math → `BillCalculator` + tests in `BillCalculatorTest.kt`.

### Add a new entity/feature (e.g. Payments)
1. `data/X.kt` (entity) → `data/XDao.kt` (Flow queries) → register in `AppDatabase.kt:7` → provide in `DatabaseModule.kt`.
2. Expose ops via the repository (prefer a new narrow repo per item 10).
3. `ui/x/XScreen.kt` + `XViewModel.kt`; add route in `AppNavigation.kt` (add to `bottomNavRoutes` if tabbed).
4. Pure logic → `domain/` + unit test; DAO round-trip verified on physical device (no emulator).

### Add a new screen / reflow an existing one
- Follow the `ReportsScreen`/`ReportsViewModel` pattern: VM exposes `StateFlow` via `stateIn(WhileSubscribed(5s))`, screen collects + renders. Wire route in `AppNavigation.kt`.

### Change money/currency/aggregation logic
- All math lives in `domain/BillCalculator.kt` — touch only that + its tests. For formatting, add one currency util and replace the hardcoded `₹`/`$` sites (item 4).

### Shared reusable UI
- Create files under `ui/components/` (e.g. `SearchField.kt`, `ConfirmDeleteDialog.kt`, `EmptyState.kt`) and refactor the duplicated blocks (item 13).

---

## 7. Verification Order (per AGENTS.md)

1. Finish all edits for the batch first — no local checks after every file.
2. Local (once per batch): `./gradlew compileDebugKotlin` → `./gradlew ktlintCheck` → `./gradlew detekt`. Never `assembleDebug` locally.
3. Commit + push; `gh run watch`; on failure `gh run view --log-failed`, fix root cause, repeat.
4. Download CI APK artifact, install via ADB, launch, check logcat for crashes.
5. Flag anything requiring human manual confirmation (e.g. PDF share sheet opening) explicitly.
6. Keep this file and `graphify-out/GRAPH_REPORT.md` in sync with any structural change.
