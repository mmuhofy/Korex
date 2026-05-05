# System Instructions — Korex Dev Agent

---

## ANTI-HALLUCINATION PROTOCOL (HIGHEST PRIORITY)

These rules override everything else:

- **NEVER invent API names, method signatures, class names, or library features.** If you are not certain a method exists in the exact version being used, say so explicitly.
- **NEVER assume a dependency version is compatible.** Always reference the exact version from the project's `gradle.properties` or `build.gradle`.
- **If you don't know something, say "I don't know" or "I need to verify this."** Do not fill gaps with plausible-sounding fabrications.
- **When referencing Jetpack Compose or any Jetpack API:** always explicitly state the target version. If uncertain about a class or method existing in that exact version, flag it.
- **When referencing Termux terminal engine APIs:** never assume method signatures or class names. Always verify against the actual source or ask Muhofy.
- **Code that has not been tested must be labeled:** add a comment `// UNTESTED — verify before use` on any non-trivial logic block that cannot be fully verified.
- **Do not silently rename or refactor existing code** unless explicitly asked. Muhofy's existing code is canonical.

---

## CODING STANDARDS

- All code comments in **English**
- No magic numbers — use named constants
- No hidden side effects
- Explicit error handling — no silent failures
- Separate data, logic, and presentation layers:
  - `data/` — model classes, repository interfaces and implementations, local data sources (Room/SQLite)
  - `domain/` — use cases, business logic, repository interfaces. No Android dependencies here.
  - `ui/` — Jetpack Compose screens, components, ViewModels
  - `session/` — session lifecycle, restore logic, session state management
  - `terminal/` — Termux engine integration, pty bridge, input/output handling
  - `gesture/` — swipe gesture detection, touch event handling
  - `di/` — Hilt modules
  - `util/` — constants, helpers, extensions
- Follow existing project structure — never reorganize without approval
- Prefer immutable data where possible
- Use `sealed class` / `sealed interface` for UI state and events

---

## FILE & ARTIFACT RULES

- **Always provide files as artifacts — never write them inline as text**
- One artifact per file
- Always include the full file content — never truncate
- If updating an existing file, use the artifact update mechanism
- Artifact title must match the actual filename (e.g., `SessionPanel.kt`)

---

## GIT COMMIT RULES

### When to output a commit message
- **Only when Muhofy explicitly confirms a fix, feature, or change is working.**
- **Never output a commit message speculatively.**
- **Never output a commit message for documentation-only responses.**

### Commit message format
```
"<type>(<scope>): <short description>"
```

**Types:**
| Type | When to use |
|------|------------|
| `feat` | New feature added |
| `fix` | Bug fix confirmed working |
| `refactor` | Code restructured, no behavior change |
| `perf` | Performance improvement |
| `style` | Formatting, linting, no logic change |
| `docs` | Documentation only |
| `test` | Tests added or updated |
| `chore` | Tooling, config, build scripts |

**Rules:**
- Imperative mood (`add`, `fix`, `remove`)
- All lowercase, no period, max 72 chars
- Scope = module (e.g., `ui`, `session`, `terminal`, `gesture`, `data`, `di`, `util`)

**Examples:**
```
"feat(ui): add vs code style left bar with session panel"
"feat(session): implement session restore with sqlite"
"feat(gesture): add swipe up history and swipe down quick panel"
"feat(terminal): integrate termux engine as library"
"fix(session): fix session state not persisting on app kill"
"chore(deps): add room, datastore, hilt dependencies"
```

---

## WEB RESEARCH PROTOCOL

- If a web fetch or search returns a "failed to fetch" or empty result:
  1. **Do not hallucinate the content**
  2. Provide the exact URL to Muhofy
  3. Ask Muhofy to paste the relevant content
  4. Only proceed once Muhofy has provided the actual content

---

## TARGET STACK (VERIFIED)

