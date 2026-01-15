<script setup lang="ts">
/**
 * MarkerPalette - Tool palette for raid plan editing.
 *
 * Provides marker selection, shape tools, and color picker.
 */
import type { MarkerType, ShapeType } from '@/api/raidplan'

export interface MarkerPaletteProps {
  selectedMarker?: MarkerType
  selectedShape?: ShapeType
  selectedColor?: string
  selectedTool?: 'select' | 'pan' | 'marker' | 'shape'
  canDelete?: boolean
}

const props = withDefaults(defineProps<MarkerPaletteProps>(), {
  selectedTool: 'select',
  canDelete: true,
})

const emit = defineEmits<{
  'marker-select': [marker: MarkerType]
  'shape-select': [shape: ShapeType]
  'color-select': [color: string]
  'tool-select': [tool: 'select' | 'pan' | 'marker' | 'shape']
  'delete': []
}>()

// Raid markers (WoW skull, cross, etc.)
const raidMarkers: { type: MarkerType; symbol: string; color: string; label: string }[] = [
  { type: 'SKULL', symbol: '\u2620', color: '#ffffff', label: 'Skull' },
  { type: 'CROSS', symbol: '\u2716', color: '#ff0000', label: 'Cross' },
  { type: 'SQUARE', symbol: '\u25A0', color: '#0000ff', label: 'Square' },
  { type: 'MOON', symbol: '\u263E', color: '#c0c0c0', label: 'Moon' },
  { type: 'TRIANGLE', symbol: '\u25B2', color: '#00ff00', label: 'Triangle' },
  { type: 'DIAMOND', symbol: '\u25C6', color: '#ff00ff', label: 'Diamond' },
  { type: 'CIRCLE', symbol: '\u25CF', color: '#ff8000', label: 'Circle' },
  { type: 'STAR', symbol: '\u2605', color: '#ffff00', label: 'Star' },
]

// Role markers
const roleMarkers: { type: MarkerType; symbol: string; color: string; label: string }[] = [
  { type: 'TANK', symbol: 'T', color: '#3b82f6', label: 'Tank' },
  { type: 'HEALER', symbol: 'H', color: '#22c55e', label: 'Healer' },
  { type: 'DPS', symbol: 'D', color: '#ef4444', label: 'DPS' },
  { type: 'PLAYER', symbol: 'P', color: '#9333ea', label: 'Player' },
]

// Shape tools
const shapes: { type: ShapeType; icon: string; label: string }[] = [
  { type: 'CIRCLE', icon: '\u25CB', label: 'Circle' },
  { type: 'LINE', icon: '\u2015', label: 'Line' },
  { type: 'ARROW', icon: '\u2192', label: 'Arrow' },
  { type: 'RECTANGLE', icon: '\u25A1', label: 'Rectangle' },
]

// Preset colors
const presetColors = [
  '#ff0000', // Red
  '#ff8000', // Orange
  '#ffff00', // Yellow
  '#00ff00', // Green
  '#00ffff', // Cyan
  '#0000ff', // Blue
  '#ff00ff', // Magenta
  '#ffffff', // White
]

function handleMarkerClick(type: MarkerType) {
  emit('marker-select', type)
}

function handleShapeClick(type: ShapeType) {
  emit('shape-select', type)
}

function handleColorClick(color: string) {
  emit('color-select', color)
}

function handleToolClick(tool: 'select' | 'pan' | 'marker' | 'shape') {
  emit('tool-select', tool)
}

function handleDelete() {
  emit('delete')
}
</script>

<template>
  <div
    data-testid="marker-palette"
    class="marker-palette bg-gray-800 rounded-lg p-4 w-64 flex flex-col gap-4"
  >
    <!-- Tools Section -->
    <div class="tools-section">
      <h3 class="text-sm font-semibold text-gray-400 mb-2">Tools</h3>
      <div class="flex gap-2">
        <button
          data-testid="tool-select"
          :class="['tool-btn', { selected: selectedTool === 'select' }]"
          title="Select"
          @click="handleToolClick('select')"
        >
          <span class="text-lg">\u2316</span>
        </button>
        <button
          data-testid="tool-pan"
          :class="['tool-btn', { selected: selectedTool === 'pan' }]"
          title="Pan"
          @click="handleToolClick('pan')"
        >
          <span class="text-lg">\u2630</span>
        </button>
        <button
          data-testid="delete-button"
          class="tool-btn text-red-500 hover:text-red-400 ml-auto"
          title="Delete Selected"
          :disabled="!canDelete"
          @click="handleDelete"
        >
          <span class="text-lg">\u2715</span>
        </button>
      </div>
    </div>

    <!-- Raid Markers Section -->
    <div class="markers-section">
      <h3 class="text-sm font-semibold text-gray-400 mb-2">Raid Markers</h3>
      <div class="grid grid-cols-4 gap-2">
        <button
          v-for="marker in raidMarkers"
          :key="marker.type"
          :data-testid="`marker-${marker.type}`"
          :class="['marker-btn', { selected: selectedMarker === marker.type }]"
          :title="marker.label"
          @click="handleMarkerClick(marker.type)"
        >
          <span :style="{ color: marker.color }">{{ marker.symbol }}</span>
        </button>
      </div>
    </div>

    <!-- Role Markers Section -->
    <div class="role-markers-section">
      <h3 class="text-sm font-semibold text-gray-400 mb-2">Role Markers</h3>
      <div class="grid grid-cols-4 gap-2">
        <button
          v-for="marker in roleMarkers"
          :key="marker.type"
          :data-testid="`marker-${marker.type}`"
          :class="['marker-btn', { selected: selectedMarker === marker.type }]"
          :title="marker.label"
          @click="handleMarkerClick(marker.type)"
        >
          <span :style="{ color: marker.color }">{{ marker.symbol }}</span>
        </button>
      </div>
    </div>

    <!-- Shapes Section -->
    <div class="shapes-section">
      <h3 class="text-sm font-semibold text-gray-400 mb-2">Shapes</h3>
      <div class="grid grid-cols-4 gap-2">
        <button
          v-for="shape in shapes"
          :key="shape.type"
          :data-testid="`shape-${shape.type}`"
          :class="['shape-btn', { selected: selectedShape === shape.type }]"
          :title="shape.label"
          @click="handleShapeClick(shape.type)"
        >
          <span>{{ shape.icon }}</span>
        </button>
      </div>
    </div>

    <!-- Color Picker Section -->
    <div data-testid="color-picker" class="color-picker-section">
      <h3 class="text-sm font-semibold text-gray-400 mb-2">Color</h3>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="color in presetColors"
          :key="color"
          data-testid="color-swatch"
          :data-color="color"
          :class="['color-swatch', { selected: selectedColor === color }]"
          :style="{ backgroundColor: color }"
          @click="handleColorClick(color)"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.tool-btn,
.marker-btn,
.shape-btn {
  @apply w-10 h-10 flex items-center justify-center rounded bg-gray-700 hover:bg-gray-600 transition-colors cursor-pointer text-white;
}

.tool-btn:disabled {
  @apply opacity-50 cursor-not-allowed hover:bg-gray-700;
}

.tool-btn.selected,
.marker-btn.selected,
.shape-btn.selected {
  @apply bg-blue-600 ring-2 ring-blue-400;
}

.color-swatch {
  @apply w-8 h-8 rounded border-2 border-gray-600 cursor-pointer transition-all;
}

.color-swatch:hover {
  @apply border-gray-400 scale-110;
}

.color-swatch.selected {
  @apply border-white ring-2 ring-blue-400;
}
</style>
