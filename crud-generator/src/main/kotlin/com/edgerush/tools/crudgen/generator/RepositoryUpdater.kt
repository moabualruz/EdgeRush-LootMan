package com.edgerush.tools.crudgen.generator

import com.edgerush.tools.crudgen.model.EntityModel
import com.edgerush.tools.crudgen.model.GeneratorConfig

class RepositoryUpdater(private val config: GeneratorConfig) {
    
    fun generateRepositoryUpdate(entity: EntityModel): String {
        val packageName = entity.packageName.replace(".entity", ".repository")
        
        return buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import ${entity.packageName}.${entity.name}")
            appendLine("import org.springframework.data.domain.Page")
            appendLine("import org.springframework.data.domain.Pageable")
            appendLine("import org.springframework.data.repository.CrudRepository")
            appendLine("import org.springframework.data.repository.PagingAndSortingRepository")
            appendLine("import org.springframework.stereotype.Repository")
            appendLine()
            appendLine("@Repository")
            appendLine("interface ${entity.repositoryName} : CrudRepository<${entity.name}, ${entity.idField.type}>, PagingAndSortingRepository<${entity.name}, ${entity.idField.type}> {")
            
            // Add custom methods
            if (entity.hasGuildId()) {
                appendLine("    fun findByGuildId(guildId: String, pageable: Pageable): Page<${entity.name}>")
            }
            
            appendLine("}")
        }
    }
}
