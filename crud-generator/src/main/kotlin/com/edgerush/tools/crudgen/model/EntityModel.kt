package com.edgerush.tools.crudgen.model

data class EntityModel(
    val name: String,
    val packageName: String,
    val tableName: String,
    val fields: List<FieldModel>,
    val idField: FieldModel,
    val imports: List<String> = emptyList()
) {
    val entityNameWithoutSuffix: String
        get() = name.removeSuffix("Entity")
    
    val requestName: String
        get() = "${entityNameWithoutSuffix}Request"
    
    val responseName: String
        get() = "${entityNameWithoutSuffix}Response"
    
    val mapperName: String
        get() = "${entityNameWithoutSuffix}Mapper"
    
    val serviceName: String
        get() = "${entityNameWithoutSuffix}CrudService"
    
    val controllerName: String
        get() = "${entityNameWithoutSuffix}Controller"
    
    val repositoryName: String
        get() = "${entityNameWithoutSuffix}Repository"
    
    val apiPath: String
        get() = entityNameWithoutSuffix
            .replace(Regex("([a-z])([A-Z])"), "$1-$2")
            .lowercase()
            .let { if (it.endsWith("s")) it else "${it}s" }
    
    fun hasGuildId(): Boolean = fields.any { it.name == "guildId" }
    
    fun getNonIdFields(): List<FieldModel> = fields.filter { it.name != idField.name }
    
    fun getRequiredFields(): List<FieldModel> = getNonIdFields().filter { !it.isNullable && it.defaultValue == null }
    
    fun getOptionalFields(): List<FieldModel> = getNonIdFields().filter { it.isNullable || it.defaultValue != null }
}

data class FieldModel(
    val name: String,
    val type: String,
    val isNullable: Boolean,
    val defaultValue: String? = null,
    val isId: Boolean = false,
    val annotations: List<String> = emptyList(),
    val needsBackticks: Boolean = false
) {
    val kotlinType: String
        get() = if (isNullable) "$type?" else type
    
    val escapedName: String
        get() = if (needsBackticks) "`$name`" else name
    
    val validationAnnotations: List<String>
        get() = buildList {
            when {
                type == "String" && !isNullable -> {
                    add("@field:NotBlank(message = \"${name.toReadable()} is required\")")
                    add("@field:Size(max = 255, message = \"${name.toReadable()} must not exceed 255 characters\")")
                }
                type in listOf("Int", "Long") && !isNullable -> {
                    add("@field:Min(value = 0, message = \"${name.toReadable()} must be positive\")")
                }
                type == "BigDecimal" -> {
                    add("@field:DecimalMin(value = \"0.0\", message = \"${name.toReadable()} must be at least 0.0\")")
                }
                type == "Boolean" -> {
                    // No validation needed for boolean
                }
            }
        }
    
    private fun String.toReadable(): String {
        return replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}
