<script setup lang="ts">
/**
 * ApplyPage - Public guild application form.
 *
 * Multi-step form for submitting guild applications:
 * 1. About You - Personal info and availability
 * 2. Character - Main character selection
 * 3. Guild History - Previous guild and experience
 * 4. Motivation - Why join and what you bring
 * 5. Review - Review and submit
 */
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMutation } from '@tanstack/vue-query'
import { applicationsApi } from '@/api/applications'
import { useToast } from '@/composables/useToast'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { ProgressBar } from '@/components/charts'

const router = useRouter()
const toast = useToast()

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

            <div class="bg-gray-800/50 rounded-lg p-4">
              <p class="text-sm text-gray-400">
                We will automatically fetch your character data from Blizzard, Warcraft Logs,
                and Raider.IO to help officers review your application.
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
