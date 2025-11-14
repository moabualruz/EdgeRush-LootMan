package com.edgerush.datasync.application.applications

import com.edgerush.datasync.domain.applications.model.Application
import com.edgerush.datasync.domain.applications.repository.ApplicationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use case for withdrawing a guild application
 */
@Service
class WithdrawApplicationUseCase(
    private val applicationRepository: ApplicationRepository
) {

    @Transactional
    fun execute(command: WithdrawApplicationCommand): Result<Application> = runCatching {
        val application = applicationRepository.findById(command.applicationId)
            ?: throw ApplicationNotFoundException(command.applicationId)

        val withdrawn = application.withdraw()
        applicationRepository.save(withdrawn)
    }
}
