package com.edgerush.datasync.model

import java.time.LocalDate

data class LootAward(
    val tier: LootTier,
    val awardedOn: LocalDate,
)
