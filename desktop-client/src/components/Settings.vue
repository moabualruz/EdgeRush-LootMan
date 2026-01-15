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
  <div class="settings">
    <div class="header">
      <h2 class="title">Settings</h2>
      <button class="save-btn" :disabled="saving" @click="save">
        {{ saved ? "Saved!" : saving ? "Saving..." : "Save" }}
      </button>
    </div>

    <!-- WoW Configuration -->
    <div class="section">
      <h3 class="section-title">World of Warcraft</h3>

      <div class="field">
        <label class="field-label">WoW Installation</label>
        <select
          v-model="localConfig.wow_path"
          class="field-select"
          @change="handleWowPathChange"
        >
          <option :value="null">Select WoW installation...</option>
          <option
            v-for="install in wowInstallations"
            :key="install.path"
            :value="install.path"
          >
            {{ install.flavor }} - {{ install.path }}
          </option>
        </select>
        <p class="field-hint">Select your World of Warcraft installation folder</p>
      </div>

      <div class="field">
        <label class="field-label">Account Name</label>
        <select
          v-model="localConfig.account_name"
          class="field-select"
          :disabled="!localConfig.wow_path"
        >
          <option :value="null">Select account...</option>
          <option v-for="account in wowAccounts" :key="account" :value="account">
            {{ account }}
          </option>
        </select>
        <p class="field-hint">The account folder name in WTF/Account/</p>
      </div>
    </div>

    <!-- API Configuration -->
    <div class="section">
      <h3 class="section-title">EdgeRush API</h3>

      <div class="field">
        <label class="field-label">API URL</label>
        <input
          v-model="localConfig.api_url"
          type="text"
          class="field-input"
          placeholder="https://api.edgerush.gg"
        />
      </div>

      <div class="field">
        <label class="field-label">API Key</label>
        <input
          v-model="localConfig.api_key"
          type="password"
          class="field-input"
          placeholder="Enter your API key"
        />
        <p class="field-hint">Get your API key from the EdgeRush web dashboard</p>
      </div>

      <div class="field">
        <label class="field-label">Guild ID</label>
        <input
          v-model="localConfig.guild_id"
          type="text"
          class="field-input"
          placeholder="Enter your guild ID"
        />
      </div>
    </div>

    <!-- Sync Settings -->
    <div class="section">
      <h3 class="section-title">Sync Settings</h3>

      <div class="toggle-field">
        <div class="toggle-info">
          <span class="toggle-label">Auto Sync</span>
          <span class="toggle-hint">Automatically sync when SavedVariables changes</span>
        </div>
        <label class="toggle">
          <input v-model="localConfig.auto_sync" type="checkbox" />
          <span class="toggle-slider"></span>
        </label>
      </div>

      <div class="toggle-field">
        <div class="toggle-info">
          <span class="toggle-label">Desktop Notifications</span>
          <span class="toggle-hint">Show notifications on sync completion</span>
        </div>
        <label class="toggle">
          <input v-model="localConfig.notifications_enabled" type="checkbox" />
          <span class="toggle-slider"></span>
        </label>
      </div>
    </div>

    <!-- Startup Settings -->
    <div class="section">
      <h3 class="section-title">Startup</h3>

      <div class="toggle-field">
        <div class="toggle-info">
          <span class="toggle-label">Start Minimized</span>
          <span class="toggle-hint">Start in system tray</span>
        </div>
        <label class="toggle">
          <input v-model="localConfig.start_minimized" type="checkbox" />
          <span class="toggle-slider"></span>
        </label>
      </div>

      <div class="toggle-field">
        <div class="toggle-info">
          <span class="toggle-label">Start with Windows</span>
          <span class="toggle-hint">Launch automatically on system startup</span>
        </div>
        <label class="toggle">
          <input v-model="localConfig.start_with_windows" type="checkbox" />
          <span class="toggle-slider"></span>
        </label>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.save-btn {
  padding: 8px 24px;
  border: none;
  background: var(--accent);
  color: white;
  font-size: 14px;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.save-btn:hover:not(:disabled) {
  background: var(--accent-hover);
}

.save-btn:disabled {
  opacity: 0.7;
}

.section {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.field {
  margin-bottom: 16px;
}

.field:last-child {
  margin-bottom: 0;
}

.field-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.field-input,
.field-select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--bg-tertiary);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 14px;
  border-radius: 6px;
  transition: border-color 0.2s;
}

.field-input:focus,
.field-select:focus {
  outline: none;
  border-color: var(--accent);
}

.field-select {
  cursor: pointer;
}

.field-select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.field-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.toggle-field {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--bg-tertiary);
}

.toggle-field:last-child {
  border-bottom: none;
}

.toggle-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toggle-label {
  font-size: 14px;
  font-weight: 500;
}

.toggle-hint {
  font-size: 12px;
  color: var(--text-secondary);
}

.toggle {
  position: relative;
  width: 48px;
  height: 26px;
}

.toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--bg-tertiary);
  transition: 0.3s;
  border-radius: 26px;
}

.toggle-slider::before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

.toggle input:checked + .toggle-slider {
  background-color: var(--accent);
}

.toggle input:checked + .toggle-slider::before {
  transform: translateX(22px);
}
</style>
