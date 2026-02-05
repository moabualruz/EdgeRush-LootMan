import type { Ref } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { lootApi } from '@/api/loot'
import type { AwardLootRequest, LootAward } from '@/types'
import { useToast } from '@/composables/useToast'

/**
 * Composable for awarding loot with optimistic updates.
 *
 * @param guildId - Reactive guild ID reference
 * @returns TanStack Query mutation with optimistic update handlers
 */
export function useLootAward(guildId: Ref<string | undefined>) {
  const queryClient = useQueryClient()
  const { success, error } = useToast()

  return useMutation({
    mutationFn: (data: AwardLootRequest): Promise<LootAward> =>
      lootApi.awardLoot(guildId.value!, data),

    onMutate: async (newAward) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['myLootHistory', guildId.value] })

      // Snapshot previous value
      const previousAwards = queryClient.getQueryData(['myLootHistory', guildId.value])

      // Optimistically update
      queryClient.setQueryData(['myLootHistory', guildId.value], (old: any) => {
        if (!old) return old
        return {
          ...old,
          awards: [
            { ...newAward, id: -1, awardedAt: new Date().toISOString(), flpsAtAward: 0, rdfExpired: false },
            ...old.awards,
          ],
        }
      })

      return { previousAwards }
    },

    onError: (_err, _variables, context) => {
      // Rollback on error
      if (context?.previousAwards) {
        queryClient.setQueryData(['myLootHistory', guildId.value], context.previousAwards)
      }
      error('Award Failed', 'Could not award loot. Please try again.')
    },

    onSuccess: () => {
      success('Loot Awarded', 'Item has been assigned to the raider.')
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['myLootHistory', guildId.value] })
    },
  })
}
