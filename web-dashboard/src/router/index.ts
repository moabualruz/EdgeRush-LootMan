import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useGuildContextStore } from '@/stores/guildContext'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/LoginPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/pages/ProfilePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/auth/:provider/callback',
    name: 'oauth-callback',
    component: () => import('@/pages/OAuthCallbackPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/apply',
    name: 'apply',
    component: () => import('@/pages/ApplyPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard',
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/pages/DashboardPage.vue'),
      },
      {
        path: 'leaderboard',
        name: 'leaderboard',
        component: () => import('@/pages/LeaderboardPage.vue'),
      },
      {
        path: 'history',
        name: 'history',
        component: () => import('@/pages/LootHistoryPage.vue'),
      },
      {
        path: 'wishlist',
        name: 'wishlist',
        component: () => import('@/pages/WishlistPage.vue'),
      },
      {
        path: 'performance',
        name: 'performance',
        component: () => import('@/pages/PerformancePage.vue'),
      },
      {
        path: 'attendance',
        name: 'attendance',
        component: () => import('@/pages/AttendancePage.vue'),
      },
      {
        path: 'raids',
        name: 'raids',
        component: () => import('@/pages/RaidsPage.vue'),
      },
      {
        path: 'raids/:id',
        name: 'raid-detail',
        component: () => import('@/pages/RaidDetailPage.vue'),
      },
      {
        path: 'raid-plans',
        name: 'raid-plans',
        component: () => import('@/pages/RaidPlansPage.vue'),
      },
      {
        path: 'raid-plans/:id',
        name: 'raid-plan-editor',
        component: () => import('@/pages/RaidPlanPage.vue'),
      },
      {
        path: 'gear',
        name: 'gear',
        component: () => import('@/pages/GearPage.vue'),
      },
      {
        path: 'admin',
        name: 'admin',
        component: () => import('@/pages/AdminPage.vue'),
        meta: { requiresAdmin: true },
      },
      {
        path: 'admin/applications',
        name: 'applications',
        component: () => import('@/pages/ApplicationsPage.vue'),
        meta: { requiresAdmin: true },
      },
      {
        path: 'admin/discord',
        name: 'discord-config',
        component: () => import('@/pages/DiscordConfigPage.vue'),
        meta: { requiresAdmin: true },
      },
      {
        path: 'admin/sync',
        name: 'sync-history',
        component: () => import('@/pages/SyncHistoryPage.vue'),
        meta: { requiresAdmin: true },
      },
      {
        path: 'guild-settings',
        name: 'guild-settings',
        component: () => import('@/pages/GuildSettingsPage.vue'),
        meta: { requiresSettingsAccess: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const guildContextStore = useGuildContextStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next('/dashboard')
    return
  }

  if (to.meta.requiresSettingsAccess && !guildContextStore.canAccessSettings) {
    next('/dashboard')
    return
  }

  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/dashboard')
    return
  }

  next()
})

export default router
