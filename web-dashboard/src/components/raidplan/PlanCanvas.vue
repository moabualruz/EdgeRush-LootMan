<script setup lang="ts">
/**
 * PlanCanvas - SVG-based canvas for raid plan visualization.
 *
 * Renders encounter backgrounds, markers, and shapes with zoom/pan support.
 */
import { computed, ref } from 'vue'
import type { PlanMarker, PlanShape, MarkerType } from '@/api/raidplan'

export interface PlanCanvasProps {
  width: number
  height: number
  backgroundImage?: string
  markers: PlanMarker[]
  shapes: PlanShape[]
  zoom?: number
  panX?: number
  panY?: number
  selectedMarkerIndex?: number
  selectedShapeIndex?: number
  currentTool?: 'select' | 'pan' | 'marker' | 'shape'
  /** Enable grid snapping for marker placement */
  gridEnabled?: boolean
  /** Grid size in percentage units (default: 5 = 5% of canvas) */
  gridSize?: number
  /** Show grid overlay lines */
  showGrid?: boolean
}

const props = withDefaults(defineProps<PlanCanvasProps>(), {
  zoom: 1,
  panX: 0,
  panY: 0,
  selectedMarkerIndex: -1,
  selectedShapeIndex: -1,
  currentTool: 'select',
  gridEnabled: true,
  gridSize: 5,
  showGrid: true,
})

const emit = defineEmits<{
  'canvas-click': [position: { x: number; y: number }]
  'marker-click': [index: number, marker: PlanMarker]
  'shape-click': [index: number, shape: PlanShape]
  'zoom-change': [zoom: number]
  'pan-change': [pan: { x: number; y: number }]
}>()

const isDragging = ref(false)

// Convert percentage coordinates to pixel positions
function toPixelX(percent: number): number {
  return (percent / 100) * props.width
}

function toPixelY(percent: number): number {
  return (percent / 100) * props.height
}

// Convert pixel coordinates to percentage
function toPercentX(pixel: number): number {
  return (pixel / props.width) * 100
}

function toPercentY(pixel: number): number {
  return (pixel / props.height) * 100
}

// Snap coordinate to nearest grid point
function snapToGrid(value: number): number {
  if (!props.gridEnabled || props.gridSize <= 0) return value
  return Math.round(value / props.gridSize) * props.gridSize
}

// Generate grid lines for overlay
const gridLines = computed(() => {
  if (!props.showGrid || props.gridSize <= 0) return { horizontal: [], vertical: [] }
  
  const horizontal: number[] = []
  const vertical: number[] = []
  
  // Generate horizontal lines (y coordinates)
  for (let y = props.gridSize; y < 100; y += props.gridSize) {
    horizontal.push(toPixelY(y))
  }
  
  // Generate vertical lines (x coordinates)
  for (let x = props.gridSize; x < 100; x += props.gridSize) {
    vertical.push(toPixelX(x))
  }
  
  return { horizontal, vertical }
})

// Container transform style
const containerStyle = computed(() => ({
  transform: `translate(${props.panX}px, ${props.panY}px) scale(${props.zoom})`,
  transformOrigin: 'center center',
}))

// Cursor class based on current tool
const cursorClass = computed(() => {
  switch (props.currentTool) {
    case 'pan':
      return 'cursor-grab'
    case 'marker':
    case 'shape':
      return 'cursor-crosshair'
    default:
      return 'cursor-default'
  }
})

// Get marker symbol based on type
function getMarkerSymbol(type: MarkerType): string {
  const symbols: Record<MarkerType, string> = {
    SKULL: '\u2620',
    CROSS: '\u2716',
    SQUARE: '\u25A0',
    MOON: '\u263E',
    TRIANGLE: '\u25B2',
    DIAMOND: '\u25C6',
    CIRCLE: '\u25CF',
    STAR: '\u2605',
    TANK: 'T',
    HEALER: 'H',
    DPS: 'D',
    PLAYER: 'P',
  }
  return symbols[type] || '?'
}

// Get default marker color
function getMarkerColor(type: MarkerType, customColor?: string): string {
  if (customColor) return customColor
  const colors: Record<MarkerType, string> = {
    SKULL: '#ffffff',
    CROSS: '#ff0000',
    SQUARE: '#0000ff',
    MOON: '#c0c0c0',
    TRIANGLE: '#00ff00',
    DIAMOND: '#ff00ff',
    CIRCLE: '#ff8000',
    STAR: '#ffff00',
    TANK: '#3b82f6',
    HEALER: '#22c55e',
    DPS: '#ef4444',
    PLAYER: '#9333ea',
  }
  return colors[type] || '#ffffff'
}

