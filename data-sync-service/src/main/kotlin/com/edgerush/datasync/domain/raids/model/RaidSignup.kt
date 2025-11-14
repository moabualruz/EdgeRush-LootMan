package com.edgerush.datasync.domain.raids.model

/**
 * Entity representing a raider's signup for a raid.
 */
data class RaidSignup private constructor(
    val id: Long?,
    val raiderId: RaiderId,
    val role: RaidRole,
    val status: SignupStatus,
    val comment: String?,
    val selected: Boolean
) {
    enum class SignupStatus {
        CONFIRMED,
        TENTATIVE,
        DECLINED,
        LATE;
        
        companion object {
            fun fromString(value: String?): SignupStatus? {
                return when (value?.uppercase()) {
                    "CONFIRMED" -> CONFIRMED
                    "TENTATIVE" -> TENTATIVE
                    "DECLINED" -> DECLINED
                    "LATE" -> LATE
                    else -> null
                }
            }
        }
    }
    
    companion object {
        fun create(
            raiderId: RaiderId,
            role: RaidRole,
            status: SignupStatus = SignupStatus.CONFIRMED,
            comment: String? = null
        ): RaidSignup {
            return RaidSignup(
                id = null,
                raiderId = raiderId,
                role = role,
                status = status,
                comment = comment,
                selected = false
            )
        }
        
        fun reconstitute(
            id: Long,
            raiderId: RaiderId,
            role: RaidRole,
            status: SignupStatus,
            comment: String?,
            selected: Boolean
        ): RaidSignup {
            return RaidSignup(
                id = id,
                raiderId = raiderId,
                role = role,
                status = status,
                comment = comment,
                selected = selected
            )
        }
    }
    
    fun select(): RaidSignup {
        require(status == SignupStatus.CONFIRMED) { "Can only select confirmed signups" }
        return copy(selected = true)
    }
    
    fun deselect(): RaidSignup = copy(selected = false)
    
    fun updateStatus(newStatus: SignupStatus): RaidSignup = copy(status = newStatus)
    
    fun updateComment(newComment: String?): RaidSignup = copy(comment = newComment)
    
    fun isConfirmed(): Boolean = status == SignupStatus.CONFIRMED
}
