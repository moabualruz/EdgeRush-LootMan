import type { Ref } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { lootApi } from '@/api/loot'
import type { LootAward, UpdateLootRequest } from '@/types'
import { useToast } from '@/composables/useToast'

export interface EditLootParams {
  awardId: number
  notes?: string
}

/**
 * Composable for editing loot awards with optimistic updates.
 *
 * @param guildId - Reactive guild ID reference
 * @returns TanStack Query mutation with optimistic update handlers
 */
export function useLootEdit(guildId: Ref<string | undefined>) {
  const queryClient = useQueryClient()
  const { success, error } = useToast()

  return useMutation({
    mutationFn: ({ awardId, notes }: EditLootParams): Promise<LootAward> =>
      lootApi.updateLoot(awardId, { notes } as UpdateLootRequest),

    onMutate: async ({ awardId, notes }) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['myLootHistory', guildId.value] })

      // Snapshot previous value
      const previousData = queryClient.getQueryData(['myLootHistory', guildId.value])

      // Optimistically update
      queryClient.setQueryData(['myLootHistory', guildId.value], (old: any) => {
        if (!old) return old
        return {
          ...old,
          awards: old.awards.map((award: LootAward) =>
            award.id === awardId ? { ...award, notes } : award
          ),
        }
      })

      return { previousData }
    },

    onError: (_err, _variables, context) => {
      // Rollback on error
      if (context?.previousData) {
        queryClient.setQueryData(['myLootHistory', guildId.value], context.previousData)
      }
      error('Update Failed', 'Could not update loot award. Please try again.')
    },

    onSuccess: () => {
      success('Loot Updated', 'Award notes have been updated.')
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['myLootHistory', guildId.value] })
    },
  })
}
