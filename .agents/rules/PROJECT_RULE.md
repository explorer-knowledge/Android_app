---
trigger: always_on
---

# Project Rules

## UI / UX

- Follow a consistent design system across the entire app: spacing, typography, colors, shapes, icons, buttons, inputs, cards, and navigation must feel like one product.
- Prefer clean, minimal layouts with strong visual hierarchy. Do not add decoration, gradients, animations, cards, or UI elements unless they improve usability.
- Every screen must have a clear primary action and an obvious information hierarchy.
- Reuse existing UI components before creating new ones. If the same pattern appears more than once, extract a shared component.
- Never use arbitrary colors, typography, spacing, or component styles when an existing theme/token/component can be reused.
- Design for states, not just the happy path: loading, empty, error, success, validation, and disabled states must be intentional.
- Keep touch targets comfortable and accessible. Do not rely on color alone to communicate meaning.
- Handle long text, keyboard interaction, small screens, and system back navigation without breaking the layout or losing user input.
- Optimize important workflows for the fewest reasonable steps, especially:
  `Home → New Bill → Person → Items → Review → Save → Export/Share`.

## Compose / Architecture

- Keep Composables focused on rendering UI and emitting events.
- Keep business logic, calculations, validation, persistence, and PDF logic outside Composables.
- Use unidirectional data flow: `UI → ViewModel → Domain/Repository → UI State`.
- Maintain a single source of truth. Do not duplicate state or calculations between UI layers.
- Prefer existing project architecture and patterns over introducing new abstractions or dependencies.

## Billing Integrity

- `BillCalculator` is the single source of truth for bill calculations. Never duplicate subtotal, tax, discount, or total logic.
- Bill items must preserve historical snapshot values. Never replace invoice history with current Product data.
- Totals must update immediately from current line-item state.
- Never allow UI changes to compromise historical invoice data.

## Persistence / PDF

- Business settings remain persisted through DataStore.
- Store the selected business logo in internal storage and persist its filesystem path; do not revert to storing a fragile Content URI.
- PDF generation must use persisted business settings and invoice snapshot data rather than hardcoded or live product values.
- Long product names must not overlap or break PDF layout.

## Implementation Discipline

- Before changing a screen, inspect and reuse its existing patterns.
- Do not rewrite working architecture or unrelated code for a feature.
- Do not introduce a dependency when the existing stack can solve the problem cleanly.
- Add or update tests when changing business logic.
- Verify changes incrementally with:
  `compileDebugKotlin → ktlintCheck → detekt`
  rather than running heavy Gradle checks concurrently.
- A feature is complete only when it works visually, functionally, and across its important UI states—not merely when it compiles.