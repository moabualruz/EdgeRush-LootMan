package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditWishlistRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditWishlistRawRepository : CrudRepository<WoWAuditWishlistRawEntity, Long>

