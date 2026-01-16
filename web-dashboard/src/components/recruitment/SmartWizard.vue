<script setup lang="ts">
import { ref, computed } from 'vue';
import { recruitmentApi, type RecruitmentCharacter, type CreateApplicationCommand } from '@/api/recruitment';
import { useGuildContextStore } from '@/stores/guildContext';

const guildStore = useGuildContextStore();
const searchQuery = ref('');
const searchRealm = ref('');
const loading = ref(false);
const error = ref<string | null>(null);
const candidate = ref<RecruitmentCharacter | null>(null);
const applicationCreated = ref(false);

const isSearchValid = computed(() => searchQuery.value.length > 2 && searchRealm.value.length > 2);

// Mock analysis
const greenFlags = computed(() => {
    if (!candidate.value) return [];
    const flags = [];
    if (candidate.value.itemLevel > 625) flags.push("High Item Level");
    if ((candidate.value.scores.raiderIoScore || 0) > 3000) flags.push("Pro Keystone Master");
    if ((candidate.value.scores.bestParseAverage || 0) > 90) flags.push("Exceptional Performance (90+ Avg)");
    return flags;
});

const redFlags = computed(() => {
    if (!candidate.value) return [];
    const flags = [];
    if (candidate.value.itemLevel < 610) flags.push("Low Item Level");
    if (!candidate.value.specialization) flags.push("No Specialization Data");
    return flags;
});

async function handleSearch() {
    if (!isSearchValid.value) return;
    loading.value = true;
    error.value = null;
    candidate.value = null;
    applicationCreated.value = false;

    try {
        candidate.value = await recruitmentApi.searchCandidate(searchQuery.value, searchRealm.value);
    } catch (e) {
        error.value = "Candidate not found or API error.";
    } finally {
        loading.value = false;
    }
}

async function quickInvite() {
    if (!candidate.value || !guildStore.currentGuildId) return;
    loading.value = true;
    try {
        const cmd: CreateApplicationCommand = {
            battleNetId: "Unknown", // In real wizard this would come from a form or be mocked
            discordId: "Unknown",
            email: "unknown@example.com",
            characterName: candidate.value.name,
            characterRealm: candidate.value.realm,
            characterClass: candidate.value.characterClass,
            specialization: candidate.value.specialization,
            itemLevel: candidate.value.itemLevel,
            raiderIoScore: candidate.value.scores.raiderIoScore,
            bestParseAverage: candidate.value.scores.bestParseAverage,
            age: 0,
            location: "Unknown",
            timezone: "UTC",
            raidDaysAvailable: ["All"],
            previousGuilds: "Quick Invite",
            reasonForLeaving: "N/A",
            whyThisGuild: "Scouted"
        };
        await recruitmentApi.createApplication(guildStore.currentGuildId, cmd);
        applicationCreated.value = true;
        candidate.value = null; // Reset
        searchQuery.value = '';
    } catch (e) {
        error.value = "Failed to create application.";
    } finally {
        loading.value = false;
    }
}
</script>

