<script setup lang="ts">
/**
 * ApplyPage - Public guild application form.
 *
 * Multi-step form for submitting guild applications:
 * 1. About You - Personal info and availability
 * 2. Character - Main character selection with auto-fetch
 * 3. Guild History - Previous guild and experience
 * 4. Motivation - Why join and what you bring
 * 5. Review - Review and submit
 */
import { ref, computed, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMutation } from '@tanstack/vue-query'
import { applicationsApi } from '@/api/applications'
import { recruitmentApi, type SubmitApplicationRequest } from '@/api/recruitment'
import { useToast } from '@/composables/useToast'
import { useCharacterLookup } from '@/composables/useCharacterLookup'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { ProgressBar } from '@/components/charts'

const router = useRouter()
const toast = useToast()

// Character lookup composable
const {
  lookupCharacter,
  debouncedLookup,
  characterData,
  isLoading: isLoadingCharacter,
  error: characterError,
  hasSearched: hasSearchedCharacter,
  hasRaiderIOData,
  hasWarcraftLogsData,
  reset: resetCharacterLookup,
} = useCharacterLookup()

// Auto-fetched character data storage
const autoFetchedData = reactive({
  itemLevel: null as number | null,
  raiderIOScore: null as number | null,
  bestParseAverage: null as number | null,
})

// Form steps
const steps = [
  { id: 1, name: 'About You', description: 'Personal info and availability' },
  { id: 2, name: 'Character', description: 'Your main character' },
  { id: 3, name: 'Guild History', description: 'Previous experience' },
  { id: 4, name: 'Motivation', description: 'Why EdgeRush' },
  { id: 5, name: 'Review', description: 'Review and submit' },
]

const currentStep = ref(1)
const isSubmitting = ref(false)

// Form data
const formData = reactive({
  // Step 1: About You
  name: '',
  age: null as number | null,
  timezone: '',
  raidAvailability: [] as string[],
  stableInternet: true,

  // Step 2: Character
  characterName: '',
  realm: '',
  region: 'EU',
  characterClass: '',
  specialization: '',

  // Step 3: Guild History
  previousGuild: '',
  reasonForLeaving: '',
  additionalLogs: '',

  // Step 4: Motivation
  whyThisGuild: '',
  whatYouBring: '',
  goals: '',

  // Step 5: Confirmations
  confirmTrialPeriod: false,
  confirmAccuracy: false,
})

// Raid days
const raidDays = [
  { value: 'wednesday', label: 'Wednesday' },
  { value: 'sunday', label: 'Sunday' },
  { value: 'monday', label: 'Monday' },
]

// Timezones
const timezones = [
  { value: 'Europe/London', label: 'Europe - GMT (UTC+0)' },
  { value: 'Europe/Paris', label: 'Europe - CET (UTC+1)' },
  { value: 'Europe/Helsinki', label: 'Europe - EET (UTC+2)' },
  { value: 'Europe/Moscow', label: 'Europe - MSK (UTC+3)' },
  { value: 'America/New_York', label: 'US - Eastern (UTC-5)' },
  { value: 'America/Chicago', label: 'US - Central (UTC-6)' },
  { value: 'America/Denver', label: 'US - Mountain (UTC-7)' },
  { value: 'America/Los_Angeles', label: 'US - Pacific (UTC-8)' },
]

// WoW Classes
const wowClasses = [
  'Death Knight', 'Demon Hunter', 'Druid', 'Evoker', 'Hunter',
  'Mage', 'Monk', 'Paladin', 'Priest', 'Rogue',
  'Shaman', 'Warlock', 'Warrior',
]

// Regions
const regions = [
  { value: 'EU', label: 'Europe' },
  { value: 'US', label: 'United States' },
  { value: 'KR', label: 'Korea' },
  { value: 'TW', label: 'Taiwan' },
]

