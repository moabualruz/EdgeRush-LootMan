# G-05: Desktop Client Implementation Plan

**Requirement:** R16 (Desktop Client)  
**Priority:** P2  
**Estimated Effort:** 12-16 hours  
**Status:** ⬜ TODO

---

## Current State

- **Framework:** Tauri (Rust backend + TypeScript frontend)
- **Folder:** `desktop-client/`
- **Status:** Basic scaffold exists, core features missing

---

## Requirements Checklist

### R16: Desktop Client
- [ ] Windows support (primary)
- [ ] macOS support (secondary)
- [ ] Linux support (tertiary)
- [ ] Sync addon SavedVariables on logout/reload
- [ ] Real-time file watching for WoW directory
- [ ] Sync character gear and bags to web
- [ ] Sync loot council decisions
- [ ] Sync attendance confirmations
- [ ] System tray operation
- [ ] Auto-start with Windows (optional)
- [ ] Configure WoW installation path
- [ ] Support multiple WoW installations (retail, PTR)
- [ ] Authenticate with web platform

---

## Implementation Steps

### Phase 1: Tauri Core Setup (3h)
1. **Configuration**
   - File: `src-tauri/tauri.conf.json`
   - Enable system tray, auto-start capabilities
   - Configure file system permissions

2. **WoW Path Detection**
   - File: `src-tauri/src/wow.rs`
   - Auto-detect WoW installations from registry (Windows)
   - Support manual path configuration

### Phase 2: File Watching (4h)
3. **SavedVariables Watcher**
   - File: `src-tauri/src/watcher.rs`
   - Use `notify` crate for file system events
   - Watch: `WTF/Account/*/SavedVariables/EdgeRushLootMan.lua`
   - Detect logout/reload events

4. **Lua Parser**
   - File: `src-tauri/src/lua_parser.rs`
   - Parse SavedVariables to JSON
   - Extract gear, bags, loot decisions

### Phase 3: Backend Sync (4h)
5. **API Client**
   - File: `src-tauri/src/api.rs`
   - Authenticate via stored JWT
   - POST endpoints: `/api/v1/sync/character`, `/api/v1/sync/loot`

6. **Sync Queue**
   - File: `src-tauri/src/sync.rs`
   - Queue sync requests during offline
   - Retry with backoff on failure

### Phase 4: Frontend UI (3h)
7. **Settings Page**
   - File: `src/pages/Settings.vue`
   - WoW path selector
   - Account management
   - Sync frequency settings

8. **Dashboard**
   - File: `src/pages/Dashboard.vue`
   - Sync status indicator
   - Last sync time
   - Character list

### Phase 5: System Integration (2h)
9. **System Tray**
   - File: `src-tauri/src/tray.rs`
   - Minimize to tray
   - Quick actions menu

10. **Auto-start**
    - Windows: Registry key
    - macOS: LaunchAgent
    - Linux: systemd user service

---

## Files to Create/Modify

| File | Action | Description |
|------|--------|-------------|
| `src-tauri/src/main.rs` | Modify | Add entrypoints for new commands |
| `src-tauri/src/wow.rs` | Create | WoW installation detection |
| `src-tauri/src/watcher.rs` | Create | File system watcher |
| `src-tauri/src/lua_parser.rs` | Create | SavedVariables parser |
| `src-tauri/src/api.rs` | Create | Backend API client |
| `src-tauri/src/sync.rs` | Create | Sync queue management |
| `src-tauri/src/tray.rs` | Create | System tray integration |
| `src/pages/Settings.vue` | Create | Settings UI |
| `src/pages/Dashboard.vue` | Create | Main dashboard |
| `src-tauri/tauri.conf.json` | Modify | Enable capabilities |

---

## Dependencies (Rust)

```toml
# src-tauri/Cargo.toml
[dependencies]
notify = "6.0"
reqwest = { version = "0.11", features = ["json"] }
serde_json = "1.0"
mlua = "0.9"  # For Lua parsing
keyring = "2.0"  # Secure credential storage
```

---

## Testing Strategy

1. **Unit Tests:** Lua parsing, API client
2. **Integration:** File watch → sync flow
3. **Manual:** Install on Windows, verify with real WoW

---

## Verification Commands

```bash
cd desktop-client

# Build
cargo tauri build

# Development
cargo tauri dev

# Run tests
cargo test
```
