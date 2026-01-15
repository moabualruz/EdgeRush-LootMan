<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterView, RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

// Mobile sidebar state
const sidebarOpen = ref(false)

// Close sidebar when route changes (mobile)
watch(() => route.path, () => {
  sidebarOpen.value = false
})

const navItems = [
  { path: '/dashboard', label: 'Dashboard', icon: 'home' },
  { path: '/leaderboard', label: 'Leaderboard', icon: 'trophy' },
  { path: '/wishlist', label: 'Wishlist', icon: 'list' },
  { path: '/performance', label: 'Performance', icon: 'chart' },
  { path: '/attendance', label: 'Attendance', icon: 'calendar' },
  { path: '/raids', label: 'Raids', icon: 'users' },
  { path: '/gear', label: 'Gear', icon: 'shield' },
  { path: '/history', label: 'Loot History', icon: 'history' },
]

const adminNavItems = [
  { path: '/admin', label: 'Admin Panel', icon: 'settings' },
  { path: '/admin/applications', label: 'Applications', icon: 'clipboard' },
  { path: '/admin/discord', label: 'Discord', icon: 'message' },
  { path: '/admin/sync', label: 'Sync History', icon: 'sync' },
]

function handleLogout() {
  authStore.logout()
}
</script>

<template>
  <div class="min-h-screen bg-gray-900">
    <!-- Mobile header with hamburger -->
    <header class="fixed top-0 left-0 right-0 z-40 flex items-center h-16 px-4 bg-gray-800 border-b border-gray-700 md:hidden">
      <button
        @click="sidebarOpen = !sidebarOpen"
        class="p-2 -ml-2 text-gray-400 hover:text-white focus:outline-none focus:ring-2 focus:ring-primary-500 rounded-md"
        aria-label="Toggle navigation"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            v-if="!sidebarOpen"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 6h16M4 12h16M4 18h16"
          />
          <path
            v-else
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M6 18L18 6M6 6l12 12"
          />
        </svg>
      </button>
      <span class="ml-4 text-xl font-bold text-primary-400">LootMan</span>
    </header>

    <!-- Mobile backdrop -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-40 bg-black/50 md:hidden"
      @click="sidebarOpen = false"
    />

    <!-- Sidebar -->
    <aside
      class="fixed inset-y-0 left-0 z-50 w-64 bg-gray-800 border-r border-gray-700 transition-transform duration-300 md:translate-x-0"
      :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <!-- Logo -->
      <div class="flex items-center h-16 px-6 border-b border-gray-700">
        <span class="text-xl font-bold text-primary-400">LootMan</span>
        <!-- Close button on mobile -->
        <button
          @click="sidebarOpen = false"
          class="ml-auto p-2 text-gray-400 hover:text-white md:hidden"
          aria-label="Close navigation"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Navigation -->
      <nav class="p-4 space-y-1 overflow-y-auto" style="max-height: calc(100vh - 8rem);">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center px-4 py-3 md:py-2 rounded-md transition-colors"
          :class="[
            route.path === item.path
              ? 'bg-primary-600 text-white'
              : 'text-gray-300 hover:bg-gray-700 hover:text-white'
          ]"
        >
          {{ item.label }}
        </RouterLink>

        <!-- Admin section -->
        <template v-if="authStore.isAdmin">
          <div class="pt-4 mt-4 border-t border-gray-700">
            <p class="px-4 mb-2 text-xs font-semibold text-gray-500 uppercase">Admin</p>
            <RouterLink
              v-for="item in adminNavItems"
              :key="item.path"
              :to="item.path"
              class="flex items-center px-4 py-3 md:py-2 rounded-md transition-colors"
              :class="[
                route.path === item.path
                  ? 'bg-primary-600 text-white'
                  : 'text-gray-300 hover:bg-gray-700 hover:text-white'
              ]"
            >
              {{ item.label }}
            </RouterLink>
          </div>
        </template>
      </nav>

      <!-- User section -->
      <div class="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-700 safe-area-inset">
        <div class="flex items-center justify-between">
          <div class="flex items-center min-w-0">
            <div class="flex-shrink-0 w-8 h-8 rounded-full bg-primary-600 flex items-center justify-center">
              {{ authStore.user?.username?.charAt(0).toUpperCase() }}
            </div>
            <span class="ml-3 text-sm font-medium text-gray-300 truncate">
              {{ authStore.user?.username }}
            </span>
          </div>
          <button
            @click="handleLogout"
            class="flex-shrink-0 ml-2 px-3 py-2 text-sm text-gray-400 hover:text-white transition-colors"
            title="Logout"
          >
            Logout
          </button>
        </div>
      </div>
    </aside>

    <!-- Main content -->
    <main class="min-h-screen pt-16 md:pt-0 md:ml-64">
      <div class="p-4 md:p-8">
        <RouterView />
      </div>
    </main>
  </div>
</template>
