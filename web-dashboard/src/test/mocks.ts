import { vi } from 'vitest'
import type { FlpsScore, WishlistItem, PerformanceMetrics, LootAward, LeaderboardEntry } from '@/types'
import type { WishlistResponse, SimulationStatus } from '@/api/wishlist'
import type { AttendanceReport, AttendanceRecord } from '@/api/attendance'
import type { Raid, RaidDetail, RaidSignup } from '@/api/raids'
import type { RaiderGear, VaultOptions } from '@/api/gear'
import type { WarcraftLogsEntry, WarcraftLogsReport } from '@/api/performance'

// Mock FLPS data
export const mockFlpsScore: FlpsScore = {
  raiderId: 1,
  characterName: 'TestCharacter',
  characterClass: 'WARRIOR',
  role: 'DPS',
  flps: 0.85,
  rms: {
    value: 0.9,
    acs: 0.95,
    mas: 0.88,
    eps: 0.87,
  },
  ipi: {
    value: 0.8,
    uv: 10.5,
    tierBonus: 0.1,
    roleMultiplier: 1.0,
  },
  rdf: 0.75,
  eligible: true,
  rank: 5,
}

// Mock wishlist data
export const mockWishlistItems: WishlistItem[] = [
  {
    itemId: 1001,
    itemName: 'Heroic Sword of Testing',
    slot: 'MAIN_HAND',
    upgradeValue: 12.5,
    simulationSource: 'RAIDBOTS',
    lastSimulatedAt: '2024-01-15T10:00:00Z',
    isStale: false,
  },
  {
    itemId: 1002,
    itemName: 'Epic Helm of Mocking',
    slot: 'HEAD',
    upgradeValue: 8.3,
    simulationSource: 'RAIDBOTS',
    lastSimulatedAt: '2024-01-10T10:00:00Z',
    isStale: true,
  },
  {
    itemId: 1003,
    itemName: 'Tier Chest',
    slot: 'CHEST',
    upgradeValue: 3.2,
    simulationSource: 'WISHLIST_PERCENTAGE',
    isStale: false,
  },
]

export const mockWishlistResponse: WishlistResponse = {
  raiderId: 1,
  characterName: 'TestCharacter',
  items: mockWishlistItems,
  lastSimulatedAt: '2024-01-15T10:00:00Z',
}

export const mockSimulationStatus: SimulationStatus = {
  raiderId: 1,
  status: 'COMPLETED',
  progress: 1.0,
  lastRunAt: '2024-01-15T10:00:00Z',
}

// Mock performance data
export const mockPerformanceMetrics: PerformanceMetrics = {
  raiderId: 1,
  characterName: 'TestCharacter',
  dpa: 125000,
  adt: 0.92,
  specAverage: 110000,
  performanceTrend: [
    { date: '2024-01-10', dpa: 120000, adt: 0.90 },
    { date: '2024-01-11', dpa: 122000, adt: 0.91 },
    { date: '2024-01-12', dpa: 125000, adt: 0.92 },
  ],
  lastUpdated: '2024-01-15T10:00:00Z',
}

export const mockWarcraftLogsReports: WarcraftLogsReport = {
  raiderId: 1,
  characterName: 'TestCharacter',
  reports: [
    {
      reportId: 'abc123',
      encounterId: 2820,
      encounterName: 'Test Boss',
      difficulty: 'Mythic',
      date: '2024-01-15',
      dps: 125000,
      ilvl: 489,
      spec: 'Arms',
      percentile: 85,
      deaths: 0,
    },
  ],
}

// Mock attendance data
export const mockAttendanceRecords: AttendanceRecord[] = [
  {
    id: 1,
    raiderId: 1,
    raidId: 100,
    raidName: 'Test Raid Night',
    raidDate: '2024-01-15T20:00:00Z',
    status: 'PRESENT',
  },
  {
    id: 2,
    raiderId: 1,
    raidId: 99,
    raidName: 'Test Raid Night',
    raidDate: '2024-01-12T20:00:00Z',
    status: 'LATE',
    notes: 'Traffic delay',
  },
]

export const mockAttendanceReport: AttendanceReport = {
  raiderId: 1,
  characterName: 'TestCharacter',
  totalRaids: 20,
  attendedRaids: 18,
  lateRaids: 2,
  excusedRaids: 1,
  attendanceRate: 0.9,
  lastRaidDate: '2024-01-15T20:00:00Z',
  streak: 5,
  records: mockAttendanceRecords,
}

// Mock raid data
export const mockRaids: Raid[] = [
  {
    id: 100,
    teamId: 1,
    teamName: 'Main Raid',
    instanceName: 'Test Instance',
    difficulty: 'MYTHIC',
    scheduledAt: '2024-01-20T20:00:00Z',
    status: 'SCHEDULED',
    signupCount: 22,
    maxPlayers: 25,
    description: 'Weekly progression raid',
  },
  {
    id: 99,
    teamId: 1,
    teamName: 'Main Raid',
    instanceName: 'Test Instance',
    difficulty: 'MYTHIC',
    scheduledAt: '2024-01-15T20:00:00Z',
    endedAt: '2024-01-15T23:30:00Z',
    status: 'COMPLETED',
    signupCount: 25,
    maxPlayers: 25,
  },
]

export const mockRaidSignups: RaidSignup[] = [
  {
    id: 1,
    raidId: 100,
    raiderId: 1,
    characterName: 'TestTank',
    role: 'TANK',
    status: 'CONFIRMED',
    signedUpAt: '2024-01-18T10:00:00Z',
  },
  {
    id: 2,
    raidId: 100,
    raiderId: 2,
    characterName: 'TestHealer',
    role: 'HEALER',
    status: 'CONFIRMED',
    signedUpAt: '2024-01-18T11:00:00Z',
  },
]

