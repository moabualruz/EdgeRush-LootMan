// User and Auth types
export interface User {
  id: number
  discordId?: string
  battlenetId?: string
  username: string
  email?: string
  role: UserRole
  guildId?: string
  createdAt?: string
  lastLogin?: string
  linkedCharacters: CharacterLink[]
}

export type UserRole = 'RAIDER' | 'OFFICER' | 'ADMIN'

export interface CharacterLink {
  characterName: string
  realm: string
  isPrimary: boolean
  raiderId?: number
}

export interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
}

// FLPS types
export interface FlpsScore {
  raiderId: number
  characterName: string
  characterClass: CharacterClass
  role: Role
  flps: number
  rms: RmsBreakdown
  ipi: IpiBreakdown
  rdf: number
  eligible: boolean
  ineligibilityReasons?: string[]
  rank?: number
}

export interface RmsBreakdown {
  value: number
  acs: number
  mas: number
  eps: number
}

export interface IpiBreakdown {
  value: number
  uv: number
  tierBonus: number
  roleMultiplier: number
}

export interface FlpsReport {
  guildId: string
  raiders: FlpsScore[]
  generatedAt: string
}

// Leaderboard types
export interface LeaderboardEntry {
  rank: number
  raiderId: number
  characterName: string
  characterClass: CharacterClass
  role: Role
  flps: number
  eligible: boolean
}

export interface LeaderboardResponse {
  guildId: string
  entries: LeaderboardEntry[]
  total: number
  limit: number
  offset: number
  filters: {
    role: string | null
    characterClass: string | null
    eligible: boolean | null
  }
}

// Loot types
export interface LootAward {
  id: number
  itemId: number
  itemName: string
  raiderId: number
  characterName: string
  awardedAt: string
  flpsAtAward: number
  rdfExpired: boolean
  rdfExpiresAt?: string
}

export interface LootHistoryResponse {
  raiderId: number
  characterName: string
  awards: LootAward[]
}

export interface AwardLootRequest {
  raiderId: number
  itemId: number
  itemName: string
  raidId?: number
  notes?: string
}

export interface UpdateLootRequest {
  itemName?: string
  notes?: string
}

export interface WowItem {
  id: number
  name: string
  iconUrl?: string
  quality: 'POOR' | 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY'
}

// Wishlist types
export interface WishlistItem {
  itemId: number
  itemName: string
  slot: string
  upgradeValue: number
  simulationSource: 'RAIDBOTS' | 'WISHLIST_PERCENTAGE'
  lastSimulatedAt?: string
  isStale: boolean
}

// Performance types
export interface PerformanceMetrics {
  raiderId: number
  characterName: string
  dpa: number
  adt: number
  specAverage: number
  performanceTrend: PerformanceDataPoint[]
  lastUpdated: string
}

export interface PerformanceDataPoint {
  date: string
  dpa: number
  adt: number
}

// Admin types
export interface BehavioralAction {
  id: number
  raiderId: number
  characterName: string
  actionType: 'PENALTY' | 'BONUS'
  reason: string
  flpsModifier: number
  startDate: string
  endDate?: string
  createdBy: string
  active: boolean
}

export interface LootBan {
  id: number
  raiderId: number
  characterName: string
  reason: string
  startDate: string
  endDate?: string
  createdBy: string
  active: boolean
}

export interface FlpsConfig {
  rmsWeights: {
    attendance: number
    mechanical: number
    preparation: number
  }
  ipiWeights: {
    upgradeValue: number
    tierBonus: number
    roleMultiplier: number
  }
  roleMultipliers: {
    dps: number
    tank: number
    healer: number
  }
  thresholds: {
    eligibilityAttendance: number
    eligibilityActivity: number
  }
}

// Enums
export type CharacterClass =
  | 'WARRIOR'
  | 'PALADIN'
  | 'HUNTER'
  | 'ROGUE'
  | 'PRIEST'
  | 'DEATH_KNIGHT'
  | 'SHAMAN'
  | 'MAGE'
  | 'WARLOCK'
  | 'MONK'
  | 'DRUID'
  | 'DEMON_HUNTER'
  | 'EVOKER'

export type Role = 'TANK' | 'HEALER' | 'DPS'

// API response wrapper
export interface ApiResponse<T> {
  data: T
  message?: string
  timestamp: string
}

export interface ApiError {
  status: number
  message: string
  details?: string
}

// Guild Context types
export interface GuildContext {
  guildId: string
  guildName: string
  characterName: string
  characterRealm: string
  characterClass: string
  characterMappingId: number
  raiderId: number
  rank: string | null
  permissions: GuildPermissionType[]
  isActive: boolean
}

export type GuildPermissionType =
  | 'SETTINGS_ACCESS'
  | 'LOOT_MANAGEMENT'
  | 'MEMBER_MANAGEMENT'
  | 'VIEW_ALL_SCORES'

export interface GuildPermission {
  id: number
  guildId: string
  rankName: string
  permissionType: string
  createdAt: string
}

export interface PermissionTypeInfo {
  name: string
  description: string
}
