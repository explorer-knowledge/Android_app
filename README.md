# BillEase — Universal Bill Generator App

Native Android (Kotlin + Jetpack Compose + Material 3) billing & invoice app for small business owners. Fully offline. Manage **Persons (Customers)**, **Products**, and **Bills**, and share any bill as a PDF via the Android share sheet.

## Stack
- Kotlin 100%, Jetpack Compose (Material 3, BOM 2024.04.01), MVVM + Repository
- Room 2.6.1 (SQLite, KSP), Hilt DI, Navigation Compose, Coroutines + Flow
- Min SDK 24, target/compile SDK 34
- PDF via Android built-in `android.graphics.pdf.PdfDocument` (no third-party lib)

## Database Migrations
| Version | Change |
|---|---|
| 1 | Initial schema (Person, Product, Bill, BillItem) |
| 2 | `@ForeignKey` + `@Index` constraints (personId, billId, productId) with `onDelete = ForeignKey.RESTRICT` |
| 3 | Added `bills.paymentStatus` TEXT NOT NULL DEFAULT 'PENDING' |
| 4 | Added `bill_sequences` table (prefix PK, lastNumber) for sequential bill numbers |
| 5 | Added `bill_items.unitSnapshot` TEXT NOT NULL DEFAULT '' (preserves unit in historical bills) |
| 6 | Added `UNIQUE` index on `bills.billNumber` |

- `MIGRATION_2_3` through `MIGRATION_5_6` all live in `di/DatabaseModule.kt` alongside the DB builder — every version from v2 onward now has a real migration.
- **Destructive migration is scoped to v1 only**, via `fallbackToDestructiveMigrationFrom(1)`: v1 predates the `ForeignKey`/`RESTRICT` constraints added in v2 (would need a full table recreation, not a plain `ALTER TABLE`) and has no real-world installs to preserve. A missing migration on any future version bump now fails loudly (`IllegalStateException`) instead of silently wiping data.
- **Not done:** Room `@AutoMigration` + `exportSchema = true` (would remove the need to hand-write future migrations) — deferred because `exportSchema` was never enabled, so there's no historical schema JSON to generate v2→v3/v3→v4/v4→v5/v5→v6 from retroactively; enabling it now would only benefit v6→v7 onward. Revisit alongside the next schema change.

## Architecture Decisions
- **Delete behavior:** deletion is blocked (not cascaded). Enforced twice — a ViewModel count check and a DB-level `ForeignKey.RESTRICT`. Attempting to delete a person/product with existing bills fails with a confirmation warning.
- **PDF generation:** Android's built-in `PdfDocument` API — lightweight, no third-party dependency, sufficient for the invoice layout.
- **Bill snapshots:** `BillItem` stores `productNameSnapshot` / `unitPriceSnapshot` / `taxPercentSnapshot` / `unitSnapshot` so historical bills never change when a product is later edited or deleted.
- **Invoice prefix:** configurable in Settings (DataStore-backed, default `BILL-`). Numbers are sequential per prefix (`BILL-0001`, `BILL-0002`, …) via a `bill_sequences` table, atomic in the DAO transaction.
- **Money math:** all totals live in `domain/BillCalculator.kt` (single source of truth), unit-tested in `BillCalculatorTest.kt`.
- **Payment status:** `Bill.paymentStatus` is a typed `BillStatus` enum (`data/BillStatus.kt`). Persists as TEXT via Room's built-in enum converter (no custom `@TypeConverter`), so the v3 `paymentStatus` column needs no schema change.
- **Lint/quality:** `config/detekt/detekt.yml` overrides detekt's default `FunctionNaming` pattern to allow PascalCase (the Jetpack Compose composable convention); `buildUponDefaultConfig` inherits all other defaults.

## Known Issues
A full-codebase audit on 2026-08-29 found 19 gaps (`docs/improvements.md` §C in the vault); most have since been fixed, including the destructive-migration risk (see the Database Migrations note above). Still open, highest-stakes first:
- **PDF pages are all numbered `1`** and the output stream is never closed (`PdfGenerator.kt:34`, `:224`).
- Only `BillCalculatorTest`/`DateUtilsTest` exist; there's still no test exercising the migration chain end-to-end (`MIGRATION_2_3` → `MIGRATION_5_6`) or `BillFormViewModel`'s validation/recalc logic.

Full triage with file:line evidence is `docs/improvements.md` in the Obsidian vault (§C and the verification table).

## Docs
The planning/reference docs live in the Obsidian vault (`Android_app_easebill/docs/`), not in this repo. `AGENTS.md` here is the index — it maps each doc to its vault path and when to read it.

## Building & CI
- **Never build `assembleDebug` locally** (low-end machine). Builds run in GitHub Actions (`.github/workflows/android-build.yml`): ktlint → detekt → unit tests → assembleDebug, then upload the `app-debug-apk` artifact.
- See `docs/CI_CD_SETUP.md` (vault) for the workflow and `AGENTS.md` for the full local-check + device-testing playbook.

## Tests
- Unit tests: `app/src/test/.../domain/BillCalculatorTest.kt` (only test suite; no instrumented/androidTest coverage yet — see `docs/improvements.md` #16 in the vault).
