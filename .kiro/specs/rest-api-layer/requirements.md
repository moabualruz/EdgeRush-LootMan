# Requirements Document

## Introduction

This specification defines a comprehensive REST API layer for the EdgeRush LootMan system, providing full CRUD (Create, Read, Update, Delete) operations for all database entities with proper authentication, authorization, OpenAPI documentation, and comprehensive testing. The API will support both production use with role-based access control and development/testing use with an admin bypass mode.

## Glossary

- **API_System**: The REST API layer providing HTTP endpoints for all database entities
- **Auth_System**: The authentication and authorization subsystem managing access control
- **OpenAPI_Generator**: The system component that generates interactive API documentation
- **Admin_Mode**: A development/testing configuration that bypasses authentication requirements
- **CRUD_Operations**: Create, Read, Update, Delete operations on database entities
- **DTO**: Data Transfer Object - serialization-safe representations of entities
- **Guild_Admin**: A user role with administrative privileges for a specific guild
- **System_Admin**: A user role with full system-wide administrative privileges
- **Public_User**: An authenticated user with read-only access to non-sensitive data
- **Test_Suite**: Automated tests covering unit, integration, and API contract validation

## Requirements

### Requirement 1: Comprehensive CRUD Endpoints

**User Story:** As a frontend developer, I want REST endpoints for all database entities, so that I can build user interfaces without direct database access.

#### Acceptance Criteria

1. WHEN THE API_System receives a GET request for any entity collection, THE API_System SHALL return a paginated list of entities with default page size of 20 items
2. WHEN THE API_System receives a GET request for a specific entity by ID, THE API_System SHALL return the complete entity data or HTTP 404 if not found
3. WHEN THE API_System receives a POST request with valid entity data, THE API_System SHALL create the entity and return HTTP 201 with the created resource
4. WHEN THE API_System receives a PUT request with valid entity data, THE API_System SHALL update the entity and return the updated resource
5. WHEN THE API_System receives a DELETE request for an entity, THE API_System SHALL remove the entity and return HTTP 204

### Requirement 2: Role-Based Access Control

**User Story:** As a system administrator, I want different access levels for different user roles, so that sensitive data and operations are protected.

#### Acceptance Criteria

1. WHEN THE Auth_System receives a request for a write operation (POST, PUT, DELETE), THE Auth_System SHALL verify the user has Guild_Admin or System_Admin role
2. WHEN THE Auth_System receives a request for sensitive data (behavioral actions, loot bans, guild configuration), THE Auth_System SHALL verify the user has Guild_Admin role for that guild
3. WHEN THE Auth_System receives a request for public data (FLPS scores, raid schedules), THE Auth_System SHALL allow access for any Public_User
4. IF THE Auth_System detects an unauthorized access attempt, THEN THE Auth_System SHALL return HTTP 403 with an error message
5. WHEN THE Auth_System validates a request, THE Auth_System SHALL extract guild context from the JWT token or request path

### Requirement 3: Admin Development Mode

**User Story:** As a developer, I want to run the system without authentication during testing, so that I can quickly test API endpoints without token management.

#### Acceptance Criteria

1. WHERE Admin_Mode is enabled via environment variable, THE Auth_System SHALL bypass all authentication checks
2. WHERE Admin_Mode is enabled, THE Auth_System SHALL simulate System_Admin privileges for all requests
3. WHEN Admin_Mode is enabled, THE API_System SHALL log a warning message at startup indicating reduced security
4. WHEN Admin_Mode is disabled (production), THE Auth_System SHALL enforce all authentication and authorization rules
5. WHERE Admin_Mode is enabled, THE OpenAPI_Generator SHALL display a banner indicating the system is in development mode

### Requirement 4: OpenAPI Documentation

**User Story:** As an API consumer, I want interactive API documentation, so that I can understand and test endpoints without reading code.

#### Acceptance Criteria

