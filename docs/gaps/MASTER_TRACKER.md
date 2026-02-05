# EdgeRush LootMan - Gap Remediation Master Plan

**Created:** 2026-02-05  
**Goal:** Achieve 100% requirement coverage across all platforms

---

## 🔄 REUSABLE SESSION PROMPT

Copy and paste this prompt at the start of each new chat session:

```markdown
# SYSTEM PROTOCOL: GAP REMEDIATION ENGINE

**Project:** EdgeRush LootMan (f:\work\dod\looter)
**Objective:** Implement remaining gaps to achieve full product completion.

## CONTEXT FILES TO READ FIRST
1. Read `docs/gaps/MASTER_TRACKER.md` for current gap status
2. Read the gap-specific plan file for your target gap

## COMMAND PROTOCOL
- `/status` - Show current gap completion status
- `/plan [GAP_ID]` - Generate implementation plan for a specific gap
- `/implement [GAP_ID]` - Begin TDD implementation of a gap
- `/verify [GAP_ID]` - Run verification tests for completed gap
- `/next` - Suggest next highest priority gap to tackle

## CURRENT PRIORITY ORDER
1. G-02: Discord Bot (R20-R23)
2. G-03: Droptimizer UI (R12-R14)
3. G-05: Desktop Client (R16)
4. G-06: WoW Addon (R17-R19)

## TDD WORKFLOW REQUIREMENT
All implementations must follow: RED → GREEN → REFACTOR
Reference: `.agent/workflows/tdd-workflow.md`

**[START COMMAND]:** /status
```

---

## GAP SUMMARY TABLE

| Gap ID | Scope | Files Affected | Est. Hours | Status |
|--------|-------|----------------|------------|--------|
| G-02 | Discord Bot | `discord-bot/src/**` | 16-24h | ✅ **VERIFIED** |
| G-03 | Droptimizer UI | `web-dashboard/src/pages/`, `api/` | 8-12h | ⬜ TODO |
| G-05 | Desktop Client | `desktop-client/src-tauri/**` | 12-16h | ⬜ TODO |
| G-06 | WoW Addon | `wow-addon/Modules/**` | 8-12h | ⬜ TODO |


---

## DETAILED GAP FILES

Each gap has a dedicated plan file:
- `docs/gaps/G-02_DISCORD_BOT.md`
- `docs/gaps/G-03_DROPTIMIZER_UI.md`
- `docs/gaps/G-05_DESKTOP_CLIENT.md`
- `docs/gaps/G-06_WOW_ADDON.md`

---

## COMPLETION CRITERIA

A gap is marked ✅ DONE when:
1. All acceptance criteria from requirements met
2. Unit tests passing (≥80% coverage)
3. Integration with existing systems verified
4. Manual smoke test completed

---

## PROGRESS LOG

| Date | Gap | Action | Notes |
|------|-----|--------|-------|
| 2026-02-05 | - | Initial audit complete | Identified 4 major gaps |
| 2026-02-05 | G-02 | **VERIFIED COMPLETE** | Full impl found: 15 Kotlin files, 6 tests |

