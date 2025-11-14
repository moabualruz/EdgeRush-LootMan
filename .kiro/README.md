# Kiro Workspace Configuration

This directory contains Kiro-specific configuration and specifications for the EdgeRush LootMan project.

## Directory Structure

- `specs/` - Feature specifications following the spec-driven development workflow
- `steering/` - Steering rules and guidelines for AI agents working on this project
- `settings/` - Workspace-level settings and configurations

## Current Specs

### ✅ Complete Specifications (Ready for Implementation)

1. **Warcraft Logs Integration** (`specs/warcraft-logs-integration/`)
   - **Status**: ✅ Specification Complete
   - **Priority**: 🔴 Critical
   - **Effort**: 3-4 weeks
   - **Purpose**: Enable accurate MAS (Mechanical Adherence Score) calculation using real combat log data
   - **Impact**: MAS currently returns 0.0; this integration will provide production-accurate FLPS scores
   - **Tasks**: 12 major tasks, 50+ sub-tasks

2. **Raidbots Integration** (`specs/raidbots-integration/`)
   - **Status**: ✅ Specification Complete
   - **Priority**: 🔴 Critical
   - **Effort**: 3-4 weeks
   - **Purpose**: Enable accurate upgrade value calculations using Raidbots Droptimizer simulations
   - **Impact**: Currently using wishlist percentages (less accurate); simulations will provide precise IPI calculations
   - **Tasks**: 14 major tasks

3. **Web Dashboard** (`specs/web-dashboard/`)
   - **Status**: ✅ Specification Complete
   - **Priority**: 🟡 High
   - **Effort**: 6-8 weeks
   - **Purpose**: Provide user-facing web interface for FLPS transparency and guild management
   - **Impact**: Enables self-service access to scores, history, and configuration
   - **Tasks**: 22 major tasks

4. **Discord Bot** (`specs/discord-bot/`)
   - **Status**: ✅ Specification Complete
   - **Priority**: 🟡 High
   - **Effort**: 4-5 weeks
   - **Purpose**: Automate notifications and provide Discord commands for FLPS data
   - **Impact**: Eliminates manual communication, improves operational efficiency
   - **Tasks**: 22 major tasks

### Implementation Order Recommendation

**Phase 1 - Critical Accuracy** (6-8 weeks):
1. Warcraft Logs Integration
2. Raidbots Integration

**Phase 2 - User Experience** (10-13 weeks):
3. Web Dashboard
4. Discord Bot

### Total Estimated Effort

- **All Specs**: 16-21 weeks (4-5 months)
- **Critical Path**: 6-8 weeks (Warcraft Logs + Raidbots)

See individual spec directories for detailed requirements, design, and implementation tasks.
