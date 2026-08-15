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

- `MIGRATION_2_3` and `MIGRATION_3_4` live in `di/DatabaseModule.kt` alongside the DB builder.
- **Known risk:** the builder still calls `fallbackToDestructiveMigration()` (`DatabaseModule.kt:34`) — any un-migrated schema bump wipes user data. Planned to be replaced with Room `@AutoMigration` + schema export (see `docs/improvements.md` #5 in the Obsidian vault).

## Architecture Decisions
- **Delete behavior:** deletion is blocked (not cascaded). Enforced twice — a ViewModel count check and a DB-level `ForeignKey.RESTRICT`. Attempting to delete a person/product with existing bills fails with a confirmation warning.
- **PDF generation:** Android's built-in `PdfDocument` API — lightweight, no third-party dependency, sufficient for the invoice layout.
- **Bill snapshots:** `BillItem` stores `productNameSnapshot` / `unitPriceSnapshot` / `taxPercentSnapshot` so historical bills never change when a product is later edited or deleted.
- **Invoice prefix:** configurable in Settings (DataStore-backed, default `BILL-`). Numbers are sequential per prefix (`BILL-0001`, `BILL-0002`, …) via a `bill_sequences` table, atomic in the DAO transaction.
- **Money math:** all totals live in `domain/BillCalculator.kt` (single source of truth), unit-tested in `BillCalculatorTest.kt`.

## Docs
The planning/reference docs live in the Obsidian vault (`Android_app_easebill/docs/`), not in this repo. `AGENTS.md` here is the index — it maps each doc to its vault path and when to read it.

## Building & CI
- **Never build `assembleDebug` locally** (low-end machine). Builds run in GitHub Actions (`.github/workflows/android-build.yml`): ktlint → detekt → unit tests → assembleDebug, then upload the `app-debug-apk` artifact.
- See `docs/CI_CD_SETUP.md` (vault) for the workflow and `AGENTS.md` for the full local-check + device-testing playbook.

## Tests
- Unit tests: `app/src/test/.../domain/BillCalculatorTest.kt` (only test suite; no instrumented/androidTest coverage yet — see `docs/improvements.md` #16 in the vault).
