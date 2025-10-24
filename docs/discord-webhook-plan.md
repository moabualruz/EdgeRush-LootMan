# Discord Webhook Integration Plan

## Goals
- Automate loot award announcements, RDF cooldown expirations, penalty flags, and weekly summaries using templates from `docs/discord-templates.md`.
- Integrate with the Kotlin data-sync service so notifications trigger after each sync cycle or loot decision import.

## Architecture
1. **Configuration**
   - Add properties under `notifications.discord` with webhook URLs per channel (`loot`, `progress`, `officer`, `general`).
   - Load secrets via `.env` (`DISCORD_WEBHOOK_LOOT`, etc.).
2. **Publisher Component**
   - Create `DiscordWebhookClient` using Spring WebClient with JSON payloads.
   - Support rate limiting via resilience4j retry/backoff and fallback logging.
3. **Event Model**
   - Define domain events (`LootAwarded`, `RdfExpired`, `PenaltyFlagged`, `WeeklySummary`).
   - Each event maps to a template string (reuse in docs) with token replacement.
4. **Trigger Points**
   - Loot awards: after FLPS ranking computed and an item is assigned.
   - RDF expirations: scheduler checks penalties nearing expiry (<= 0.95) and emits events.
   - Penalty flags: when RMS or MAS thresholds breached during sync.
   - Weekly summary: scheduled Sunday summary using aggregated stats.
5. **Testing**
   - Use MockWebServer to validate payload structure.
   - Provide dry-run mode that logs payloads without sending.
6. **Rollout Checklist**
   - Create Discord webhooks per channel and store in secret manager.
   - Enable dry-run in staging, verify message formatting, then go live.
   - Document an emergency disable flag (`notifications.discord.enabled=false`).

## Deliverables
- `DiscordConfig.kt` and `DiscordWebhookClient.kt` in `data-sync-service`.
- Unit tests covering payload rendering.
- Update runbook (`docs/automation-notes.md`) with on-call instructions for disabling notifications.
