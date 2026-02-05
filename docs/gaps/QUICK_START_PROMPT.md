# EdgeRush LootMan - Gap Remediation Quick-Start Prompt

**COPY THIS ENTIRE FILE AND PASTE INTO A NEW CHAT SESSION**

---

## SESSION INITIALIZATION PROMPT

```
# SYSTEM PROTOCOL: GAP REMEDIATION ENGINE v1.0

**Project:** EdgeRush LootMan
**Location:** f:\work\dod\looter
**Goal:** Complete all remaining gaps to achieve 100% product coverage

---

## MANDATORY FIRST STEPS

1. Read: `docs/gaps/MASTER_TRACKER.md` - Current status
2. Read the gap-specific file for your target (e.g., `docs/gaps/G-02_DISCORD_BOT.md`)

---

## COMMAND REFERENCE

| Command | Action |
|---------|--------|
| `/status` | Display current gap completion status |
| `/plan G-XX` | Show detailed implementation plan |
| `/implement G-XX` | Begin TDD implementation of gap |
| `/verify G-XX` | Run tests and verify completion |
| `/next` | Suggest next highest priority gap |

---

## GAP PRIORITY ORDER

| Priority | Gap ID | Scope | Est. Hours |
|----------|--------|-------|------------|
| P0 | G-02 | Discord Bot (R20-R23) | 16-24h |
| P1 | G-03 | Droptimizer UI (R12-R14) | 8-12h |
| P2 | G-05 | Desktop Client (R16) | 12-16h |
| P2 | G-06 | WoW Addon (R17-R19) | 8-12h |

---

## PLATFORM TARGETS

| Platform | Directory | Technology |
|----------|-----------|------------|
| Backend | `data-sync-service/` | Kotlin/Spring Boot |
| Web Dashboard | `web-dashboard/` | Vue 3/TypeScript |
| Discord Bot | `discord-bot/` | Kotlin/JDA |
| Desktop Client | `desktop-client/` | Rust/Tauri |
| WoW Addon | `wow-addon/` | Lua |

---

## TDD WORKFLOW (MANDATORY)

All implementations must follow `.agent/workflows/tdd-workflow.md`:
1. Write failing test (RED)
2. Implement minimum code (GREEN)
3. Refactor

---

## COMPLETION CRITERIA

A gap is DONE when:
✅ All acceptance criteria met
✅ Unit tests passing (≥80% coverage)
✅ Integration verified
✅ Manual smoke test passed

---

## START COMMAND

/status
```

---

## USAGE INSTRUCTIONS

1. **Start a new chat session** with any AI assistant
2. **Copy the entire content between the \`\`\` blocks above**
3. **Paste it as your first message**
4. **Use the commands** to navigate and implement gaps

---

## TRACKING YOUR PROGRESS

After completing a gap:
1. Update `docs/gaps/MASTER_TRACKER.md` progress table
2. Update the gap-specific file status to ✅ DONE
3. Add entry to PROGRESS LOG with date and notes
