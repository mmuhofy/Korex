# Korex — TODO
> Priority order. Top = first. Do not skip phases.

---

## Phase 1 — Core

### Setup
- [ ] Create GitHub repository (`korex` — public, GPL-3.0)
- [ ] Create Android project (Kotlin + Jetpack Compose)
- [ ] Configure `build.gradle` (minSdk 26, targetSdk 34)
- [ ] Add dependencies: Hilt, Room, DataStore, Compose Navigation
- [ ] Set up project package structure (`data`, `domain`, `ui`, `session`, `terminal`, `gesture`, `di`, `util`)
- [ ] Add JetBrains Mono font to project assets
- [ ] Configure base theme (#0D1117, #58A6FF, #E6EDF3)

### Terminal Engine
- [ ] Integrate Termux terminal emulator as library dependency
- [ ] Implement `terminal/` bridge — pty process creation, I/O handling
- [ ] Render terminal output in Compose (TerminalView)
- [ ] Handle keyboard input from Compose to pty
- [ ] Test basic terminal functionality (bash, zsh)

### Session System
- [ ] Design Room DB schema for sessions (id, name, CWD, history, env, status, createdAt, lastActive)
- [ ] Implement `SessionRepository` interface (domain) + implementation (data)
- [ ] Implement `SessionManager` (session/) — create, switch, close, restore
- [ ] Implement session restore on app start
- [ ] Session status tracking (active, background, crashed)
- [ ] New session dialog (name input → create)
- [ ] Session card UI (name, CWD, last active, status dot)
- [ ] Long press session card → context menu (rename, pin, reorder, close)
- [ ] Pinned sessions always at top of list

### UI — Left Bar
- [ ] Implement VS Code style icon rail (always visible, left edge)
- [ ] Hamburger (≡) button — top left, opens/closes panel
- [ ] Panel open → terminal shrinks (animated)
- [ ] Panel close → terminal expands back (animated)
- [ ] Sessions panel (default on open)
- [ ] Settings panel (basic — placeholder for now)
- [ ] Active panel highlight on icon rail

### UI — Terminal Screen
- [ ] Full screen terminal when panel closed
- [ ] Terminal shrinks when panel open
- [ ] No top bar (full screen terminal)

### Gestures
- [ ] Implement `GestureDetector` (gesture/)
- [ ] Swipe LEFT → next session
- [ ] Swipe RIGHT → previous session
- [ ] Gesture threshold constants (not magic numbers)

### Extra Key Bar
- [ ] Horizontal scrollable key bar at bottom
- [ ] Keys: ESC, TAB, CTRL, ALT, ↑, ↓, ←, →
- [ ] Key sends correct input to terminal pty

### Theme
- [ ] Dark theme (default)
- [ ] Light theme (toggle in settings)
- [ ] Theme persisted via DataStore

---

## Phase 2 — Power

### Split Screen
- [ ] Pinch OUT gesture → split screen (horizontal or vertical)
- [ ] Each pane = independent terminal session
- [ ] Drag divider to resize panes
- [ ] Pinch IN → return to single terminal

### Snippet System
- [ ] Snippet data model (id, title, command, createdAt)
- [ ] Room DB table for snippets
- [ ] Snippets panel in left bar (⚡)
- [ ] Add / edit / delete snippets
- [ ] Tap snippet → execute in active terminal
- [ ] Long press snippet → edit

### Command History Panel
- [ ] Swipe UP → command history panel (slides up)
- [ ] Search / filter history
- [ ] Tap → execute command
- [ ] Long press → edit then execute
- [ ] History persisted per session in Room DB

### Quick Actions Panel
- [ ] Swipe DOWN → quick actions panel (slides down)
- [ ] Actions: New Session, Snippets, Settings shortcut
- [ ] Customizable quick actions (later)

### Theme Marketplace
- [ ] Theme model (id, name, bg, accent, text, font)
- [ ] Built-in themes (5+)
- [ ] Theme preview
- [ ] Theme persistence via DataStore

### Extra Key Bar Customization
- [ ] Settings screen to reorder / add / remove keys
- [ ] Custom key mapping support

---

## Phase 3 — AI & Security

### Voice Terminal
- [ ] Integrate Whisper (offline STT)
- [ ] Hold-to-talk mic button (extra key bar or floating)
- [ ] Natural language → bash command (AI translation)
- [ ] Android TTS → read output aloud (optional toggle)
- [ ] Dangerous command confirmation before execution
- [ ] "Hey Korex" wake word (optional, off by default)

### AI Shell Assistant
- [ ] AI error explainer (paste error → AI explains + suggests fix)
- [ ] Natural language command suggestion
- [ ] Command autocomplete with AI (Tab)
- [ ] Error memory (same error seen before → remind user of past fix)

### Vault
- [ ] Encrypted local storage (secrets, tokens, notes)
- [ ] Biometric unlock
- [ ] Never logged, never backed up to cloud

### SSH Manager
- [ ] SSH key generation + import
- [ ] Saved host profiles (alias, host, port, user, key)
- [ ] One-tap SSH connect → opens in new session

### Paranoia Mode
- [ ] `nparanoia on` command
- [ ] History disabled
- [ ] Logs cleared
- [ ] Screenshot blocked
- [ ] Network traffic encrypted

### Dangerous Command Guard
- [ ] Detect dangerous patterns (`rm -rf`, `dd`, `mkfs`, etc.)
- [ ] Confirmation dialog + biometric required
- [ ] 3 second countdown with cancel

### XED Editor Integration
- [ ] Integrate XED open source editor
- [ ] Built-in file editor (tap file → opens in editor)
- [ ] Syntax highlighting
- [ ] Save / discard changes

---

## Backlog (No Phase Yet)

- [ ] Wear OS companion (remote session control from watch)
- [ ] Android widget (run command from home screen)
- [ ] GitHub Live (tab auto-renames to branch, PR notifications)
- [ ] Session Snapshot / Restore (full env snapshot)
- [ ] Command Replay (`nreplay`)
- [ ] Gamification (badges, command streaks)
- [ ] Live session sharing (read-only browser link)
- [ ] Plugin / extension system
- [ ] Dotfile sync (GitHub backup)
- [ ] Port monitor UI
- [ ] Built-in HTTP client (Postman lite)