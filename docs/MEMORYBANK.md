# Korex — Memory Bank
> Single source of truth. Always read at session start. Always update after confirmed changes.

---

## What Is Korex?

Korex is a next-generation Android terminal emulator built on top of the Termux terminal engine (used as a library, not forked). It reimagines the terminal experience on Android with a modern VS Code-inspired UI, gesture controls, session management, and a phased roadmap toward AI and security features.

**Tagline:** "Terminal, reimagined."

---

## Core Decisions

| Decision | Choice | Reason |
|---|---|---|
| App name | Korex | Unique, powerful, memorable |
| Language | Kotlin | Native Android, best performance |
| UI Framework | Jetpack Compose | Modern, declarative, smooth |
| Terminal engine | Termux (library) | Battle-tested, not forked to stay independent |
| Architecture | MVVM + Clean Architecture | Scalable, testable, maintainable |
| DI | Hilt | Standard Android DI |
| DB | Room | Session persistence |
| Font | JetBrains Mono | Best terminal readability |
| License | GPL-3.0 | Required by Termux engine dependency |
| Distribution | F-Droid + Play Store | Open source first |
| Left bar style | VS Code icon rail | Familiar, space-efficient, modern |
| Navigation | Left bar panels (not drawer) | Always visible, no extra tap |
| Theme | Dark default | Terminal standard |

---

## UI Layout

```
┌──┬─────────────────────┐
│🖥️│                     │
│  │                     │
│📋│      Terminal       │
│  │    (full width      │
│⚡│     when panel      │
│  │      closed)        │
│🎨│                     │
│⚙️│                     │
├──┴─────────────────────┤
│ ESC│TAB│CTRL│ALT│↑│↓│←│→│
└────────────────────────┘
```

- **Hamburger (≡)** top left → opens/closes panel
- Panel open → terminal shrinks (does not overlay)
- Panel closed → terminal full width
- Extra key bar always visible at bottom

---

## Left Bar Panels

| Icon | Panel | Phase |
|---|---|---|
| 🖥️ | Sessions (default) | 1 |
| 📋 | Command History | 2 |
| ⚡ | Snippets | 2 |
| 🎨 | Theme | 2 |
| ⚙️ | Settings | 1 |

---

## Swipe Gestures

| Gesture | Action | Phase |
|---|---|---|
| Swipe UP | Command History | 2 |
| Swipe DOWN | Quick Actions panel | 2 |
| Swipe LEFT | Next session | 1 |
| Swipe RIGHT | Previous session | 1 |
| Pinch OUT | Split screen | 2 |
| Long press session | Rename / Pin / Close | 1 |

---

## Session System

- Named sessions (user defined or auto)
- Session card shows: name, CWD, last activity, status
- Status: ● active / ○ background / ⚠️ unexpected exit / ❌ crashed
- Pinned sessions stay at top
- No session limit
- Full session restore on app kill/crash (Room DB)
- Session state saved: PID, CWD, history, env, name

---

## Theme

```
Background  #0D1117
Accent      #58A6FF
Text        #E6EDF3
```

Dark default. Theme marketplace in Phase 2.

---

## Phase Plan

### Phase 1 — Core
- [ ] Termux engine integration (library)
- [ ] VS Code left bar + session panel
- [ ] Swipe LEFT/RIGHT session navigation
- [ ] Session restore (Room DB)
- [ ] Extra key bar
- [ ] Basic dark/light theme
- [ ] Settings panel

### Phase 2 — Power
- [ ] Split screen
- [ ] Snippet system
- [ ] Theme marketplace
- [ ] Command history panel (swipe UP)
- [ ] Quick actions panel (swipe DOWN)
- [ ] Extra key bar customization

### Phase 3 — AI & Security
- [ ] Voice terminal (Whisper STT offline + Android TTS)
- [ ] AI shell assistant
- [ ] Vault + SSH manager
- [ ] Paranoia mode
- [ ] Dangerous command guard (biometric confirm)
- [ ] XED editor integration (built-in file editor)

---

## Project Structure

```
com.korexx/
├── data/         — Room DAOs, repository implementations, models
├── domain/       — use cases, repository interfaces, business logic
├── ui/           — Compose screens, components, ViewModels
├── session/      — session lifecycle, restore, state management
├── terminal/     — Termux engine bridge, pty I/O
├── gesture/      — swipe detector, touch event dispatcher
├── di/           — Hilt modules
└── util/         — constants, extensions, helpers
```

---

## Open Source & Legal

- License: GPL-3.0 (Termux engine requires this)
- Termux engine used as library (not forked) — stays independent from Termux updates
- XED editor (open source) — planned for Phase 3 built-in file editing
- All third-party licenses declared in `NOTICE`
- GitHub: public repository
- F-Droid: primary distribution
- Play Store: secondary distribution

---

## Known Decisions Still Pending

| Item | Status |
|---|---|
| Kotlin version | TBD — confirm from build.gradle |
| Compose version | TBD — confirm from build.gradle |
| Hilt version | TBD — confirm from build.gradle |
| Room version | TBD — confirm from build.gradle |
| DataStore version | TBD — confirm from build.gradle |
| Compose Navigation version | TBD — confirm from build.gradle |
| GitHub repo URL | TBD |
| Play Store / F-Droid listing | TBD |

---

## Active Context

**Current focus:** Planning & architecture phase. No code written yet.

**Last confirmed decisions:**
- App name: Korex
- UI: VS Code left bar style
- Terminal engine: Termux as library (not fork)
- License: GPL-3.0
- Phase 1 feature set locked

**Next step:** Create GitHub repo → Android project setup → Termux engine integration → First UI screen (terminal + left bar)

---

## Progress

| Item | Status |
|---|---|
| Name decision | ✅ Korex |
| UI/UX design | ✅ Planned |
| Feature priority | ✅ Planned |
| Architecture | ✅ Planned |
| GitHub repo | ❌ Not created |
| Android project | ❌ Not created |
| Termux integration | ❌ Not started |
| First UI screen | ❌ Not started |

---

## ✅ 
Verified Final Version Table

| Dependency | Versiyon | Kaynak |
|---|---|---|
| **Kotlin** | `2.1.10` | Hilt 2.57.1 release notes — Kotlin 2.1.10 ile çıktı |
| **AGP** | `8.9.0` | Navigation Safe Args `8.4.2` gerektirir, 8.9.x son stabil |
| **Compose BOM** | `2026.04.01` | Android Developers — en son stable, Compose 1.11.0 içerir |
| **Hilt** | `2.57.1` | Android Developers resmi dokümantasyonu |
| **Room** | `2.6.1` | Room 3.0 alpha çıktı ama **breaking changes** var, `androidx.room3` ayrı paket. Korex için `2.6.1` stable, güvenli |
| **DataStore** | `1.1.1` | Son stable |
| **Navigation Compose** | `2.9.7` | Android Developers resmi release sayfası |
| **terminal-view** | `0.118.0` | Termux wiki — resmi belgede en son kararlı versiyon, `0.118.3` JitPack build garantisi yok |
| **KSP** | `2.1.10-1.0.31` | Hilt 2.57.1 release: "Kotlin upgraded to 2.1.10 to support KSP 2.1.10-1.0.31" |
