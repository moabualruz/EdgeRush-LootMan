<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { api } from '@/api/client'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const discordUrl = ref('')
const battlenetUrl = ref('')

// Format date helper
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// Fetch OAuth URLs
const fetchOAuthUrls = async () => {
  try {
    const { data: discordData } = await api.get('/v1/auth/discord/url')
    discordUrl.value = discordData.url

    const { data: bnetData } = await api.get('/v1/auth/battlenet/url')
    battlenetUrl.value = bnetData.url
  } catch (err) {
    console.error('Failed to fetch OAuth URLs', err)
  }
}

onMounted(() => {
  fetchOAuthUrls()
})
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">My Profile</h1>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6" v-if="user">
      <!-- User Info Card -->
      <div class="card space-y-4">
        <h2 class="text-lg font-semibold border-b border-gray-700 pb-2">User Information</h2>
        
        <div class="flex items-center space-x-4">
          <div class="w-16 h-16 rounded-full bg-primary-600 flex items-center justify-center text-2xl font-bold">
            {{ user.username.charAt(0).toUpperCase() }}
          </div>
          <div>
            <div class="text-xl font-bold text-white">{{ user.username }}</div>
            <div class="text-sm text-gray-400">{{ user.email }}</div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4 pt-2">
          <div>
            <div class="text-xs text-gray-500 uppercase">Role</div>
            <div class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary-900/50 text-primary-200 border border-primary-700/50 mt-1">
              {{ user.role }}
            </div>
          </div>
          <div>
            <div class="text-xs text-gray-500 uppercase">Member Since</div>
            <div class="text-gray-300 mt-1">{{ formatDate(user.createdAt?.toString()) }}</div>
          </div>
          <div class="col-span-2">
            <div class="text-xs text-gray-500 uppercase">Guild Status</div>
            <div class="mt-1">
              <span v-if="user.guildId" class="text-green-400 flex items-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                Member (ID: {{ user.guildId }})
              </span>
              <span v-else class="text-yellow-500 flex items-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                No Guild
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Linked Accounts Card -->
      <div class="card space-y-4">
        <h2 class="text-lg font-semibold border-b border-gray-700 pb-2">Linked Accounts</h2>

        <!-- Discord -->
        <div class="flex items-center justify-between p-3 bg-gray-800/50 rounded-lg border border-gray-700">
          <div class="flex items-center space-x-3">
            <div class="text-[#5865F2]">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037 19.019 19.019 0 0 0-3.361 6.912 19.014 19.014 0 0 0-3.356-6.913.075.075 0 0 0-.079-.037A19.736 19.736 0 0 0 "></path><path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037 19.019 19.019 0 0 0-3.361 6.912 19.014 19.014 0 0 0-3.356-6.913.075.075 0 0 0-.079-.037A19.736 19.736 0 0 0 1.255 12.37C1.41 12.63 1.6 13.075 1.77 13.56A18.877 18.877 0 0 0 2.126 14.5a.078.078 0 0 0 .117.027c.484-.23.94-.492 1.353-.787a.076.076 0 0 0 .025-.094 13.12 13.12 0 0 1-1.996-3.136.074.074 0 0 1 .055-.104c.145-.045.292-.093.435-.145.076-.027.152-.056.225-.087a.074.074 0 0 1 .094.015c3.67 2.82 8.163 2.82 11.758 0a.074.074 0 0 1 .094-.015c.074.03.15.06.225.087.143.052.29.1.435.145a.074.074 0 0 1 .055.104 13.155 13.155 0 0 1-2.007 3.146.076.076 0 0 0 .025.093c.414.296.87.558 1.354.788a.078.078 0 0 0 .117-.027c.18-.485.37-.93.525-1.465.174-.61.272-1.254.272-1.92h-4.99c-.668 0-1.21-.492-1.21-1.1s.542-1.1 1.21-1.1h5.95c3.27 0 5.92 2.16 5.92 4.825 0 2.665-2.65 4.825-5.92 4.825H8.08c-3.27 0-5.92-2.16-5.92-4.825h1.22c0 1.665 2.108 3.015 4.7 3.015 2.592 0 4.7-1.35 4.7-3.015 0-1.665-2.108-3.015-4.7-3.015C6.42 12.37 5.07 13.565 5.07 15.025c0 .325.068.636.185.925H4.11a3.02 3.02 0 0 0-.25 1.075c0 1.665 1.35 3.015 3.015 3.015h11.25c1.665 0 3.015-1.35 3.015-3.015 0-.37-.08-.73-.225-1.075H19.76c.117-.29.185-.6.185-.925 0-1.46-1.35-2.655-3.015-2.655z"></path></svg>
              <!-- simplified discord icon path replacement for brevity, assume valid svg -->
              <span class="font-medium text-white ml-2">Discord</span>
            </div>
            <span v-if="user.discordId" class="text-xs text-green-400 border border-green-800 bg-green-900/30 px-2 py-0.5 rounded">Connected</span>
            <span v-else class="text-xs text-gray-500 border border-gray-700 bg-gray-800 px-2 py-0.5 rounded">Not Connected</span>
          </div>
          <a v-if="!user.discordId" :href="discordUrl" class="btn btn-sm btn-primary">Connect</a>
          <button v-else disabled class="text-sm text-gray-500 cursor-not-allowed">Linked</button>
        </div>

        <!-- Battle.net -->
        <div class="flex items-center justify-between p-3 bg-gray-800/50 rounded-lg border border-gray-700">
          <div class="flex items-center space-x-3">
            <div class="text-[#00AEFF]">
               <!-- Battle.net icon placeholder -->
               <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"></path></svg>
               <span class="font-medium text-white ml-2">Battle.net</span>
            </div>
            <span v-if="user.battlenetId" class="text-xs text-green-400 border border-green-800 bg-green-900/30 px-2 py-0.5 rounded">Connected</span>
            <span v-else class="text-xs text-gray-500 border border-gray-700 bg-gray-800 px-2 py-0.5 rounded">Not Connected</span>
          </div>
           <a v-if="!user.battlenetId" :href="battlenetUrl" class="btn btn-sm btn-primary">Connect</a>
           <button v-else disabled class="text-sm text-gray-500 cursor-not-allowed">Linked</button>
        </div>
      </div>
    </div>
    
  </div>
</template>
