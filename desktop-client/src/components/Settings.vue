<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { invoke } from "@tauri-apps/api/core";

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

interface WowInstallation {
  path: string;
  flavor: string;
}

const props = defineProps<{
  config: AppConfig | null;
}>();

const emit = defineEmits<{
  save: [config: AppConfig];
}>();

// Form state
const localConfig = ref<AppConfig>({
  wow_path: null,
  account_name: null,
  api_url: "https://api.edgerush.gg",
  api_key: null,
  guild_id: null,
  auto_sync: true,
  notifications_enabled: true,
  start_minimized: false,
  start_with_windows: false,
});

const wowInstallations = ref<WowInstallation[]>([]);
const wowAccounts = ref<string[]>([]);
const saving = ref(false);
const saved = ref(false);

// Initialize from props
watch(
  () => props.config,
  (newConfig) => {
    if (newConfig) {
      localConfig.value = { ...newConfig };
    }
  },
  { immediate: true }
);

// Load WoW installations on mount
onMounted(async () => {
  try {
    wowInstallations.value = await invoke<WowInstallation[]>("detect_wow_paths");
    if (localConfig.value.wow_path) {
      await loadAccounts();
    }
  } catch (e) {
    console.error("Failed to detect WoW installations:", e);
  }
});

// Load accounts when WoW path changes
async function loadAccounts() {
  if (!localConfig.value.wow_path) {
    wowAccounts.value = [];
    return;
  }

  try {
    wowAccounts.value = await invoke<string[]>("get_wow_accounts", {
      wowPath: localConfig.value.wow_path,
    });
  } catch (e) {
    console.error("Failed to load accounts:", e);
    wowAccounts.value = [];
  }
}

async function handleWowPathChange() {
  localConfig.value.account_name = null;
  await loadAccounts();
}

