import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { triggerWowauditSync, triggerWarcraftLogsSync, type GuildSyncTriggerResponse } from '@/api/guildSync'
import { useToast } from '@/composables/useToast'
import { useGuildContextStore } from '@/stores/guildContext'

/**
 * Composable for triggering sync operations.
 *
 * @param source - The sync source ('WoWAudit' or 'WarcraftLogs')
 * @returns TanStack Query mutation for triggering sync
 */
export function useSyncTrigger(source: 'WoWAudit' | 'WarcraftLogs') {
  const queryClient = useQueryClient()
  const { success, error } = useToast()
  const guildStore = useGuildContextStore()

  return useMutation({
    mutationFn: async (): Promise<GuildSyncTriggerResponse> => {
      const guildId = guildStore.currentGuildId
      if (!guildId) {
        throw new Error('No active guild selected')
      }

      if (source === 'WoWAudit') {
        return triggerWowauditSync(guildId)
      } else {
        return triggerWarcraftLogsSync(guildId)
      }
    },

    onSuccess: (data) => {
      success('Sync Started', `${source} sync triggered: ${data.message}`)
      // Invalidate sync runs query to show the new run
      queryClient.invalidateQueries({ queryKey: ['syncRuns'] })
    },

    onError: (err) => {
      error('Sync Failed', err instanceof Error ? err.message : `Could not trigger ${source} sync.`)
    },
  })
}
