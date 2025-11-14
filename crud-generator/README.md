# CRUD API Generator

A Kotlin-based code generator that creates complete CRUD REST APIs from entity classes.

## Features

- **Entity Parsing**: Automatically parses Kotlin entity classes
- **DTO Generation**: Creates Request and Response DTOs with validation
- **Mapper Generation**: Generates entity-DTO mappers
- **Service Generation**: Creates CRUD service implementations
- **Controller Generation**: Generates REST controllers with OpenAPI annotations
- **Repository Updates**: Adds pagination and custom query methods
- **Configurable**: Supports custom templates and naming conventions
- **Reusable**: Works with any Spring Boot + Kotlin project

## Usage

### Command Line

```bash
# Generate CRUD API for a single entity
./gradlew run --args="generate --entity=path/to/RaiderEntity.kt --output=data-sync-service/src/main/kotlin"

# Generate for multiple entities
./gradlew run --args="generate-batch --entities-dir=data-sync-service/src/main/kotlin/com/edgerush/datasync/entity --output=data-sync-service/src/main/kotlin"

# Generate with custom configuration
./gradlew run --args="generate --entity=RaiderEntity.kt --output=src --config=generator-config.json"
```

### Configuration File

Create `generator-config.json`:

```json
{
  "basePackage": "com.edgerush.datasync",
  "apiVersion": "v1",
  "enableAuditLogging": true,
  "enablePagination": true,
  "validationAnnotations": true,
  "openApiDocumentation": true,
  "customMethods": {
    "RaiderEntity": ["findByGuildId", "findByCharacterName"],
    "LootAwardEntity": ["findByRaiderId", "findByItemId"]
  }
}
```

## Generated Files

For each entity, the generator creates:

1. **Request DTOs** (`api/dto/request/{Entity}Request.kt`)
   - `Create{Entity}Request`
   - `Update{Entity}Request`
   - With Jakarta validation annotations

2. **Response DTO** (`api/dto/response/{Entity}Response.kt`)
   - `{Entity}Response`

3. **Mapper** (`service/mapper/{Entity}Mapper.kt`)
   - `toEntity()`, `updateEntity()`, `toResponse()`

4. **CRUD Service** (`service/crud/{Entity}CrudService.kt`)
   - Implements `CrudService` interface
   - Includes audit logging

5. **REST Controller** (`api/v1/{Entity}Controller.kt`)
   - Extends `BaseCrudController`
   - OpenAPI annotations
   - Custom endpoints

6. **Repository Update** (updates existing repository)
   - Adds `PagingAndSortingRepository`
   - Adds custom query methods

## Architecture

```
crud-generator/
├── src/main/kotlin/com/edgerush/tools/crudgen/
│   ├── Main.kt                    # CLI entry point
│   ├── parser/
│   │   ├── EntityParser.kt        # Parses Kotlin entity files
│   │   └── FieldExtractor.kt      # Extracts field information
│   ├── generator/
│   │   ├── DtoGenerator.kt        # Generates DTOs
│   │   ├── MapperGenerator.kt     # Generates mappers
│   │   ├── ServiceGenerator.kt    # Generates services
│   │   ├── ControllerGenerator.kt # Generates controllers
│   │   └── RepositoryUpdater.kt   # Updates repositories
│   ├── model/
│   │   ├── EntityModel.kt         # Entity metadata model
│   │   ├── FieldModel.kt          # Field metadata model
│   │   └── GeneratorConfig.kt     # Configuration model
│   └── template/
│       ├── TemplateEngine.kt      # Template processing
│       └── templates/             # Code templates
└── build.gradle.kts
```

## Example

### Input Entity

```kotlin
@Table("raiders")
data class RaiderEntity(
    @Id val id: Long? = null,
    val guildId: String,
    val characterName: String,
    val realm: String,
    val class: String,
    val spec: String,
    val role: String,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
```

### Generated Output

The generator creates 5 files with complete CRUD implementation:
- Request DTOs with validation
- Response DTO
- Mapper with conversion logic
- Service with audit logging
- Controller with OpenAPI docs

## Benefits

- **Time Savings**: Reduces 30 minutes of manual work to 30 seconds
- **Consistency**: All APIs follow the same pattern
- **Quality**: Generated code follows best practices
- **Maintainability**: Easy to update all APIs by regenerating
- **Reusability**: Works with any Spring Boot Kotlin project

## Requirements

- JDK 17+
- Kotlin 1.9+
- Gradle 8+

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```

## Installation

```bash
# Build executable JAR
./gradlew shadowJar

# Run from anywhere
java -jar crud-generator/build/libs/crud-generator-1.0.0-all.jar generate --entity=MyEntity.kt
```

## License

MIT License - Free to use in any project
