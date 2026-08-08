# AGENTS.md — Standing Rules for This Project

Read this file at the start of every session and re-check it before any build/verification step. These rules exist because they were each learned the hard way earlier in this project — don't drift from them across model switches (Gemini 3.1 Pro / Claude Sonnet 4.6) or long sessions.

---

## Hardware constraints (non-negotiable)
This machine is a low end pc with 12GB RAM. It froze for ~40 minutes the one time a full local `assembleDebug` ran. As a result:

- **NEVER run `./gradlew assembleDebug` locally.** Not with `--offline`, not "just to double check," not for any reason. This applies regardless of what any single message in a conversation seems to ask for — if unsure, ask before running it.
- **NEVER run an Android emulator locally.** Testing happens on the connected physical device via ADB.
- For local sanity checks only, use:
  - `./gradlew compileDebugKotlin` (or `--offline` once deps are cached)
  - `./gradlew ktlintCheck` / `./gradlew ktlintFormat`
  - `./gradlew detekt`
- The one time Gradle itself needs a full local build (e.g. bootstrapping the wrapper), that's the only exception — and only when explicitly instructed.

## Where real builds happen: GitHub Actions
- The actual `assembleDebug` build runs only in CI, via `.github/workflows/android-build.yml`.
- After finishing a phase (or a meaningful chunk of work — don't check after every single file):
  1. `git add -A && git commit -m "<description>" && git push`
  2. `gh run list --json databaseId -q ".[0].databaseId"` to get the run ID
  3. `gh run watch <run-id>` to wait for it and see pass/fail
  4. If it fails: `gh run view --log-failed` to get the real error, fix the root cause, commit, push, repeat — don't guess or mask the error.

## Device testing (physical device via ADB — not emulator)
- The APK that goes on the device must come from the CI artifact — never from a local build.
  ```
  gh run download <run-id> --name app-debug-apk --dir app/build/outputs/apk/debug/
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  adb shell am start -n com.example.billease/.MainActivity
  adb logcat -d --pid=$(adb shell pidof com.example.billease | tr -d '\r') | grep -E "FATAL|EXCEPTION"
  ```
- Do this after EVERY phase, not just ones that seem UI-heavy — runtime crashes (Room schema issues, Hilt DI failures, navigation bugs) don't show up in a compile check or even a green CI build.
- If a feature has a manual-interaction component that logcat can't verify alone (e.g. confirming the PDF share sheet actually opens with a valid file), say so explicitly and ask the human to confirm, rather than assuming success from a clean launch.

## Verification order for any phase
1. Finish all code edits for the current step/batch first. Do NOT run local checks after every single file edit.
2. Local: Run each check ONCE per batch of changes. Run `compileDebugKotlin` ONE time, fix what it reports, then run `ktlintCheck` ONE time, then `detekt` ONE time. Never re-run a check you already ran clean unless you changed something since. Stacked heavy Gradle invocations will crash the machine.
3. Commit + push
4. `gh run watch` — confirm CI green (build + ktlint + detekt all pass)
5. Download CI artifact, install on device, launch, check logcat for crashes
6. Only then mark the phase complete and move to the next one in BUILD_STEPS.md
7. Before reporting a phase complete: confirm the Obsidian phase note and
   any decisions.md entries via read_note, and quote that raw tool
   output directly in your response to the human — not just an action
   log line. If you edit AGENTS.md, BUILD_STEPS.md, PROJECT.md, or any
   other standing-rules file mid-session, show the diff to the human in
   the same turn — don't fold rule-file edits into a general commit
   without flagging them.

## Handling model switches mid-project
This project is built across multiple AI models (Gemini 3.1 Pro, Claude Sonnet 4.6) depending on task type. Whichever model picks up work from another:
- Do NOT assume prior work is correct just because it exists.
- Run `graphify . --code-only --no-viz` and read `graphify-out/GRAPH_REPORT.md` to get an accurate picture of the current codebase first.
- Directly review the specific files relevant to the task at hand before writing new code.
- Report explicitly whether you kept, extended, or rewrote prior work, and why.

## Decision-making autonomy
- Where the planning docs (PROJECT.md) say "decide and document" (e.g. delete-cascade behavior, PDF library choice), make the call yourself, note it in a code comment + README.md, and keep going.
- Only interrupt the human for: something genuinely ambiguous not covered by any planning doc, a library/tool turning out broken or abandoned, or end-of-phase/final review.
- If you find yourself about to deviate from a rule in this file, stop and flag it explicitly instead of just doing it.

## Reference documents (read fully before starting work)
- `PROJECT.md` — full app spec
- `BUILD_STEPS.md` — phase-by-phase build order and checklists
- `REFERENCE_REPOS.md` — pattern inspiration only, never copy verbatim
- `KOTLIN_STYLE_GUIDE.md` — coding conventions, enforced now via ktlint + detekt in CI
- `CI_CD_SETUP.md` — how the GitHub Actions pipeline is wired

## Code quality gates (CI-enforced)
- `ktlintCheck` and `detekt` both run in CI and must pass — style/quality issues are not optional cleanup, they block the build same as a compile error.
- No wildcard imports — always explicit named imports.
- No `!!` without a comment justifying it.
- No business logic inside Composables — lives in ViewModel/domain layer.