package com.edgerush.lootman.domain.recruitment

interface RecruitmentRepository {
    fun findById(id: String): RecruitmentApplication?
    fun findByGuildId(guildId: String, status: RecruitmentStatus? = null): List<RecruitmentApplication>
    fun save(application: RecruitmentApplication): RecruitmentApplication
    fun addComment(comment: RecruitmentComment): RecruitmentComment
    fun getComments(applicationId: String): List<RecruitmentComment>
}
