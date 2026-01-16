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
  <div class="space-y-8 animate-fade-in">
    <div>
      <h1 class="text-3xl font-bold tracking-tight text-white mb-2 text-glow">My Profile</h1>
      <p class="text-muted-foreground">Manage your identity and linked connections</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6" v-if="user">
      <!-- User Info Card -->
      <div class="glass-card p-6 border-white/10 flex flex-col h-full bg-gradient-to-br from-black/40 to-black/20">
        <h2 class="text-lg font-semibold text-white mb-6 flex items-center gap-2">
          <svg class="w-5 h-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
          User Information
        </h2>
        
        <div class="flex items-start gap-5 mb-8">
          <div class="w-20 h-20 rounded-2xl bg-gradient-to-br from-primary to-accent flex items-center justify-center text-4xl font-bold text-white shadow-lg shadow-primary/20 shrink-0">
            {{ user.username.charAt(0).toUpperCase() }}
          </div>
          <div class="flex-1 min-w-0 py-1">
            <div class="text-2xl font-bold text-white truncate mb-1">{{ user.username }}</div>
            <div class="text-sm text-muted-foreground truncate">{{ user.email }}</div>
            <div class="mt-3 flex flex-wrap gap-2">
               <div class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary/10 text-primary border border-primary/20">
                {{ user.role }}
              </div>
              <div class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-white/5 text-muted-foreground border border-white/10">
                Joined {{ formatDate(user.createdAt?.toString()) }}
              </div>
            </div>
          </div>
        </div>

        <div class="mt-auto pt-6 border-t border-white/10">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Guild Status</span>
            <span v-if="user.guildId" class="text-green-400 flex items-center text-sm font-medium bg-green-500/10 px-3 py-1 rounded-full border border-green-500/20">
              <span class="w-2 h-2 rounded-full bg-green-500 mr-2 animate-pulse"></span>
              Member (ID: {{ user.guildId }})
            </span>
            <span v-else class="text-yellow-400 flex items-center text-sm font-medium bg-yellow-500/10 px-3 py-1 rounded-full border border-yellow-500/20">
              <span class="w-2 h-2 rounded-full bg-yellow-500 mr-2"></span>
              No Guild
            </span>
          </div>
        </div>
      </div>

      <!-- Linked Accounts Card -->
      <div class="glass-card p-6 border-white/10 flex flex-col h-full bg-gradient-to-br from-black/40 to-black/20">
        <h2 class="text-lg font-semibold text-white mb-6 flex items-center gap-2">
          <svg class="w-5 h-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" /></svg>
          Linked Accounts
        </h2>

        <div class="space-y-4">
          <!-- Discord -->
          <div class="p-4 rounded-xl border border-white/5 bg-black/20 hover:bg-black/40 transition-colors">
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <div class="text-[#5865F2] bg-[#5865F2]/10 p-2 rounded-lg">
                  <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037 19.019 19.019 0 0 0-3.361 6.912 19.014 19.014 0 0 0-3.356-6.913.075.075 0 0 0-.079-.037A19.736 19.736 0 0 0 1.255 12.37C1.41 12.63 1.6 13.075 1.77 13.56A18.877 18.877 0 0 0 2.126 14.5a.078.078 0 0 0 .117.027c.484-.23.94-.492 1.353-.787a.076.076 0 0 0 .025-.094 13.12 13.12 0 0 1-1.996-3.136.074.074 0 0 1 .055-.104c.145-.045.292-.093.435-.145.076-.027.152-.056.225-.087a.074.074 0 0 1 .094.015c3.67 2.82 8.163 2.82 11.758 0a.074.074 0 0 1 .094-.015c.074.03.15.06.225.087.143.052.29.1.435.145a.074.074 0 0 1 .055.104 13.155 13.155 0 0 1-2.007 3.146.076.076 0 0 0 .025.093c.414.296.87.558 1.354.788a.078.078 0 0 0 .117-.027c.18-.485.37-.93.525-1.465.174-.61.272-1.254.272-1.92h-4.99c-.668 0-1.21-.492-1.21-1.1s.542-1.1 1.21-1.1h5.95c3.27 0 5.92 2.16 5.92 4.825 0 2.665-2.65 4.825-5.92 4.825H8.08c-3.27 0-5.92-2.16-5.92-4.825h1.22c0 1.665 2.108 3.015 4.7 3.015 2.592 0 4.7-1.35 4.7-3.015 0-1.665-2.108-3.015-4.7-3.015C6.42 12.37 5.07 13.565 5.07 15.025c0 .325.068.636.185.925H4.11a3.02 3.02 0 0 0-.25 1.075c0 1.665 1.35 3.015 3.015 3.015h11.25c1.665 0 3.015-1.35 3.015-3.015 0-.37-.08-.73-.225-1.075H19.76c.117-.29.185-.6.185-.925 0-1.46-1.35-2.655-3.015-2.655z"></path></svg>
                </div>
                <div>
                  <div class="font-medium text-white">Discord</div>
                  <div class="text-xs text-muted-foreground">Identity & Communication</div>
                </div>
              </div>
              <span v-if="user.discordId" class="text-xs font-medium text-green-400 border border-green-500/20 bg-green-500/10 px-2.5 py-1 rounded-full flex items-center gap-1.5">
                <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                Connected
              </span>
              <span v-else class="text-xs font-medium text-muted-foreground border border-white/10 bg-white/5 px-2.5 py-1 rounded-full">Not Connected</span>
            </div>
            
            <a v-if="!user.discordId" :href="discordUrl" class="block w-full text-center py-2 rounded-lg bg-[#5865F2] hover:bg-[#4752c4] text-white text-sm font-semibold transition-colors shadow-lg shadow-[#5865F2]/20">
              Connect Discord
            </a>
            <button v-else disabled class="w-full py-2 rounded-lg bg-white/5 border border-white/10 text-muted-foreground text-sm font-medium cursor-not-allowed flex items-center justify-center gap-2">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
              Account Linked
            </button>
          </div>

          <!-- Battle.net -->
          <div class="p-4 rounded-xl border border-white/5 bg-black/20 hover:bg-black/40 transition-colors">
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <div class="text-[#148EFF] bg-[#148EFF]/10 p-2 rounded-lg">
                   <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"></path></svg>
                </div>
                <div>
                  <div class="font-medium text-white">Battle.net</div>
                  <div class="text-xs text-muted-foreground">Game Data & Imports</div>
                </div>
              </div>
              <span v-if="user.battlenetId" class="text-xs font-medium text-green-400 border border-green-500/20 bg-green-500/10 px-2.5 py-1 rounded-full flex items-center gap-1.5">
                <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                Connected
              </span>
              <span v-else class="text-xs font-medium text-muted-foreground border border-white/10 bg-white/5 px-2.5 py-1 rounded-full">Not Connected</span>
            </div>

             <a v-if="!user.battlenetId" :href="battlenetUrl" class="block w-full text-center py-2 rounded-lg bg-[#148EFF] hover:bg-[#0070dd] text-white text-sm font-semibold transition-colors shadow-lg shadow-[#148EFF]/20">
              Connect Battle.net
            </a>
            <button v-else disabled class="w-full py-2 rounded-lg bg-white/5 border border-white/10 text-muted-foreground text-sm font-medium cursor-not-allowed flex items-center justify-center gap-2">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
              Account Linked
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
