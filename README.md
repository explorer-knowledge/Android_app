# Universal Bill Generator App

## Database Migrations
Version 1 -> Version 2:
- Added `@ForeignKey` constraints and `@Index` to `Bill.kt` (personId) and `BillItem.kt` (billId, productId) using `onDelete = ForeignKey.RESTRICT` to enforce delete-blocking at the database level.
- Note: Since there is no real user data yet in early development, no explicit migration script is provided. You may need to uninstall and reinstall the app on the test device to recreate the database.

## Technical Decisions
- **PDF Generation**: We use Android's built-in `android.graphics.pdf.PdfDocument` API. It is lightweight, does not require third-party dependencies, and is sufficient for the simple invoice layout required.
