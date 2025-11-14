package com.edgerush.tools.crudgen.model

import java.io.File

data class GeneratorConfig(
    val basePackage: String = "com.example",
    val apiVersion: String = "v1",
    val enableAuditLogging: Boolean = true,
    val enablePagination: Boolean = true,
    val validationAnnotations: Boolean = true,
    val openApiDocumentation: Boolean = true,
    val customMethods: Map<String, List<String>> = emptyMap()
) {
    companion object {
        fun fromFile(file: File): GeneratorConfig {
            // Simple implementation - in production, use kotlinx.serialization or Jackson
            return GeneratorConfig()
        }
    }
}
