<script setup lang="ts">
/**
 * RaidPlanPage - Raid plan editor page.
 *
 * Provides a full editor for creating and editing raid positioning plans.
 */
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useQuery } from '@tanstack/vue-query'
import { raidPlanApi, type MarkerType, type ShapeType, type PlanMarker, type PlanShape } from '@/api/raidplan'
import PlanCanvas from '@/components/raidplan/PlanCanvas.vue'
import MarkerPalette from '@/components/raidplan/MarkerPalette.vue'
import StepTimeline from '@/components/raidplan/StepTimeline.vue'
import Skeleton from '@/components/Skeleton.vue'

const route = useRoute()
const router = useRouter()

const planId = computed(() => route.params.id as string)

// Fetch plan data
const {
  data: plan,
  isLoading,
  isError,
  error,
} = useQuery({
  queryKey: ['raid-plan', planId],
  queryFn: () => raidPlanApi.getPlan(planId.value),
  enabled: computed(() => !!planId.value),
})

// Editor state
const currentStep = ref(0)
const currentTool = ref<'select' | 'pan' | 'marker' | 'shape'>('select')
const selectedMarker = ref<MarkerType | undefined>()
const selectedShape = ref<ShapeType | undefined>()
const selectedColor = ref('#ffffff')
const selectedMarkerIndex = ref(-1)
const selectedShapeIndex = ref(-1)
const zoom = ref(1)
const panX = ref(0)
const panY = ref(0)

// Computed current step data
const currentStepData = computed(() => plan.value?.steps[currentStep.value])
const currentMarkers = computed(() => currentStepData.value?.markers ?? [])
const currentShapes = computed(() => currentStepData.value?.shapes ?? [])

// Whether we can delete selected item
const canDelete = computed(() => selectedMarkerIndex.value >= 0 || selectedShapeIndex.value >= 0)

// Event handlers
function handleStepChange(index: number) {
  currentStep.value = index
  selectedMarkerIndex.value = -1
  selectedShapeIndex.value = -1
}

function handleToolSelect(tool: 'select' | 'pan' | 'marker' | 'shape') {
  currentTool.value = tool
  if (tool === 'select' || tool === 'pan') {
    selectedMarker.value = undefined
    selectedShape.value = undefined
  }
}

function handleMarkerSelect(marker: MarkerType) {
  selectedMarker.value = marker
  selectedShape.value = undefined
  currentTool.value = 'marker'
}

function handleShapeSelect(shape: ShapeType) {
  selectedShape.value = shape
  selectedMarker.value = undefined
  currentTool.value = 'shape'
}

function handleColorSelect(color: string) {
  selectedColor.value = color
}

function handleCanvasClick(position: { x: number; y: number }) {
  if (currentTool.value === 'marker' && selectedMarker.value) {
    // Add marker at clicked position (would call API in real implementation)
    console.log('Add marker:', selectedMarker.value, 'at', position)
  } else if (currentTool.value === 'shape' && selectedShape.value) {
    // Start drawing shape (would call API in real implementation)
    console.log('Add shape:', selectedShape.value, 'at', position)
  } else if (currentTool.value === 'select') {
    selectedMarkerIndex.value = -1
    selectedShapeIndex.value = -1
  }
}

function handleMarkerClick(index: number, _marker: PlanMarker) {
  selectedMarkerIndex.value = index
  selectedShapeIndex.value = -1
  currentTool.value = 'select'
}

function handleShapeClick(index: number, _shape: PlanShape) {
  selectedShapeIndex.value = index
  selectedMarkerIndex.value = -1
  currentTool.value = 'select'
}

function handleZoomChange(newZoom: number) {
  zoom.value = newZoom
}

function handlePanChange(newPan: { x: number; y: number }) {
  panX.value = newPan.x
  panY.value = newPan.y
}

function handleDelete() {
  if (selectedMarkerIndex.value >= 0) {
    console.log('Delete marker:', selectedMarkerIndex.value)
    selectedMarkerIndex.value = -1
  } else if (selectedShapeIndex.value >= 0) {
    console.log('Delete shape:', selectedShapeIndex.value)
    selectedShapeIndex.value = -1
  }
}

