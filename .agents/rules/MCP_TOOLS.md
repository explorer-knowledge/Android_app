---
trigger: always_on
---

# MCP_TOOLS.md — Concrete Usage Guide for the Obsidian and Context7 MCP Servers

`MEMORY.md` covers *when* to use these tools and what belongs in each. This file covers *how* — the actual tool-call mechanics, so a session doesn't fall back to narrating an action instead of performing one (the Phase 7 failure this file exists to prevent).

If any tool name below doesn't match what's in your live tool list, **do not guess** — call the client's tool-listing mechanism first, find the real name/schema, and use that. This doc describes the commonly-shipped shape of these two servers; exact names can vary by version.

---

## 0. The one rule that matters more than any tool name

**A tool is only "used" if you actually see a tool-result come back.** If you write a sentence like "I've saved this to Obsidian" without a preceding tool call and result in the same turn, that sentence is false. Every claim in this section maps to a required tool call — treat the mapping as mandatory, not illustrative.

---

## 1. Obsidian MCP (`@bitbonsai/mcpvault`)

This project runs the real `@bitbonsai/mcpvault` package, configured in `opencode.json`:

```json
"obsidian": {
  "type": "local",
  "command": ["npx", "@bitbonsai/mcpvault@latest", "/home/user/Documents/Obsidian Vault/Antigravity_MCP/Android_app_easebill"]
}
```

### ⚠️ The vault root is already the phase-notes folder — paths must be relative to it

The path passed as a CLI arg at startup **becomes the server's `/`**. This server is a sandboxed filesystem tool — it enforces "relative path enforcement" and "path traversal protection" against that root, and refuses anything outside it. Concretely:

- Vault root (as configured) = `~/Documents/Obsidian Vault/Antigravity_MCP/Android_app_easebill/`
- Every `path` argument you pass to any tool below is **relative to that folder**, not to `~/Documents/Obsidian Vault/`.
- So the correct path for the phase-6 note is `phase-6-pdf-export.md` — **not** `Antigravity_MCP/Android_app_easebill/phase-6-pdf-export.md` and **not** `Android_app_easebill/phase-6-pdf-export.md`.

The `Error: Directory not found: Antigravity_MCP` you hit was caused by exactly this: a call like `{"path": "Antigravity_MCP"}` asks the server for a subfolder named `Antigravity_MCP` *inside* the already-scoped root, which doesn't exist there. The project's own README lists this precisely: *"File not found when paths look correct → Cause: the server is using the wrong vault root → Solution: pass paths relative to the vault root."* If you ever want to sanity-check the root, call `list_directory` with no `path` (or `path: "/"` or `""`) first — whatever it returns *is* the vault root's contents.

### Real tool set (18 tools total — these are the ones you'll actually use)

| Tool | Purpose |
|---|---|
| `list_directory` | List files/dirs at a given path (omit `path`, or use `"/"`/`""`, for vault root) |
| `read_note` | Read one note's parsed frontmatter + content |
| `read_multiple_notes` | Batch-read up to 10 notes at once |
| `write_note` | Create or overwrite a note; supports `mode: "overwrite" \| "append" \| "prepend"` |
| `patch_note` | Replace an exact substring inside an existing note without rewriting the whole file — best for adding one `## <decision>` block to `decisions.md` |
| `delete_note` | Delete a note — requires `confirmPath` to exactly match `path` (safety gate) |
| `get_frontmatter` / `update_frontmatter` | Read/update just the YAML frontmatter |
| `search_notes` | BM25-ranked search across vault content/frontmatter |
| `get_notes_info` / `get_vault_stats` | Metadata without reading full content |
| `manage_tags` / `list_all_tags` | Add/remove/list tags |
| `move_note` / `move_file` | Rename/relocate |

### Standard workflow: writing a new phase note
1. `list_directory` with no path (vault root) — confirm `phase-7-home-settings.md` doesn't already exist, and see the real current contents (this is also how you'll catch stale files like `Untitled.md`).
2. `write_note` with `path: "phase-7-home-settings.md"`, `mode: "overwrite"`, and the full note body per the structure in `MEMORY.md` Part 1.
3. `read_note` with `path: "phase-7-home-settings.md"` — confirm the returned `content` matches what you wrote.
4. Only now report success, quoting the confirmed path and the tool's own response message (e.g. `"Successfully wrote note: phase-7-home-settings.md (mode: overwrite)"`).

