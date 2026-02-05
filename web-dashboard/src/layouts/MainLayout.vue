<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { RouterView, RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useGuildContextStore } from '@/stores/guildContext'
import CharacterSelector from '@/components/CharacterSelector.vue'

import NavigationIcon from '@/components/NavigationIcon.vue'

const route = useRoute()
const authStore = useAuthStore()
const guildContextStore = useGuildContextStore()

// Fetch guilds when layout mounts
onMounted(() => {
  if (authStore.isAuthenticated) {
    guildContextStore.fetchGuilds()
  }
})

// Watch for delayed auth (e.g. page refresh)
watch(() => authStore.isAuthenticated, (isAuthenticated) => {
  if (isAuthenticated) {
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

const router = useRouter()

function handleLogout() {
  guildContextStore.clear()
  authStore.logout()
  router.push('/login')
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
            <NavigationIcon :name="item.icon" class="w-5 h-5 transition-colors group-hover:text-primary" :class="route.path === item.path ? 'text-primary' : 'text-muted-foreground'" />
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
            <NavigationIcon :name="item.icon" class="w-5 h-5 transition-colors group-hover:text-primary" :class="route.path === item.path ? 'text-primary' : 'text-muted-foreground'" />
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
            <NavigationIcon :name="item.icon" class="w-5 h-5" />
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