async function save() {
  saving.value = true;
  try {
    emit("save", localConfig.value);
    saved.value = true;
    setTimeout(() => {
      saved.value = false;
    }, 2000);
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div class="space-y-6 max-w-2xl mx-auto pb-10">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-white tracking-tight">Settings</h2>
        <p class="text-muted-foreground">Configure connection and behavior</p>
      </div>
      <button
        class="px-6 py-2.5 rounded-lg font-bold text-sm transition-all duration-300 transform shadow-lg"
        :class="saved 
          ? 'bg-green-500 text-white shadow-green-500/25' 
          : saving 
            ? 'bg-muted text-muted-foreground cursor-wait' 
            : 'bg-primary hover:bg-primary-600 hover:-translate-y-0.5 text-white shadow-primary/25'"
        :disabled="saving"
        @click="save"
      >
        {{ saved ? "Changes Saved!" : saving ? "Saving..." : "Save Changes" }}
      </button>
    </div>

    <!-- WoW Configuration -->
    <div class="glass-card p-6 border-white/5">
      <h3 class="text-lg font-semibold text-white mb-6 flex items-center gap-2">
         <svg class="w-5 h-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
         Game Client
      </h3>

      <div class="space-y-6">
        <div class="space-y-2">
          <label class="text-sm font-medium text-gray-300">WoW Installation</label>
           <div class="relative">
            <select
              v-model="localConfig.wow_path"
              class="w-full px-4 py-3 bg-black/40 border border-white/10 rounded-lg text-white appearance-none focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
              @change="handleWowPathChange"
            >
              <option :value="null" class="bg-gray-900 text-gray-400">Select WoW installation directory...</option>
              <option
                v-for="install in wowInstallations"
                :key="install.path"
                :value="install.path"
                class="bg-gray-900"
              >
                {{ install.flavor }} - {{ install.path }}
              </option>
            </select>
             <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-muted-foreground">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
             </div>
           </div>
          <p class="text-xs text-muted-foreground">The root folder containing the _retail_, _classic_, or _classic_era_ directories.</p>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium text-gray-300">Account Name</label>
           <div class="relative">
            <select
              v-model="localConfig.account_name"
              class="w-full px-4 py-3 bg-black/40 border border-white/10 rounded-lg text-white appearance-none focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="!localConfig.wow_path"
            >
              <option :value="null" class="bg-gray-900 text-gray-400">Select account...</option>
              <option v-for="account in wowAccounts" :key="account" :value="account" class="bg-gray-900">
                {{ account }}
              </option>
            </select>
             <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-muted-foreground">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
             </div>
           </div>
          <p class="text-xs text-muted-foreground">The specific account folder name found in WTF/Account/.</p>
        </div>
      </div>
    </div>

    <!-- API Configuration -->
    <div class="glass-card p-6 border-white/5">
      <h3 class="text-lg font-semibold text-white mb-6 flex items-center gap-2">
        <svg class="w-5 h-5 text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
        EdgeRush API
      </h3>

      <div class="space-y-6">
        <div class="space-y-2">
          <label class="text-sm font-medium text-gray-300">API URL</label>
          <input
            v-model="localConfig.api_url"
            type="text"
             class="w-full px-4 py-2.5 bg-black/40 border border-white/10 rounded-lg text-white placeholder-muted-foreground focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
            placeholder="https://api.edgerush.gg"
          />
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium text-gray-300">API Key</label>
          <input
            v-model="localConfig.api_key"
            type="password"
             class="w-full px-4 py-2.5 bg-black/40 border border-white/10 rounded-lg text-white placeholder-muted-foreground focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all font-mono"
            placeholder="Enter your API key"
          />
          <p class="text-xs text-muted-foreground">Obtain this key from your EdgeRush web dashboard profile.</p>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium text-gray-300">Guild ID</label>
          <input
            v-model="localConfig.guild_id"
            type="text"
             class="w-full px-4 py-2.5 bg-black/40 border border-white/10 rounded-lg text-white placeholder-muted-foreground focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
            placeholder="Enter your guild ID"
          />
        </div>
      </div>
    </div>

    <!-- Sync Settings -->
    <div class="glass-card p-6 border-white/5">
      <h3 class="text-lg font-semibold text-white mb-6 flex items-center gap-2">
        <svg class="w-5 h-5 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
        Sync Behavior
      </h3>

      <div class="space-y-6">
        <div class="flex items-center justify-between p-3 rounded-lg bg-white/5">
          <div>
            <div class="text-sm font-medium text-white">Auto Sync</div>
            <div class="text-xs text-muted-foreground">Automatically sync when you reload UI</div>
          </div>
          <label class="relative inline-flex items-center cursor-pointer">
            <input v-model="localConfig.auto_sync" type="checkbox" class="sr-only peer">
            <div class="w-11 h-6 bg-gray-700 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
          </label>
        </div>

        <div class="flex items-center justify-between p-3 rounded-lg bg-white/5">
          <div>
            <div class="text-sm font-medium text-white">Desktop Notifications</div>
            <div class="text-xs text-muted-foreground">Show popup when sync completes</div>
          </div>
           <label class="relative inline-flex items-center cursor-pointer">
            <input v-model="localConfig.notifications_enabled" type="checkbox" class="sr-only peer">
            <div class="w-11 h-6 bg-gray-700 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
          </label>
        </div>
      </div>
    </div>

    <!-- Startup Settings -->
    <div class="glass-card p-6 border-white/5">
      <h3 class="text-lg font-semibold text-white mb-6 flex items-center gap-2">
        <svg class="w-5 h-5 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
        Startup
      </h3>

      <div class="space-y-6">
        <div class="flex items-center justify-between p-3 rounded-lg bg-white/5">
          <div>
            <div class="text-sm font-medium text-white">Start Minimized</div>
            <div class="text-xs text-muted-foreground">Start quietly in system tray</div>
          </div>
           <label class="relative inline-flex items-center cursor-pointer">
            <input v-model="localConfig.start_minimized" type="checkbox" class="sr-only peer">
            <div class="w-11 h-6 bg-gray-700 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
          </label>
        </div>

        <div class="flex items-center justify-between p-3 rounded-lg bg-white/5">
          <div>
            <div class="text-sm font-medium text-white">Start with Windows</div>
            <div class="text-xs text-muted-foreground">Launch automatically on booting</div>
          </div>
           <label class="relative inline-flex items-center cursor-pointer">
            <input v-model="localConfig.start_with_windows" type="checkbox" class="sr-only peer">
            <div class="w-11 h-6 bg-gray-700 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
          </label>
        </div>
      </div>
    </div>
  </div>
</template>