1. WHEN THE OpenAPI_Generator starts, THE OpenAPI_Generator SHALL generate complete OpenAPI 3.0 specification from controller annotations
2. WHEN a user accesses the documentation endpoint, THE API_System SHALL serve an interactive Swagger UI interface
3. WHEN THE OpenAPI_Generator documents an endpoint, THE OpenAPI_Generator SHALL include request schemas, response schemas, and example payloads
4. WHEN THE OpenAPI_Generator documents an endpoint, THE OpenAPI_Generator SHALL include authentication requirements and role restrictions
5. WHEN THE OpenAPI_Generator documents an endpoint, THE OpenAPI_Generator SHALL include HTTP status codes with descriptions

### Requirement 5: Data Transfer Objects

**User Story:** As a backend developer, I want separate DTOs from entities, so that API responses don't expose internal database structures or sensitive fields.

#### Acceptance Criteria

1. WHEN THE API_System serializes an entity for response, THE API_System SHALL convert the entity to a corresponding DTO
2. WHEN THE API_System receives request data, THE API_System SHALL validate the DTO before converting to an entity
3. WHEN THE API_System creates a DTO, THE API_System SHALL exclude internal fields (database IDs, audit timestamps) from create requests
4. WHEN THE API_System creates a DTO, THE API_System SHALL include all readable fields in response DTOs
5. WHEN THE API_System processes nested entities, THE API_System SHALL use nested DTOs to prevent circular references

### Requirement 6: Input Validation

**User Story:** As a system administrator, I want all API inputs validated, so that invalid data cannot corrupt the database.

#### Acceptance Criteria

1. WHEN THE API_System receives a request with invalid data, THE API_System SHALL return HTTP 400 with detailed validation errors
2. WHEN THE API_System validates a DTO, THE API_System SHALL check required fields are present and non-null
3. WHEN THE API_System validates a DTO, THE API_System SHALL verify field values meet constraints (length, range, format)
4. WHEN THE API_System validates a DTO, THE API_System SHALL verify foreign key references exist in the database
5. WHEN THE API_System returns validation errors, THE API_System SHALL include field names and specific error messages

### Requirement 7: Pagination and Filtering

**User Story:** As a frontend developer, I want to paginate and filter large result sets, so that I can build responsive user interfaces.

#### Acceptance Criteria

1. WHEN THE API_System receives a GET request for a collection, THE API_System SHALL accept page and size query parameters
2. WHEN THE API_System returns a paginated response, THE API_System SHALL include total count, current page, and total pages metadata
3. WHEN THE API_System receives filter parameters, THE API_System SHALL apply filters to the database query
4. WHEN THE API_System receives sort parameters, THE API_System SHALL order results by the specified field and direction
5. WHEN THE API_System processes pagination, THE API_System SHALL limit maximum page size to 100 items

### Requirement 8: Error Handling

**User Story:** As an API consumer, I want consistent error responses, so that I can handle errors predictably in client code.

#### Acceptance Criteria

1. WHEN THE API_System encounters an error, THE API_System SHALL return a standardized error response with timestamp, status, message, and path
2. WHEN THE API_System encounters a validation error, THE API_System SHALL return HTTP 400 with field-level error details
3. WHEN THE API_System encounters a resource not found, THE API_System SHALL return HTTP 404 with a descriptive message
4. WHEN THE API_System encounters an internal error, THE API_System SHALL return HTTP 500 and log the full stack trace
5. WHEN THE API_System encounters a database constraint violation, THE API_System SHALL return HTTP 409 with a user-friendly message

### Requirement 9: Comprehensive Testing

**User Story:** As a quality assurance engineer, I want automated tests for all endpoints, so that I can verify API behavior and prevent regressions.

#### Acceptance Criteria

1. WHEN THE Test_Suite executes, THE Test_Suite SHALL verify all CRUD operations for each entity controller
2. WHEN THE Test_Suite executes, THE Test_Suite SHALL verify authentication and authorization rules for protected endpoints
3. WHEN THE Test_Suite executes, THE Test_Suite SHALL verify input validation for all request DTOs
4. WHEN THE Test_Suite executes, THE Test_Suite SHALL verify OpenAPI specification matches actual endpoint behavior
5. WHEN THE Test_Suite executes, THE Test_Suite SHALL achieve minimum 80% code coverage on controller and service layers

### Requirement 10: Entity-Specific Endpoints

**User Story:** As a guild leader, I want specialized endpoints for common operations, so that I can perform complex tasks with single API calls.

