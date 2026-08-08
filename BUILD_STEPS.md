# BUILD_STEPS.md — Autonomous Execution Plan for Antigravity

Read this alongside `PROJECT.md` (full spec) and `REFERENCE_REPOS.md` (pattern references). Execute phases **in order**. After each phase, self-verify against its checklist before moving to the next — don't wait for manual approval unless something in "Out of Scope" or the data model is ambiguous. Only pause for the human when you hit a genuine judgment call not already decided in PROJECT.md.

---

## Phase 0 — Project Bootstrap
- Create a new Android Studio project: Empty Activity, Compose, Kotlin, `build.gradle.kts`, Min SDK 24
- Set up package structure:
  ```
  com.<org>.billease/
    data/        (Room entities, DAOs, database, repositories)
    di/          (Hilt modules)
    domain/      (use-cases, BillCalculator)
    ui/
      persons/
      products/
      bills/
      home/
      settings/
      theme/
      components/  (shared composables: SearchBar, ConfirmDialog, etc.)
    navigation/
  ```
- Add dependencies: Compose BOM, Material3, Room + ksp, Hilt + hilt-navigation-compose, Navigation Compose, Coroutines
- Verify: app builds and runs to a blank Compose screen

## Phase 1 — Data Layer
- Implement Room entities exactly as specified in `PROJECT.md` §4 (Person, Product, Bill, BillItem)
- Write DAOs with Flow-returning queries (getAll, getById, insert, update, delete, search)
- Set up `AppDatabase` (Room), version 1
- Write Repository classes wrapping DAOs
- Set up Hilt `DatabaseModule` providing DB + DAOs
- Verify: unit test or simple insert/read round-trip works for each entity

## Phase 2 — Persons Module
- ViewModel: list (Flow of all persons, search filter), add/edit form state, delete with confirmation
- Compose screens: PersonsListScreen, PersonFormScreen, PersonDetailScreen (shows their bills)
- Wire navigation routes
- Verify against PROJECT.md §3.1 and the delete-behavior decision recorded there

## Phase 3 — Products Module
- Same pattern as Phase 2, per PROJECT.md §3.2
- Verify against acceptance checklist items for Products

## Phase 4 — Bill Calculation Logic (domain layer, no UI)
- `BillCalculator` class: given a list of (product, quantity) pairs + discount, returns subtotal, taxTotal, grandTotal
- Pure function, unit-testable — write a few unit tests with known inputs/expected outputs
- Verify: totals match hand-calculated examples (e.g., 2 items, one taxed, one discount) before touching UI

## Phase 5 — Bills Module (Create/Edit/View/Delete)
- Bill form: person picker, dynamic line-item list (add/remove rows, product picker + quantity per row), live total using `BillCalculator`, auto bill number generation
- Bill list screen: sorted newest-first, search by bill number/person, date filter
- Bill detail (view) screen: invoice-style read-only layout
- Delete with confirmation dialog
- Verify against PROJECT.md §3.3 and full acceptance checklist for Bills

## Phase 6 — PDF Export + Share
- Implement PDF generation (pick `PdfDocument` or `iText` per PROJECT.md §3 decision) rendering the same invoice layout as the View screen
- Save to app's cache dir, expose via `FileProvider`
- Wire `Intent.ACTION_SEND` share sheet from the Bill Detail screen
- Verify: share a bill to at least one target (e.g., Gmail/Drive) on an emulator and confirm the PDF opens correctly

## Phase 7 — Home Dashboard + Settings
- Home: quick stats (bill count this month, revenue this month), shortcuts to New Bill / Persons / Products
- Settings: business name/address/logo used in the PDF header
- Verify: dashboard numbers match manually-counted DB contents

## Phase 8 — Polish Pass
- Empty states for all three list screens
- Input validation on all forms (per PROJECT.md §7)
- Dark mode / Material You theming check
- Rotation survival check (state hoisting correct, no data loss)
- Verify: walk the full acceptance checklist in PROJECT.md §10 top to bottom, check every box

## Phase 9 — Final Delivery
- Write `README.md`: setup steps, architecture overview, the delete-cascade decision, the PDF library choice and why
- Confirm the project builds clean from a fresh clone (no local-only config left in)
- Summarize to the human: what was built, any deviations from PROJECT.md and why, and the acceptance checklist with pass/fail per line

---

## Ground rules for Antigravity while executing
1. Don't skip Phase 4's unit tests — money math bugs are the easiest thing to ship silently broken.
2. Don't introduce a backend/cloud dependency — this is fully offline per PROJECT.md §7/§8.
3. If a decision point in PROJECT.md is marked "decide and document" (e.g. delete-cascade behavior), make the call, document it in code comments + README, and keep moving — don't stall waiting for approval on things already flagged as your call to make.
4. Only interrupt the human for: (a) ambiguity not covered by PROJECT.md, (b) a chosen library turning out to be broken/abandoned, (c) end of the full build for final review.