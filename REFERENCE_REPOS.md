# Reference Repositories

Give Antigravity these links as **reference/inspiration**, not to be cloned wholesale — tell it explicitly: "study these for patterns, then build a fresh project per PROJECT.md and BUILD_STEPS.md." Mixing in a full clone tends to drag in unwanted architecture/dependency choices.

## Closest match to this app (bills + customers + items + PDF share)
- **QuickBills** — `github.com/Synergise-IIT-Bhubaneswar/QuickBills`
  Kotlin Android app: shopkeeper bill generation, PDF sharing, customer records, item/price records, logo upload on invoices. Very close 1:1 to your feature set — good for UX flow and PDF-sharing pattern.

- **android-invoice-generator** — `github.com/fidisys/android-invoice-generator`
  Basic invoice generation app — useful as a minimal reference for invoice math/layout.

- **MyMobills** — `github.com/siddhraj-sinh/MyMobills`
  Invoice generator using MVVM, Navigation Components, Room DB for storing generated invoices. Good for MVVM + Room wiring patterns.

## Architecture / stack references (Compose + Room + Hilt + MVVM)
- **roomDBDemo** — `github.com/PanktiSP13/roomDBDemo`
  Clean CRUD-with-Room example using MVVM, Hilt, LiveData, and Kotlin Flow, plus DB migrations — good template for your Person/Product DAOs.

- Search GitHub topics `room-database` + `jetpack-compose` + `mvvm` for more current examples (this space moves fast; re-check before building since results shift month to month).

## Cross-platform invoice PDF generation write-up (good for the PdfDocument logic specifically)
- Medium: "Building a Cross-Platform Invoice Generator with Compose Multiplatform & Kotlin Multiplatform" — shows raw `android.graphics.pdf.PdfDocument` usage to draw an invoice canvas (order id, customer, line items) — directly reusable for the Android-only PDF export step.

## What to tell Antigravity about these
> "Look at QuickBills and MyMobills for feature/UX parity with what I'm building. Look at roomDBDemo for the Room+Hilt+MVVM wiring pattern. Do not copy their code verbatim or reuse their package names — use them only as a reference for structure, then implement fresh according to PROJECT.md and BUILD_STEPS.md, using Jetpack Compose + Material 3 throughout (some of these references still use XML/Fragments — port the *patterns*, not the *UI toolkit*)."

Note: repo activity/quality can change — if any of these look stale or archived when Antigravity checks, that's fine, they're for pattern reference only, not a dependency.