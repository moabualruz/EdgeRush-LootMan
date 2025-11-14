# 🚀 EdgeRush LootMan - Quick Start Guide

**New to this project? Start here!**

## 📋 Essential Documents (Read in Order)

1. **[PROJECT_PRIORITIES.md](./PROJECT_PRIORITIES.md)** ⭐ **READ THIS FIRST**
   - Current project priorities and status
   - What to work on next
   - Progress tracking for all features

2. **[.kiro/steering/project-context.md](./.kiro/steering/project-context.md)**
   - Project overview and architecture
   - Current implementation status
   - Key constraints and patterns

3. **Current Priority Spec: [.kiro/specs/graphql-tdd-refactor/](./.kiro/specs/graphql-tdd-refactor/)**
   - Requirements, design, and tasks for TDD refactoring
   - This is what we're working on NOW

## 🎯 What Should I Work On?

### Current Priority: TDD Standards & Project Refactoring

**Status:** Spec complete, ready to start implementation  
**Timeline:** 13 weeks  
**Next Action:** Open `.kiro/specs/graphql-tdd-refactor/tasks.md` and start Task 1

**Why this is priority:**
- Establishes foundation for all future development
- Improves code quality and testability
- Required before adding GraphQL or other major features

## 🏗️ Project Structure

```
EdgeRush-LootMan/
├── PROJECT_PRIORITIES.md          ⭐ Current priorities and status
├── START_HERE.md                  📍 You are here
├── .kiro/
│   ├── specs/                     📋 All feature specifications
│   │   ├── graphql-tdd-refactor/  🔥 CURRENT PRIORITY
│   │   ├── rest-api-layer/        (40% complete)
│   │   ├── raidbots-integration/  (40% complete, blocked)
│   │   ├── web-dashboard/         (planned)
│   │   └── discord-bot/           (planned)
│   └── steering/
│       └── project-context.md     📖 Project overview
├── data-sync-service/             💻 Main Kotlin application
│   └── src/main/kotlin/...
└── docs/                          📚 Additional documentation
```

## ⚡ Quick Commands

### Build & Test
```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run code quality checks
./gradlew ktlintCheck detekt
```

### Database
```bash
# Start PostgreSQL with Docker
docker-compose up -d postgres

# Run migrations
./gradlew flywayMigrate
```

### Run Application
```bash
# Start the application
./gradlew bootRun

# Or with Docker
docker-compose up
```

## 📊 Current Status Summary

| Feature | Status | Progress |
|---------|--------|----------|
| **TDD Refactoring** | 📋 Ready | 0% - Start here! |
| **GraphQL API** | 📋 Planned | 0% - After refactoring |
| **REST API** | 🔄 In Progress | 40% |
| **Raidbots** | ⚠️ Blocked | 40% |
| **Warcraft Logs** | ✅ Complete | 100% |
| **Core FLPS** | ✅ Complete | 100% |

## 🎓 Understanding the Project

### What is EdgeRush LootMan?

A progression-first guild operations platform for World of Warcraft that automates loot distribution using the FLPS (Final Loot Priority Score) algorithm.

**FLPS Formula:** `FLPS = (RMS × IPI) × RDF`
- **RMS:** Raider Merit Score (attendance + performance + preparation + behavior)
- **IPI:** Item Priority Index (upgrade value + tier impact + role multiplier)
- **RDF:** Recency Decay Factor (reduces score for recent loot recipients)

### Technology Stack

- **Backend:** Kotlin + Spring Boot 3.x
- **Database:** PostgreSQL 18 with Flyway migrations
- **Build:** Gradle 8.10.1 with JDK 24
- **Testing:** JUnit 5, MockK, Testcontainers
- **External APIs:** WoWAudit, Warcraft Logs, Raidbots

## 🔄 Workflow for New Sessions

1. **Read PROJECT_PRIORITIES.md** to understand current focus
2. **Check the current priority spec** (currently: graphql-tdd-refactor)
3. **Open tasks.md** in the spec directory
4. **Start with the first incomplete task**
5. **Follow TDD workflow:** Write tests first, then implementation
6. **Update PROJECT_PRIORITIES.md** when completing major milestones

## 📝 Development Guidelines

### Test-Driven Development (TDD)

**Always follow this workflow:**
1. Write failing test first
2. Implement minimal code to pass test
3. Refactor while keeping tests green
4. Repeat

**Coverage Requirements:**
- Minimum 85% code coverage for new code
- All tests must pass before committing

### Code Quality

- Run `ktlint` for formatting
- Run `detekt` for static analysis
- Fix all violations before committing
- Follow Kotlin idioms and best practices

### Commit Guidelines

- Write clear, descriptive commit messages
- Reference task numbers from specs
- Keep commits focused and atomic

## 🆘 Need Help?

### Common Issues

**Build fails:**
- Ensure JDK 24 is installed and configured
- Run `./gradlew clean build`

**Database connection errors:**
- Check Docker is running: `docker ps`
- Verify PostgreSQL is up: `docker-compose logs postgres`

**Tests failing:**
- Check test database is clean
- Verify Testcontainers can access Docker

### Where to Find Information

- **Architecture:** `.kiro/steering/project-context.md`
- **API Documentation:** `docs/` directory
- **Specs:** `.kiro/specs/[feature-name]/`
- **Testing Standards:** Will be created in Priority 0 (Task 3)
- **Code Standards:** Will be created in Priority 0 (Task 4)

## 🎯 Next Steps

**Ready to start?**

1. Open [PROJECT_PRIORITIES.md](./PROJECT_PRIORITIES.md) to see detailed priorities
2. Navigate to `.kiro/specs/graphql-tdd-refactor/tasks.md`
3. Click "Start task" next to Task 1
4. Follow the TDD workflow and implementation guidelines

**Questions?** Review the spec documents or check the project context for more details.

---

**Last Updated:** 2025-11-14  
**Current Priority:** TDD Standards & Project Refactoring (Priority 0)