<template>
  <div class="space-y-6">
    <div class="glass-card p-6 border-white/5">
        <h3 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <span class="w-1 h-6 bg-primary rounded-full"></span>
            Smart Scout
        </h3>
        <p class="text-muted-foreground text-sm mb-6">
            Search for a player to instantly analyze their performance and eligibility.
        </p>

        <div class="flex gap-4">
            <input 
                v-model="searchQuery"
                placeholder="Character Name"
                class="flex-1 px-4 py-3 bg-black/40 border border-white/10 rounded-lg text-white focus:outline-none focus:border-primary/50"
                @keyup.enter="handleSearch"
            />
            <input 
                v-model="searchRealm"
                placeholder="Realm"
                class="flex-1 px-4 py-3 bg-black/40 border border-white/10 rounded-lg text-white focus:outline-none focus:border-primary/50"
                @keyup.enter="handleSearch"
            />
             <button
                class="px-6 py-3 rounded-lg font-bold text-sm transition-all duration-300 shadow-lg shadow-primary/20 flex items-center gap-2"
                :class="isSearchValid ? 'bg-primary hover:bg-primary-600 text-white' : 'bg-muted text-muted-foreground cursor-not-allowed'"
                :disabled="!isSearchValid || loading"
                @click="handleSearch"
            >
                <span v-if="loading" class="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent"></span>
                {{ loading ? "Analyzing..." : "Analyze" }}
            </button>
        </div>

        <div v-if="error" class="mt-4 p-4 bg-red-500/20 text-red-400 rounded-lg text-sm border border-red-500/30">
            {{ error }}
        </div>
        
        <div v-if="applicationCreated" class="mt-4 p-4 bg-green-500/20 text-green-400 rounded-lg text-sm border border-green-500/30">
            Application created successfully!
        </div>
    </div>

    <!-- Results Card -->
    <div v-if="candidate" class="glass-card overflow-hidden border-white/5 animate-in fade-in slide-in-from-bottom-4 duration-300">
        <!-- Header -->
        <div class="p-6 border-b border-white/5 bg-gradient-to-r from-primary/10 to-transparent flex justify-between items-start">
            <div class="flex items-center gap-4">
                <div class="w-16 h-16 rounded-full bg-black/40 border border-white/10 flex items-center justify-center text-2xl font-bold text-white uppercase shadow-inner">
                    {{ candidate.name.substring(0, 2) }}
                </div>
                <div>
                    <h2 class="text-2xl font-bold text-white">{{ candidate.name }}</h2>
                    <div class="flex items-center gap-2 text-muted-foreground">
                        <span class="text-primary font-medium">{{ candidate.itemLevel.toFixed(1) }} ilvl</span>
                        <span>•</span>
                        <span>{{ candidate.specialization }} {{ candidate.characterClass }}</span>
                        <span>•</span>
                        <span>{{ candidate.realm }}</span>
                    </div>
                </div>
            </div>
            <div class="text-right">
                <div class="text-3xl font-bold text-white">{{ candidate.scores.bestParseAverage?.toFixed(0) ?? '-' }}</div>
                <div class="text-xs uppercase tracking-wider text-muted-foreground">Avg Parse</div>
            </div>
        </div>

        <!-- Analysis Grid -->
        <div class="p-6 grid grid-cols-2 gap-6">
            <!-- Green Flags -->
            <div>
                <h4 class="text-xs uppercase tracking-wider text-green-400 font-bold mb-3 flex items-center gap-2">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                    Green Flags
                </h4>
                <ul class="space-y-2">
                   <li v-for="flag in greenFlags" :key="flag" class="flex items-center gap-2 text-sm text-gray-300">
                      <div class="w-1.5 h-1.5 rounded-full bg-green-500"></div>
                      {{ flag }}
                   </li>
                   <li v-if="greenFlags.length === 0" class="text-sm text-muted-foreground italic">No exceptional stats found.</li>
                </ul>
            </div>

             <!-- Red Flags -->
            <div>
                <h4 class="text-xs uppercase tracking-wider text-red-400 font-bold mb-3 flex items-center gap-2">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                    Red Flags
                </h4>
                <ul class="space-y-2">
                   <li v-for="flag in redFlags" :key="flag" class="flex items-center gap-2 text-sm text-gray-300">
                      <div class="w-1.5 h-1.5 rounded-full bg-red-500"></div>
                      {{ flag }}
                   </li>
                   <li v-if="redFlags.length === 0" class="text-sm text-muted-foreground italic">No red flags detected.</li>
                </ul>
            </div>
        </div>

        <!-- Actions -->
        <div class="p-4 bg-white/5 border-t border-white/5 flex justify-end gap-3">
            <button class="px-4 py-2 rounded-lg text-sm font-medium text-white hover:bg-white/10 transition-colors">
                Save for Later
            </button>
             <button 
                class="px-4 py-2 rounded-lg text-sm font-bold bg-primary hover:bg-primary-600 text-white shadow-lg shadow-primary/20 transition-all flex items-center gap-2"
                @click="quickInvite"
                :disabled="loading"
            >
                Quick Invite Candidate
            </button>
        </div>
    </div>
  </div>
</template>
