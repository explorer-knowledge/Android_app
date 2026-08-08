# KOTLIN_STYLE_GUIDE.md — Coding Standards for This Project

Give this to Antigravity alongside PROJECT.md / BUILD_STEPS.md / REFERENCE_REPOS.md. It's not a new feature spec — it's the "how to write the code" layer, so the output reads like idiomatic, production-grade Kotlin instead of generic AI boilerplate.

## Authoritative sources to follow
- **Official Kotlin coding conventions** — kotlinlang.org/docs/coding-conventions.html (naming, formatting, idioms)
- **Android Kotlin style guide** — developer.android.com/kotlin/style-guide (Android-specific additions on top of the above)
- **Jetpack Compose API guidelines** — developer.android.com/develop/ui/compose/api-guidelines (naming/structuring composables)

Tell Antigravity explicitly: *"Follow the official Kotlin coding conventions and the Android Kotlin style guide throughout. Where they conflict with a convenience shortcut, follow the style guide."*

## Concrete rules to enforce in this project

**Null safety & idioms**
- No `!!` unless truly unavoidable and commented why
- Prefer `?:`, `?.let`, sealed classes / `when` exhaustiveness over nullable-chain spaghetti
- Use `data class` for all entities, DTOs, and UI state holders

**Coroutines & Flow**
- No `GlobalScope` — always scope to `viewModelScope` or a repository-level `CoroutineScope` injected via Hilt
- Use `Flow`/`StateFlow` for anything observed by the UI; use `suspend fun` for one-shot operations
- All Room/DB access off the main thread (Room does this by default for suspend DAOs — don't fight it)

**Compose**
- Stateless, hoisted composables: state and event lambdas passed in, not read from a ViewModel inside a deeply nested composable
- One `@Composable` per logical UI piece — favor small composables over 300-line screen functions
- Use `remember`/`rememberSaveable` correctly (saveable for anything that must survive rotation)
- Preview functions (`@Preview`) for at least the key reusable components (bill row, product row, person row)

**Architecture discipline**
- ViewModels never reference `Context`-heavy Android framework classes directly (inject what's needed via Hilt, e.g. `@ApplicationContext`)
- Repositories are the only classes touching DAOs; ViewModels never touch DAOs directly
- No business logic (tax/total math) inside Composables — lives in `BillCalculator` / domain layer per BUILD_STEPS.md Phase 4

**Naming**
- Classes/Composables: `PascalCase`; functions/vals: `camelCase`; constants: `UPPER_SNAKE_CASE`
- Composable functions are nouns describing what they show (`BillDetailScreen`, `ProductRow`) — not `render...` or `draw...`

**Testing**
- Every pure function in the domain layer (esp. `BillCalculator`) gets a unit test with at least 2–3 cases (including an edge case like zero quantity or 100% discount)

## Why this file matters
Without an explicit style layer, AI-generated Android code tends to drift into: God-ViewModels, business logic leaking into Composables, `!!` everywhere, and inconsistent naming across files it wrote in different passes. Pointing Antigravity at this file keeps every phase of BUILD_STEPS.md consistent with the last.