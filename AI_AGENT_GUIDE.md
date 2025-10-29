# AI Agent Development Guide - EdgeRush LootMan

## 🤖 For AI Agents Working on This Project

This document provides essential context and guidelines for AI agents contributing to the EdgeRush LootMan (ELM) project.

## Project Purpose & Context

EdgeRush LootMan is a **progression-first guild operations platform** for World of Warcraft raid teams. The system automates loot distribution decisions using the **Final Loot Priority Score (FLPS)** algorithm, which combines:

- **Raider Merit Score (RMS)**: Attendance, mechanical skill, preparation
- **Item Priority Index (IPI)**: Simulated upgrades, tier completion, role multipliers  
- **Recency Decay Factor (RDF)**: Prevents loot hoarding by top performers

## Key Technical Stack

- **Language**: Kotlin (JDK 24)
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL 18 (with Flyway migrations)
- **Build Tool**: Gradle 8.10.1
- **Container**: Docker with custom JDK 24 setup
- **Web Server**: Nginx reverse proxy

## 🏗️ Project Structure

```
├── data-sync-service/          # Main Kotlin Spring Boot application
│   ├── src/main/kotlin/        # Application source code
│   ├── src/main/resources/     # Configuration and migrations
│   └── build.gradle.kts        # Build configuration
├── docs/                       # Comprehensive documentation
├── assets/                     # Data files and resources
├── deploy/                     # Deployment configurations
├── wowaudit/                   # Reference data (DO NOT use in production)
└── docker-compose.yml          # Container orchestration
```

## 🎯 Core Modules & Responsibilities

### 1. Data Sync Service (`data-sync-service/`)
**Purpose**: Fetch, normalize, and store data from WoWAudit API

**Key Components**:
- `WoWAuditClient`: API communication
- `ScoreCalculator`: FLPS algorithm implementation
- `SyncScheduler`: Automated data collection
- Database entities and repositories

### 2. Score Calculation Engine
**Location**: `src/main/kotlin/com/edgerush/datasync/service/`
**Purpose**: Implements FLPS formula: `(RMS × IPI) × RDF`

### 3. Database Layer
**Migrations**: `src/main/resources/db/migration/postgres/`
**Purpose**: Schema management and data persistence

## 📋 Development Workflow for AI Agents

### Understanding the Codebase
1. **Start with**: `README.md` for business context
2. **Architecture**: `docs/system-overview.md`
3. **Data Flow**: `docs/wowaudit-data-map.md`
4. **Algorithms**: `docs/score-model.md`

### Making Changes
1. **Always check**: `PROJECT_PROGRESS.md` for current status
2. **Test changes**: Use `docker-compose` for integration testing
3. **Follow patterns**: Match existing Kotlin code style
4. **Update docs**: Keep documentation in sync

### Common Tasks
- **Adding new API endpoints**: Follow Spring Boot conventions in `data-sync-service/`
- **Database changes**: Create new Flyway migrations in `db/migration/postgres/`
- **Algorithm updates**: Modify `ScoreCalculator` and update tests
- **New integrations**: Add clients following `WoWAuditClient` pattern

## 🔍 Key Files for AI Context

### Business Logic
- `docs/score-model.md` - FLPS algorithm details
- `docs/flps-walkthrough.md` - Worked examples
- `ScoreCalculator.kt` - Core algorithm implementation

### Data Sources
- `docs/wowaudit-data-map.md` - API endpoint mapping
- `WoWAuditClient.kt` - External API integration
- Migration files - Database schema evolution

### Configuration
- `.env.local` - Environment variables
- `application.yaml` - Spring Boot configuration
- `docker-compose.yml` - Service orchestration

## 🚨 Critical Guidelines

### Data Sources
- **NEVER** use files in `wowaudit/` directory for production
- **ALWAYS** fetch live data via WoWAudit API
- **VALIDATE** all external data before persistence

### Algorithm Changes
- **TEST** FLPS calculations with mock data first
- **DOCUMENT** any formula modifications in `docs/score-model.md`
- **MAINTAIN** transparency for guild members

### Performance
- **CACHE** expensive calculations when possible
- **BATCH** database operations for large datasets
- **MONITOR** API rate limits for external services

## 🧪 Testing & Validation

### Local Development
```bash
# Start full stack
docker compose --env-file .env.local up --build

# Health check
curl http://localhost/api/actuator/health

# Check logs
docker compose logs data-sync
```

### Key Test Scenarios
1. **Score Calculation**: Verify FLPS output with known inputs
2. **Data Sync**: Test WoWAudit API integration
3. **Database Migrations**: Ensure schema updates work
4. **Error Handling**: Test API failures and recovery

## 📚 Essential Documentation

- `README.md` - Project overview and setup
- `docs/system-overview.md` - Technical architecture
- `docs/local-setup.md` - Development environment
- `docs/project-plan.md` - Roadmap and features
- `PROJECT_PROGRESS.md` - Current implementation status

## 🎮 Domain Knowledge

### World of Warcraft Context
- **Guilds**: Player organizations for raiding
- **Loot**: Equipment dropped by defeated enemies
- **Progression**: Advancing through increasingly difficult content
- **Raid Composition**: Tank/Healer/DPS role requirements

### Guild Operations
- **Loot Council**: Group that decides loot distribution
- **DKP/EPGP**: Traditional point-based loot systems
- **Simulations**: Mathematical modeling of gear upgrades
- **Attendance**: Tracking player participation

This project modernizes these traditional systems with data-driven automation while maintaining transparency and fairness.

---

## 🆘 Common Issues & Solutions

### Build Problems
- **Java Version**: Ensure JDK 24 is properly configured
- **Permissions**: Docker volume mounting may need fixes
- **Dependencies**: Check Gradle build logs for conflicts

### Database Issues
- **Migrations**: Check Flyway logs in application startup
- **Connections**: Verify PostgreSQL container is running
- **Data**: Use mock data files in `docs/data/` for testing

### API Integration
- **Rate Limits**: Implement backoff strategies
- **Authentication**: Check WoWAudit API key configuration
- **Data Format**: Validate against schema documentation

Remember: This system impacts real guild relationships and decisions. Prioritize transparency, fairness, and reliability in all changes.