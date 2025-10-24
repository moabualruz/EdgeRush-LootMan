package com.edgerush.datasync.service

import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.datasync.repository.SyncRunRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class SyncRunService(
    private val repository: SyncRunRepository
) {

    fun startRun(source: String): SyncRunEntity = repository.save(
        SyncRunEntity(
            source = source,
            status = "RUNNING",
            startedAt = OffsetDateTime.now(),
            completedAt = null,
            message = null
        )
    )

    fun complete(run: SyncRunEntity, status: String, message: String? = null) {
        repository.save(
            run.copy(
                status = status,
                completedAt = OffsetDateTime.now(),
                message = message
            )
        )
    }
}
