package com.edgerush.tools.crudgen.generator

import com.edgerush.tools.crudgen.model.EntityModel
import com.edgerush.tools.crudgen.model.GeneratorConfig

class MapperGenerator(private val config: GeneratorConfig) {
    
    fun generate(entity: EntityModel): String {
        val packageName = "${config.basePackage}.service.mapper"
        val entityPackage = entity.packageName
        
        return buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import ${config.basePackage}.api.dto.request.Create${entity.entityNameWithoutSuffix}Request")
            appendLine("import ${config.basePackage}.api.dto.request.Update${entity.entityNameWithoutSuffix}Request")
            appendLine("import ${config.basePackage}.api.dto.response.${entity.responseName}")
            appendLine("import $entityPackage.${entity.name}")
            appendLine("import org.springframework.stereotype.Component")
            
            // Collect unique imports
            val imports = mutableSetOf<String>()
            entity.fields.forEach { field ->
                when {
                    field.type.contains("OffsetDateTime") -> imports.add("import java.time.OffsetDateTime")
                    field.type.contains("LocalDateTime") -> imports.add("import java.time.LocalDateTime")
                    field.type.contains("LocalDate") -> imports.add("import java.time.LocalDate")
                    field.type.contains("LocalTime") -> imports.add("import java.time.LocalTime")
                    field.type.contains("Instant") -> imports.add("import java.time.Instant")
                }
            }
            imports.sorted().forEach { appendLine(it) }
            
            appendLine()
            appendLine("@Component")
            appendLine("class ${entity.mapperName} {")
            appendLine()
            
            // toEntity method
            appendLine("    fun toEntity(request: Create${entity.entityNameWithoutSuffix}Request): ${entity.name} {")
            appendLine("        return ${entity.name}(")
            
            // Get list of fields that are in the request DTO
            val requestFields = entity.getNonIdFields().filter { it.name !in listOf("createdAt", "updatedAt") }.map { it.name }.toSet()
            
            entity.fields.forEach { field ->
                when {
                    field.isId && field.kotlinType.endsWith("?") -> appendLine("            ${field.name} = null,")
                    field.isId && !field.kotlinType.endsWith("?") -> {
                        // Non-nullable ID - provide default based on type
                        val defaultValue = when {
                            field.kotlinType == "String" -> "\"\""
                            field.kotlinType == "Long" -> "0L"
                            field.kotlinType == "Int" -> "0"
                            else -> "null"
                        }
                        appendLine("            ${field.escapedName} = $defaultValue, // TODO: Set from request or context")
                    }
                    field.name == "createdAt" && field.type.contains("OffsetDateTime") -> 
                        appendLine("            ${field.name} = OffsetDateTime.now(),")
                    field.name == "updatedAt" && field.type.contains("OffsetDateTime") -> 
                        appendLine("            ${field.name} = OffsetDateTime.now(),")
                    field.name == "createdAt" && field.type.contains("Instant") -> 
                        appendLine("            ${field.name} = Instant.now(),")
                    field.name == "updatedAt" && field.type.contains("Instant") -> 
                        appendLine("            ${field.name} = Instant.now(),")
                    field.name == "createdAt" && field.type.contains("LocalDateTime") -> 
                        appendLine("            ${field.name} = LocalDateTime.now(),")
                    field.name == "updatedAt" && field.type.contains("LocalDateTime") -> 
                        appendLine("            ${field.name} = LocalDateTime.now(),")
                    field.name in requestFields -> {
                        // Field exists in request - use it with appropriate defaults
                        val nullSafeOperator = if (!field.kotlinType.endsWith("?")) {
                            // Entity field is non-null, provide default
                            val defaultValue = when {
                                field.kotlinType == "String" -> " ?: \"\""
                                field.kotlinType == "Int" -> " ?: 0"
                                field.kotlinType == "Long" -> " ?: 0L"
                                field.kotlinType == "Double" -> " ?: 0.0"
                                field.kotlinType == "Boolean" -> " ?: false"
                                field.kotlinType.contains("BigDecimal") -> " ?: java.math.BigDecimal.ZERO"
                                field.kotlinType.contains("Instant") -> " ?: Instant.now()"
                                field.kotlinType.contains("OffsetDateTime") -> " ?: OffsetDateTime.now()"
                                field.kotlinType.contains("LocalDateTime") -> " ?: LocalDateTime.now()"
                                else -> " ?: throw IllegalArgumentException(\"${field.name} is required\")"
                            }
                            defaultValue
                        } else {
                            ""
                        }
                        appendLine("            ${field.escapedName} = request.${field.escapedName}$nullSafeOperator,")
                    }
                    else -> {
                        // Field not in request - provide default value based on nullability
                        // IMPORTANT: Respect nullable types - use null for nullable fields
                        val defaultValue = if (field.kotlinType.endsWith("?")) {
                            "null"
                        } else {
                            // Non-nullable field needs a default value
                            when {
                                field.kotlinType == "String" -> "\"\""
                                field.kotlinType == "Int" -> "0"
                                field.kotlinType == "Long" -> "0L"
                                field.kotlinType == "Double" -> "0.0"
                                field.kotlinType == "Boolean" -> "false"
                                field.kotlinType.contains("BigDecimal") -> "java.math.BigDecimal.ZERO"
                                field.kotlinType.contains("Instant") -> "Instant.now()"
                                field.kotlinType.contains("OffsetDateTime") -> "OffsetDateTime.now()"
                                field.kotlinType.contains("LocalDateTime") -> "LocalDateTime.now()"
                                else -> "null // TODO: Set appropriate default for ${field.kotlinType}"
                            }
                        }
                        appendLine("            ${field.escapedName} = $defaultValue, // System populated")
                    }
                }
            }
            appendLine("        )")
            appendLine("    }")
            appendLine()
            
            // updateEntity method
            appendLine("    fun updateEntity(entity: ${entity.name}, request: Update${entity.entityNameWithoutSuffix}Request): ${entity.name} {")
            appendLine("        return entity.copy(")
            entity.fields.filter { !it.isId && it.name !in listOf("createdAt", "updatedAt") }.forEach { field ->
                appendLine("            ${field.escapedName} = request.${field.escapedName} ?: entity.${field.escapedName},")
            }
            val updatedAtField = entity.fields.find { it.name == "updatedAt" }
            if (updatedAtField != null) {
                when {
                    updatedAtField.type.contains("OffsetDateTime") -> appendLine("            updatedAt = OffsetDateTime.now()")
                    updatedAtField.type.contains("Instant") -> appendLine("            updatedAt = Instant.now()")
                    else -> appendLine("            updatedAt = entity.updatedAt")
                }
            }
            appendLine("        )")
            appendLine("    }")
            appendLine()
            
            // toResponse method
            appendLine("    fun toResponse(entity: ${entity.name}): ${entity.responseName} {")
            appendLine("        return ${entity.responseName}(")
            entity.fields.forEach { field ->
                // Add !! for nullable entity fields that map to non-null response fields
                val nullSafeOperator = if (field.kotlinType.endsWith("?")) "!!" else ""
                appendLine("            ${field.escapedName} = entity.${field.escapedName}$nullSafeOperator,")
            }
            appendLine("        )")
            appendLine("    }")
            appendLine("}")
        }
    }
}
