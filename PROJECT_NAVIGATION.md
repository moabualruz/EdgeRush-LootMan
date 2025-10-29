# 🔗 EdgeRush LootMan - Project Navigation Index

## 📋 Documentation Hub

### Core Documentation Files
| Document | Purpose | Use When |
|----------|---------|----------|
| [AI_AGENT_GUIDE.md](./AI_AGENT_GUIDE.md) | Complete project overview & business context | Starting new work, understanding project goals |
| [CODE_ARCHITECTURE.md](./CODE_ARCHITECTURE.md) | Technical architecture & component relationships | Understanding code structure, planning changes |
| [AI_DEVELOPMENT_STANDARDS.md](./AI_DEVELOPMENT_STANDARDS.md) | Coding conventions & best practices | Writing new code, reviewing existing code |
| [API_REFERENCE.md](./API_REFERENCE.md) | Interface documentation & usage examples | Working with APIs, integrating services |
| [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) | Quick start guide & common tasks | Daily development, troubleshooting |

### Project Documentation
| Location | Content |
|----------|---------|
| [docs/](./docs/) | Detailed technical documentation |
| [docs/score-model.md](./docs/score-model.md) | FLPS algorithm specification |
| [docs/wowaudit-data-map.md](./docs/wowaudit-data-map.md) | External API data mapping |
| [docs/system-overview.md](./docs/system-overview.md) | High-level system design |

---

## 🎯 AI Agent Workflow

### Phase 1: Understanding
1. **Start Here**: [AI_AGENT_GUIDE.md](./AI_AGENT_GUIDE.md) - Project context & business logic
2. **Then Read**: [CODE_ARCHITECTURE.md](./CODE_ARCHITECTURE.md) - Technical structure

### Phase 2: Development
1. **Follow Standards**: [AI_DEVELOPMENT_STANDARDS.md](./AI_DEVELOPMENT_STANDARDS.md) - Coding conventions
2. **Reference APIs**: [API_REFERENCE.md](./API_REFERENCE.md) - Interface documentation
3. **Quick Help**: [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) - Common tasks & troubleshooting

### Phase 3: Implementation
1. **Code Structure**: Follow layered architecture patterns
2. **Testing**: Implement unit + integration tests
3. **Documentation**: Update relevant docs for changes

---

## 🗂️ Code Organization

### Primary Source Code
```
data-sync-service/src/main/kotlin/com/edgerush/datasync/
├── DataSyncApplication.kt           # Main application entry point
├── api/                            # External API integrations
│   └── wowaudit/                   # WoWAudit API client & models
├── client/                         # HTTP clients
│   └── WoWAuditClient.kt          # Primary external API client
├── config/                         # Configuration classes
│   ├── SyncProperties.kt          # Application properties
│   └── WebClientConfig.kt         # HTTP client configuration
├── domain/                         # Core business entities
│   ├── Character.kt               # Character entity
│   ├── LootHistory.kt             # Loot distribution records
│   └── AttendanceRecord.kt        # Raid attendance data
├── repository/                     # Data access layer
│   ├── CharacterRepository.kt     # Character data access
│   └── LootHistoryRepository.kt   # Loot history data access
├── service/                        # Business logic
│   ├── ScoreCalculator.kt         # FLPS algorithm implementation
│   ├── SyncService.kt             # Data synchronization
│   └── AttendanceService.kt       # Attendance calculations
└── schema/                         # Data validation
    └── WoWAuditSchema.kt          # API response validation
```

### Test Organization
```
data-sync-service/src/test/kotlin/com/edgerush/datasync/
├── AcceptanceSmokeTest.kt          # End-to-end smoke tests
├── api/wowaudit/                   # API client tests
├── client/                         # HTTP client tests
├── config/                         # Configuration tests
├── service/                        # Business logic tests
└── schema/                         # Data validation tests
```

---

## 🚀 Development Workflow

### Starting Development

1. **Environment Setup**
   ```bash
   docker-compose up -d    # Start all services
   ```

2. **Verify Health**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **Run Tests**
   ```bash
   cd data-sync-service && ./gradlew test
   ```

### Making Changes

1. **Choose Appropriate Layer**
   - **API Changes**: `api/` or `client/` packages
   - **Business Logic**: `service/` package
   - **Data Model**: `domain/` and `repository/` packages
   - **Configuration**: `config/` package

2. **Follow Standards**
   - File headers (see [AI_DEVELOPMENT_STANDARDS.md](./AI_DEVELOPMENT_STANDARDS.md))
   - Error handling patterns
   - Testing requirements

3. **Update Documentation**
   - Code comments for complex logic
   - API documentation for interface changes
   - Architecture docs for structural changes

---

## 🔍 Finding Specific Information

### Business Logic Questions
- **FLPS Algorithm**: `service/ScoreCalculator.kt` + [docs/score-model.md](./docs/score-model.md)
- **Data Synchronization**: `service/SyncService.kt` + [AI_AGENT_GUIDE.md](./AI_AGENT_GUIDE.md)
- **External APIs**: `client/WoWAuditClient.kt` + [API_REFERENCE.md](./API_REFERENCE.md)

### Technical Implementation
- **Database Schema**: `src/main/resources/db/migration/`
- **Configuration**: `src/main/resources/application.yaml`
- **HTTP Endpoints**: `api/` package (limited endpoints currently)
- **Error Handling**: Search for `Result<T>` patterns

### Data Models & Entities
- **Character Data**: `domain/Character.kt`
- **Loot Records**: `domain/LootHistory.kt`
- **Attendance**: `domain/AttendanceRecord.kt`
- **API Responses**: `api/wowaudit/` package

---

## 🛠️ Troubleshooting Guide

### Common Issues
| Problem | Investigation Steps | Solution Location |
|---------|-------------------|------------------|
| API Integration Failure | Check `client/WoWAuditClient.kt` logs | [API_REFERENCE.md](./API_REFERENCE.md) |
| Score Calculation Error | Review `service/ScoreCalculator.kt` | [docs/score-model.md](./docs/score-model.md) |
| Database Connection | Check docker-compose services | [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) |
| Build Failures | Verify JDK 24 configuration | [AI_DEVELOPMENT_STANDARDS.md](./AI_DEVELOPMENT_STANDARDS.md) |

### Log Locations
- **Application Logs**: `docker-compose logs data-sync`
- **Database Logs**: `docker-compose logs postgres`
- **Test Results**: `data-sync-service/build/reports/tests/`

---

## 📈 Success Metrics

### Code Quality Indicators
- All tests passing: `./gradlew test`
- Health checks green: `/actuator/health`
- No lint violations: Follow standards document
- Documentation updated: Relevant to changes

### Business Logic Validation
- FLPS calculations produce reasonable scores (0.0-1.0 range)
- Data sync operations complete successfully
- External API integrations remain stable
- Performance metrics within acceptable bounds

---

*This navigation index provides structured access to all project resources for efficient AI agent development.*