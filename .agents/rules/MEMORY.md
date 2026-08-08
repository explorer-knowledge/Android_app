---
trigger: always_on
---

# MEMORY.md — How to Use the Memory & Docs MCP Tools

You now have two MCP tools connected beyond the standard file/terminal ones:
- **obsidian** (via mcpvault) — persistent project memory across sessions, pointed at `~/Documents/Obsidian Vault/Antigravity_MCP/Android_app_easebill/`
- **context7** — fetches current, version-accurate library documentation on demand

Use both deliberately, not as a dumping ground or a crutch for skipping the planning docs.

See `MCP_TOOLS.md` (in this same `.agents/rules/` folder) for the concrete tool-call patterns for both servers. This file covers *when* and *why*; that one covers *how*.

---

## Part 0 — Verification rule (read this before claiming anything is saved)

This rule exists because Phase 7 reported "saved to Obsidian" when nothing was written — no tool call happened, only a narrated intention.

**A memory write is not complete until it has been verified by a second tool call.** Specifically:

1. Call the Obsidian MCP write tool (create/append/patch) for the note.
2. Immediately call the Obsidian MCP read or list tool on that same path.
3. Only report "saved to memory" to the human if step 2's tool output actually shows the content.
4. If any step in 1–2 errors, is unavailable, or the tool isn't actually connected in this session, say so explicitly — "I could not confirm this was saved, the Obsidian tool did not return content" — instead of assuming it worked.
5. Never describe a memory write in past tense ("I've saved...", "this has been recorded...") based on intent alone. Only after verified.
6. Reporting a memory action as complete requires quoting the tool's raw
response in the same message — an action-log entry like "Used MCP tool:
obsidian/write_note" is not sufficient evidence on its own, since it
doesn't show what the tool actually returned.

This applies to every note type below (phase notes, decisions.md entries, bugs-and-fixes.md entries) — not just end-of-project summaries.

---

## Part 1 — Obsidian memory (project history & decisions)

### What goes in memory vs. what doesn't
- **Goes in memory:** architectural decisions and why they were made, bugs that were hard to find and their root cause, anything explicitly marked "decide and document" in PROJECT.md, the outcome of any model-handoff audit (what was kept vs. rewritten and why), anything a future session (possibly a different model) needs to know before touching related code.
- **Does NOT go in memory:** routine progress updates, anything already fully captured in git commit messages, anything already in AGENTS.md/PROJECT.md/BUILD_STEPS.md (link to them instead of duplicating), raw file contents (the code itself is the source of truth, not a copy of it in a note).

### When to write a note
- At the end of each phase in BUILD_STEPS.md, before moving to the next
- Any time you make a "decide and document" call
- Any time you find and fix a non-obvious bug (e.g. the snapshot-vs-live-data issue in BillCalculator, the RESTRICT delete-order fix in BillDao, the PDF pagination/null-handling fixes)
- Any time you audit prior work from a different model and decide to keep/extend/rewrite it

### When to read memory
- At the start of every session, before doing anything else: list and skim recent notes to re-orient
- Before starting work on a file/feature a past note might cover — search first, don't assume you're starting cold
- Specifically after a model switch (Gemini ↔ Claude): read memory before running the graphify-based audit, not instead of it — memory tells you *why*, graphify tells you *what currently exists*

### Suggested note structure
```
~/Documents/Obsidian Vault/Antigravity_MCP/Android_app_easebill/
  phase-1-data-layer.md
  phase-2-persons.md
  phase-3-products.md
  phase-4-bill-calculator.md
  phase-5-bills-ui.md
  phase-6-pdf-export.md
  phase-7-home-settings.md
  phase-8-polish.md          <- add as each new phase completes
  phase-9-final-delivery.md
  decisions.md                <- running log of every "decide and document" call
  bugs-and-fixes.md           <- running log of non-obvious bugs and their root cause
```

Do not leave placeholder/empty notes (e.g. an untitled note created by opening the Obsidian app) sitting in the vault — delete or fill them before ending a session.

Each phase note, kept short:
```markdown
# Phase N: <name>
Date: <date>
Model: <which model did this phase>

## What was built
<2-4 bullet points, not a full file listing>

## Decisions made
<link to decisions.md entries relevant to this phase>

## Gotchas for future sessions
<anything a future session needs to know before touching this code>
```

`decisions.md` entries, one per decision:
```markdown
## <decision title>
Date: <date> | Phase: <N>
Decision: <what was decided>
Why: <reasoning>
Alternatives considered: <if any>
```

---

## Part 2 — Context7 (current library docs)

### When to use it
- Before writing code against any Jetpack Compose, Room, Hilt, or Navigation Compose API you're not 100% certain is current — training data can be stale on fast-moving library APIs
- When something doesn't compile and the error suggests an API signature has changed
- When implementing a feature that touches a library API for the first time in this project (e.g. a new Room annotation, a new Compose component)

### When NOT to use it
- For basic Kotlin language features or general programming logic — that's not what it's for
- As a substitute for reading this project's own code/docs — Context7 is for *external* library docs, not project-specific context (that's what obsidian memory + graphify are for)
- On every single file edit — only when there's real uncertainty about a current API shape

### How it fits with the other tools
Rough division of labor when starting new work:
1. **AGENTS.md / PROJECT.md / BUILD_STEPS.md** — what to build and the standing rules (always read first)
2. **obsidian memory** — why past decisions were made, what's already been tried
3. **graphify** — what currently exists in the codebase and how it connects
4. **context7** — whether the external library API you're about to use is still current

---

## Ground rule
Memory and Context7 are supplements to the planning docs, not replacements. If AGENTS.md, PROJECT.md, or BUILD_STEPS.md already answers a question, use those first — they're the source of truth and are always in context. Memory exists for the *history and reasoning* those docs don't capture; Context7 exists for *external API accuracy* neither the docs nor training data can guarantee. And per Part 0: nothing counts as "in memory" until a tool call has actually confirmed it's there.