import { describe, it, expect, vi, beforeEach } from 'vitest'
import { syncApi } from './sync'
import { api } from './client'

vi.mock('./client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('syncApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getSyncRuns', () => {
    it('should fetch paginated sync runs', async () => {
      const mockResponse = {
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      }
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      const result = await syncApi.getSyncRuns()

      expect(api.get).toHaveBeenCalledWith('/api/sync-runs', { params: { page: 0, size: 20 } })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('triggerSync', () => {
    it('should trigger WoWAudit sync', async () => {
      const mockRun = {
        id: 1,
        source: 'WoWAudit',
        status: 'RUNNING',
        startedAt: '2026-02-05T10:00:00Z',
        completedAt: null,
        message: null,
      }
      vi.mocked(api.post).mockResolvedValue({ data: mockRun })

      const result = await syncApi.triggerSync('WoWAudit')

      expect(api.post).toHaveBeenCalledWith('/api/sync/trigger/WoWAudit')
      expect(result).toEqual(mockRun)
      expect(result.status).toBe('RUNNING')
    })

    it('should trigger WarcraftLogs sync', async () => {
      const mockRun = {
        id: 2,
        source: 'WarcraftLogs',
        status: 'RUNNING',
        startedAt: '2026-02-05T10:00:00Z',
        completedAt: null,
        message: null,
      }
      vi.mocked(api.post).mockResolvedValue({ data: mockRun })

      const result = await syncApi.triggerSync('WarcraftLogs')

      expect(api.post).toHaveBeenCalledWith('/api/sync/trigger/WarcraftLogs')
      expect(result.source).toBe('WarcraftLogs')
    })
  })

  describe('getSyncLogs', () => {
    it('should fetch logs for a sync run', async () => {
      const mockLogs = [
        { timestamp: '2026-02-05T10:00:00Z', level: 'INFO', message: 'Sync started' },
        { timestamp: '2026-02-05T10:00:05Z', level: 'INFO', message: 'Fetching characters' },
        { timestamp: '2026-02-05T10:00:10Z', level: 'WARN', message: 'Rate limited, retrying' },
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockLogs })

      const result = await syncApi.getSyncLogs(1)

      expect(api.get).toHaveBeenCalledWith('/api/sync-runs/1/logs')
      expect(result).toHaveLength(3)
      expect(result[0].level).toBe('INFO')
      expect(result[2].level).toBe('WARN')
    })
  })
})
