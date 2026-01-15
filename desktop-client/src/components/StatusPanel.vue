<script setup lang="ts">
import { computed } from "vue";

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
  <div class="status-panel">
    <!-- Sync Status Card -->
    <div class="card">
      <h2 class="card-title">Sync Status</h2>
      <div class="status-grid">
        <div class="status-item">
          <span class="label">Status</span>
          <span class="value" :class="{ syncing: isSyncing }">
            {{ isSyncing ? "Syncing..." : isConfigured ? "Ready" : "Not Configured" }}
          </span>
        </div>
        <div class="status-item">
          <span class="label">Last Sync</span>
          <span class="value">{{ lastSyncText }}</span>
        </div>
        <div class="status-item">
          <span class="label">Auto Sync</span>
          <span class="value" :class="{ enabled: syncStatus?.auto_sync_enabled }">
            {{ syncStatus?.auto_sync_enabled ? "Enabled" : "Disabled" }}
          </span>
        </div>
        <div class="status-item">
          <span class="label">Watching</span>
          <span class="value path">{{ syncStatus?.watched_path ?? "N/A" }}</span>
        </div>
      </div>
      <button
        class="sync-btn"
        :disabled="!isConfigured || isSyncing"
        @click="emit('sync')"
      >
        {{ isSyncing ? "Syncing..." : "Sync Now" }}
      </button>
    </div>

    <!-- Configuration Status Card -->
    <div class="card">
      <h2 class="card-title">Configuration</h2>
      <div class="config-list">
        <div
          v-for="item in configItems"
          :key="item.label"
          class="config-item"
        >
          <div class="config-status">
            <span class="config-dot" :class="{ configured: item.configured }"></span>
            <span class="config-label">{{ item.label }}</span>
          </div>
          <span class="config-value" :class="{ missing: !item.configured }">
            {{ item.value }}
          </span>
        </div>
      </div>
    </div>

    <!-- Quick Help -->
    <div class="card help">
      <h2 class="card-title">Quick Start</h2>
      <ol class="help-list">
        <li>Configure your WoW installation path in Settings</li>
        <li>Select your WoW account name</li>
        <li>Enter your EdgeRush API key (from web dashboard)</li>
        <li>Enable auto-sync to sync automatically when you /reload</li>
      </ol>
    </div>
  </div>
</template>

<style scoped>
.status-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.label {
  font-size: 12px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.value {
  font-size: 14px;
  color: var(--text-primary);
}

.value.syncing {
  color: var(--accent);
}

.value.enabled {
  color: var(--success);
}

.value.path {
  font-size: 12px;
  word-break: break-all;
  color: var(--text-secondary);
}

.sync-btn {
  width: 100%;
  padding: 12px;
  border: none;
  background: var(--accent);
  color: white;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.sync-btn:hover:not(:disabled) {
  background: var(--accent-hover);
}

.sync-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--bg-tertiary);
}

.config-item:last-child {
  border-bottom: none;
}

.config-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.config-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--error);
}

.config-dot.configured {
  background: var(--success);
}

.config-label {
  font-size: 14px;
}

.config-value {
  font-size: 14px;
  color: var(--text-secondary);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.config-value.missing {
  color: var(--warning);
}

.help {
  background: var(--bg-tertiary);
}

.help-list {
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.help-list li {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
}
</style>