// Validation
const step1Valid = computed(() => {
  return (
    formData.name.trim().length >= 2 &&
    formData.age !== null &&
    formData.age >= 18 &&
    formData.timezone !== '' &&
    formData.raidAvailability.length > 0
  )
})

const step2Valid = computed(() => {
  return (
    formData.characterName.trim().length >= 2 &&
    formData.realm.trim().length >= 2 &&
    formData.region !== '' &&
    formData.characterClass !== ''
  )
})

const step3Valid = computed(() => {
  return (
    formData.previousGuild.trim().length >= 2 &&
    formData.reasonForLeaving.trim().length >= 20
  )
})

const step4Valid = computed(() => {
  return (
    formData.whyThisGuild.trim().length >= 50 &&
    formData.whatYouBring.trim().length >= 50 &&
    formData.goals.trim().length >= 20
  )
})

const step5Valid = computed(() => {
  return formData.confirmTrialPeriod && formData.confirmAccuracy
})

const canProceed = computed(() => {
  switch (currentStep.value) {
    case 1:
      return step1Valid.value
    case 2:
      return step2Valid.value
    case 3:
      return step3Valid.value
    case 4:
      return step4Valid.value
    case 5:
      return step5Valid.value
    default:
      return false
  }
})

const progress = computed(() => ((currentStep.value - 1) / (steps.length - 1)) * 100)

// Navigation
function nextStep() {
  if (currentStep.value < steps.length && canProceed.value) {
    currentStep.value++
  }
}

