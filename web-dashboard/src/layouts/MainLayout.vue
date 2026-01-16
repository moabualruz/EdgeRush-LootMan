<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { RouterView, RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useGuildContextStore } from '@/stores/guildContext'
import CharacterSelector from '@/components/CharacterSelector.vue'

const route = useRoute()
const authStore = useAuthStore()
const guildContextStore = useGuildContextStore()

// Fetch guilds when layout mounts
onMounted(() => {
  if (authStore.isAuthenticated) {
    guildContextStore.fetchGuilds()
  }
})

// Mobile sidebar state
const sidebarOpen = ref(false)

// Close sidebar when route changes (mobile)
watch(() => route.path, () => {
  sidebarOpen.value = false
})

const navItems = [
  { path: '/dashboard', label: 'Mission Control', icon: 'home' },
  { path: '/raids', label: 'Raid Operations', icon: 'sword' }, // Renamed for premium feel
  { path: '/wishlist', label: 'Loot & Wishlist', icon: 'gem' }, // Grouped concepts
  { path: '/leaderboard', label: 'Leaderboards', icon: 'trophy' },
  { path: '/performance', label: 'Analysis', icon: 'chart' },
  { path: '/attendance', label: 'Attendance', icon: 'calendar' },
]

const secondaryNavItems = [
  { path: '/gear', label: 'My Gear', icon: 'shield' },
  { path: '/history', label: 'History', icon: 'clock' },
]

const adminNavItems = [
  { path: '/admin', label: 'System Admin', icon: 'settings' },
  { path: '/admin/applications', label: 'Recruitment', icon: 'user-plus' },
  { path: '/admin/discord', label: 'Discord Bot', icon: 'message-circle' },
  { path: '/admin/sync', label: 'Data Sync', icon: 'refresh-cw' },
]

function handleLogout() {
  guildContextStore.clear()
  authStore.logout()
}

// Icon component helper (can be moved to separate component later)
const Icon = {
  props: ['name'],
  template: `
    <svg v-if="name === 'home'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" /></svg>
    <svg v-else-if="name === 'sword'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg> <!-- Using Lightning as Sword/Ops placeholder -->
    <svg v-else-if="name === 'gem'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" /></svg>
    <svg v-else-if="name === 'trophy'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" /></svg> <!-- Placeholder for Trophy -->
    <svg v-else-if="name === 'chart'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z" /></svg>
    <svg v-else-if="name === 'calendar'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
    <svg v-else-if="name === 'shield'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
    <svg v-else-if="name === 'history'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
    <svg v-else-if="name === 'clock'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
    <svg v-else-if="name === 'settings'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" /><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
    <svg v-else-if="name === 'user-plus'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" /></svg>
    <svg v-else-if="name === 'message'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>
    <svg v-else-if="name === 'message-circle'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>
    <svg v-else-if="name === 'refresh-cw'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
    <svg v-else-if="name === 'clipboard'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>
    <svg v-else-if="name === 'sync'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
    <svg v-else-if="name === 'list'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>
    <svg v-else-if="name === 'users'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" /></svg>
    <svg v-else xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
  `
}
</script>