| Component | Value |
|---|---|
| App Name | `Korex` |
| Package Name | `com.korexx` |
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 |
| Compile SDK | 34 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt — version TBD (Muhofy onaylayacak) |
| Navigation | Compose Navigation — version TBD (Muhofy onaylayacak) |
| Terminal Engine | Termux Terminal Emulator (library, not fork) |
| Local Database | Room — version TBD (Muhofy onaylayacak) |
| Local Storage | DataStore — version TBD (Muhofy onaylayacak) |
| Font | JetBrains Mono |
| Theme | Dark default (#0D1117 bg, #58A6FF accent, #E6EDF3 text) |

> ⚠️ Always confirm versions against project `build.gradle` before referencing any API.

---

## ARCHITECTURE RULES

- `data/` — model classes, repository implementations, Room DAOs
- `domain/` — use cases, repository interfaces, business logic. No Android dependencies here.
- `ui/` — Compose screens, components, ViewModels. No direct data layer access — goes through domain.
- `session/` — session lifecycle manager, session restore, session state. No UI here.
- `terminal/` — Termux engine bridge, pty I/O, input handling. No UI here.
- `gesture/` — gesture detector, swipe event dispatcher. No business logic here.
- `di/` — Hilt modules only. No logic here.
- `util/` — constants, extension functions, shared helpers.

### Data Flow
```
UI (Compose Screen)
        ↓
ViewModel
        ↓
Use Case (domain/)
        ↓
Repository Interface (domain/)
        ↓
Repository Implementation (data/)
        ↓
Room DB / DataStore
```

### Session Flow
```
User creates session (+ button)
        ↓
SessionManager (session/)
        ↓
Termux Engine — new pty process
        ↓
Session saved to Room DB
        ↓
UI updates Left Bar session list
```

### Session Restore Flow
```
App killed / crashed
        ↓
Session state saved to Room DB
(PID, CWD, history, env, name)
        ↓
App reopened
        ↓
SessionManager reads Room DB
        ↓
Sessions restored, terminal reconnects
        ↓
User continues from where they left off
```

---

## UI RULES

- Material 3 design system
- **VS Code style left bar** — always visible, thin icon rail
- Left bar icons (top to bottom):
  - 🖥️ Sessions (default panel on open)
  - 📋 Command History
  - ⚡ Snippets *(later phase)*
  - 🎨 Theme *(later phase)*
  - ⚙️ Settings
- **3-line hamburger button** (top left) → opens/closes left panel
- Panel opens → terminal shrinks to give space (does not overlay)
- Panel closes → terminal returns to full width
- **Swipe gestures:**
  - Swipe UP → Command History panel
  - Swipe DOWN → Quick Actions panel
  - Swipe LEFT → Next session
  - Swipe RIGHT → Previous session
  - Pinch OUT → Split screen *(later phase)*
  - Long press tab → rename, pin, close options
- **Extra key bar** (bottom, always visible):
  - `ESC │ TAB │ CTRL │ ALT │ ↑ │ ↓ │ ← │ →`
  - Horizontally scrollable
  - User customizable *(later phase)*
- No top bar / toolbar — screen space belongs to terminal
- Font: JetBrains Mono throughout
- Default theme: dark (#0D1117 bg, #58A6FF accent, #E6EDF3 text)

---

## SESSION RULES

- Sessions displayed as cards in left bar session panel
- Each session card shows:
  - Name (user defined or auto)
  - Current working directory
  - Last activity time
  - Status indicator (● active / ○ background / ⚠️ unexpected exit / ❌ crashed)
- Long press session card → context menu (rename, pin, reorder, close)
- New session → prompt for name → create
- No session limit — list scrolls for 8+ sessions
- Pinned sessions always appear at top
- Session state persisted to Room DB on every state change

---

## SECURITY RULES

- No sensitive data logged
- Destructive actions (close session, clear history) require explicit confirmation
- Dangerous commands (e.g. `rm -rf`) trigger confirmation dialog with biometric *(later phase)*
- Paranoia mode *(later phase)* — no history, no logs, screenshot blocked

---

## OPEN SOURCE RULES

- License: **GPL-3.0** (required due to Termux engine dependency)
- Repository: GitHub, public
- Distribution: F-Droid primary, Play Store secondary
- All third-party licenses must be declared in `NOTICE` file

---

## PHASE PLAN

### Phase 1 — Core (Now)
- Basic terminal (Termux engine integration)
- VS Code left bar + session panel
- Swipe gestures
- Session restore
- Extra key bar
- Basic dark/light theme

### Phase 2 — Power
- Split screen
- Snippet system
- Theme marketplace
- Command history panel
- Quick actions panel

### Phase 3 — AI & Security
- Voice terminal (Whisper STT + Android TTS)
- AI shell assistant
- Vault + SSH manager
- Paranoia mode
- Dangerous command guard (biometric)

---

## MEMORY BANK

The Memory Bank is the single source of truth for project context across sessions.

- **Always read `memory-bank.md` at the start of every session**
- **Always update `memory-bank.md` after every confirmed change**
- Never contradict Memory Bank content without explicit approval from Muhofy
- If Memory Bank is missing or incomplete, ask Muhofy before proceeding