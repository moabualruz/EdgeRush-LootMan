package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.applications.model.Application
import com.edgerush.datasync.domain.applications.model.ApplicationId
import com.edgerush.datasync.domain.applications.model.ApplicationStatus
import com.edgerush.datasync.domain.applications.repository.ApplicationRepository
import com.edgerush.datasync.entity.ApplicationAltEntity
import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import com.edgerush.datasync.infrastructure.persistence.mapper.ApplicationAltMapper
import com.edgerush.datasync.infrastructure.persistence.mapper.ApplicationMapper
import com.edgerush.datasync.infrastructure.persistence.mapper.ApplicationQuestionMapper
import com.edgerush.datasync.repository.ApplicationAltRepository
import com.edgerush.datasync.repository.ApplicationQuestionFileRepository
import com.edgerush.datasync.repository.ApplicationQuestionRepository
import org.springframework.stereotype.Repository
import com.edgerush.datasync.repository.ApplicationRepository as SpringApplicationRepository

/**
 * JDBC implementation of ApplicationRepository
 */
@Repository
class JdbcApplicationRepository(
    private val springRepository: SpringApplicationRepository,
    private val altRepository: ApplicationAltRepository,
    private val questionRepository: ApplicationQuestionRepository,
    private val questionFileRepository: ApplicationQuestionFileRepository,
    private val mapper: ApplicationMapper,
    private val altMapper: ApplicationAltMapper,
    private val questionMapper: ApplicationQuestionMapper
) : ApplicationRepository {

    override fun findById(id: ApplicationId): Application? {
        val entity = springRepository.findById(id.value).orElse(null) ?: return null
        
        val alts = altRepository.findAll()
            .filter { it.applicationId == id.value }
            .map { altMapper.toDomain(it) }
        
        val questionEntities = questionRepository.findAll()
            .filter { it.applicationId == id.value }
        
        val questions = questionEntities.map { questionEntity ->
            val files = questionFileRepository.findAll()
                .filter { it.applicationId == id.value && it.questionPosition == questionEntity.position }
            questionMapper.toDomain(questionEntity, files)
        }
        
        return mapper.toDomain(entity, alts, questions)
    }

    override fun findByStatus(status: ApplicationStatus): List<Application> {
        val entities = springRepository.findByStatus(status.name)
        return entities.map { entity ->
            val alts = altRepository.findAll()
                .filter { it.applicationId == entity.applicationId }
                .map { altMapper.toDomain(it) }
            
            val questionEntities = questionRepository.findAll()
                .filter { it.applicationId == entity.applicationId }
            
            val questions = questionEntities.map { questionEntity ->
                val files = questionFileRepository.findAll()
                    .filter { it.applicationId == entity.applicationId && it.questionPosition == questionEntity.position }
                questionMapper.toDomain(questionEntity, files)
            }
            
            mapper.toDomain(entity, alts, questions)
        }
    }

    override fun findAll(): List<Application> {
        val entities = springRepository.findAll()
        return entities.map { entity ->
            val alts = altRepository.findAll()
                .filter { it.applicationId == entity.applicationId }
                .map { altMapper.toDomain(it) }
            
            val questionEntities = questionRepository.findAll()
                .filter { it.applicationId == entity.applicationId }
            
            val questions = questionEntities.map { questionEntity ->
                val files = questionFileRepository.findAll()
                    .filter { it.applicationId == entity.applicationId && it.questionPosition == questionEntity.position }
                questionMapper.toDomain(questionEntity, files)
            }
            
            mapper.toDomain(entity, alts, questions)
        }
    }

    override fun save(application: Application): Application {
        // Save main application entity
        val entity = mapper.toEntity(application)
        springRepository.save(entity)
        
        // Delete existing alts and questions
        altRepository.findAll()
            .filter { it.applicationId == application.id.value }
            .forEach { altRepository.delete(it) }
        
        questionRepository.findAll()
            .filter { it.applicationId == application.id.value }
            .forEach { questionRepository.delete(it) }
        
        questionFileRepository.findAll()
            .filter { it.applicationId == application.id.value }
            .forEach { questionFileRepository.delete(it) }
        
        // Save alts
        application.altCharacters.forEach { alt ->
            val altEntity = altMapper.toEntity(alt, application.id.value)
            altRepository.save(altEntity)
        }
        
        // Save questions and files
        application.questions.forEach { question ->
            val questionEntity = questionMapper.toEntity(question, application.id.value)
            questionRepository.save(questionEntity)
            
            question.files.forEach { file ->
                val fileEntity = questionMapper.toFileEntity(
                    file, 
                    application.id.value, 
                    question.position, 
                    question.question
                )
                questionFileRepository.save(fileEntity)
            }
        }
        
        return application
    }

    override fun delete(id: ApplicationId) {
        springRepository.deleteById(id.value)
    }
}