### Standard workflow: appending a decision
1. `read_note` on `decisions.md` — check the existing content isn't already covering this decision (it may be, from earlier work — verify rather than assume either way).
2. `write_note` with `path: "decisions.md"`, `mode: "append"`, and the new `## <decision title>` block — or `patch_note` if inserting mid-file rather than at the end.
3. `read_note` on `decisions.md` again — confirm the new block is present in the returned `content`.

### Standard workflow: reading memory at session start
1. `list_directory` with no path — get the real current file list (don't trust what a past session's chat transcript claimed was there).
2. `read_note` on anything relevant to the current task, and on `decisions.md` if about to make an architectural call.
3. Do this *before* running graphify or writing new code — memory tells you *why* past decisions were made; graphify only tells you what currently exists.

### Standard workflow: cleaning up a stray note
`delete_note` with matching `path` and `confirmPath` (both required, must be identical, or the call is rejected) — e.g. to remove `Untitled.md`:
```json
{"path": "Untitled.md", "confirmPath": "Untitled.md", "trashMode": "local"}
```
`trashMode: "local"` moves it to the vault's `.trash` instead of permanently deleting it — safer default than `"none"`.

### Common failure modes to watch for
- **Path includes the vault-root folder name** — this is what caused today's error. Paths are relative to the configured root, never repeat `Antigravity_MCP` or `Android_app_easebill` inside a `path` argument.
- **Tool not actually connected this session** — the tool doesn't appear in your available tools list even though it's documented here. State this plainly rather than proceeding as if it's available.
- **Silent no-op** — a `write_note` call returns a success message but a follow-up `read_note` shows no change. Always do the read-back; don't trust the response message alone.
- **Stray notes** — files like `Untitled.md` accumulate from manual Obsidian app use. If `list_directory` shows one, flag it and offer `delete_note` rather than ignoring it.
- **`patch_note` needs an exact, unique substring** — if `oldString` matches multiple places it fails by default (`matchCount` > 1) unless `replaceAll: true` is set; check the response for `matchCount` before assuming it worked.

---

## 2. Context7 MCP

Used for current, version-accurate docs on external libraries (Compose, Room, Hilt, Navigation Compose, etc.) — never for this project's own code.

### Typical tool set
| Tool | Purpose |
|---|---|
| `resolve-library-id` | Turn a plain library name (e.g. "androidx room", "hilt") into the Context7-specific library ID it needs for the next call |
| `get-library-docs` | Fetch current documentation/snippets for a resolved library ID, optionally scoped to a topic (e.g. "migrations", "compose navigation arguments") |

### Standard workflow
1. Call `resolve-library-id` with the plain-language library name.
2. Take the returned ID and call `get-library-docs`, passing a `topic` if you have a specific API area in mind (e.g. "Room ForeignKey onDelete") — this narrows the returned docs instead of dumping the whole library reference.
3. Use the returned snippets to confirm the API shape before writing code against it. If the docs contradict what you were about to write, prefer the docs — that's the entire point of calling this tool.

### When to skip straight to writing code instead
- Core Kotlin language features (no library involved)
- APIs you've already confirmed via Context7 earlier in *this same session* and haven't changed
- Anything that's this project's own code (BillCalculator, the Repository classes, etc.) — Context7 has no visibility into this codebase

### Common failure modes to watch for
- Calling `get-library-docs` with a guessed library ID instead of one returned by `resolve-library-id` — this silently returns empty or irrelevant results.
- Treating a Context7 response as confirmation of *this project's* usage when it's actually generic library documentation — cross-check against the project's actual style (`docs/KOTLIN_STYLE_GUIDE.md` in the vault) before applying a suggested pattern verbatim.

---

## 3. Quick reference: what to do at the start of any session

1. Read `AGENTS.md` (the index), plus any vault planning docs relevant to the task via `obsidian_read_note(path="docs/...")`.
2. `list_files_in_dir` the Obsidian vault, then `get_file_contents` anything relevant to the task at hand.
3. Proceed with the task.
4. Before ending the session or moving to the next `docs/BUILD_STEPS.md` phase: write the phase note and any decisions, and **verify each write with a read-back per Section 0 of this file** before reporting anything as saved.