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
    const response = await api.get<AttendanceReport>(`/api/v1/attendance/guilds/${guildId}/me`)
    return response.data
  },

  async getAttendanceReport(guildId: string, raiderId: number): Promise<AttendanceReport> {
    const response = await api.get<AttendanceReport>(`/api/v1/attendance/raiders/${raiderId}/report`)
    return response.data
  },

  async getGuildSummary(guildId: string): Promise<GuildAttendanceSummary> {
    const response = await api.get<GuildAttendanceSummary>(`/api/v1/attendance/guild/${guildId}/summary`)
    return response.data
  },

  async getRaiderRecords(raiderId: number, limit = 50): Promise<AttendanceRecord[]> {
    const response = await api.get<AttendanceRecord[]>(`/api/v1/attendance/raider/${raiderId}?limit=${limit}`)
    return response.data
  },
}
