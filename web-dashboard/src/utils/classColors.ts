import type { CharacterClass } from '@/types'

const classColors: Record<CharacterClass, string> = {
  WARRIOR: 'text-class-warrior',
  PALADIN: 'text-class-paladin',
  HUNTER: 'text-class-hunter',
  ROGUE: 'text-class-rogue',
  PRIEST: 'text-class-priest',
  DEATH_KNIGHT: 'text-class-deathknight',
  SHAMAN: 'text-class-shaman',
  MAGE: 'text-class-mage',
  WARLOCK: 'text-class-warlock',
  MONK: 'text-class-monk',
  DRUID: 'text-class-druid',
  DEMON_HUNTER: 'text-class-demonhunter',
  EVOKER: 'text-class-evoker',
}

export function getClassColor(characterClass: CharacterClass): string {
  return classColors[characterClass] || 'text-white'
}

export function getClassBackgroundColor(characterClass: CharacterClass): string {
  return classColors[characterClass].replace('text-', 'bg-') + '/20'
}