#### Acceptance Criteria

1. WHEN THE API_System receives a request for guild roster, THE API_System SHALL return all raiders with their current FLPS scores
2. WHEN THE API_System receives a request for raid loot history, THE API_System SHALL return loot awards with raider and item details
3. WHEN THE API_System receives a request for attendance summary, THE API_System SHALL return aggregated attendance statistics per raider
4. WHEN THE API_System receives a request for wishlist items, THE API_System SHALL return items grouped by priority with raider counts
5. WHEN THE API_System receives a request for behavioral actions, THE API_System SHALL return active and expired actions with raider details

### Requirement 11: API Versioning

**User Story:** As a product manager, I want API versioning support, so that we can evolve the API without breaking existing clients.

#### Acceptance Criteria

1. WHEN THE API_System serves an endpoint, THE API_System SHALL include version prefix in the URL path (e.g., /api/v1/)
2. WHEN THE API_System introduces breaking changes, THE API_System SHALL increment the major version number
3. WHEN THE API_System supports multiple versions, THE API_System SHALL maintain backward compatibility for at least one previous version
4. WHEN THE API_System deprecates an endpoint, THE API_System SHALL include deprecation warnings in response headers
5. WHEN THE OpenAPI_Generator documents the API, THE OpenAPI_Generator SHALL clearly indicate the current version

### Requirement 12: Rate Limiting

**User Story:** As a system administrator, I want rate limiting on API endpoints, so that the system remains stable under high load.

#### Acceptance Criteria

1. WHEN THE API_System receives requests from a client, THE API_System SHALL track request count per client per time window
2. WHEN THE API_System detects a client exceeding rate limits, THE API_System SHALL return HTTP 429 with retry-after header
3. WHERE Admin_Mode is enabled, THE API_System SHALL disable rate limiting for development testing
4. WHEN THE API_System applies rate limits, THE API_System SHALL use different limits for read operations (100/min) versus write operations (20/min)
5. WHEN THE API_System returns rate limit information, THE API_System SHALL include remaining requests in response headers

### Requirement 13: Audit Logging

**User Story:** As a compliance officer, I want all API operations logged, so that I can audit system access and changes.

#### Acceptance Criteria

1. WHEN THE API_System processes a write operation, THE API_System SHALL log the user, timestamp, entity type, and operation
2. WHEN THE API_System processes a sensitive data access, THE API_System SHALL log the user, timestamp, and accessed resource
3. WHEN THE API_System logs an operation, THE API_System SHALL include request ID for correlation with application logs
4. WHEN THE API_System logs an operation, THE API_System SHALL store logs in a structured format for analysis
5. WHERE Admin_Mode is enabled, THE API_System SHALL log all operations with "ADMIN_MODE" indicator

### Requirement 14: Health and Metrics Endpoints

**User Story:** As a DevOps engineer, I want health check and metrics endpoints, so that I can monitor API availability and performance.

#### Acceptance Criteria

1. WHEN THE API_System receives a health check request, THE API_System SHALL return HTTP 200 if all dependencies are available
2. WHEN THE API_System performs a health check, THE API_System SHALL verify database connectivity and external API availability
3. WHEN THE API_System exposes metrics, THE API_System SHALL include request counts, response times, and error rates per endpoint
4. WHEN THE API_System exposes metrics, THE API_System SHALL include database connection pool statistics
5. WHEN THE API_System exposes metrics, THE API_System SHALL use Prometheus-compatible format for integration with monitoring tools

### Requirement 15: CORS Configuration

**User Story:** As a frontend developer, I want CORS properly configured, so that my web application can call the API from different domains.

#### Acceptance Criteria

1. WHEN THE API_System receives a preflight OPTIONS request, THE API_System SHALL return appropriate CORS headers
2. WHEN THE API_System is configured for production, THE API_System SHALL restrict CORS to whitelisted domains
3. WHERE Admin_Mode is enabled, THE API_System SHALL allow CORS from all origins for development testing
4. WHEN THE API_System returns a response, THE API_System SHALL include Access-Control-Allow-Origin header
5. WHEN THE API_System handles CORS, THE API_System SHALL support credentials (cookies, authorization headers)
