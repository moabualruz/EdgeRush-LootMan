package com.edgerush.tools.crudgen.parser

import com.edgerush.tools.crudgen.model.EntityModel
import com.edgerush.tools.crudgen.model.FieldModel
import java.io.File

class EntityParser {
    
    fun parse(file: File): EntityModel {
        val content = file.readText()
        
        val packageName = extractPackage(content)
        val imports = extractImports(content)
        val className = extractClassName(content)
        val tableName = extractTableName(content)
        val fields = extractFields(content)
        val idField = fields.firstOrNull { it.isId } 
            ?: throw IllegalArgumentException("No @Id field found in entity")
        
        return EntityModel(
            name = className,
            packageName = packageName,
            tableName = tableName,
            fields = fields,
            idField = idField,
            imports = imports
        )
    }
    
    private fun extractPackage(content: String): String {
        val packageRegex = Regex("""package\s+([\w.]+)""")
        return packageRegex.find(content)?.groupValues?.get(1) 
            ?: throw IllegalArgumentException("No package declaration found")
    }
    
    private fun extractImports(content: String): List<String> {
        val importRegex = Regex("""import\s+([\w.]+)""")
        return importRegex.findAll(content).map { it.groupValues[1] }.toList()
    }
    
    private fun extractClassName(content: String): String {
        val classRegex = Regex("""data\s+class\s+(\w+)""")
        return classRegex.find(content)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("No data class found")
    }
    
    private fun extractTableName(content: String): String {
        val tableRegex = Regex("""@Table\("(\w+)"\)""")
        return tableRegex.find(content)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("No @Table annotation found")
    }
    
    private fun extractFields(content: String): List<FieldModel> {
        val fields = mutableListOf<FieldModel>()
        
        // Extract the data class constructor
        val constructorRegex = Regex("""data\s+class\s+\w+\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)
        val constructorMatch = constructorRegex.find(content)
            ?: throw IllegalArgumentException("Could not parse data class constructor")
        
        val constructorContent = constructorMatch.groupValues[1]
        
        // Split by comma, but be careful with nested generics
        val fieldDeclarations = splitFields(constructorContent)
        
        fieldDeclarations.forEach { fieldDecl ->
            val field = parseField(fieldDecl.trim())
            if (field != null) {
                fields.add(field)
            }
        }
        
        return fields
    }
    
    private fun splitFields(content: String): List<String> {
        val fields = mutableListOf<String>()
        var currentField = StringBuilder()
        var depth = 0
        
        content.forEach { char ->
            when (char) {
                '<' -> {
                    depth++
                    currentField.append(char)
                }
                '>' -> {
                    depth--
                    currentField.append(char)
                }
                ',' -> {
                    if (depth == 0) {
                        fields.add(currentField.toString())
                        currentField = StringBuilder()
                    } else {
                        currentField.append(char)
                    }
                }
                else -> currentField.append(char)
            }
        }
        
        if (currentField.isNotEmpty()) {
            fields.add(currentField.toString())
        }
        
        return fields
    }
    
    private fun parseField(fieldDecl: String): FieldModel? {
        // Remove annotations and extract field info
        val cleanDecl = fieldDecl.lines()
            .filter { !it.trim().startsWith("@") || it.contains("@Id") }
            .joinToString(" ")
        
        val isId = cleanDecl.contains("@Id")
        
        // Parse: val fieldName: Type = defaultValue
        // Handle both regular identifiers and backtick-escaped identifiers (e.g., `class`)
        val fieldRegex = Regex("""val\s+(`?\w+`?):\s*([\w.<>?]+)(?:\s*=\s*(.+))?""")
        val match = fieldRegex.find(cleanDecl) ?: return null
        
        val rawFieldName = match.groupValues[1]
        val needsBackticks = rawFieldName.startsWith("`") && rawFieldName.endsWith("`")
        val fieldName = rawFieldName.removeSurrounding("`")
        val fieldType = match.groupValues[2].trim()
        val defaultValue = match.groupValues.getOrNull(3)?.trim()
        
        val isNullable = fieldType.endsWith("?")
        val baseType = fieldType.removeSuffix("?")
        
        return FieldModel(
            name = fieldName,
            type = baseType,
            isNullable = isNullable,
            defaultValue = defaultValue,
            isId = isId,
            needsBackticks = needsBackticks
        )
    }
}
