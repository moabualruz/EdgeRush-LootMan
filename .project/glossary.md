# Domain Glossary

## Terms

| Term | Definition |
|------|------------|
| FLPS | Final Loot Priority Score - the composite score determining loot priority |
| RMS | Raider Merit Score - component measuring player commitment and performance |
| IPI | Item Priority Index - component measuring upgrade value and role fit |
| RDF | Recency Decay Factor - component preventing loot hoarding |
| MAS | Mechanical Adherence Score - performance metric from Warcraft Logs |
| Loot Council | Guild officers who make final loot decisions |
| Tier Set | Raid armor sets with bonuses at 2/4 piece thresholds |
| BiS | Best in Slot - optimal gear piece for a character |
| Wishlist | Player's prioritized list of desired items |
| Droptimizer | Raidbots tool for simulating item upgrade value |
| Raid Difficulty | Normal, Heroic, or Mythic raid modes |
| Encounter | A boss fight within a raid instance |
| Parse | Performance percentile from Warcraft Logs |
| ilvl | Item Level - gear power indicator |
| DPS | Damage Per Second - damage dealer performance metric |
| HPS | Healing Per Second - healer performance metric |
| Tank | Role responsible for absorbing boss damage |
| Healer | Role responsible for keeping raid alive |
| DPS (role) | Damage dealer role |

## Acronyms

| Acronym | Expansion |
|---------|-----------|
| WoW | World of Warcraft |
| WCL | Warcraft Logs |
| API | Application Programming Interface |
| REST | Representational State Transfer |
| GraphQL | Graph Query Language |
| JWT | JSON Web Token |
| CRUD | Create, Read, Update, Delete |
| DDD | Domain-Driven Design |
| TDD | Test-Driven Development |
| CI/CD | Continuous Integration/Continuous Deployment |
| MVP | Minimum Viable Product |
| DTO | Data Transfer Object |
| JPA | Java Persistence API |
| JDBC | Java Database Connectivity |
| ORM | Object-Relational Mapping |

## Context

### FLPS Formula
```
FLPS = (RMS × IPI) × RDF

Where:
- RMS = weighted(attendance, performance, behavior)
- IPI = weighted(upgradeValue, roleNeeds, tierCompletion)
- RDF = decay(lastLootDate, lootTier)
```

### WoWAudit Data Model
- **Characters**: Guild roster with specs, gear, progression
- **Raids**: Scheduled events with signups and attendance
- **Encounters**: Boss kills within raids
- **LootAwards**: Items distributed to characters
- **Wishlists**: Player item preferences
- **Applications**: Guild membership requests

### Warcraft Logs Integration
- **Reports**: Combat logs uploaded to WCL
- **Fights**: Individual boss encounters within reports
- **Performance**: Parse percentiles, deaths, damage taken
- **MAS Calculation**: Combines death rate, avoidable damage, uptime
