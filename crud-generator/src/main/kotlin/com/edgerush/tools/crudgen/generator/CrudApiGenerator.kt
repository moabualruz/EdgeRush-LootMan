package com.edgerush.tools.crudgen.generator

import com.edgerush.tools.crudgen.model.EntityModel
import com.edgerush.tools.crudgen.model.GeneratorConfig
import java.io.File

class CrudApiGenerator(
    private val config: GeneratorConfig,
    private val outputDir: File
) {
    private val dtoGenerator = DtoGenerator(config)
    private val mapperGenerator = MapperGenerator(config)
    private val serviceGenerator = ServiceGenerator(config)
    private val controllerGenerator = ControllerGenerator(config)
    private val repositoryUpdater = RepositoryUpdater(config)
    
    fun generate(entity: EntityModel) {
        println("Generating CRUD API for: ${entity.name}")
        
        // Generate Request DTOs
        val requestDtos = dtoGenerator.generateRequestDtos(entity)
        writeFile(
            outputDir,
            "${config.basePackage.replace('.', '/')}/api/dto/request",
            "${entity.requestName}.kt",
            requestDtos
        )
        
        // Generate Response DTO
        val responseDto = dtoGenerator.generateResponseDto(entity)
        writeFile(
            outputDir,
            "${config.basePackage.replace('.', '/')}/api/dto/response",
            "${entity.responseName}.kt",
            responseDto
        )
        
        // Generate Mapper
        val mapper = mapperGenerator.generate(entity)
        writeFile(
            outputDir,
            "${config.basePackage.replace('.', '/')}/service/mapper",
            "${entity.mapperName}.kt",
            mapper
        )
        
        // Generate Service
        val service = serviceGenerator.generate(entity)
        writeFile(
            outputDir,
            "${config.basePackage.replace('.', '/')}/service/crud",
            "${entity.serviceName}.kt",
            service
        )
        
        // Generate Controller
        val controller = controllerGenerator.generate(entity)
        writeFile(
            outputDir,
            "${config.basePackage.replace('.', '/')}/api/${config.apiVersion}",
            "${entity.controllerName}.kt",
            controller
        )
        
        println("✓ Generated all files for ${entity.name}")
    }
    
    private fun writeFile(baseDir: File, packagePath: String, fileName: String, content: String) {
        val dir = File(baseDir, packagePath)
        dir.mkdirs()
        
        val file = File(dir, fileName)
        file.writeText(content)
        
        println("  Created: ${file.relativeTo(baseDir).path}")
    }
}