function handleAddStep() {
  console.log('Add step')
}

function handleDeleteStep(index: number) {
  console.log('Delete step:', index)
}

function handleNotesEdit() {
  console.log('Edit notes for step:', currentStep.value)
}

function goBack() {
  router.back()
}

function save() {
  console.log('Save plan')
}

function share() {
  console.log('Share plan')
}
</script>

<template>
  <div class="raid-plan-page min-h-screen bg-gray-900 text-white">
    <!-- Loading State -->
    <div v-if="isLoading" class="p-8">
      <Skeleton class="h-8 w-64 mb-4" />
      <Skeleton class="h-96" />
    </div>

    <!-- Error State -->
    <div v-else-if="isError" class="p-8 text-center">
      <h2 class="text-xl text-red-400 mb-4">Failed to load plan</h2>
      <p class="text-gray-400 mb-4">{{ error?.message || 'Unknown error' }}</p>
      <button
        data-testid="back-button"
        class="btn-primary"
        @click="goBack"
      >
        Go Back
      </button>
    </div>

    <!-- Main Content -->
    <div v-else-if="plan" class="flex flex-col h-screen">
      <!-- Header -->
      <header class="flex items-center justify-between p-4 bg-gray-800 border-b border-gray-700">
        <div class="flex items-center gap-4">
          <button
            data-testid="back-button"
            class="btn-secondary"
            @click="goBack"
          >
            &larr; Back
          </button>
          <div>
            <h1 class="text-lg font-semibold">{{ plan.name }}</h1>
            <p class="text-sm text-gray-400">{{ plan.encounterName }}</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button
            data-testid="share-button"
            class="btn-secondary"
            @click="share"
          >
            Share
          </button>
          <button
            data-testid="save-button"
            class="btn-primary"
            @click="save"
          >
            Save
          </button>
        </div>
      </header>

      <!-- Editor Area -->
      <div class="flex flex-1 overflow-hidden">
        <!-- Left Sidebar: Marker Palette -->
        <aside class="w-64 bg-gray-800 border-r border-gray-700 overflow-y-auto">
          <MarkerPalette
            :selected-marker="selectedMarker"
            :selected-shape="selectedShape"
            :selected-color="selectedColor"
            :selected-tool="currentTool"
            :can-delete="canDelete"
            @marker-select="handleMarkerSelect"
            @shape-select="handleShapeSelect"
            @color-select="handleColorSelect"
            @tool-select="handleToolSelect"
            @delete="handleDelete"
          />
        </aside>

        <!-- Main Canvas Area -->
        <main class="flex-1 flex flex-col overflow-hidden">
          <div class="flex-1 flex items-center justify-center bg-gray-900 p-4">
            <PlanCanvas
              :width="800"
              :height="600"
              :markers="currentMarkers"
              :shapes="currentShapes"
              :zoom="zoom"
              :pan-x="panX"
              :pan-y="panY"
              :selected-marker-index="selectedMarkerIndex"
              :selected-shape-index="selectedShapeIndex"
              :current-tool="currentTool"
              @canvas-click="handleCanvasClick"
              @marker-click="handleMarkerClick"
              @shape-click="handleShapeClick"
              @zoom-change="handleZoomChange"
              @pan-change="handlePanChange"
            />
          </div>

          <!-- Bottom: Step Timeline -->
          <footer class="border-t border-gray-700">
            <StepTimeline
              :steps="plan.steps"
              :current-step="currentStep"
              @step-change="handleStepChange"
              @add-step="handleAddStep"
              @delete-step="handleDeleteStep"
              @notes-edit="handleNotesEdit"
            />
          </footer>
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.btn-primary {
  @apply px-4 py-2 bg-blue-600 hover:bg-blue-500 rounded text-white text-sm font-medium transition-colors;
}

.btn-secondary {
  @apply px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded text-white text-sm font-medium transition-colors;
}
</style>
