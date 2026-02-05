import type { Ref } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { lootApi } from '@/api/loot'
import type { LootAward } from '@/types'
import { useToast } from '@/composables/useToast'

/**
 * Composable for revoking loot awards with optimistic updates.
 *
 * @param guildId - Reactive guild ID reference
 * @returns TanStack Query mutation with optimistic update handlers
 */
export function useLootRevoke(guildId: Ref<string | undefined>) {
  const queryClient = useQueryClient()
  const { success, error } = useToast()

  return useMutation({
    mutationFn: (awardId: number): Promise<void> => lootApi.revokeLoot(awardId),

    onMutate: async (awardId) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['myLootHistory', guildId.value] })

      // Snapshot previous value
      const previousData = queryClient.getQueryData(['myLootHistory', guildId.value])

      // Optimistically remove the award
      queryClient.setQueryData(['myLootHistory', guildId.value], (old: any) => {
        if (!old) return old
        return {
          ...old,
          awards: old.awards.filter((award: LootAward) => award.id !== awardId),
        }
      })

      return { previousData }
    },

    onError: (_err, _variables, context) => {
      // Rollback on error
      if (context?.previousData) {
        queryClient.setQueryData(['myLootHistory', guildId.value], context.previousData)
      }
      error('Revoke Failed', 'Could not revoke loot award. Please try again.')
    },

    onSuccess: () => {
      success('Loot Revoked', 'The loot award has been removed.')
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['myLootHistory', guildId.value] })
    },
  })
}
