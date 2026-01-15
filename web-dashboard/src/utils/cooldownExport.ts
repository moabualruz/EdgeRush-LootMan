/**
 * Cooldown Export Utilities
 *
 * Functions to generate export formats for raid cooldown assignments.
 * Supports MRT (Method Raid Tools) note format and WeakAura data format.
 */

export interface ExportAssignment {
  playerName: string
  playerClass: string
  cooldownName: string
  cooldownSpellId: number
  abilityName: string
  time: number
}

/**
 * Format seconds to mm:ss display format
 */
export function formatTime(seconds: number): string {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

/**
 * Generate MRT (Method Raid Tools) note format
 *
 * Format:
 * |cff00ff00--- Cooldown Assignments ---|r
 * |cffff9900{time} - {ability}:|r
 *   {spell:spellId} {playerName}
 */
export function generateMRTNote(assignments: ExportAssignment[]): string {
  const lines: string[] = []
  lines.push('|cff00ff00--- Cooldown Assignments ---|r')

  if (assignments.length === 0) {
    lines.push('|cffaaaaaa(No assignments)|r')
    return lines.join('\n')
  }

  // Group assignments by ability and sort by time
  const byAbility = new Map<string, ExportAssignment[]>()

  // Sort assignments by time first
  const sortedAssignments = [...assignments].sort((a, b) => a.time - b.time)

  sortedAssignments.forEach((assignment) => {
    const key = `${assignment.time}-${assignment.abilityName}`
    if (!byAbility.has(key)) {
      byAbility.set(key, [])
    }
    byAbility.get(key)!.push(assignment)
  })

  // Generate lines grouped by ability
  byAbility.forEach((abilityAssignments, key) => {
    const first = abilityAssignments[0]
    lines.push(`|cffff9900${formatTime(first.time)} - ${first.abilityName}:|r`)

    abilityAssignments.forEach((assignment) => {
      lines.push(`  {spell:${assignment.cooldownSpellId}} ${assignment.playerName}`)
    })
  })

  return lines.join('\n')
}

/**
 * Generate WeakAura compatible data format
 *
 * Returns a JSON string that can be used to configure WeakAuras
 * for showing cooldown assignments at specific times.
 */
export function generateWeakAuraData(assignments: ExportAssignment[]): string {
  const data = {
    version: 1,
    generator: 'LootMan',
    assignments: assignments.map((a) => ({
      time: a.time,
      spellId: a.cooldownSpellId,
      spellName: a.cooldownName,
      playerName: a.playerName,
      playerClass: a.playerClass,
      abilityName: a.abilityName,
    })),
  }

  return JSON.stringify(data, null, 2)
}

/**
 * Generate a compact MRT note with class colors
 *
 * Uses WoW class color codes for player names.
 */
export function generateMRTNoteWithColors(assignments: ExportAssignment[]): string {
  const classColors: Record<string, string> = {
    WARRIOR: 'c79c6e',
    PALADIN: 'f58cba',
    HUNTER: 'abd473',
    ROGUE: 'fff569',
    PRIEST: 'ffffff',
    SHAMAN: '0070de',
    MAGE: '69ccf0',
    WARLOCK: '9482c9',
    MONK: '00ff96',
    DRUID: 'ff7d0a',
    DEMON_HUNTER: 'a330c9',
    DEATH_KNIGHT: 'c41f3b',
    EVOKER: '33937f',
  }

  const lines: string[] = []
  lines.push('|cff00ff00--- Cooldown Assignments ---|r')

  if (assignments.length === 0) {
    lines.push('|cffaaaaaa(No assignments)|r')
    return lines.join('\n')
  }

  // Group by time and ability
  const byAbility = new Map<string, ExportAssignment[]>()
  const sortedAssignments = [...assignments].sort((a, b) => a.time - b.time)

  sortedAssignments.forEach((assignment) => {
    const key = `${assignment.time}-${assignment.abilityName}`
    if (!byAbility.has(key)) {
      byAbility.set(key, [])
    }
    byAbility.get(key)!.push(assignment)
  })

  byAbility.forEach((abilityAssignments) => {
    const first = abilityAssignments[0]
    lines.push(`|cffff9900${formatTime(first.time)} - ${first.abilityName}:|r`)

    abilityAssignments.forEach((assignment) => {
      const color = classColors[assignment.playerClass] || 'ffffff'
      lines.push(`  {spell:${assignment.cooldownSpellId}} |cff${color}${assignment.playerName}|r`)
    })
  })

  return lines.join('\n')
}