<template>
  <div class="min-h-screen bg-background text-foreground flex">
    <!-- Mobile sidebar backdrop -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm md:hidden"
      @click="sidebarOpen = false"
    />

    <!-- Sidebar -->
    <aside
      class="fixed inset-y-0 left-0 z-50 w-72 glass border-r border-border transition-transform duration-300 md:translate-x-0 flex flex-col"
      :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <!-- Logo Area -->
      <div class="relative h-20 flex items-center px-6 border-b border-border/50">
        <div class="absolute inset-0 bg-primary/5 blur-xl"></div>
        <div class="relative flex items-center gap-3">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-accent flex items-center justify-center shadow-lg shadow-primary/20">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
          </div>
          <span class="text-xl font-bold tracking-tight text-white text-glow">LootMan</span>
        </div>
        
        <!-- Mobile Close -->
        <button
          @click="sidebarOpen = false"
          class="ml-auto p-2 text-muted-foreground hover:text-white md:hidden"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
        </button>
      </div>

      <!-- Character Selector & Context -->
      <div class="p-4 border-b border-border/50 bg-black/20">
        <CharacterSelector />
      </div>

      <!-- Navigation Scroll Area -->
      <nav class="flex-1 overflow-y-auto py-6 px-3 space-y-8 scrollbar-hide">
        
        <!-- Main Group -->
        <div class="space-y-1">
          <p class="px-3 text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">Operations</p>
          <RouterLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group"
            :class="[
              route.path === item.path
                ? 'bg-primary/10 text-primary border border-primary/20 shadow-sm shadow-primary/10'
                : 'text-muted-foreground hover:text-foreground hover:bg-white/5'
            ]"
          >
            <Icon :name="item.icon" class="w-5 h-5 transition-colors group-hover:text-primary" :class="route.path === item.path ? 'text-primary' : 'text-muted-foreground'" />
            {{ item.label }}
          </RouterLink>
        </div>

        <!-- Personal Group -->
        <div class="space-y-1">
          <p class="px-3 text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">My Raider</p>
          <RouterLink
            v-for="item in secondaryNavItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group"
            :class="[
              route.path === item.path
                ? 'bg-primary/10 text-primary border border-primary/20 shadow-sm shadow-primary/10'
                : 'text-muted-foreground hover:text-foreground hover:bg-white/5'
            ]"
          >
            <Icon :name="item.icon" class="w-5 h-5 transition-colors group-hover:text-primary" :class="route.path === item.path ? 'text-primary' : 'text-muted-foreground'" />
            {{ item.label }}
          </RouterLink>
        </div>

        <!-- Admin Group -->
        <div v-if="authStore.isAdmin" class="space-y-1">
          <p class="px-3 text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">Administration</p>
          <RouterLink
            v-for="item in adminNavItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group"
            :class="[
              route.path === item.path
                ? 'bg-destructive/10 text-destructive-foreground border border-destructive/20'
                : 'text-muted-foreground hover:text-destructive-foreground hover:bg-destructive/10'
            ]"
          >
            <Icon :name="item.icon" class="w-5 h-5" />
            {{ item.label }}
          </RouterLink>
        </div>
      </nav>

      <!-- User Footer -->
      <div class="p-4 border-t border-border/50 bg-black/20">
        <div class="flex items-center gap-3 p-2 rounded-lg hover:bg-white/5 transition-colors cursor-pointer group">
          <RouterLink to="/profile" class="flex items-center gap-3 flex-1 min-w-0">
            <div class="w-9 h-9 rounded-full bg-gradient-to-tr from-gray-700 to-gray-600 border border-white/10 flex items-center justify-center text-white ring-2 ring-transparent group-hover:ring-primary/50 transition-all">
               {{ authStore.user?.username?.charAt(0).toUpperCase() }}
            </div>
            <div class="flex flex-col min-w-0">
              <span class="text-sm font-medium text-white truncate group-hover:text-primary transition-colors">{{ authStore.user?.username }}</span>
              <span class="text-xs text-muted-foreground truncate">View Profile</span>
            </div>
          </RouterLink>
          <button
            @click="handleLogout"
            class="p-2 rounded-md text-muted-foreground hover:text-white hover:bg-white/10 transition-colors"
            title="Logout"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content Area -->
    <main class="flex-1 min-h-screen transition-all duration-300 md:pl-72 flex flex-col">
      <!-- Mobile Header -->
      <header class="md:hidden h-16 bg-background/80 backdrop-blur-md border-b border-border flex items-center px-4 sticky top-0 z-30">
        <button @click="sidebarOpen = true" class="p-2 -ml-2 text-muted-foreground">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" /></svg>
        </button>
        <span class="ml-4 text-lg font-bold text-white">LootMan</span>
      </header>

      <!-- Content Container -->
      <div class="flex-1 p-6 md:p-8 max-w-7xl mx-auto w-full animate-fade-in">
        <RouterView v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </div>
    </main>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
