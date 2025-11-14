# REST API Completion Tasks

## Status: 2 Entities Missing

### Missing Entities

1. **ApplicationEntity** - Application management
2. **GuestEntity** - Guest raider tracking

### Tasks Per Entity

For each entity, create 5 files:

1. Request DTOs (`CreateXRequest`, `UpdateXRequest`)
2. Response DTO (`XResponse`)
3. Mapper (`XMapper`)
4. CRUD Service (`XCrudService`)
5. REST Controller (`XController`)

### Estimated Time

- 2 entities × 15 minutes = 30 minutes total

### Execution Order

1. Application entity (5 files)
2. Guest entity (5 files)
3. Build and verify
4. Update OpenAPI documentation
5. Test in Swagger UI

---

## Then: GraphQL API Layer

After REST completion, create GraphQL spec and implementation.