// Event handlers
function handleBackgroundClick(event: MouseEvent) {
  const rect = (event.currentTarget as SVGElement).getBoundingClientRect()
  let x = toPercentX(event.offsetX)
  let y = toPercentY(event.offsetY)
  
  // Apply grid snapping
  x = snapToGrid(x)
  y = snapToGrid(y)
  
  emit('canvas-click', { x, y })
}

function handleMarkerClick(index: number, marker: PlanMarker, event: Event) {
  event.stopPropagation()
  emit('marker-click', index, marker)
}

function handleShapeClick(index: number, shape: PlanShape, event: Event) {
  event.stopPropagation()
  emit('shape-click', index, shape)
}

function handleWheel(event: WheelEvent) {
  event.preventDefault()
  const delta = event.deltaY > 0 ? -0.1 : 0.1
  const newZoom = Math.max(0.5, Math.min(3, props.zoom + delta))
  emit('zoom-change', newZoom)
}

function handleMouseDown(event: MouseEvent) {
  if (event.button === 0) {
    isDragging.value = true
  }
}

function handleMouseMove(event: MouseEvent) {
  if (isDragging.value) {
    emit('pan-change', {
      x: props.panX + event.movementX,
      y: props.panY + event.movementY,
    })
  }
}

function handleMouseUp() {
  isDragging.value = false
}
</script>

