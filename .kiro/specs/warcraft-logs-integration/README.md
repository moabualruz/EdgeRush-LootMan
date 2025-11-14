# Warcraft Logs Integration Spec

## Overview

This specification defines the integration of Warcraft Logs API to provide accurate combat performance data for the Mechanical Adherence Score (MAS) component of the FLPS algorithm.

## Current Problem

MAS (Mechanical Adherence Score) currently returns 0.0 because there is no source of combat performance data. This significantly impacts FLPS accuracy, as MAS represents 40% of the RMS (Raider Merit Score) weight.

## Solution

Integrate Warcraft Logs API v2 to:
- Fetch combat log reports for configured guilds
- Extract performance metrics (deaths per attempt, avoidable damage)
- Calculate spec-specific averages for normalization
- Provide accurate MAS scores based on actual raid performance

## Key Features

### Configurability
- Guild-specific configuration for all parameters
- Customizable weights, thresholds, and timeframes
- Character name mapping support
- Flexible sync schedules

### Resilience
- Graceful degradation when API unavailable
- Circuit breaker and retry patterns
- Fallback to cached or default values
- Comprehensive error handling

### Performance
- Async processing for sync operations
- Caching for MAS scores and spec averages
- Database indexing for fast queries
- Connection pooling and request batching

### Security
- Encrypted credential storage (AES-256-GCM)
- OAuth2 authentication
- HTTPS/TLS for all API calls
- Admin-only configuration access

### Observability
- Comprehensive metrics (sync success, API latency, cache hit rate)
- Health check indicators
- Structured logging with context
- Admin endpoints for monitoring

## Documents

- **requirements.md** - User stories and acceptance criteria (12 requirements)
- **design.md** - Technical design with architecture, data models, and implementation details
- **tasks.md** - Implementation plan with 12 major tasks and 50+ sub-tasks

## Implementation Approach

The implementation follows an incremental approach:

1. **Foundation** (Tasks 1-3): Project structure, database schema, configuration system
2. **API Client** (Task 4): Warcraft Logs API client with OAuth2 and error handling
3. **Sync Service** (Task 5): Report discovery, fight processing, metric extraction
4. **Performance Service** (Task 6): MAS calculation and ScoreCalculator integration
5. **Resilience** (Task 7): Circuit breaker, retry logic, fallback mechanisms
6. **REST API** (Task 8): Configuration and sync management endpoints
7. **Monitoring** (Task 9): Metrics, health checks, enhanced logging
8. **Testing** (Tasks 10-11): Comprehensive unit and integration tests
9. **Documentation** (Task 12): Configuration guides, API docs, troubleshooting

## Success Criteria

- ✅ Warcraft Logs API authentication working
- ✅ Reports syncing automatically on schedule
- ✅ Performance data extracted and stored
- ✅ MAS calculation using real Warcraft Logs data
- ✅ MAS no longer returns 0.0 for characters with data
- ✅ FLPS scores reflect actual combat performance
- ✅ Configuration manageable via REST API
- ✅ Health checks showing integration status
- ✅ Metrics available for monitoring
- ✅ Documentation complete and accurate

## Configuration Example

```yaml
warcraft-logs:
  enabled: true
  client-id: ${WARCRAFT_LOGS_CLIENT_ID}
  client-secret: ${WARCRAFT_LOGS_CLIENT_SECRET}
  
  sync:
    cron: "0 0 */6 * * *"  # Every 6 hours
    time-window-days: 30
    included-difficulties:
      - Mythic
      - Heroic
  
  mas:
    dpa-weight: 0.25
    adt-weight: 0.25
    critical-threshold: 1.5
    fallback-mas: 0.0
```

## Guild-Specific Configuration

```json
{
  "guildId": "my-guild",
  "enabled": true,
  "guildName": "My Awesome Guild",
  "realm": "Area 52",
  "region": "US",
  "syncIntervalHours": 4,
  "dpaWeight": 0.3,
  "adtWeight": 0.2,
  "criticalThreshold": 1.8,
  "characterNameMappings": {
    "PlayerOne-AreaFiftyTwo": "PlayerOne-Area52"
  }
}
```

## API Endpoints

### Configuration
- `GET /api/warcraft-logs/config/{guildId}` - Get guild configuration
- `PUT /api/warcraft-logs/config/{guildId}` - Update guild configuration
- `POST /api/warcraft-logs/config/{guildId}/character-mapping` - Add character mapping
- `DELETE /api/warcraft-logs/config/{guildId}/character-mapping/{id}` - Remove mapping

### Sync Management
- `POST /api/warcraft-logs/sync/{guildId}` - Trigger manual sync
- `GET /api/warcraft-logs/sync/{guildId}/status` - Get sync status
- `GET /api/warcraft-logs/sync/{guildId}/history` - Get sync history

### Performance Data
- `GET /api/warcraft-logs/performance/{guildId}/{characterName}` - Get character performance
- `GET /api/warcraft-logs/reports/{guildId}` - List reports
- `GET /api/warcraft-logs/reports/{reportCode}/fights` - Get fight data

### Monitoring
- `GET /actuator/health/warcraftLogs` - Health check
- `GET /actuator/metrics/wcl.*` - Metrics

## Dependencies

- Spring Boot WebFlux (WebClient)
- Spring Security OAuth2 Client
- Resilience4j (Circuit Breaker, Retry)
- Spring Cache
- PostgreSQL
- Flyway

## Estimated Effort

- **Total Tasks**: 12 major tasks, 50+ sub-tasks
- **Estimated Time**: 3-4 weeks for complete implementation
- **Priority**: Critical (blocks production-accurate FLPS scores)

## Next Steps

1. Review and approve this specification
2. Begin implementation starting with Task 1 (project structure)
3. Follow incremental approach, testing each component
4. Deploy to staging for integration testing
5. Monitor performance and adjust configuration as needed

## Related Specifications

- **Raidbots Integration** (Planned) - For accurate upgrade value calculations
- **Web Dashboard** (Planned) - To display Warcraft Logs performance data
- **Discord Bot** (Planned) - To notify raiders of performance issues

---

**Status**: ✅ Specification Complete - Ready for Implementation  
**Last Updated**: 2025-01-13  
**Spec Version**: 1.0
