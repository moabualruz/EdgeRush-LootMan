package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.ApplicationEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository :
    CrudRepository<ApplicationEntity, Long>,
    PagingAndSortingRepository<ApplicationEntity, Long> {
    fun findByStatus(status: String): List<ApplicationEntity>
}
