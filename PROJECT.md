# Project: BillEase — Kotlin Android Billing & Invoice App

## 1. Overview

BillEase is a native Android application, written entirely in **Kotlin**, that lets a small business owner or freelancer create, manage, and share bills/invoices. The app manages three core entities — **Bills**, **Products**, and **Persons (Customers)** — and supports full CRUD across all of them, plus generating a shareable PDF/image of a bill.

This document is the single source of truth for scope, architecture, data model, and acceptance criteria. Build strictly against it; do not invent extra features not listed in "Out of Scope."

---

## 2. Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin (100%, no Java) |
| UI Toolkit | Jetpack Compose (Material 3) |
| Architecture | MVVM + Repository pattern |
| Local Database | Room (SQLite) |
| Async | Kotlin Coroutines + Flow |
| Dependency Injection | Hilt |
| Navigation | Jetpack Navigation Compose |
| PDF Generation | Android `PdfDocument` API or `iText`/`itextpdf` (pick one, justify choice in README) |
| Sharing | Android `Intent.ACTION_SEND` with `FileProvider` |
| Min SDK | 24 (Android 7.0) |
| Target SDK | Latest stable |
| Build System | Gradle (Kotlin DSL — `build.gradle.kts`) |

---

## 3. Core Modules & Features

### 3.1 Persons (Customers) Module
- Add / Edit / Delete a person
- Fields: `id`, `name`, `phone`, `email (optional)`, `address (optional)`, `gstNumber (optional)`
- List view with search/filter by name or phone
- Tap a person to view their bill history
- Prevent deletion of a person who has existing bills (show confirmation dialog with warning), or cascade-delete their bills — **decide one behavior and document it**

### 3.2 Products Module
- Add / Edit / Delete a product
- Fields: `id`, `name`, `unitPrice`, `unit (pcs/kg/box/etc.)`, `taxPercent (optional)`, `description (optional)`
- List view with search/filter by name
- Prevent deletion of a product referenced in existing bills (same rule as above — document the chosen behavior)

### 3.3 Bills Module (core feature)
- **Create Bill**:
  - Select a person from the Persons list (or quick-add inline)
  - Add one or more line items by selecting products, each with quantity (unit price auto-fills from product, editable)
  - Auto-calculate: subtotal, tax, discount (optional field), grand total
  - Auto-generate bill number (e.g. `INV-0001`, incrementing) and bill date (editable, defaults to today)
  - Optional notes/terms field
- **View Bill**: Read-only invoice-style layout (see Section 5) with a "Share" and "Edit" action
- **Edit Bill**: Reopen an existing bill in the same form used to create it, pre-filled
- **Delete Bill**: Confirmation dialog before permanent delete
- **Share Bill**: Export the bill as a PDF and share via Android's native share sheet (WhatsApp, Email, Drive, etc.)
- **List Bills**: All bills sorted by date (newest first), with search by bill number or person name, and filter by date range

---

## 4. Data Model (Room Entities)

```kotlin
@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String? = null,
    val address: String? = null,
    val gstNumber: String? = null
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unitPrice: Double,
    val unit: String,
    val taxPercent: Double = 0.0,
    val description: String? = null
)

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billNumber: String,
    val personId: Long,
    val billDate: Long,          // epoch millis
    val discount: Double = 0.0,
    val notes: String? = null,
    val subtotal: Double,
    val taxTotal: Double,
    val grandTotal: Double,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "bill_items")
data class BillItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val productId: Long,
    val productNameSnapshot: String,   // preserve name even if product later edited/deleted
    val quantity: Double,
    val unitPriceSnapshot: Double,
    val taxPercentSnapshot: Double,
    val lineTotal: Double
)
```

> Note the "snapshot" fields on `BillItem` — this ensures a historical bill stays accurate even if a product's price changes later. This is a deliberate design decision, keep it.

---

## 5. Screens (Compose)

1. **Home / Dashboard** — quick stats (total bills this month, total revenue), shortcuts to "New Bill," "Persons," "Products"
2. **Bills List** — search, filter, tap to view
3. **Bill Detail (View)** — invoice layout: business header (editable in Settings), person info, itemized table, totals, Share/Edit/Delete buttons
4. **Bill Create/Edit Form**
5. **Persons List** + **Person Detail** (shows that person's bills)
6. **Person Create/Edit Form**
7. **Products List**
8. **Product Create/Edit Form**
9. **Settings** — business name/logo/address used on generated invoice PDFs (optional but recommended)

---

## 6. Architecture Rules

- Strict MVVM: Composables are stateless where possible, hoist state to `ViewModel`
- One `Repository` per entity (or a unified `BillingRepository`), talking only to Room DAOs
- Use `Flow`/`StateFlow` for reactive UI updates — no manual polling
- Use Hilt for all dependency injection (DB, DAOs, Repositories, ViewModels)
- All DB operations off the main thread via coroutines (`Dispatchers.IO`)
- No business logic inside Composables — calculations (totals, tax) live in ViewModel or a `BillCalculator` use-case class

---

## 7. Non-Functional Requirements

- Works fully offline (no backend/server required for MVP)
- Handle empty states gracefully (e.g., "No bills yet — create your first one")
- Input validation on all forms (no empty name, price must be > 0, quantity > 0, etc.)
- Dark mode support via Material 3 dynamic theming
- Confirmation dialogs before any destructive action (delete bill/product/person)
- App should not crash on device rotation (state must survive config changes)

---

## 8. Out of Scope (MVP)

- User authentication / multi-user accounts
- Cloud sync / backend server
- Multi-currency support
- Payment gateway integration
- Barcode scanning for products

---

## 9. Deliverables

- Full Android Studio project (Kotlin, Gradle Kotlin DSL)
- Working Room database with migrations set up from v1
- PDF export + share working on-device
- README.md explaining setup, architecture decisions (esp. delete-cascade behavior and PDF library choice)
- (Optional, nice-to-have) Basic unit tests for `BillCalculator` totals logic

---

## 10. Acceptance Criteria Checklist

- [ ] Can add/edit/delete a Person
- [ ] Can add/edit/delete a Product
- [ ] Can create a Bill selecting an existing Person and multiple Products with quantities
- [ ] Totals (subtotal, tax, grand total) calculate correctly and update live as items change
- [ ] Can view a generated Bill in a clean invoice layout
- [ ] Can edit an existing Bill and totals recalculate
- [ ] Can delete a Bill with confirmation
- [ ] Can share a Bill as a PDF via Android share sheet
- [ ] All lists support basic search
- [ ] App survives rotation without data loss
- [ ] No crashes on empty states (zero persons/products/bills)