package com.edgerush.datasync.domain.applications.service

import com.edgerush.datasync.domain.applications.model.Application
import com.edgerush.datasync.domain.applications.model.ApplicationStatus
import org.springframework.stereotype.Service

/**
 * Domain service for application review logic
 */
@Service
class ApplicationReviewService {
    
    /**
     * Determines if an application meets basic requirements for review
     */
    fun meetsBasicRequirements(application: Application): Boolean {
        // Check main character level
        val hasMaxLevelCharacter = application.mainCharacter.level?.let { it >= 70 } ?: false
        
        // Check if all questions are answered
        val hasAnsweredAllQuestions = application.questions.isNotEmpty() && 
                                      application.questions.all { it.answer.isNotBlank() }
        
        // Check if contact info is provided
        val hasContactInfo = !application.applicantInfo.battletag.isNullOrBlank() || 
                            !application.applicantInfo.discordId.isNullOrBlank()
        
        return hasMaxLevelCharacter && hasAnsweredAllQuestions && hasContactInfo
    }
    
    /**
     * Calculates a completeness score for an application (0.0 to 1.0)
     */
    fun calculateCompletenessScore(application: Application): Double {
        var score = 0.0
        var maxScore = 0.0
        
        // Main character info (30%)
        maxScore += 30.0
        if (application.mainCharacter.level != null && application.mainCharacter.level >= 70) {
            score += 30.0
        }
        
        // Questions answered (40%)
        maxScore += 40.0
        if (application.questions.isNotEmpty()) {
            val answeredQuestions = application.questions.count { it.answer.isNotBlank() }
            score += (answeredQuestions.toDouble() / application.questions.size) * 40.0
        }
        
        // Contact information (20%)
        maxScore += 20.0
        if (!application.applicantInfo.battletag.isNullOrBlank()) score += 10.0
        if (!application.applicantInfo.discordId.isNullOrBlank()) score += 10.0
        
        // Alt characters (10%)
        maxScore += 10.0
        if (application.altCharacters.isNotEmpty()) {
            score += 10.0
        }
        
        return if (maxScore > 0) score / maxScore else 0.0
    }
    
    /**
     * Determines if an application can be auto-approved based on criteria
     */
    fun canAutoApprove(application: Application): Boolean {
        // Auto-approval criteria:
        // 1. Meets basic requirements
        // 2. Has high completeness score
        // 3. Main character is max level
        return meetsBasicRequirements(application) &&
               calculateCompletenessScore(application) >= 0.9 &&
               application.mainCharacter.level?.let { it >= 80 } ?: false
    }
}
