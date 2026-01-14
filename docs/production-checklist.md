# Production Deployment Checklist

Use this checklist before deploying EdgeRush LootMan to production.

## Pre-Deployment

### Security Configuration

- [ ] **Admin mode is DISABLED**
  ```yaml
  api:
    admin-mode: false  # MUST be false in production
  ```

- [ ] **JWT secret is configured**
  - Set via environment variable: `JWT_SECRET`
  - Use a strong, randomly generated secret (minimum 256 bits)
  - Never commit the secret to version control

- [ ] **CORS origins are restricted**
  - Only allow your frontend domain(s)
  - Example: `https://app.edgerush.com`

- [ ] **HTTPS is enforced**
  - All traffic must use TLS 1.2+
  - HTTP requests should redirect to HTTPS
  - Valid SSL certificate installed

### Rate Limiting

- [ ] **Rate limiting is enabled**
  ```yaml
  api:
    rate-limit:
      enabled: true
      read-requests-per-second: 100
      write-requests-per-second: 20
  ```

- [ ] **Rate limits are appropriate for expected load**
  - Adjust based on expected traffic patterns
  - Consider separate limits for different client types

### Database

- [ ] **Database credentials are secured**
  - Use environment variables or secret management
  - Never commit credentials to version control

- [ ] **Database migrations are up to date**
  ```bash
  ./gradlew flywayInfo
  ```

- [ ] **Database backups are configured**
  - Regular automated backups
  - Tested restore procedure

- [ ] **Connection pool is properly sized**
  - Based on expected concurrent connections
  - Monitor pool usage in production

### External Integrations

- [ ] **WoWAudit credentials are configured**
  - Guild ID: `WOWAUDIT_GUILD_ID`
  - Verify API connectivity

- [ ] **Warcraft Logs credentials are configured**
  - Client ID: `WARCRAFTLOGS_CLIENT_ID`
  - Client Secret: `WARCRAFTLOGS_CLIENT_SECRET`
  - Verify OAuth token refresh works

- [ ] **Simulation Docker configuration (if used)**
  - Docker image: `SIMULATION_DOCKER_IMAGE`
  - Timeout: `SIMULATION_DOCKER_TIMEOUT_MINUTES`

## Monitoring & Observability

### Health Checks

- [ ] **Health endpoint is accessible**
  ```bash
  curl https://api.edgerush.com/actuator/health
  ```

- [ ] **Health check includes all components**
  - Database connectivity
  - External API availability
  - Disk space (if applicable)

### Metrics

- [ ] **Prometheus metrics are exposed**
  ```bash
  curl https://api.edgerush.com/actuator/prometheus
  ```

- [ ] **Key metrics are being collected**
  - Request latency (p50, p95, p99)
  - Error rates
  - Active connections
  - FLPS calculation times

### Logging

- [ ] **Logging level is appropriate**
  - INFO for general operations
  - WARN for potential issues
  - ERROR for failures

- [ ] **Log aggregation is configured**
  - Centralized log collection
  - Searchable log interface

- [ ] **Sensitive data is not logged**
  - No passwords or secrets
  - No PII without consent

### Alerting

- [ ] **Alerts are configured for**
  - High error rates (> 1%)
  - Slow response times (p95 > 1s)
  - Health check failures
  - Rate limit exhaustion
  - Database connection issues

## Deployment Process

### Build Verification

- [ ] **All tests pass**
  ```bash
  ./gradlew test
  ```

- [ ] **Test coverage meets target (85%)**
  ```bash
  ./gradlew jacocoTestReport
  ```

- [ ] **No critical security vulnerabilities**
  ```bash
  ./gradlew dependencyCheckAnalyze
  ```

### Deployment Steps

1. [ ] **Create database backup**

2. [ ] **Run database migrations**
   ```bash
   ./gradlew flywayMigrate
   ```

3. [ ] **Deploy application**
   - Blue-green or rolling deployment
   - Maintain previous version for rollback

4. [ ] **Verify deployment**
   - Health check passes
   - Smoke tests pass
   - No error spike in logs

5. [ ] **Monitor for 15 minutes**
   - Watch error rates
   - Watch response times
   - Watch resource usage

### Rollback Plan

- [ ] **Rollback procedure is documented**
- [ ] **Previous version is available**
- [ ] **Database rollback scripts ready (if needed)**

## Post-Deployment

### Verification

- [ ] **All endpoints respond correctly**
- [ ] **Authentication works**
- [ ] **FLPS calculations complete successfully**
- [ ] **External integrations are functional**

### Documentation

- [ ] **Release notes updated**
- [ ] **API changes documented**
- [ ] **Known issues documented**

## Environment Variables Reference

| Variable | Required | Description |
|----------|----------|-------------|
| `POSTGRES_URL` | Yes | Database connection URL |
| `POSTGRES_USER` | Yes | Database username |
| `POSTGRES_PASSWORD` | Yes | Database password |
| `JWT_SECRET` | Yes | JWT signing secret |
| `WOWAUDIT_GUILD_ID` | Yes | WoWAudit guild identifier |
| `WARCRAFTLOGS_CLIENT_ID` | Yes | WCL OAuth client ID |
| `WARCRAFTLOGS_CLIENT_SECRET` | Yes | WCL OAuth secret |
| `API_ADMIN_MODE` | No | Admin mode (default: false) |
| `API_RATE_LIMIT_ENABLED` | No | Rate limiting (default: true) |

## Emergency Contacts

- **On-Call Engineer**: [Configure in your incident management system]
- **Database Admin**: [Configure contact]
- **Security Team**: [Configure contact]

---

**Last Updated**: 2026-01-14
**Version**: 1.0.0
