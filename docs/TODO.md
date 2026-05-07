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
- [ ] Create `~/.korex/` directory structure on first launch

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

### Distro Selector
- [ ] ∨ button next to + in session panel
- [ ] Distro list: Termux (default), Arch, Ubuntu, Debian, Kali, Alpine, Custom
- [ ] proot-distro integration — auto install selected distro
- [ ] Session card shows distro icon

### UI — Left Bar
- [ ] Implement VS Code style icon rail (always visible, left edge)
- [ ] Hamburger (≡) button — top left, opens/closes panel
- [ ] Panel open → terminal shrinks (animated)
- [ ] Panel close → terminal expands back (animated)
- [ ] Sessions panel (default on open)
- [ ] Settings panel
- [ ] Active panel highlight on icon rail

### Gestures
- [ ] Implement `GestureDetector` (gesture/)
- [ ] Swipe LEFT → next session
- [ ] Swipe RIGHT → previous session
- [ ] Gesture threshold constants (not magic numbers)

### Extra Key Bar
- [ ] Horizontal scrollable key bar at bottom
- [ ] Keys: ESC, TAB, CTRL, ALT, ↑, ↓, ←, →
- [ ] Key sends correct input to terminal pty
- [ ] CTRL long press → popup (CTRL+C, CTRL+Z, CTRL+D, CTRL+L, CTRL+R)

### Theme
- [ ] Dark theme (default)
- [ ] Light theme (toggle in settings)
- [ ] Theme persisted via DataStore

### Settings — Phase 1
- [ ] Settings screen UI (categorized, single row per setting)
- [ ] Appearance: theme toggle (dark/light), font size
- [ ] Terminal: default shell (bash/zsh), extra key bar customization
- [ ] Backup: manual backup, restore
- [ ] Security: app lock (fingerprint/PIN)

---

## Phase 2 — Power

### Notifications & Feedback
- [ ] Command duration indicator — live timer next to running command
- [ ] Status notification on finish (success ✅ / fail ❌) for commands over threshold (default 5s)
- [ ] `nws set notify-threshold X` to configure threshold
- [ ] Package update toast on app launch ("14 packages can be updated")
- [ ] Snippet toast feedback on execute
- [ ] Haptic feedback — success vibration / error vibration (different patterns)

### Snippet System
- [ ] Snippet data model (id, title, command, createdAt)
- [ ] Room DB table for snippets
- [ ] Snippets panel in left bar (⚡)
- [ ] Add / edit / delete snippets
- [ ] Tap snippet → execute in active terminal
- [ ] Long press snippet → edit
- [ ] Auto snippet suggestion — same command typed 3 times → "Add to snippets?" banner

### Customization
- [ ] Cursor customization — block, line, underline, blink speed, color
- [ ] Background customization — solid color, gradient, blur, image, opacity
- [ ] Keyboard shortcut manager — define custom shortcuts (e.g. CTRL+K → clear)
- [ ] Two finger press + slide → font zoom in/out

### Theme Marketplace
- [ ] Theme JSON format definition
- [ ] Fetch theme list from GitHub Releases on app launch (cache for offline)
- [ ] Theme marketplace UI — Yüklü / Keşfet tabs
- [ ] Theme card with live terminal preview
- [ ] Download and apply theme
- [ ] Built-in themes: Korex Dark, Korex Light, Dracula, Nord, Tokyo Night, Solarized Dark, One Dark, Monokai
- [ ] Theme editor — color picker, live preview, export JSON
- [ ] Theme categories and sorting (popular, newest, featured)

### Terminal Features
- [ ] System overlay — CPU%, RAM%, battery, network speed (toggle in settings, corner position)
- [ ] Auto folder bookmark — tracks frequent dirs, suggests on `cd` (zoxide integration)
- [ ] Command correction — typo detection and fix suggestion (thefuck integration)
- [ ] Command output search — 🔍 in extra key bar, highlight matches, ↑↓ navigation
- [ ] File picker button — Android file picker → writes path to terminal input
- [ ] Session notes — side panel notepad per session, persisted in Room DB (`~/.korex/notes/`)
- [ ] Long press terminal output → context menu (copy, translate, search, AI explain)

### nws Command System
- [ ] `nws new session <distro> <name>` — create session
- [ ] `nws close session <name>` — close session
- [ ] `nws list sessions` — list all sessions
- [ ] `nws switch session <name>` — switch session
- [ ] `nws set tscale <value>` — text scale
- [ ] `nws set fontsize <value>` — font size
- [ ] `nws set theme <name>` — apply theme
- [ ] `nws set volume <value>` — system volume
- [ ] `nws set brightness <value>` — screen brightness
- [ ] `nws set notify-threshold <seconds>` — notification threshold
- [ ] `nws open <app>` — open app (YouTube, Settings, Files...)
- [ ] `nws get battery` — battery info
- [ ] `nws get ip` — IP address
- [ ] `nws get storage` — storage info
- [ ] `nws bookmarks` — list bookmarks
- [ ] `nws bookmark remove <path>` — remove bookmark
- [ ] `nws paranoia on/off` — paranoia mode

