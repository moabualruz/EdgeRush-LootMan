<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";
import StatusPanel from "./components/StatusPanel.vue";
import SyncHistory from "./components/SyncHistory.vue";
import Settings from "./components/Settings.vue";
import Simulation from "./components/Simulation.vue";

// State
const activeTab = ref<"status" | "history" | "settings" | "simulation">("status");
const config = ref<AppConfig | null>(null);
const syncStatus = ref<SyncStatus | null>(null);
const lastSync = ref<Date | null>(null);

// Types (Keep existing types)
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

  // Listen for sync completion (auto-sync)
  const unlistenAutoSync = await listen("sync-complete", async () => {
    lastSync.value = new Date();
    syncStatus.value = await invoke<SyncStatus>("get_sync_status");
  });

  const unlistenSettings = await listen("show-settings", () => {
    activeTab.value = "settings";
  });

  // Cleanup listeners on unmount
  onUnmounted(() => {
    unlistenSync();
    unlistenAutoSync();
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
  <div class="flex flex-col h-screen bg-background text-foreground overflow-hidden font-sans">
    <!-- Header -->
    <header class="flex-none h-16 border-b border-border flex items-center justify-between px-6 bg-black/20 backdrop-blur-sm z-50">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-accent flex items-center justify-center shadow-lg shadow-primary/20">
           <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
        </div>
        <span class="text-lg font-bold tracking-tight text-white">EdgeRush <span class="text-primary">LootMan</span></span>
      </div>
      
      <div 
        class="flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-medium transition-colors border"
        :class="syncStatus?.is_syncing 
          ? 'bg-primary/10 text-primary border-primary/20' 
          : 'bg-muted text-muted-foreground border-transparent'"
      >
        <div class="w-2 h-2 rounded-full" :class="syncStatus?.is_syncing ? 'bg-primary animate-pulse' : 'bg-muted-foreground/50'"></div>
        {{ syncStatus?.is_syncing ? 'Syncing...' : 'Idle' }}
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <!-- Sidebar Navigation -->
      <nav class="flex-none w-20 flex flex-col items-center py-6 gap-4 border-r border-border bg-black/10">
        <button
          class="p-3 rounded-xl transition-all duration-200 group relative"
          :class="activeTab === 'status' ? 'bg-primary text-white shadow-lg shadow-primary/25' : 'text-muted-foreground hover:bg-white/5 hover:text-white'"
          @click="activeTab = 'status'"
          title="Status"
        >
          <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
        </button>

        <button
          class="p-3 rounded-xl transition-all duration-200 group relative"
          :class="activeTab === 'history' ? 'bg-primary text-white shadow-lg shadow-primary/25' : 'text-muted-foreground hover:bg-white/5 hover:text-white'"
          @click="activeTab = 'history'"
          title="Sync History"
        >
          <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
        </button>

        <button
          class="p-3 rounded-xl transition-all duration-200 group relative"
          :class="activeTab === 'simulation' ? 'bg-primary text-white shadow-lg shadow-primary/25' : 'text-muted-foreground hover:bg-white/5 hover:text-white'"
          @click="activeTab = 'simulation'"
          title="Simulation"
        >
          <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
        </button>

        <div class="flex-1"></div>

        <button
          class="p-3 rounded-xl transition-all duration-200 group relative"
          :class="activeTab === 'settings' ? 'bg-primary text-white shadow-lg shadow-primary/25' : 'text-muted-foreground hover:bg-white/5 hover:text-white'"
          @click="activeTab = 'settings'"
          title="Settings"
        >
          <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
        </button>
      </nav>

      <!-- Main Content -->
      <main class="flex-1 overflow-y-auto p-6 relative">
        <!-- Background Elements -->
        <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
          <div class="absolute top-0 right-0 w-96 h-96 bg-primary/5 rounded-full blur-3xl opacity-50 translate-x-1/2 -translate-y-1/2"></div>
          <div class="absolute bottom-0 left-0 w-96 h-96 bg-purple-500/5 rounded-full blur-3xl opacity-50 -translate-x-1/2 translate-y-1/2"></div>
        </div>

        <div class="max-w-4xl mx-auto h-full animate-fade-in">
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
          <Simulation
            v-else-if="activeTab === 'simulation'"
          />
          <Settings
            v-else-if="activeTab === 'settings'"
            :config="config"
            @save="saveConfig"
          />
        </div>
      </main>
    </div>
  </div>
</template>
