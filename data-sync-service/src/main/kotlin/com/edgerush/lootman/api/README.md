# API Layer

This layer contains REST and GraphQL endpoints. It depends on the application layer.

## Structure

```
api/
├── rest/
│   └── v1/
│       ├── flps/      # FLPS endpoints
│       ├── loot/      # Loot endpoints
│       ├── attendance/# Attendance endpoints
│       └── raids/     # Raid endpoints
├── graphql/           # (Phase 2)
│   ├── resolvers/
│   ├── schema/
│   └── dataloaders/
└── dto/               # Data Transfer Objects
    ├── request/
    └── response/
```

## Principles

- **Thin Controllers**: Delegate to use cases
- **DTOs**: Use DTOs for request/response, not domain objects
- **Validation**: Validate input at API boundary
- **Error Handling**: Convert domain exceptions to HTTP responses
- **Documentation**: OpenAPI/GraphQL schema documentation

## Guidelines

- Controllers should only handle HTTP concerns
- Use DTOs to decouple API from domain
- Validate all inputs
- Return appropriate HTTP status codes
- Document all endpoints
