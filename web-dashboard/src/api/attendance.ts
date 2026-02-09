import { api } from './client'

export interface AttendanceRecord {
  id: number
  raiderId: number
  raidId: number
  raidName: string
  raidDate: string
  status: AttendanceStatus
  signupTime?: string
  arrivalTime?: string
  notes?: string
}

export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED' | 'BENCH'

export interface AttendanceReport {
  raiderId: number
  characterName: string
  totalRaids: number
  attendedRaids: number
  lateRaids: number
  excusedRaids: number
  attendanceRate: number
  lastRaidDate?: string
  streak: number
  records: AttendanceRecord[]
}

export interface GuildAttendanceSummary {
  guildId: string
  totalRaids: number
  averageAttendance: number
  raiders: RaiderAttendanceSummary[]
}

export interface RaiderAttendanceSummary {
  raiderId: number
  characterName: string
  attendanceRate: number
  totalRaids: number
  attendedRaids: number
}

export interface AttendanceCalendarEntry {
  date: string
  raidName: string
  status: AttendanceStatus
}

export const attendanceApi = {
  async getMyAttendance(guildId: string): Promise<AttendanceReport> {
    // Backend returns AttendanceReportResponse with nested stats object
    // We transform it into the flat AttendanceReport shape the UI expects
    const response = await api.get<any>(`/v1/attendance/guilds/${guildId}/me`)
    const raw = response.data

    // Handle both shapes: flat (from MeController) or nested stats (from AttendanceController)
    if (raw.stats) {
      return {
        raiderId: raw.raiderId ?? 0,
        characterName: raw.characterName ?? raw.raiderName ?? '',
        totalRaids: raw.stats.totalRaids ?? 0,
        attendedRaids: raw.stats.attendedRaids ?? 0,
        lateRaids: raw.stats.lateRaids ?? raw.stats.missedRaids ?? 0,
        excusedRaids: raw.stats.excusedRaids ?? 0,
        attendanceRate: (raw.stats.attendancePercentage ?? 0),
        lastRaidDate: raw.lastRaidDate,
        streak: raw.currentStreak ?? 0,
        records: [],
      }
    }

    // Flat shape (from MeController PersonalAttendanceResponse)
    return {
      raiderId: raw.raiderId ?? 0,
      characterName: raw.characterName ?? raw.raiderName ?? '',
      totalRaids: raw.totalRaids ?? 0,
      attendedRaids: raw.attendedRaids ?? 0,
      lateRaids: raw.breakdown?.late ?? 0,
      excusedRaids: raw.breakdown?.excused ?? 0,
      attendanceRate: raw.overallRate ?? raw.attendanceRate ?? 0,
      lastRaidDate: raw.lastRaidDate,
      streak: raw.currentStreak ?? raw.streak ?? 0,
      records: (raw.recentAttendance ?? raw.records ?? []).map((r: any) => ({
        id: r.id ?? 0,
        raiderId: raw.raiderId ?? 0,
        raidId: r.raidId ?? 0,
        raidName: r.raidName ?? '',
        raidDate: r.raidDate ?? '',
        status: r.status ?? 'ABSENT',
        notes: r.note ?? r.notes,
      })),
    }
  },

  async getAttendanceReport(guildId: string, raiderId: number): Promise<AttendanceReport> {
    const response = await api.get<AttendanceReport>(`/v1/attendance/raiders/${raiderId}/report`)
    return response.data
  },

  async getGuildSummary(guildId: string): Promise<GuildAttendanceSummary> {
    const response = await api.get<GuildAttendanceSummary>(`/v1/attendance/guild/${guildId}/summary`)
    return response.data
  },

  async getRaiderRecords(raiderId: number, limit = 50): Promise<AttendanceRecord[]> {
    const response = await api.get<AttendanceRecord[]>(`/v1/attendance/raider/${raiderId}?limit=${limit}`)
    return response.data
  },
}
