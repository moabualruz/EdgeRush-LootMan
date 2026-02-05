import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { syncApi, type SyncRun } from '@/api/sync'
import { useToast } from '@/composables/useToast'

/**
 * Composable for triggering sync operations.
 *
 * @param source - The sync source ('WoWAudit' or 'WarcraftLogs')
 * @returns TanStack Query mutation for triggering sync
 */
export function useSyncTrigger(source: 'WoWAudit' | 'WarcraftLogs') {
  const queryClient = useQueryClient()
  const { success, error } = useToast()

  return useMutation({
    mutationFn: (): Promise<SyncRun> => syncApi.triggerSync(source),

    onSuccess: (data) => {
      success('Sync Started', `${source} sync has been triggered.`)
      // Invalidate sync runs query to show the new run
      queryClient.invalidateQueries({ queryKey: ['syncRuns'] })
    },

    onError: () => {
      error('Sync Failed', `Could not trigger ${source} sync. Please try again.`)
    },
  })
}
