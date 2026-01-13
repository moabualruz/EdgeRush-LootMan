# Non-Goals and Out of Scope

## Explicit Non-Goals

1. **Replace Loot Council entirely** - FLPS provides data to inform decisions, not make them automatically
2. **Support all WoW guilds immediately** - Focus on EdgeRush guild first, generalize later
3. **Real-time combat analysis** - Post-raid analysis is sufficient
4. **Mobile app** - Web dashboard covers mobile use cases
5. **Multi-game support** - WoW-specific design is acceptable
6. **Public leaderboards** - Internal guild use only
7. **Automated loot distribution** - Always require human confirmation
8. **Cross-guild comparisons** - Each guild is independent

## Out of Scope

### Technical
- Direct Blizzard API integration (WoWAudit aggregates this)
- Real-time WebSocket notifications (polling/scheduled refresh sufficient)
- Machine learning for score prediction
- Custom addon development beyond basic integration
- Multi-database support (PostgreSQL only)
- Kubernetes orchestration (Docker Compose sufficient)

### Functional
- Guild recruitment features (external tools exist)
- Raid scheduling (WoWAudit handles this)
- Voice chat integration
- Streaming/recording features
- Gold/economy tracking
- Achievement tracking
- PvP scoring

### Business
- Public SaaS offering (internal tool)
- Monetization/subscriptions
- White-labeling for other guilds
- Support contracts

## Deferred

| Item | Reason for Deferral |
|------|---------------------|
| GraphQL API | REST sufficient for MVP; Phase 2 |
| Raidbots integration | API key availability uncertain |
| Web dashboard | Needs API completion first |
| Discord bot | Needs API completion first |
| RC Loot Council addon | Needs Web/Discord first |
| Analytics dashboard | Nice-to-have after core features |
| Multi-guild support | Focus on single guild first |
| Internationalization | English-only audience |
| Dark mode | Cosmetic, low priority |
| Mobile-optimized views | Desktop-first acceptable |
