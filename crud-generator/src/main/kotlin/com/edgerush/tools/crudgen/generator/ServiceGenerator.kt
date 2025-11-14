package com.edgerush.tools.crudgen.generator

import com.edgerush.tools.crudgen.model.EntityModel
import com.edgerush.tools.crudgen.model.GeneratorConfig

class ServiceGenerator(private val config: GeneratorConfig) {
    
    fun generate(entity: EntityModel): String {
        val packageName = "${config.basePackage}.service.crud"
        
        return buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import ${config.basePackage}.api.dto.request.Create${entity.entityNameWithoutSuffix}Request")
            appendLine("import ${config.basePackage}.api.dto.request.Update${entity.entityNameWithoutSuffix}Request")
            appendLine("import ${config.basePackage}.api.dto.response.${entity.responseName}")
            appendLine("import ${entity.packageName}.${entity.name}")
            appendLine("import ${entity.packageName.replace(".entity", ".repository")}.${entity.repositoryName}")
            appendLine("import ${config.basePackage}.service.mapper.${entity.mapperName}")
            appendLine("import ${config.basePackage}.security.AuthenticatedUser")
            appendLine("import ${config.basePackage}.api.exception.ResourceNotFoundException")
            
            if (config.enableAuditLogging) {
                appendLine("import ${config.basePackage}.service.AuditLogger")
            }
            
            appendLine("import org.springframework.data.domain.Page")
            appendLine("import org.springframework.data.domain.Pageable")
            appendLine("import org.springframework.stereotype.Service")
            appendLine("import org.springframework.transaction.annotation.Transactional")
            appendLine()
            appendLine("@Service")
            append("class ${entity.serviceName}(")
            appendLine()
            appendLine("    private val repository: ${entity.repositoryName},")
            appendLine("    private val mapper: ${entity.mapperName}")
            if (config.enableAuditLogging) {
                appendLine(",")
                appendLine("    private val auditLogger: AuditLogger")
            } else {
                appendLine()
            }
            appendLine(") : CrudService<${entity.name}, ${entity.idField.type}, Create${entity.entityNameWithoutSuffix}Request, Update${entity.entityNameWithoutSuffix}Request, ${entity.responseName}> {")
            appendLine()
            
            // findAll
            appendLine("    override fun findAll(pageable: Pageable): Page<${entity.responseName}> {")
            appendLine("        val allEntities = repository.findAll().toList()")
            appendLine("        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(allEntities.size)")
            appendLine("        val end = (start + pageable.pageSize).coerceAtMost(allEntities.size)")
            appendLine("        val pageContent = allEntities.subList(start, end)")
            appendLine("        return org.springframework.data.domain.PageImpl(")
            appendLine("            pageContent.map(mapper::toResponse),")
            appendLine("            pageable,")
            appendLine("            allEntities.size.toLong()")
            appendLine("        )")
            appendLine("    }")
            appendLine()
            
            // findById
            appendLine("    override fun findById(id: ${entity.idField.type}): ${entity.responseName} {")
            appendLine("        val entity = repository.findById(id)")
            appendLine("            .orElseThrow { ResourceNotFoundException(\"${entity.entityNameWithoutSuffix} not found with id: \$id\") }")
            appendLine("        return mapper.toResponse(entity)")
            appendLine("    }")
            appendLine()
            
            // create
            appendLine("    @Transactional")
            appendLine("    override fun create(request: Create${entity.entityNameWithoutSuffix}Request, user: AuthenticatedUser): ${entity.responseName} {")
            appendLine("        val entity = mapper.toEntity(request)")
            appendLine("        val saved = repository.save(entity)")
            appendLine()
            if (config.enableAuditLogging) {
                appendLine("        auditLogger.logCreate(")
                appendLine("            entityType = \"${entity.entityNameWithoutSuffix}\",")
                appendLine("            entityId = saved.${entity.idField.name}!!,")
                appendLine("            user = user")
                appendLine("        )")
                appendLine()
            }
            appendLine("        return mapper.toResponse(saved)")
            appendLine("    }")
            appendLine()
            
            // update
            appendLine("    @Transactional")
            appendLine("    override fun update(id: ${entity.idField.type}, request: Update${entity.entityNameWithoutSuffix}Request, user: AuthenticatedUser): ${entity.responseName} {")
            appendLine("        val existing = repository.findById(id)")
            appendLine("            .orElseThrow { ResourceNotFoundException(\"${entity.entityNameWithoutSuffix} not found with id: \$id\") }")
            appendLine("        val updated = mapper.updateEntity(existing, request)")
            appendLine("        val saved = repository.save(updated)")
            appendLine()
            if (config.enableAuditLogging) {
                appendLine("        auditLogger.logUpdate(")
                appendLine("            entityType = \"${entity.entityNameWithoutSuffix}\",")
                appendLine("            entityId = id,")
                appendLine("            user = user")
                appendLine("        )")
                appendLine()
            }
            appendLine("        return mapper.toResponse(saved)")
            appendLine("    }")
            appendLine()
            
            // delete
            appendLine("    @Transactional")
            appendLine("    override fun delete(id: ${entity.idField.type}, user: AuthenticatedUser) {")
            appendLine("        if (!repository.existsById(id)) {")
            appendLine("            throw ResourceNotFoundException(\"${entity.entityNameWithoutSuffix} not found with id: \$id\")")
            appendLine("        }")
            appendLine()
            appendLine("        repository.deleteById(id)")
            appendLine()
            if (config.enableAuditLogging) {
                appendLine("        auditLogger.logDelete(")
                appendLine("            entityType = \"${entity.entityNameWithoutSuffix}\",")
                appendLine("            entityId = id,")
                appendLine("            user = user")
                appendLine("        )")
            }
            appendLine("    }")
            appendLine()
            
            // validateAccess
            appendLine("    override fun validateAccess(entity: ${entity.name}, user: AuthenticatedUser) {")
            appendLine("        // Add guild-based access control if needed")
            appendLine("    }")
            
            appendLine("}")
        }
    }
}