### Process Manager
- [ ] Visual process list (htop-style UI)
- [ ] CPU and RAM usage per process
- [ ] Tap process → kill option
- [ ] Accessible from left bar panel

### Git Integration — Basic
- [ ] GitHub OAuth login
- [ ] Git status panel — changed files, untracked
- [ ] Stage all / stage single file
- [ ] Commit with message input
- [ ] Push / Pull buttons
- [ ] Branch list — create, switch, delete

### Backup System
- [ ] `~/.korex/` directory structure:
  - `themes/` — theme JSON files
  - `snippets/` — snippets.json
  - `dotfiles/` — .zshrc, .bashrc, .gitconfig, .vimrc etc.
  - `ssh/` — config (encrypted)
  - `notes/` — notes.json
  - `bookmarks/` — bookmarks.json
  - `git/` — profile.json
  - `korex.config` — app settings
- [ ] Dotfile tracking — select which dotfiles to back up
- [ ] Export as `.korex` file (ZIP with encrypted vault)
- [ ] Import `.korex` file — restore with fingerprint/PIN confirmation
- [ ] Overwrite confirmation for existing dotfiles on restore
- [ ] Google Drive auto backup (daily/weekly/manual)
- [ ] Max backup count setting (5/10/unlimited)

### System & Multitasking
- [ ] Home screen widget — session status, last command, quick command input
- [ ] Each session as separate Android window (multi-instance, Android 12+)
- [ ] Floating terminal — overlay on other apps (overlay permission)
- [ ] Swipe UP → Command History panel
- [ ] Swipe DOWN → Quick Actions panel
- [ ] Pinch OUT → Split screen

---

## Phase 3 — AI & Security

### Security
- [ ] Vault — encrypted local storage (`~/.korex/vault/`, AES-256, biometric unlock)
- [ ] Vault terminal injection — inject secrets as env variables, never appears in history
- [ ] SSH manager — key generation, import, saved host profiles, one-tap connect, keys stored in Vault
- [ ] Paranoia mode — no history, no logs, screenshot blocked, Vault auto-locks
- [ ] Dangerous command guard — detect `rm -rf`, `dd`, `mkfs` etc. → biometric confirm + 3s countdown

### Smart Features
- [ ] Smart project detection — detect project type (React, Python...) → suggest relevant commands
- [ ] Package manager UI — visual apt/pkg manager (search, install, remove, update list)
- [ ] XED editor integration — built-in file editor with syntax highlighting
- [ ] Error message translation — long press error → translate to Turkish or any language
- [ ] Error → Stack Overflow search — failed command → fetch top answer inline
- [ ] AI commit message — reads `git diff` → generates conventional commit message → confirm → commit

### Git Integration — Advanced
- [ ] Actions log viewer — workflow runs, status, logs
- [ ] Re-run failed actions

### AI & Voice
- [ ] Voice terminal — Whisper offline STT + Android TTS
- [ ] Hold-to-talk mic button in extra key bar (🎤)
- [ ] Volume down long press → trigger voice terminal
- [ ] Power button double press → trigger voice terminal
- [ ] Natural language → bash command
- [ ] DSP / SoundTrigger API check on launch — warn if not supported
- [ ] "Hey Korex" wake word — off by default, DSP-based when available
- [ ] AI shell assistant — error explainer, command suggestions, error memory

### Advanced
- [ ] Terminal replay — record and playback session (asciinema style)
- [ ] Cross-device clipboard — copy on phone → paste on PC via Korex Sync
- [ ] Korex Web — access terminal from browser (phone as server)
- [ ] PC bridge — connect to PC terminal via Korex protocol
- [ ] Korex Sync — real-time shared terminal between two phones (WebSocket, view-only or collaborative)
- [ ] Parallel execution — run same command across multiple sessions simultaneously (uncertain)

---

## Backlog (No Phase Yet)
- [ ] Wear OS companion (remote session control from watch)
- [ ] Command dependency graph (visual flow of script execution)
- [ ] Smart SSH tunnel (detect remote vs local env version mismatches)
- [ ] Korex Share — export full setup as QR code
- [ ] Auto documentation generator (records install steps → produces Markdown)
- [ ] Gamification (badges, command streaks)
- [ ] Command timeline ("what did I do 3 days ago?")