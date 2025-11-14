package com.edgerush.tools.crudgen

import com.edgerush.tools.crudgen.generator.*
import com.edgerush.tools.crudgen.model.GeneratorConfig
import com.edgerush.tools.crudgen.parser.EntityParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import java.io.File

class CrudGeneratorCli : CliktCommand(name = "crud-generator", help = "Generate CRUD REST APIs from entity classes") {
    init {
        subcommands(GenerateCommand(), GenerateBatchCommand())
    }

    override fun run() = Unit
}

class GenerateCommand : CliktCommand(name = "generate", help = "Generate CRUD API for a single entity") {
    private val entityPath by option("--entity", "-e", help = "Path to entity file").required()
    private val outputPath by option("--output", "-o", help = "Output directory").required()
    private val configPath by option("--config", "-c", help = "Configuration file path").default("")
    private val basePackage by option("--package", "-p", help = "Base package name").default("com.example")

    override fun run() {
        echo("Generating CRUD API for entity: $entityPath")
        
        val config = if (configPath.isNotEmpty()) {
            GeneratorConfig.fromFile(File(configPath))
        } else {
            GeneratorConfig(basePackage = basePackage)
        }

        val entityFile = File(entityPath)
        if (!entityFile.exists()) {
            echo("Error: Entity file not found: $entityPath", err = true)
            return
        }

        val parser = EntityParser()
        val entityModel = parser.parse(entityFile)

        echo("Parsed entity: ${entityModel.name}")
        echo("Fields: ${entityModel.fields.size}")

        val outputDir = File(outputPath)
        val generator = CrudApiGenerator(config, outputDir)
        
        generator.generate(entityModel)

        echo("✓ Generated CRUD API successfully!")
        echo("  - Request DTOs")
        echo("  - Response DTO")
        echo("  - Mapper")
        echo("  - Service")
        echo("  - Controller")
    }
}

class GenerateBatchCommand : CliktCommand(name = "generate-batch", help = "Generate CRUD APIs for multiple entities") {
    private val entitiesDir by option("--entities-dir", "-d", help = "Directory containing entity files").required()
    private val outputPath by option("--output", "-o", help = "Output directory").required()
    private val configPath by option("--config", "-c", help = "Configuration file path").default("")
    private val basePackage by option("--package", "-p", help = "Base package name").default("com.example")

    override fun run() {
        echo("Generating CRUD APIs for entities in: $entitiesDir")
        
        val config = if (configPath.isNotEmpty()) {
            GeneratorConfig.fromFile(File(configPath))
        } else {
            GeneratorConfig(basePackage = basePackage)
        }

        val entitiesDirectory = File(entitiesDir)
        if (!entitiesDirectory.exists() || !entitiesDirectory.isDirectory) {
            echo("Error: Entities directory not found: $entitiesDir", err = true)
            return
        }

        val entityFiles = entitiesDirectory.walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name.endsWith("Entity.kt") }
            .toList()

        echo("Found ${entityFiles.size} entity files")

        val parser = EntityParser()
        val outputDir = File(outputPath)
        val generator = CrudApiGenerator(config, outputDir)

        var successCount = 0
        var failCount = 0

        entityFiles.forEach { file ->
            try {
                echo("\nProcessing: ${file.name}")
                val entityModel = parser.parse(file)
                generator.generate(entityModel)
                echo("  ✓ Generated successfully")
                successCount++
            } catch (e: Exception) {
                echo("  ✗ Failed: ${e.message}", err = true)
                failCount++
            }
        }

        echo("\n" + "=".repeat(50))
        echo("Generation complete!")
        echo("  Success: $successCount")
        echo("  Failed: $failCount")
        echo("  Total: ${entityFiles.size}")
    }
}

fun main(args: Array<String>) = CrudGeneratorCli().main(args)