<template>
  <div class="plan-canvas-wrapper overflow-hidden" :style="{ width: `${width}px`, height: `${height}px` }">
    <div
      data-testid="canvas-container"
      :style="containerStyle"
    >
      <svg
        :width="width"
        :height="height"
        :class="['plan-canvas', cursorClass]"
        @wheel="handleWheel"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
        @mouseleave="handleMouseUp"
      >
        <!-- Background -->
        <rect
          data-testid="canvas-background"
          x="0"
          y="0"
          :width="width"
          :height="height"
          fill="#1a1a2e"
          @click="handleBackgroundClick"
        />

        <!-- Grid Overlay -->
        <g v-if="showGrid" class="grid-overlay" data-testid="grid-overlay" pointer-events="none">
          <!-- Vertical grid lines -->
          <line
            v-for="(x, i) in gridLines.vertical"
            :key="`grid-v-${i}`"
            :x1="x"
            :y1="0"
            :x2="x"
            :y2="height"
            stroke="rgba(255, 255, 255, 0.08)"
            stroke-width="1"
            stroke-dasharray="4,4"
          />
          <!-- Horizontal grid lines -->
          <line
            v-for="(y, i) in gridLines.horizontal"
            :key="`grid-h-${i}`"
            :x1="0"
            :y1="y"
            :x2="width"
            :y2="y"
            stroke="rgba(255, 255, 255, 0.08)"
            stroke-width="1"
            stroke-dasharray="4,4"
          />
        </g>

        <!-- Background Image -->
        <image
          v-if="backgroundImage"
          :href="backgroundImage"
          x="0"
          y="0"
          :width="width"
          :height="height"
          preserveAspectRatio="xMidYMid slice"
        />

        <!-- Shapes Layer (rendered behind markers) -->
        <g class="shapes-layer">
          <!-- Circle Shapes -->
          <circle
            v-for="(shape, index) in shapes.filter(s => s.shapeType === 'CIRCLE')"
            :key="`circle-${index}`"
            data-testid="shape-circle"
            :cx="toPixelX(shape.x1)"
            :cy="toPixelY(shape.y1)"
            :r="shape.radius ? (shape.radius / 100) * Math.min(width, height) : 20"
            fill="none"
            :stroke="shape.color || '#ffffff'"
            :stroke-width="shape.strokeWidth"
            :class="{ selected: selectedShapeIndex === shapes.indexOf(shape) }"
            @click="(e) => handleShapeClick(shapes.indexOf(shape), shape, e)"
          />

          <!-- Line Shapes -->
          <line
            v-for="(shape, index) in shapes.filter(s => s.shapeType === 'LINE')"
            :key="`line-${index}`"
            data-testid="shape-line"
            :x1="toPixelX(shape.x1)"
            :y1="toPixelY(shape.y1)"
            :x2="toPixelX(shape.x2 ?? shape.x1)"
            :y2="toPixelY(shape.y2 ?? shape.y1)"
            :stroke="shape.color || '#ffffff'"
            :stroke-width="shape.strokeWidth"
            :class="{ selected: selectedShapeIndex === shapes.indexOf(shape) }"
            @click="(e) => handleShapeClick(shapes.indexOf(shape), shape, e)"
          />

          <!-- Arrow Shapes -->
          <g
            v-for="(shape, index) in shapes.filter(s => s.shapeType === 'ARROW')"
            :key="`arrow-${index}`"
            data-testid="shape-arrow"
            :class="{ selected: selectedShapeIndex === shapes.indexOf(shape) }"
            @click="(e) => handleShapeClick(shapes.indexOf(shape), shape, e)"
          >
            <defs>
              <marker
                :id="`arrowhead-${index}`"
                markerWidth="10"
                markerHeight="7"
                refX="9"
                refY="3.5"
                orient="auto"
              >
                <polygon
                  points="0 0, 10 3.5, 0 7"
                  :fill="shape.color || '#ffffff'"
                />
              </marker>
            </defs>
            <line
              :x1="toPixelX(shape.x1)"
              :y1="toPixelY(shape.y1)"
              :x2="toPixelX(shape.x2 ?? shape.x1)"
              :y2="toPixelY(shape.y2 ?? shape.y1)"
              :stroke="shape.color || '#ffffff'"
              :stroke-width="shape.strokeWidth"
              :marker-end="`url(#arrowhead-${index})`"
            />
          </g>

          <!-- Rectangle Shapes -->
          <rect
            v-for="(shape, index) in shapes.filter(s => s.shapeType === 'RECTANGLE')"
            :key="`rect-${index}`"
            data-testid="shape-rectangle"
            :x="Math.min(toPixelX(shape.x1), toPixelX(shape.x2 ?? shape.x1))"
            :y="Math.min(toPixelY(shape.y1), toPixelY(shape.y2 ?? shape.y1))"
            :width="Math.abs(toPixelX(shape.x2 ?? shape.x1) - toPixelX(shape.x1))"
            :height="Math.abs(toPixelY(shape.y2 ?? shape.y1) - toPixelY(shape.y1))"
            fill="none"
            :stroke="shape.color || '#ffffff'"
            :stroke-width="shape.strokeWidth"
            :class="{ selected: selectedShapeIndex === shapes.indexOf(shape) }"
            @click="(e) => handleShapeClick(shapes.indexOf(shape), shape, e)"
          />
        </g>

        <!-- Markers Layer -->
        <g class="markers-layer">
          <g
            v-for="(marker, index) in markers"
            :key="`marker-${index}`"
            data-testid="marker"
            :data-marker-type="marker.type"
            :transform="`translate(${toPixelX(marker.x)}, ${toPixelY(marker.y)})`"
            :class="['marker', { selected: selectedMarkerIndex === index }]"
            @click="(e) => handleMarkerClick(index, marker, e)"
          >
            <!-- Marker background circle -->
            <circle
              r="16"
              :fill="getMarkerColor(marker.type, marker.color)"
              stroke="#000"
              stroke-width="2"
            />
            <!-- Marker symbol -->
            <text
              text-anchor="middle"
              dominant-baseline="central"
              font-size="14"
              font-weight="bold"
              fill="#000"
            >
              {{ getMarkerSymbol(marker.type) }}
            </text>
            <!-- Label (if provided) -->
            <text
              v-if="marker.label"
              y="24"
              text-anchor="middle"
              font-size="12"
              fill="#fff"
              stroke="#000"
              stroke-width="0.5"
            >
              {{ marker.label }}
            </text>
          </g>
        </g>
      </svg>
    </div>
  </div>
</template>

<style scoped>
.plan-canvas {
  display: block;
  user-select: none;
}

.marker {
  cursor: pointer;
  transition: transform 0.1s;
}

.marker:hover {
  transform: scale(1.1);
}

.marker.selected circle {
  stroke: #fbbf24;
  stroke-width: 3;
}

.shapes-layer > * {
  cursor: pointer;
}

.shapes-layer > *.selected {
  stroke-dasharray: 5, 5;
  animation: dash 0.5s linear infinite;
}

@keyframes dash {
  to {
    stroke-dashoffset: -10;
  }
}
</style>
