<script setup lang="ts">
import { ref, onMounted } from "vue";
import { invoke } from "@tauri-apps/api/core";

const characters = ref<string[]>([]);
const selectedCharacter = ref<string | null>(null);
const simcInput = ref<string | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const copied = ref(false);

onMounted(async () => {
  try {
    characters.value = await invoke<string[]>("get_characters");
    if (characters.value.length > 0) {
      selectedCharacter.value = characters.value[0];
    }
  } catch (e) {
    console.error("Failed to load characters:", e);
    error.value = "Failed to load characters. Please ensure WoW path is configured.";
  }
});

async function generate() {
  if (!selectedCharacter.value) return;
  
  loading.value = true;
  error.value = null;
  simcInput.value = null;
  copied.value = false;

  try {
    const [name, realm] = selectedCharacter.value.split(" - ");
    const result = await invoke<string>("generate_simc_input", {
      characterName: name,
      realm: realm,
    });
    simcInput.value = result;
  } catch (e) {
    error.value = String(e);
  } finally {
    loading.value = false;
  }
}

async function copyToClipboard() {
  if (!simcInput.value) return;
  
  try {
    await navigator.clipboard.writeText(simcInput.value);
    copied.value = true;
    setTimeout(() => {
      copied.value = false;
    }, 2000);
  } catch (e) {
    console.error("Failed to copy:", e);
  }
}
</script>

<template>
  <div class="space-y-6 h-full flex flex-col">
    <div class="flex items-center justify-between flex-none">
      <div>
        <h2 class="text-2xl font-bold text-white tracking-tight">SimulationCraft</h2>
        <p class="text-muted-foreground">Generate input for Raidbots or SimC</p>
      </div>
    </div>

    <div class="glass-card p-6 border-white/5 flex-none">
      <div class="space-y-4">
        <div class="space-y-2">
          <label class="text-sm font-medium text-gray-300">Select Character</label>
           <div class="relative">
            <select
              v-model="selectedCharacter"
              class="w-full px-4 py-3 bg-black/40 border border-white/10 rounded-lg text-white appearance-none focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
              :disabled="loading || characters.length === 0"
            >
              <option v-if="characters.length === 0" :value="null">No characters found</option>
              <option v-for="char in characters" :key="char" :value="char">
                {{ char }}
              </option>
            </select>
             <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-muted-foreground">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
             </div>
           </div>
        </div>

        <button
          class="w-full py-3 rounded-lg font-bold text-sm transition-all duration-300 shadow-lg shadow-primary/20 flex items-center justify-center gap-2"
          :class="loading ? 'bg-muted text-muted-foreground cursor-wait' : 'bg-primary hover:bg-primary-600 text-white'"
          :disabled="loading || !selectedCharacter"
          @click="generate"
        >
          <span v-if="loading" class="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent"></span>
          {{ loading ? "Generating..." : "Generate SimC Input" }}
        </button>
      </div>
    </div>

    <!-- Output Area -->
    <div class="flex-1 min-h-0 relative glass-card p-0 border-white/5 overflow-hidden flex flex-col">
      <div v-if="error" class="p-6 text-destructive flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
        {{ error }}
      </div>
      
      <div v-else-if="simcInput" class="flex flex-col h-full">
        <div class="flex-none p-4 border-b border-white/5 bg-black/20 flex justify-between items-center">
          <span class="text-xs font-mono text-muted-foreground">Output generated successfully</span>
          <button 
            @click="copyToClipboard"
            class="px-3 py-1.5 rounded-md text-xs font-medium transition-all"
            :class="copied ? 'bg-green-500/20 text-green-400' : 'bg-white/10 hover:bg-white/20 text-white'"
          >
            {{ copied ? "Copied!" : "Copy to Clipboard" }}
          </button>
        </div>
        <div class="flex-1 overflow-auto p-4 bg-black/40">
           <pre class="font-mono text-xs text-gray-300 whitespace-pre-wrap break-all">{{ simcInput }}</pre>
        </div>
      </div>
      
      <div v-else class="flex-1 flex flex-col items-center justify-center text-muted-foreground p-6">
        <svg class="w-12 h-12 mb-4 opacity-20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
        <p>Select a character and click generate to view SimC input</p>
      </div>
    </div>
  </div>
</template>