export const mockRaidDetail: RaidDetail = {
  ...mockRaids[0],
  encounters: [
    {
      id: 1,
      raidId: 100,
      encounterId: 2820,
      encounterName: 'Boss 1',
      status: 'KILLED',
      pullCount: 3,
      killedAt: '2024-01-15T20:30:00Z',
      duration: 300,
    },
  ],
  signups: mockRaidSignups,
}

// Mock gear data
export const mockRaiderGear: RaiderGear = {
  raiderId: 1,
  characterName: 'TestCharacter',
  averageItemLevel: 485.5,
  equippedItemLevel: 484.2,
  items: [
    {
      id: 1,
      itemId: 2001,
      itemName: 'Helm of Testing',
      slot: 'HEAD',
      itemLevel: 489,
      quality: 'EPIC',
      enchantId: 100,
      enchantName: 'Incandescent Essence',
      gems: [],
      socketCount: 0,
      isTierPiece: true,
    },
  ],
  missingEnchants: [],
  missingGems: [],
  tierPieceCount: 4,
  lastUpdated: '2024-01-15T10:00:00Z',
}

export const mockVaultOptions: VaultOptions = {
  raiderId: 1,
  weekOf: '2024-01-15',
  raid: [
    { id: 1, raiderId: 1, slotType: 'RAID', slotIndex: 0, unlocked: true, isSelected: false, progress: 2, progressRequired: 2 },
    { id: 2, raiderId: 1, slotType: 'RAID', slotIndex: 1, unlocked: false, isSelected: false, progress: 0, progressRequired: 4 },
    { id: 3, raiderId: 1, slotType: 'RAID', slotIndex: 2, unlocked: false, isSelected: false, progress: 0, progressRequired: 6 },
  ],
  mythicPlus: [
    { id: 4, raiderId: 1, slotType: 'MYTHIC_PLUS', slotIndex: 0, unlocked: true, isSelected: false, progress: 1, progressRequired: 1 },
    { id: 5, raiderId: 1, slotType: 'MYTHIC_PLUS', slotIndex: 1, unlocked: false, isSelected: false, progress: 0, progressRequired: 4 },
    { id: 6, raiderId: 1, slotType: 'MYTHIC_PLUS', slotIndex: 2, unlocked: false, isSelected: false, progress: 0, progressRequired: 8 },
  ],
  pvp: [
    { id: 7, raiderId: 1, slotType: 'PVP', slotIndex: 0, unlocked: false, isSelected: false, progress: 0, progressRequired: 1250 },
    { id: 8, raiderId: 1, slotType: 'PVP', slotIndex: 1, unlocked: false, isSelected: false, progress: 0, progressRequired: 2500 },
    { id: 9, raiderId: 1, slotType: 'PVP', slotIndex: 2, unlocked: false, isSelected: false, progress: 0, progressRequired: 5000 },
  ],
}

// Mock loot data
export const mockLootAwards: LootAward[] = [
  {
    id: 1,
    itemId: 1001,
    itemName: 'Epic Sword',
    raiderId: 1,
    characterName: 'TestCharacter',
    awardedAt: '2024-01-15T21:00:00Z',
    flpsAtAward: 0.85,
    rdfExpired: false,
    rdfExpiresAt: '2024-01-22T21:00:00Z',
  },
]

// Mock leaderboard data
export const mockLeaderboardEntries: LeaderboardEntry[] = [
  { rank: 1, raiderId: 10, characterName: 'TopPlayer', characterClass: 'MAGE', role: 'DPS', flps: 0.95, eligible: true },
  { rank: 2, raiderId: 11, characterName: 'SecondPlace', characterClass: 'WARRIOR', role: 'TANK', flps: 0.92, eligible: true },
  { rank: 3, raiderId: 1, characterName: 'TestCharacter', characterClass: 'WARRIOR', role: 'DPS', flps: 0.85, eligible: true },
]

// Mock API functions
export function createMockApi() {
  return {
    flpsApi: {
      getMyFlps: vi.fn().mockResolvedValue(mockFlpsScore),
      getLeaderboard: vi.fn().mockResolvedValue({ guildId: 'test', entries: mockLeaderboardEntries, totalRaiders: 25 }),
    },
    wishlistApi: {
      getMyWishlist: vi.fn().mockResolvedValue(mockWishlistResponse),
      getSimulationStatus: vi.fn().mockResolvedValue(mockSimulationStatus),
      triggerSimulation: vi.fn().mockResolvedValue({ ...mockSimulationStatus, status: 'QUEUED' }),
    },
    performanceApi: {
      getMyPerformance: vi.fn().mockResolvedValue(mockPerformanceMetrics),
      getWarcraftLogsReports: vi.fn().mockResolvedValue(mockWarcraftLogsReports),
    },
    attendanceApi: {
      getMyAttendance: vi.fn().mockResolvedValue(mockAttendanceReport),
    },
    raidsApi: {
      getUpcomingRaids: vi.fn().mockResolvedValue(mockRaids.filter(r => r.status === 'SCHEDULED')),
      getPastRaids: vi.fn().mockResolvedValue(mockRaids.filter(r => r.status === 'COMPLETED')),
      getRaidById: vi.fn().mockResolvedValue(mockRaidDetail),
    },
    gearApi: {
      getMyGear: vi.fn().mockResolvedValue(mockRaiderGear),
      getMyVaultOptions: vi.fn().mockResolvedValue(mockVaultOptions),
    },
    lootApi: {
      getMyLootHistory: vi.fn().mockResolvedValue({ raiderId: 1, characterName: 'TestCharacter', awards: mockLootAwards }),
    },
  }
}
