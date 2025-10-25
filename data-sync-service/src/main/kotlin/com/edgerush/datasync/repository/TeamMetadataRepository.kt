package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.TeamMetadataEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamMetadataRepository : CrudRepository<TeamMetadataEntity, Long>
