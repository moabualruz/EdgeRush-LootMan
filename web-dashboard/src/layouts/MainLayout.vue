<script setup lang="ts">
import { RouterView, RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

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
]

function handleLogout() {
  authStore.logout()
}
</script>

<template>
  <div class="min-h-screen bg-gray-900">
    <!-- Sidebar -->
    <aside class="fixed inset-y-0 left-0 w-64 bg-gray-800 border-r border-gray-700">
      <!-- Logo -->
      <div class="flex items-center h-16 px-6 border-b border-gray-700">
        <span class="text-xl font-bold text-primary-400">LootMan</span>
      </div>

      <!-- Navigation -->
      <nav class="p-4 space-y-1">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center px-4 py-2 rounded-md transition-colors"
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
              class="flex items-center px-4 py-2 rounded-md transition-colors"
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
      <div class="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-700">
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="w-8 h-8 rounded-full bg-primary-600 flex items-center justify-center">
              {{ authStore.user?.username?.charAt(0).toUpperCase() }}
            </div>
            <span class="ml-3 text-sm font-medium text-gray-300">
              {{ authStore.user?.username }}
            </span>
          </div>
          <button
            @click="handleLogout"
            class="text-gray-400 hover:text-white transition-colors"
            title="Logout"
          >
            Logout
          </button>
        </div>
      </div>
    </aside>

    <!-- Main content -->
    <main class="ml-64 min-h-screen">
      <div class="p-8">
        <RouterView />
      </div>
    </main>
  </div>
</template>
