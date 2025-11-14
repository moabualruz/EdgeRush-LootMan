package com.edgerush.tools.crudgen.generator

import com.edgerush.tools.crudgen.model.EntityModel
import com.edgerush.tools.crudgen.model.FieldModel
import com.edgerush.tools.crudgen.model.GeneratorConfig

class DtoGenerator(private val config: GeneratorConfig) {
    
    fun generateRequestDtos(entity: EntityModel): String {
        val packageName = "${config.basePackage}.api.dto.request"
        
        return buildString {
            appendLine("package $packageName")
            appendLine()
            
            if (config.validationAnnotations) {
                appendLine("import jakarta.validation.constraints.*")
            }
            
            // Collect unique imports
            val imports = mutableSetOf<String>()
            entity.fields.forEach { field ->
                when {
                    field.type.contains("BigDecimal") -> imports.add("import java.math.BigDecimal")
                    field.type.contains("LocalDateTime") -> imports.add("import java.time.LocalDateTime")
                    field.type.contains("OffsetDateTime") -> imports.add("import java.time.OffsetDateTime")
                    field.type.contains("LocalDate") -> imports.add("import java.time.LocalDate")
                    field.type.contains("LocalTime") -> imports.add("import java.time.LocalTime")
                    field.type.contains("Instant") -> imports.add("import java.time.Instant")
                }
            }
            imports.sorted().forEach { appendLine(it) }
            
            appendLine()
            
            // Generate CreateRequest - ALL fields optional for flexibility
            val createFields = entity.getNonIdFields().filter { it.name !in listOf("createdAt", "updatedAt") }
            
            if (createFields.isEmpty()) {
                // Empty data class - add a dummy field
                appendLine("data class Create${entity.entityNameWithoutSuffix}Request(")
                appendLine("    val placeholder: String? = null")
                appendLine(")")
            } else {
                appendLine("data class Create${entity.entityNameWithoutSuffix}Request(")
                createFields.forEachIndexed { index, field ->
                    appendValidationAnnotations(field)
                    // Make all fields nullable with default null
                    val fieldType = if (field.kotlinType.endsWith("?")) field.kotlinType else "${field.kotlinType}?"
                    append("    val ${field.escapedName}: $fieldType = null")
                    if (index < createFields.size - 1) {
                        appendLine(",")
                        appendLine()
                    } else {
                        appendLine()
                    }
                }
                appendLine(")")
            }
            appendLine()
            
            // Generate UpdateRequest
            val updateFields = entity.getNonIdFields().filter { it.name !in listOf("createdAt", "updatedAt") }
            
            if (updateFields.isEmpty()) {
                // Empty data class - add a dummy field
                appendLine("data class Update${entity.entityNameWithoutSuffix}Request(")
                appendLine("    val placeholder: String? = null")
                appendLine(")")
            } else {
                appendLine("data class Update${entity.entityNameWithoutSuffix}Request(")
                updateFields.forEachIndexed { index, field ->
                    appendValidationAnnotations(field)
                    val nullableType = if (field.kotlinType.endsWith("?")) field.kotlinType else "${field.kotlinType}?"
                    append("    val ${field.escapedName}: $nullableType = null")
                    if (index < updateFields.size - 1) {
                        appendLine(",")
                        appendLine()
                    } else {
                        appendLine()
                    }
                }
                appendLine(")")
            }
        }
    }
    
    fun generateResponseDto(entity: EntityModel): String {
        val packageName = "${config.basePackage}.api.dto.response"
        
        return buildString {
            appendLine("package $packageName")
            appendLine()
            
            // Collect unique imports
            val imports = mutableSetOf<String>()
            entity.fields.forEach { field ->
                when {
                    field.type.contains("BigDecimal") -> imports.add("import java.math.BigDecimal")
                    field.type.contains("LocalDateTime") -> imports.add("import java.time.LocalDateTime")
                    field.type.contains("OffsetDateTime") -> imports.add("import java.time.OffsetDateTime")
                    field.type.contains("LocalDate") -> imports.add("import java.time.LocalDate")
                    field.type.contains("LocalTime") -> imports.add("import java.time.LocalTime")
                    field.type.contains("Instant") -> imports.add("import java.time.Instant")
                }
            }
            imports.sorted().forEach { appendLine(it) }
            
            appendLine()
            appendLine("data class ${entity.responseName}(")
            entity.fields.forEachIndexed { index, field ->
                val type = field.kotlinType
                append("    val ${field.escapedName}: $type")
                if (index < entity.fields.size - 1) {
                    appendLine(",")
                } else {
                    appendLine()
                }
            }
            appendLine(")")
        }
    }
    
    private fun StringBuilder.appendValidationAnnotations(field: FieldModel) {
        if (config.validationAnnotations) {
            field.validationAnnotations.forEach { annotation ->
                appendLine("    $annotation")
            }
        }
    }
}
