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
    // Backend returns GearSetResponse with different field names
    // We transform it into the RaiderGear shape the UI expects
    const response = await api.get<any>(`/v1/gear/guilds/${guildId}/me`)
    const raw = response.data

    const items: GearItem[] = (raw.items ?? []).map((item: any) => ({
      id: item.id ?? item.itemId ?? 0,
      itemId: item.itemId ?? 0,
      itemName: item.itemName ?? item.name ?? 'Unknown Item',
      slot: item.slot ?? 'UNKNOWN',
      itemLevel: item.itemLevel ?? 0,
      quality: item.quality ?? 'COMMON',
      enchantId: item.enchantId,
      enchantName: item.enchantName ?? item.enchant ?? undefined,
      gems: item.gems ?? [],
      socketCount: item.socketCount ?? item.sockets ?? 0,
      isTierPiece: item.isTierPiece ?? false,
      upgradeLevel: item.upgradeLevel,
      maxUpgradeLevel: item.maxUpgradeLevel,
    }))

    return {
      raiderId: raw.raiderId ?? 0,
      characterName: raw.characterName ?? raw.raiderName ?? '',
      averageItemLevel: raw.averageItemLevel ?? 0,
      equippedItemLevel: raw.equippedItemLevel ?? raw.averageItemLevel ?? 0,
      items,
      missingEnchants: raw.missingEnchants ?? [],
      missingGems: raw.missingGems ?? [],
      tierPieceCount: raw.tierPieceCount ?? items.filter(i => i.isTierPiece).length,
      lastUpdated: raw.lastUpdated ?? null,
    }
  },

  async getRaiderGear(raiderId: number): Promise<RaiderGear> {
    const response = await api.get<RaiderGear>(`/v1/gear/raider-gear-items/raider/${raiderId}`)
    return response.data
  },

  async getCharacterGear(characterId: number): Promise<RaiderGear> {
    const response = await api.get<RaiderGear>(`/v1/gear/characters/${characterId}`)
    return response.data
  },

  async getMyVaultOptions(guildId: string): Promise<VaultOptions> {
    const response = await api.get<VaultOptions>(`/v1/vault/guilds/${guildId}/me`)
    return response.data
  },

  async getVaultOptions(raiderId: number): Promise<VaultOptions> {
    const response = await api.get<VaultOptions>(`/v1/vault/raider-vault-slots/raider/${raiderId}`)
    return response.data
  },
}
