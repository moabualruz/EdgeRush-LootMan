<script setup lang="ts">
/**
 * ItemHoverPreview - Enhanced hover preview wrapper for WoWHead items.
 *
 * Provides a larger, more prominent tooltip display on hover with
 * loading state and smooth transitions.
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getWowheadItemUrl } from '@/composables/useWowhead'

export interface ItemHoverPreviewProps {
  itemId: number
  /** Position of the preview relative to the trigger */
  position?: 'top' | 'bottom' | 'left' | 'right'
  /** Delay before showing preview in ms */
  showDelay?: number
  /** Whether to show the item icon */
  showIcon?: boolean
}

const props = withDefaults(defineProps<ItemHoverPreviewProps>(), {
  position: 'right',
  showDelay: 150,
  showIcon: true,
})

const isHovering = ref(false)
const showPreview = ref(false)
const previewRef = ref<HTMLDivElement | null>(null)
const triggerRef = ref<HTMLDivElement | null>(null)

let hoverTimeout: ReturnType<typeof setTimeout> | null = null

const iconUrl = computed(() => 
  `https://wow.zamimg.com/images/wow/icons/large/${props.itemId}.jpg`
)

const wowheadUrl = computed(() => getWowheadItemUrl(props.itemId))

// Position classes based on prop
const positionClasses = computed(() => {
  switch (props.position) {
    case 'top':
      return 'bottom-full left-1/2 -translate-x-1/2 mb-2'
    case 'bottom':
      return 'top-full left-1/2 -translate-x-1/2 mt-2'
    case 'left':
      return 'right-full top-1/2 -translate-y-1/2 mr-2'
    case 'right':
    default:
      return 'left-full top-1/2 -translate-y-1/2 ml-2'
  }
})

function handleMouseEnter() {
  isHovering.value = true
  hoverTimeout = setTimeout(() => {
    if (isHovering.value) {
      showPreview.value = true
    }
  }, props.showDelay)
}

function handleMouseLeave() {
  isHovering.value = false
  if (hoverTimeout) {
    clearTimeout(hoverTimeout)
    hoverTimeout = null
  }
  showPreview.value = false
}

onUnmounted(() => {
  if (hoverTimeout) {
    clearTimeout(hoverTimeout)
  }
})
</script>

<template>
  <div 
    ref="triggerRef"
    class="item-hover-preview-wrapper relative inline-block"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <!-- Slot for the trigger element (e.g., WowheadItem) -->
    <slot />

    <!-- Preview Panel -->
    <Transition name="preview-fade">
      <div
        v-if="showPreview"
        ref="previewRef"
        class="item-preview-panel absolute z-50"
        :class="positionClasses"
      >
        <div class="preview-content bg-gray-900 border border-gray-700 rounded-lg shadow-xl p-3 min-w-[240px]">
          <!-- Loading indicator -->
          <div class="flex items-start gap-3">
            <!-- Item Icon -->
            <div 
              v-if="showIcon"
              class="item-icon flex-shrink-0 w-12 h-12 rounded border border-gray-600 bg-gray-800 overflow-hidden"
            >
              <a 
                :href="wowheadUrl" 
                target="_blank"
                :data-wowhead="`item=${itemId}`"
                class="block w-full h-full"
              >
                <div class="w-full h-full flex items-center justify-center text-gray-500 text-xs">
                  Icon
                </div>
              </a>
            </div>

            <!-- Item Info -->
            <div class="flex-1 min-w-0">
              <p class="text-xs text-gray-400 mb-1">
                Hover for full tooltip
              </p>
              <a
                :href="wowheadUrl"
                target="_blank"
                :data-wowhead="`item=${itemId}`"
                class="text-sm text-purple-400 hover:text-purple-300 transition-colors"
              >
                View on WoWHead →
              </a>
            </div>
          </div>

          <!-- Quick Stats Placeholder -->
          <div class="mt-3 pt-3 border-t border-gray-700">
            <div class="flex items-center gap-2 text-xs text-gray-500">
              <span class="inline-flex items-center gap-1">
                <span class="w-2 h-2 rounded-full bg-purple-500"></span>
                Epic
              </span>
              <span>•</span>
              <span>Item ID: {{ itemId }}</span>
            </div>
          </div>
        </div>

        <!-- Arrow indicator -->
        <div 
          class="preview-arrow absolute w-3 h-3 bg-gray-900 border-gray-700 transform rotate-45"
          :class="{
            'top-full left-1/2 -translate-x-1/2 -mt-1.5 border-b border-r': position === 'top',
            'bottom-full left-1/2 -translate-x-1/2 mb-1.5 border-t border-l': position === 'bottom',
            'left-full top-1/2 -translate-y-1/2 -ml-1.5 border-t border-r': position === 'left',
            'right-full top-1/2 -translate-y-1/2 mr-1.5 border-b border-l': position === 'right',
          }"
        />
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.item-hover-preview-wrapper {
  cursor: pointer;
}

.item-preview-panel {
  pointer-events: auto;
}

.preview-content {
  backdrop-filter: blur(8px);
}

/* Fade transition */
.preview-fade-enter-active,
.preview-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.preview-fade-enter-from,
.preview-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
