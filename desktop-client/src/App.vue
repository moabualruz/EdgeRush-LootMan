<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";
import StatusPanel from "./components/StatusPanel.vue";
import SyncHistory from "./components/SyncHistory.vue";
import Settings from "./components/Settings.vue";

// State
const activeTab = ref<"status" | "history" | "settings">("status");
const config = ref<AppConfig | null>(null);
const syncStatus = ref<SyncStatus | null>(null);
const lastSync = ref<Date | null>(null);

// Types
interface AppConfig {
  wow_path: string | null;
  account_name: string | null;
  api_url: string;
  api_key: string | null;
  guild_id: string | null;
  auto_sync: boolean;
  notifications_enabled: boolean;
  start_minimized: boolean;
  start_with_windows: boolean;
}

interface SyncStatus {
  is_running: boolean;
  is_syncing: boolean;
  is_configured: boolean;
  auto_sync_enabled: boolean;
  last_sync: number | null;
  watched_path: string | null;
}

// Load initial data
onMounted(async () => {
  try {
    config.value = await invoke<AppConfig>("get_config");
    syncStatus.value = await invoke<SyncStatus>("get_sync_status");
  } catch (e) {
    console.error("Failed to load config:", e);
  }

  // Listen for tray events
  const unlistenSync = await listen("tray-sync-now", async () => {
    await triggerSync();
  });

  const unlistenSettings = await listen("show-settings", () => {
    activeTab.value = "settings";
  });

  // Cleanup listeners on unmount
  onUnmounted(() => {
    unlistenSync();
    unlistenSettings();
  });
});

// Actions
async function triggerSync() {
  try {
    await invoke("trigger_sync");
    lastSync.value = new Date();
    syncStatus.value = await invoke<SyncStatus>("get_sync_status");
  } catch (e) {
    console.error("Sync failed:", e);
  }
}

async function saveConfig(newConfig: AppConfig) {
  try {
    await invoke("save_config", { config: newConfig });
    config.value = newConfig;
    syncStatus.value = await invoke<SyncStatus>("get_sync_status");
  } catch (e) {
    console.error("Failed to save config:", e);
  }
}
</script>

<template>
  <div class="app">
    <!-- Header -->
    <header class="header">
      <div class="logo">
        <img src="/logo.svg" alt="EdgeRush" class="logo-img" />
        <span class="logo-text">EdgeRush LootMan</span>
      </div>
      <div class="status-indicator" :class="{ active: syncStatus?.is_syncing }">
        <span class="status-dot"></span>
        <span class="status-text">{{ syncStatus?.is_syncing ? 'Syncing...' : 'Idle' }}</span>
      </div>
    </header>

    <!-- Navigation -->
    <nav class="nav">
      <button
        class="nav-btn"
        :class="{ active: activeTab === 'status' }"
        @click="activeTab = 'status'"
      >
        Status
      </button>
      <button
        class="nav-btn"
        :class="{ active: activeTab === 'history' }"
        @click="activeTab = 'history'"
      >
        History
      </button>
      <button
        class="nav-btn"
        :class="{ active: activeTab === 'settings' }"
        @click="activeTab = 'settings'"
      >
        Settings
      </button>
    </nav>

    <!-- Content -->
    <main class="content">
      <StatusPanel
        v-if="activeTab === 'status'"
        :config="config"
        :sync-status="syncStatus"
        :last-sync="lastSync"
        @sync="triggerSync"
      />
      <SyncHistory
        v-else-if="activeTab === 'history'"
      />
      <Settings
        v-else-if="activeTab === 'settings'"
        :config="config"
        @save="saveConfig"
      />
    </main>
  </div>
</template>

<style scoped>
.app {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--bg-tertiary);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-img {
  width: 32px;
  height: 32px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--bg-tertiary);
  border-radius: 20px;
  font-size: 14px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--text-secondary);
  transition: background 0.3s;
}

.status-indicator.active .status-dot {
  background: var(--accent);
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.nav {
  display: flex;
  padding: 12px 20px;
  gap: 8px;
  background: var(--bg-secondary);
}

.nav-btn {
  padding: 8px 20px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.nav-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.nav-btn.active {
  background: var(--accent);
  color: white;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
