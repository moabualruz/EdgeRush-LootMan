<script setup lang="ts">
import { computed } from "vue";

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

const props = defineProps<{
  config: AppConfig | null;
  syncStatus: SyncStatus | null;
  lastSync: Date | null;
}>();

const emit = defineEmits<{
  sync: [];
}>();

const isConfigured = computed(() => props.syncStatus?.is_configured ?? false);
const isSyncing = computed(() => props.syncStatus?.is_syncing ?? false);

const lastSyncText = computed(() => {
  if (!props.lastSync) return "Never";
  const diff = Date.now() - props.lastSync.getTime();
  const seconds = Math.floor(diff / 1000);
  if (seconds < 60) return `${seconds} seconds ago`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes} minutes ago`;
  const hours = Math.floor(minutes / 60);
  return `${hours} hours ago`;
});

const configItems = computed(() => [
  {
    label: "WoW Path",
    value: props.config?.wow_path ?? "Not set",
    configured: !!props.config?.wow_path,
  },
  {
    label: "Account",
    value: props.config?.account_name ?? "Not set",
    configured: !!props.config?.account_name,
  },
  {
    label: "API Key",
    value: props.config?.api_key ? "Configured" : "Not set",
    configured: !!props.config?.api_key,
  },
  {
    label: "Guild ID",
    value: props.config?.guild_id ?? "Not set",
    configured: !!props.config?.guild_id,
  },
]);
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between mb-2">
       <div>
        <h2 class="text-2xl font-bold text-white tracking-tight">System Status</h2>
        <p class="text-muted-foreground">Monitor sync activity and configuration</p>
      </div>
    </div>

    <!-- Sync Status Card -->
    <div class="glass-card p-6 border-white/5 bg-gradient-to-br from-black/40 to-black/20">
      <div class="flex items-center justify-between mb-6">
        <h3 class="text-lg font-semibold text-white flex items-center gap-2">
          <span class="w-1 h-6 bg-primary rounded-full"></span>
          Sync Operations
        </h3>
        <button
            class="px-5 py-2 rounded-lg font-medium text-sm transition-all duration-200 shadow-lg shadow-primary/10"
            :class="isSyncing || !isConfigured ? 'bg-muted text-muted-foreground cursor-not-allowed' : 'bg-primary hover:bg-primary-600 text-white shadow-primary/20'"
            :disabled="!isConfigured || isSyncing"
            @click="emit('sync')"
          >
            <span v-if="isSyncing" class="flex items-center gap-2">
              <svg class="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
              Syncing...
            </span>
            <span v-else>Sync Now</span>
          </button>
      </div>
      
      <div class="grid grid-cols-2 gap-4">
        <div class="p-4 rounded-xl bg-white/5 border border-white/5 backdrop-blur-sm">
          <div class="text-xs uppercase tracking-wider text-muted-foreground mb-1">Status</div>
          <div class="text-lg font-medium" :class="isSyncing ? 'text-primary' : isConfigured ? 'text-green-400' : 'text-yellow-400'">
            {{ isSyncing ? "Syncing..." : isConfigured ? "Ready" : "Not Configured" }}
          </div>
        </div>

        <div class="p-4 rounded-xl bg-white/5 border border-white/5 backdrop-blur-sm">
          <div class="text-xs uppercase tracking-wider text-muted-foreground mb-1">Last Sync</div>
          <div class="text-lg font-medium text-white">{{ lastSyncText }}</div>
        </div>

        <div class="p-4 rounded-xl bg-white/5 border border-white/5 backdrop-blur-sm">
          <div class="text-xs uppercase tracking-wider text-muted-foreground mb-1">Auto Sync</div>
           <div class="text-lg font-medium" :class="syncStatus?.auto_sync_enabled ? 'text-green-400' : 'text-muted-foreground'">
            {{ syncStatus?.auto_sync_enabled ? "Enabled" : "Disabled" }}
          </div>
        </div>

         <div class="p-4 rounded-xl bg-white/5 border border-white/5 backdrop-blur-sm">
          <div class="text-xs uppercase tracking-wider text-muted-foreground mb-1">Watching</div>
          <div class="text-sm font-medium text-white/80 truncate" :title="syncStatus?.watched_path ?? ''">
             {{ syncStatus?.watched_path ?? "N/A" }}
          </div>
        </div>
      </div>
    </div>

    <!-- Configuration Status Card -->
     <div class="glass-card p-6 border-white/5 bg-gradient-to-br from-black/40 to-black/20">
      <h3 class="text-lg font-semibold text-white flex items-center gap-2 mb-6">
          <span class="w-1 h-6 bg-purple-500 rounded-full"></span>
          Configuration Check
      </h3>
      
      <div class="space-y-3">
         <div
          v-for="item in configItems"
          :key="item.label"
          class="flex items-center justify-between p-3 rounded-lg bg-black/20 border border-white/5"
        >
          <div class="flex items-center gap-3">
             <div class="w-2 h-2 rounded-full" :class="item.configured ? 'bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.5)]' : 'bg-destructive'"></div>
             <span class="text-sm font-medium text-white">{{ item.label }}</span>
          </div>
          <div class="text-sm text-muted-foreground max-w-[200px] truncate" :class="{ 'text-yellow-500/80': !item.configured }">
             {{ item.value }}
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Help -->
    <div class="rounded-xl border border-primary/20 bg-primary/5 p-6">
      <h3 class="text-sm font-bold text-primary uppercase tracking-wider mb-4 flex items-center gap-2">
         <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
         Quick Start Guide
      </h3>
      <ol class="space-y-2 text-sm text-muted-foreground list-decimal list-inside marker:text-primary">
        <li>Configure your <span class="text-white font-medium">WoW installation path</span> in Settings</li>
        <li>Select your <span class="text-white font-medium">WoW account name</span></li>
        <li>Enter your <span class="text-white font-medium">EdgeRush API key</span> (from web dashboard)</li>
        <li>Enable <span class="text-white font-medium">auto-sync</span> to sync automatically when you /reload</li>
      </ol>
    </div>
  </div>
</template>
