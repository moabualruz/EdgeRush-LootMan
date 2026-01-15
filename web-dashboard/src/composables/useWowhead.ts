/**
 * Wowhead tooltip integration composable.
 *
 * Provides reactive Wowhead tooltip functionality that:
 * - Loads Wowhead script dynamically
 * - Refreshes tooltips when new items are added to the DOM
 * - Supports item quality colors and game version switching
 */

import { ref, onMounted, watch, type Ref } from 'vue'

declare global {
  interface Window {
    $WowheadPower?: {
      refreshLinks: () => void
    }
    whTooltips?: {
      colorLinks: boolean
      iconSize: string
      iconizeLinks: boolean
    }
  }
}

export interface WowheadConfig {
  colorLinks?: boolean
  iconSize?: 'small' | 'medium' | 'large' | 'tiny'
  iconizeLinks?: boolean
}

const WOWHEAD_SCRIPT_URL = 'https://wow.zamimg.com/js/tooltips.js'
const scriptLoaded = ref(false)
const scriptLoading = ref(false)

/**
 * Load the Wowhead tooltip script.
 */
async function loadWowheadScript(config: WowheadConfig = {}): Promise<void> {
  if (scriptLoaded.value || scriptLoading.value) return

  scriptLoading.value = true

  // Configure Wowhead options before script loads
  window.whTooltips = {
    colorLinks: config.colorLinks ?? true,
    iconSize: config.iconSize ?? 'small',
    iconizeLinks: config.iconizeLinks ?? true,
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = WOWHEAD_SCRIPT_URL
    script.async = true

    script.onload = () => {
      scriptLoaded.value = true
      scriptLoading.value = false
      resolve()
    }

    script.onerror = () => {
      scriptLoading.value = false
      reject(new Error('Failed to load Wowhead tooltip script'))
    }

    document.head.appendChild(script)
  })
}

/**
 * Refresh Wowhead tooltips for newly added links.
 */
function refreshTooltips(): void {
  if (window.$WowheadPower?.refreshLinks) {
    window.$WowheadPower.refreshLinks()
  }
}

/**
 * Composable for using Wowhead tooltips in components.
 *
 * @param config - Wowhead configuration options
 * @param dependencies - Reactive dependencies that should trigger tooltip refresh
 * @returns Object with script state and refresh function
 *
 * @example
 * ```vue
 * <script setup>
 * import { useWowhead } from '@/composables/useWowhead'
 * import { ref } from 'vue'
 *
 * const items = ref([])
 * const { isLoaded, refresh } = useWowhead({}, [items])
 * </script>
 *
 * <template>
 *   <a :href="`https://www.wowhead.com/item=${item.id}`" v-for="item in items">
 *     {{ item.name }}
 *   </a>
 * </template>
 * ```
 */
export function useWowhead(
  config: WowheadConfig = {},
  dependencies: Ref<unknown>[] = []
) {
  const isLoaded = ref(scriptLoaded.value)
  const error = ref<Error | null>(null)

  onMounted(async () => {
    try {
      await loadWowheadScript(config)
      isLoaded.value = true
      // Initial refresh after mount
      setTimeout(refreshTooltips, 100)
    } catch (e) {
      error.value = e as Error
      console.error('Failed to load Wowhead tooltips:', e)
    }
  })

  // Watch dependencies and refresh tooltips when they change
  if (dependencies.length > 0) {
    watch(
      dependencies,
      () => {
        if (isLoaded.value) {
          // Use nextTick-like delay to ensure DOM is updated
          setTimeout(refreshTooltips, 50)
        }
      },
      { deep: true }
    )
  }

  return {
    isLoaded,
    error,
    refresh: refreshTooltips,
  }
}

/**
 * Generate Wowhead item URL.
 *
 * @param itemId - WoW item ID
 * @param bonus - Optional bonus IDs (for mythic+, warforged, etc.)
 * @returns Wowhead URL for the item
 */
export function getWowheadItemUrl(
  itemId: number,
  bonus?: number[]
): string {
  let url = `https://www.wowhead.com/item=${itemId}`
  if (bonus && bonus.length > 0) {
    url += `?bonus=${bonus.join(':')}`
  }
  return url
}

/**
 * Generate Wowhead spell URL.
 */
export function getWowheadSpellUrl(spellId: number): string {
  return `https://www.wowhead.com/spell=${spellId}`
}

/**
 * Generate Wowhead NPC URL.
 */
export function getWowheadNpcUrl(npcId: number): string {
  return `https://www.wowhead.com/npc=${npcId}`
}

/**
 * Generate Wowhead achievement URL.
 */
export function getWowheadAchievementUrl(achievementId: number): string {
  return `https://www.wowhead.com/achievement=${achievementId}`
}

export default useWowhead
