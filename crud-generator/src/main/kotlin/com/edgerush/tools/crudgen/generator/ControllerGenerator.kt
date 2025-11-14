package com.edgerush.tools.crudgen.generator

import com.edgerush.tools.crudgen.model.EntityModel
import com.edgerush.tools.crudgen.model.GeneratorConfig

class ControllerGenerator(private val config: GeneratorConfig) {
    
    fun generate(entity: EntityModel): String {
        val packageName = "${config.basePackage}.api.${config.apiVersion}"
        
        return buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import ${config.basePackage}.api.dto.request.Create${entity.entityNameWithoutSuffix}Request")
            appendLine("import ${config.basePackage}.api.dto.request.Update${entity.entityNameWithoutSuffix}Request")
            appendLine("import ${config.basePackage}.api.dto.response.${entity.responseName}")
            appendLine("import ${config.basePackage}.service.crud.${entity.serviceName}")
            
            if (config.openApiDocumentation) {
                appendLine("import io.swagger.v3.oas.annotations.Operation")
                appendLine("import io.swagger.v3.oas.annotations.tags.Tag")
            }
            
            appendLine("import org.springframework.data.domain.Page")
            appendLine("import org.springframework.data.domain.Pageable")
            appendLine("import ${entity.packageName}.${entity.name}")
            appendLine("import org.springframework.web.bind.annotation.*")
            appendLine()
            appendLine("@RestController")
            appendLine("@RequestMapping(\"/api/${config.apiVersion}/${entity.apiPath}\")")
            
            if (config.openApiDocumentation) {
                appendLine("@Tag(name = \"${entity.entityNameWithoutSuffix}\", description = \"Manage ${entity.entityNameWithoutSuffix.lowercase()} entities\")")
            }
            
            appendLine("class ${entity.controllerName}(")
            appendLine("    service: ${entity.serviceName}")
            appendLine(") : BaseCrudController<${entity.name}, ${entity.idField.type}, Create${entity.entityNameWithoutSuffix}Request, Update${entity.entityNameWithoutSuffix}Request, ${entity.responseName}>(service) {")
            appendLine("    // Custom endpoints can be added here manually as needed")
            appendLine("}")
        }
    }
}