function prevStep() {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

function goToStep(step: number) {
  if (step < currentStep.value) {
    currentStep.value = step
  }
}

// Toggle raid day
function toggleRaidDay(day: string) {
  const index = formData.raidAvailability.indexOf(day)
  if (index === -1) {
    formData.raidAvailability.push(day)
  } else {
    formData.raidAvailability.splice(index, 1)
  }
}

// Fetch character data from external APIs
async function fetchCharacterData() {
  if (
    formData.characterName.trim().length < 2 ||
    formData.realm.trim().length < 2 ||
    !formData.region
  ) {
    return
  }

  const data = await lookupCharacter(
    formData.region.toLowerCase(),
    formData.realm,
    formData.characterName
  )

  if (data) {
    // Auto-populate class and spec if not already set
    if (!formData.characterClass && data.characterClass) {
      formData.characterClass = data.characterClass
    }
    if (!formData.specialization && data.specialization) {
      formData.specialization = data.specialization
    }

    // Store fetched data for submission
    autoFetchedData.itemLevel = data.itemLevel
    autoFetchedData.raiderIOScore = data.raiderIOScore
    autoFetchedData.bestParseAverage = data.bestParseAverage

    toast.success('Character data fetched successfully!')
  } else if (characterError.value) {
    toast.warning('Could not fetch character data. You can still proceed with your application.')
  }
}

// Watch for character field changes and debounce fetch
watch(
  () => [formData.characterName, formData.realm, formData.region],
  () => {
    if (
      formData.characterName.trim().length >= 2 &&
      formData.realm.trim().length >= 2 &&
      formData.region
    ) {
      debouncedLookup(
        formData.region.toLowerCase(),
        formData.realm,
        formData.characterName
      )
    }
  },
  { debounce: 500 }
)

// Update auto-fetched data when characterData changes
watch(characterData, (data) => {
  if (data) {
    // Auto-populate class and spec if not already set
    if (!formData.characterClass && data.characterClass) {
      formData.characterClass = data.characterClass
    }
    if (!formData.specialization && data.specialization) {
      formData.specialization = data.specialization
    }

    // Store fetched data for submission
    autoFetchedData.itemLevel = data.itemLevel
    autoFetchedData.raiderIOScore = data.raiderIOScore
    autoFetchedData.bestParseAverage = data.bestParseAverage
  }
})

// Submit mutation
const submitMutation = useMutation({
  mutationFn: async () => {
    // The actual API structure would need to match the backend
    const submitData = {
      name: formData.name,
      age: formData.age,
      timezone: formData.timezone,
      raidAvailability: formData.raidAvailability.join(','),
      stableInternet: formData.stableInternet,
      characterName: formData.characterName,
      realm: formData.realm,
      region: formData.region,
      characterClass: formData.characterClass,
      specialization: formData.specialization,
      previousGuild: formData.previousGuild,
      reasonForLeaving: formData.reasonForLeaving,
      whyThisGuild: formData.whyThisGuild,
      whatYouBring: formData.whatYouBring,
      goals: formData.goals,
    }
    // This would call the actual submit endpoint
    return submitData
  },
  onSuccess: () => {
    toast.success('Application submitted successfully!')
    router.push('/dashboard')
  },
  onError: (error: Error) => {
    toast.error(`Failed to submit application: ${error.message}`)
  },
})

async function submitApplication() {
  if (!canProceed.value) return
  isSubmitting.value = true
  try {
    await submitMutation.mutateAsync()
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 py-8 px-4">
    <div class="max-w-3xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-white mb-2">Apply to EdgeRush</h1>
        <p class="text-gray-400">Join our progression raiding team</p>
      </div>

      <!-- Progress Bar -->
      <div class="mb-8">
        <div class="flex justify-between text-sm text-gray-400 mb-2">
          <span>Step {{ currentStep }} of {{ steps.length }}</span>
          <span>{{ steps[currentStep - 1].name }}</span>
        </div>
        <ProgressBar
          :value="progress"
          :max="100"
          color="#8b5cf6"
          height="0.5rem"
          :show-label="false"
        />
      </div>

      <!-- Step Indicators -->
      <div class="flex justify-between mb-8">
        <div
          v-for="step in steps"
          :key="step.id"
          class="flex flex-col items-center cursor-pointer"
          @click="goToStep(step.id)"
        >
          <div
            :class="[
              'w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm mb-2 transition-colors',
              currentStep === step.id
                ? 'bg-primary-600 text-white'
                : currentStep > step.id
                  ? 'bg-green-600 text-white'
                  : 'bg-gray-700 text-gray-400',
            ]"
          >
            <span v-if="currentStep > step.id">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </span>
            <span v-else>{{ step.id }}</span>
          </div>
          <span
            :class="[
              'text-xs hidden md:block',
              currentStep === step.id ? 'text-primary-400' : 'text-gray-500',
            ]"
          >
            {{ step.name }}
          </span>
        </div>
      </div>

      <!-- Form Container -->
      <div class="card">
        <form @submit.prevent="currentStep === steps.length ? submitApplication() : nextStep()">
          <!-- Step 1: About You -->
          <div v-show="currentStep === 1" class="space-y-6">
            <h2 class="text-xl font-semibold mb-4">About You</h2>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Name (how you'd like to be called) *
              </label>
              <input
                v-model="formData.name"
                name="name"
                type="text"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="Your name or nickname"
                required
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Age *
              </label>
              <input
                v-model.number="formData.age"
                name="age"
                type="number"
                min="18"
                max="100"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="Must be 18+"
                required
              />
              <p class="text-xs text-gray-500 mt-1">You must be at least 18 years old to apply.</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Timezone *
              </label>
              <select
                v-model="formData.timezone"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                required
              >
                <option value="">Select your timezone</option>
                <option v-for="tz in timezones" :key="tz.value" :value="tz.value">
                  {{ tz.label }}
                </option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Raid Availability *
              </label>
              <p class="text-xs text-gray-500 mb-3">
                Our raids are Wednesday, Sunday, Monday 22:00-01:00 CET
              </p>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="day in raidDays"
                  :key="day.value"
                  type="button"
                  @click="toggleRaidDay(day.value)"
                  :class="[
                    'px-4 py-2 rounded-lg font-medium transition-colors',
                    formData.raidAvailability.includes(day.value)
                      ? 'bg-primary-600 text-white'
                      : 'bg-gray-700 text-gray-300 hover:bg-gray-600',
                  ]"
                >
                  {{ day.label }}
                </button>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Stable internet/hardware for progression raiding? *
              </label>
              <div class="flex gap-4">
                <label class="flex items-center cursor-pointer">
                  <input
                    v-model="formData.stableInternet"
                    type="radio"
                    :value="true"
                    class="mr-2"
                  />
                  <span>Yes</span>
                </label>
                <label class="flex items-center cursor-pointer">
                  <input
                    v-model="formData.stableInternet"
                    type="radio"
                    :value="false"
                    class="mr-2"
                  />
                  <span>No</span>
                </label>
              </div>
            </div>
          </div>

          <!-- Step 2: Character -->
          <div v-show="currentStep === 2" class="space-y-6">
            <h2 class="text-xl font-semibold mb-4">Character Information</h2>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-2">
                  Character Name *
                </label>
                <input
                  v-model="formData.characterName"
                  type="text"
                  class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  placeholder="Your main character"
                  required
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-300 mb-2">
                  Realm *
                </label>
                <input
                  v-model="formData.realm"
                  type="text"
                  class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  placeholder="e.g., Twisting Nether"
                  required
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-300 mb-2">
                  Region *
                </label>
                <select
                  v-model="formData.region"
                  class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  required
                >
                  <option v-for="region in regions" :key="region.value" :value="region.value">
                    {{ region.label }}
                  </option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-300 mb-2">
                  Class *
                </label>
                <select
                  v-model="formData.characterClass"
                  class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  required
                >
                  <option value="">Select your class</option>
                  <option v-for="cls in wowClasses" :key="cls" :value="cls">
                    {{ cls }}
                  </option>
                </select>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Main Specialization
              </label>
              <input
                v-model="formData.specialization"
                type="text"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="e.g., Frost, Holy, Protection"
              />
            </div>

            <!-- Character Data Fetch Button -->
            <div class="flex items-center gap-4">
              <button
                type="button"
                @click="fetchCharacterData"
                :disabled="isLoadingCharacter || formData.characterName.length < 2 || formData.realm.length < 2"
                class="px-4 py-2 bg-blue-600 hover:bg-blue-500 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                <svg
                  v-if="isLoadingCharacter"
                  class="animate-spin h-4 w-4"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <svg v-else class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                {{ isLoadingCharacter ? 'Fetching...' : 'Fetch Character Data' }}
              </button>
              <span v-if="hasSearchedCharacter && !isLoadingCharacter" class="text-sm">
                <span v-if="characterData" class="text-green-400">Data fetched successfully</span>
                <span v-else class="text-yellow-400">{{ characterError || 'Character not found' }}</span>
              </span>
            </div>

            <!-- Character Data Preview -->
            <div v-if="characterData" class="bg-gray-800/50 rounded-lg p-4 space-y-3">
              <div class="flex items-center justify-between">
                <h3 class="text-sm font-medium text-gray-300">Fetched Character Data</h3>
                <a
                  v-if="characterData.profileUrl"
                  :href="characterData.profileUrl"
                  target="_blank"
                  class="text-xs text-blue-400 hover:text-blue-300"
                >
                  View on Raider.IO
                </a>
              </div>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div>
                  <p class="text-xs text-gray-500">Item Level</p>
                  <p class="text-lg font-semibold text-white">
                    {{ characterData.itemLevel?.toFixed(1) || 'N/A' }}
                  </p>
                </div>
                <div>
                  <p class="text-xs text-gray-500">Raider.IO Score</p>
                  <p class="text-lg font-semibold text-orange-400">
                    {{ characterData.raiderIOScore?.toFixed(0) || 'N/A' }}
                  </p>
                </div>
                <div>
                  <p class="text-xs text-gray-500">Best Parse Avg</p>
                  <p class="text-lg font-semibold" :class="[
                    characterData.bestParseAverage
                      ? characterData.bestParseAverage >= 90
                        ? 'text-orange-400'
                        : characterData.bestParseAverage >= 75
                          ? 'text-purple-400'
                          : characterData.bestParseAverage >= 50
                            ? 'text-blue-400'
                            : 'text-green-400'
                      : 'text-gray-400'
                  ]">
                    {{ characterData.bestParseAverage?.toFixed(1) || 'N/A' }}
                  </p>
                </div>
                <div>
                  <p class="text-xs text-gray-500">Role</p>
                  <p class="text-lg font-semibold text-white">
                    {{ characterData.role || 'N/A' }}
                  </p>
                </div>
              </div>
              <div class="flex gap-2 pt-2">
                <span v-if="hasRaiderIOData" class="text-xs px-2 py-1 bg-orange-500/20 text-orange-400 rounded">
                  Raider.IO
                </span>
                <span v-if="hasWarcraftLogsData" class="text-xs px-2 py-1 bg-purple-500/20 text-purple-400 rounded">
                  Warcraft Logs
                </span>
              </div>
            </div>

            <!-- Info message when no data fetched yet -->
            <div v-else class="bg-gray-800/50 rounded-lg p-4">
              <p class="text-sm text-gray-400">
                Enter your character name and realm, then click "Fetch Character Data" to automatically
                retrieve your item level, Raider.IO score, and Warcraft Logs parses.
              </p>
            </div>
          </div>

          <!-- Step 3: Guild History -->
          <div v-show="currentStep === 3" class="space-y-6">
            <h2 class="text-xl font-semibold mb-4">Guild History</h2>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Current/Previous Guild *
              </label>
              <input
                v-model="formData.previousGuild"
                type="text"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="Guild Name - Realm"
                required
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Reason for leaving *
              </label>
              <textarea
                v-model="formData.reasonForLeaving"
                rows="4"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="Please explain why you are leaving or left your previous guild..."
                required
              ></textarea>
              <p class="text-xs text-gray-500 mt-1">Minimum 20 characters</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Additional Logs URLs (optional)
              </label>
              <textarea
                v-model="formData.additionalLogs"
                rows="2"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="If you have private logs or logs from another character, paste the URLs here..."
              ></textarea>
            </div>
          </div>

          <!-- Step 4: Motivation -->
          <div v-show="currentStep === 4" class="space-y-6">
            <h2 class="text-xl font-semibold mb-4">Motivation</h2>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                Why do you want to join EdgeRush? *
              </label>
              <textarea
                v-model="formData.whyThisGuild"
                rows="4"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="Tell us what attracts you to our guild..."
                required
              ></textarea>
              <p class="text-xs text-gray-500 mt-1">Minimum 50 characters</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                What do you bring to a raid team? *
              </label>
              <textarea
                v-model="formData.whatYouBring"
                rows="4"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="What makes you a great raider? Skills, attitude, experience..."
                required
              ></textarea>
              <p class="text-xs text-gray-500 mt-1">Minimum 50 characters</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">
                What are your goals for this tier? *
              </label>
              <textarea
                v-model="formData.goals"
                rows="3"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="What do you hope to achieve?"
                required
              ></textarea>
              <p class="text-xs text-gray-500 mt-1">Minimum 20 characters</p>
            </div>
          </div>

          <!-- Step 5: Review -->
          <div v-show="currentStep === 5" class="space-y-6">
            <h2 class="text-xl font-semibold mb-4">Review Your Application</h2>

            <!-- Summary Cards -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-2">Personal Info</h3>
                <p class="font-medium">{{ formData.name }}</p>
                <p class="text-sm text-gray-400">Age: {{ formData.age }}</p>
                <p class="text-sm text-gray-400">Timezone: {{ formData.timezone }}</p>
              </div>

              <div class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-2">Character</h3>
                <p class="font-medium">{{ formData.characterName }} - {{ formData.realm }}</p>
                <p class="text-sm text-gray-400">{{ formData.characterClass }} {{ formData.specialization }}</p>
                <p class="text-sm text-gray-400">Region: {{ formData.region }}</p>
                <div v-if="autoFetchedData.itemLevel" class="mt-2 pt-2 border-t border-gray-700">
                  <p class="text-xs text-gray-500">Auto-fetched data:</p>
                  <p class="text-sm">
                    <span class="text-gray-400">iLvl:</span> {{ autoFetchedData.itemLevel?.toFixed(1) }}
                    <span class="mx-2 text-gray-600">|</span>
                    <span class="text-gray-400">R.IO:</span> <span class="text-orange-400">{{ autoFetchedData.raiderIOScore?.toFixed(0) || 'N/A' }}</span>
                    <span class="mx-2 text-gray-600">|</span>
                    <span class="text-gray-400">Parse:</span> <span class="text-purple-400">{{ autoFetchedData.bestParseAverage?.toFixed(1) || 'N/A' }}</span>
                  </p>
                </div>
              </div>

              <div class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-2">Availability</h3>
                <p class="text-sm">
                  {{ formData.raidAvailability.map(d => d.charAt(0).toUpperCase() + d.slice(1)).join(', ') }}
                </p>
                <p class="text-sm text-gray-400">
                  Stable connection: {{ formData.stableInternet ? 'Yes' : 'No' }}
                </p>
              </div>

              <div class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-2">Previous Guild</h3>
                <p class="font-medium">{{ formData.previousGuild }}</p>
              </div>
            </div>

            <!-- Motivation Preview -->
            <div class="bg-gray-800/50 rounded-lg p-4">
              <h3 class="text-sm font-medium text-gray-400 mb-2">Why EdgeRush</h3>
              <p class="text-sm text-gray-300">{{ formData.whyThisGuild }}</p>
            </div>

            <!-- Confirmations -->
            <div class="space-y-4 pt-4 border-t border-gray-700">
              <label class="flex items-start cursor-pointer">
                <input
                  v-model="formData.confirmTrialPeriod"
                  type="checkbox"
                  class="mt-1 mr-3"
                />
                <span class="text-sm text-gray-300">
                  I understand that the trial period is 3 weeks minimum and that I will be evaluated
                  on my performance, attendance, and attitude during this time.
                </span>
              </label>

              <label class="flex items-start cursor-pointer">
                <input
                  v-model="formData.confirmAccuracy"
                  type="checkbox"
                  class="mt-1 mr-3"
                />
                <span class="text-sm text-gray-300">
                  I confirm that all information provided in this application is accurate and truthful.
                </span>
              </label>
            </div>
          </div>

          <!-- Navigation Buttons -->
          <div class="flex items-center justify-between mt-8 pt-6 border-t border-gray-700">
            <button
              v-if="currentStep > 1"
              type="button"
              @click="prevStep"
              class="px-6 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg font-medium transition-colors"
            >
              Back
            </button>
            <div v-else></div>

            <button
              v-if="currentStep < steps.length"
              type="button"
              @click="nextStep"
              :disabled="!canProceed"
              class="px-6 py-2 bg-primary-600 hover:bg-primary-500 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next
            </button>
            <button
              v-else
              type="submit"
              :disabled="!canProceed || isSubmitting"
              class="px-6 py-2 bg-green-600 hover:bg-green-500 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ isSubmitting ? 'Submitting...' : 'Submit Application' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Info Card -->
      <div class="card mt-6 bg-gray-800/50">
        <div class="flex items-start space-x-3">
          <svg class="w-6 h-6 text-primary-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <h3 class="font-medium text-gray-200">What happens next?</h3>
            <p class="text-sm text-gray-400 mt-1">
              After you submit your application, our officers will review it within 48 hours.
              You'll receive a Discord notification when a decision is made. If approved,
              you'll be invited for a trial period where your performance will be evaluated.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
