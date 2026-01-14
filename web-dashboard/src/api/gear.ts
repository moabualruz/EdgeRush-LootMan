import { api } from './client'

export interface GearItem {
  id: number
  itemId: number
  itemName: string
  slot: GearSlot
  itemLevel: number
  quality: ItemQuality
  enchantId?: number
  enchantName?: string
  gems: GemInfo[]
  socketCount: number
  isTierPiece: boolean
  upgradeLevel?: number
  maxUpgradeLevel?: number
}

export type GearSlot =
  | 'HEAD'
  | 'NECK'
  | 'SHOULDER'
  | 'BACK'
  | 'CHEST'
  | 'WRIST'
  | 'HANDS'
  | 'WAIST'
  | 'LEGS'
  | 'FEET'
  | 'FINGER_1'
  | 'FINGER_2'
  | 'TRINKET_1'
  | 'TRINKET_2'
  | 'MAIN_HAND'
  | 'OFF_HAND'

export type ItemQuality = 'POOR' | 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY' | 'ARTIFACT'

export interface GemInfo {
  gemId: number
  gemName: string
  color: string
}

export interface VaultSlot {
  id: number
  raiderId: number
  slotType: 'RAID' | 'MYTHIC_PLUS' | 'PVP'
  slotIndex: number
  itemId?: number
  itemName?: string
  itemLevel?: number
  isSelected: boolean
  unlocked: boolean
  progress: number
  progressRequired: number
}

export interface RaiderGear {
  raiderId: number
  characterName: string
  averageItemLevel: number
  equippedItemLevel: number
  items: GearItem[]
  missingEnchants: GearSlot[]
  missingGems: GearSlot[]
  tierPieceCount: number
  lastUpdated: string
}

export interface VaultOptions {
  raiderId: number
  weekOf: string
  raid: VaultSlot[]
  mythicPlus: VaultSlot[]
  pvp: VaultSlot[]
}

export const gearApi = {
  async getMyGear(guildId: string): Promise<RaiderGear> {
    const response = await api.get<RaiderGear>(`/api/v1/gear/guilds/${guildId}/me`)
    return response.data
  },

  async getRaiderGear(raiderId: number): Promise<RaiderGear> {
    const response = await api.get<RaiderGear>(`/api/raider-gear-items/raider/${raiderId}`)
    return response.data
  },

  async getMyVaultOptions(guildId: string): Promise<VaultOptions> {
    const response = await api.get<VaultOptions>(`/api/v1/vault/guilds/${guildId}/me`)
    return response.data
  },

  async getVaultOptions(raiderId: number): Promise<VaultOptions> {
    const response = await api.get<VaultOptions>(`/api/raider-vault-slots/raider/${raiderId}`)
    return response.data
  },
}
