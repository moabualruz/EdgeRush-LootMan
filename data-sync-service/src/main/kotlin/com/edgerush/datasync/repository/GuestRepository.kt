package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.GuestEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface GuestRepository : 
    CrudRepository<GuestEntity, Long>,
    PagingAndSortingRepository<GuestEntity, Long> {
    
    fun findByNameAndRealm(name: String, realm: String?): GuestEntity?
}
