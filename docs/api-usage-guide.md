# EdgeRush LootMan API Usage Guide

This guide provides practical examples for using the EdgeRush LootMan REST API.

## Quick Start

### Base URL

- **Development**: `http://localhost:8080`
- **Production**: `https://api.edgerush.com`

### Interactive Documentation

- **Swagger UI**: `/swagger-ui.html`
- **OpenAPI Spec**: `/v3/api-docs`

## Authentication

Most endpoints require JWT bearer token authentication.

### Getting a Token

Contact your system administrator to obtain JWT credentials.

### Using the Token

Include the token in the `Authorization` header:

```bash
curl -X GET "http://localhost:8080/api/v1/raiders" \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Admin Mode (Development Only)

In development, admin mode can be enabled to bypass authentication:

```yaml
api:
  admin-mode: true  # WARNING: Never use in production
```

## Pagination

All list endpoints support pagination:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 20 | Items per page (max: 100) |

### Example

```bash
# Get page 2 with 50 items
curl "http://localhost:8080/api/raider-entities?page=1&size=50"
```

### Response Format

```json
{
  "content": [...],
  "page": 1,
  "size": 50,
  "totalElements": 150,
  "totalPages": 3
}
```

## Common Operations

### Raiders

#### List All Raiders

```bash
curl "http://localhost:8080/api/raider-entities"
```

#### Get Raider by ID

```bash
curl "http://localhost:8080/api/raider-entities/123"
```

#### Create Raider

```bash
curl -X POST "http://localhost:8080/api/raider-entities" \
  -H "Content-Type: application/json" \
  -d '{
    "characterName": "Thrall",
    "realm": "Illidan",
    "region": "US",
    "guildId": "guild-123"
  }'
```

#### Update Raider

```bash
curl -X PUT "http://localhost:8080/api/raider-entities/123" \
  -H "Content-Type: application/json" \
  -d '{
    "characterName": "Thrall",
    "realm": "Illidan"
  }'
```

#### Delete Raider

```bash
curl -X DELETE "http://localhost:8080/api/raider-entities/123"
```

### Loot Awards

#### List Loot Awards

```bash
curl "http://localhost:8080/api/loot-awards"
```

#### Get Awards for a Raider

```bash
curl "http://localhost:8080/api/loot-awards/raider/123"
```

#### Create Loot Award

```bash
curl -X POST "http://localhost:8080/api/loot-awards" \
  -H "Content-Type: application/json" \
  -d '{
    "raiderId": 123,
    "itemId": 456,
    "itemName": "Thunderfury",
    "tier": "mythic",
    "flps": 0.85
  }'
```

### FLPS Reports

#### Get FLPS Report for Guild

```bash
curl "http://localhost:8080/api/v1/flps/guilds/guild-123/report"
```

#### Get System Status

```bash
curl "http://localhost:8080/api/v1/flps/status"
```

### Attendance

#### Track Attendance

```bash
curl -X POST "http://localhost:8080/api/v1/attendance/track" \
  -H "Content-Type: application/json" \
  -d '{
    "raiderId": 123,
    "raidId": 456,
    "present": true
  }'
```

#### Get Attendance Report

```bash
curl "http://localhost:8080/api/v1/attendance/raiders/123/report?guildId=guild-123&startDate=2026-01-01&endDate=2026-01-31"
```

## Rate Limiting

The API enforces rate limits to ensure fair usage:

| Operation | Limit |
|-----------|-------|
| Read (GET, HEAD, OPTIONS) | 100 requests/second |
| Write (POST, PUT, DELETE, PATCH) | 20 requests/second |

### Rate Limit Headers

When a request is rate limited, the API returns:

- **Status Code**: `429 Too Many Requests`
- **Header**: `Retry-After: 1`

## Error Handling

### Error Response Format

```json
{
  "timestamp": "2026-01-14T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Raider with ID 123 not found",
  "path": "/api/raider-entities/123"
}
```

### Common HTTP Status Codes

| Code | Description | Action |
|------|-------------|--------|
| 200 | Success | - |
| 201 | Created | Resource created successfully |
| 204 | No Content | Resource deleted successfully |
| 400 | Bad Request | Check request body validation |
| 401 | Unauthorized | Check JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 429 | Too Many Requests | Wait and retry |
| 500 | Server Error | Contact support |

## Deprecated Endpoints

When an endpoint is deprecated, the response includes headers:

```
Deprecation: date="2026-01-01"
Sunset: 2026-06-01
Link: </api/v2/new-endpoint>; rel="successor-version"
```

Monitor these headers and migrate to the replacement endpoint before the sunset date.

## Health Checks

### System Health

```bash
curl "http://localhost:8080/actuator/health"
```

### Prometheus Metrics

```bash
curl "http://localhost:8080/actuator/prometheus"
```

## Troubleshooting

### Authentication Issues

1. Verify token is not expired
2. Check token is in correct format: `Bearer <token>`
3. Verify token has required roles

### Rate Limiting

1. Implement exponential backoff
2. Cache responses where possible
3. Batch operations when supported

### Validation Errors

Check the response body for specific field errors:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "characterName",
      "message": "must not be blank"
    }
  ]
}
```

## Support

- **GitHub Issues**: https://github.com/edgerush/lootman/issues
- **Email**: support@edgerush.com
