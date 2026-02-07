<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useAuthStore } from "@/stores/auth";
import { api } from "@/api/client";
import {
  fetchUserCharacters,
  refreshUserLinkages,
  type UserCharacter,
  type LinkageRefreshResult,
} from "@/api/user";
import BattlenetIcon from "@/components/icons/BattlenetIcon.vue";

const authStore = useAuthStore();
const user = computed(() => authStore.user);

const discordUrl = ref("");
const battlenetUrl = ref("");
const characters = ref<UserCharacter[]>([]);
const loadingCharacters = ref(false);
const fixingLinkages = ref(false);
const linkageResult = ref<LinkageRefreshResult | null>(null);
const linkageError = ref<string | null>(null);

// Format date helper
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return "N/A";
  return new Date(dateStr).toLocaleDateString(undefined, {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

// Class color helper
const getClassColor = (className: string) => {
  const map: Record<string, string> = {
    DEATH_KNIGHT: "bg-[#C41E3A]/20 text-[#C41E3A]",
    DEMON_HUNTER: "bg-[#A330C9]/20 text-[#A330C9]",
    DRUID: "bg-[#FF7D0A]/20 text-[#FF7D0A]",
    EVOKER: "bg-[#33937F]/20 text-[#33937F]",
    HUNTER: "bg-[#ABD473]/20 text-[#ABD473]",
    MAGE: "bg-[#69CCF0]/20 text-[#69CCF0]",
    MONK: "bg-[#00FF96]/20 text-[#00FF96]",
    PALADIN: "bg-[#F58CBA]/20 text-[#F58CBA]",
    PRIEST: "bg-[#FFFFFF]/20 text-[#FFFFFF]",
    ROGUE: "bg-[#FFF569]/20 text-[#FFF569]",
    SHAMAN: "bg-[#0070DE]/20 text-[#0070DE]",
    WARLOCK: "bg-[#9482C9]/20 text-[#9482C9]",
    WARRIOR: "bg-[#C79C6E]/20 text-[#C79C6E]",
  };
  return map[className] || "bg-gray-500/20 text-gray-500";
};

const getClassColorText = (className: string) => {
  const map: Record<string, string> = {
    DEATH_KNIGHT: "text-[#C41E3A]",
    DEMON_HUNTER: "text-[#A330C9]",
    DRUID: "text-[#FF7D0A]",
    EVOKER: "text-[#33937F]",
    HUNTER: "text-[#ABD473]",
    MAGE: "text-[#69CCF0]",
    MONK: "text-[#00FF96]",
    PALADIN: "text-[#F58CBA]",
    PRIEST: "text-[#FFFFFF]",
    ROGUE: "text-[#FFF569]",
    SHAMAN: "text-[#0070DE]",
    WARLOCK: "text-[#9482C9]",
    WARRIOR: "text-[#C79C6E]",
  };
  return map[className] || "text-gray-500";
};

// Fetch OAuth URLs
const fetchOAuthUrls = async () => {
  try {
    const { data: discordData } = await api.get("/v1/auth/discord/url");
    discordUrl.value = discordData.url;

    const { data: bnetData } = await api.get("/v1/auth/battlenet/url");
    battlenetUrl.value = bnetData.url;
  } catch (err) {
    console.error("Failed to fetch OAuth URLs", err);
  }
};

const loadCharacters = async () => {
  if (!user.value?.battlenetId) return;

  loadingCharacters.value = true;
  try {
    characters.value = await fetchUserCharacters();
  } catch (e) {
    console.error("Failed to load characters", e);
  } finally {
    loadingCharacters.value = false;
  }
};

const handleFixLinkages = async () => {
  fixingLinkages.value = true;
  linkageResult.value = null;
  linkageError.value = null;

  try {
    linkageResult.value = await refreshUserLinkages();
    // Reload characters after fixing linkages
    await loadCharacters();
  } catch (e: any) {
    console.error("Failed to fix linkages", e);
    linkageError.value =
      e.response?.data?.message ||
      e.message ||
      "Failed to fix character links. Please try again.";
  } finally {
    fixingLinkages.value = false;
  }
};

onMounted(() => {
  fetchOAuthUrls();
  loadCharacters();
});
</script>

<template>
  <div class="space-y-8 animate-fade-in">
    <div>
      <h1 class="text-3xl font-bold tracking-tight text-white mb-2 text-glow">
        My Profile
      </h1>
      <p class="text-muted-foreground">
        Manage your identity and linked connections
      </p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6" v-if="user">
      <!-- User Info Card -->
      <div
        class="glass-card p-6 border-white/10 flex flex-col h-full bg-gradient-to-br from-black/40 to-black/20"
      >
        <h2
          class="text-lg font-semibold text-white mb-6 flex items-center gap-2"
        >
          <svg
            class="w-5 h-5 text-primary"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
            />
          </svg>
          User Information
        </h2>

        <div class="flex items-start gap-5 mb-8">
          <div
            class="w-20 h-20 rounded-2xl bg-gradient-to-br from-primary to-accent flex items-center justify-center text-4xl font-bold text-white shadow-lg shadow-primary/20 shrink-0"
          >
            {{ user.username.charAt(0).toUpperCase() }}
          </div>
          <div class="flex-1 min-w-0 py-1">
            <div class="text-2xl font-bold text-white truncate mb-1">
              {{ user.username }}
            </div>
            <div class="text-sm text-muted-foreground truncate">
              {{ user.email }}
            </div>
            <div class="mt-3 flex flex-wrap gap-2">
              <div
                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary/10 text-primary border border-primary/20"
              >
                {{ user.role }}
              </div>
              <div
                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-white/5 text-muted-foreground border border-white/10"
              >
                Joined {{ formatDate(user.createdAt?.toString()) }}
              </div>
            </div>
          </div>
        </div>

        <div class="mt-auto pt-6 border-t border-white/10">
          <div class="flex items-center justify-between">
            <span
              class="text-xs font-semibold text-muted-foreground uppercase tracking-wider"
              >Guild Status</span
            >
            <span
              v-if="user.guildId"
              class="text-green-400 flex items-center text-sm font-medium bg-green-500/10 px-3 py-1 rounded-full border border-green-500/20"
            >
              <span
                class="w-2 h-2 rounded-full bg-green-500 mr-2 animate-pulse"
              ></span>
              Member (ID: {{ user.guildId }})
            </span>
            <span
              v-else
              class="text-yellow-400 flex items-center text-sm font-medium bg-yellow-500/10 px-3 py-1 rounded-full border border-yellow-500/20"
            >
              <span class="w-2 h-2 rounded-full bg-yellow-500 mr-2"></span>
              No Guild
            </span>
          </div>
        </div>
      </div>

      <!-- Linked Accounts Card -->
      <div
        class="glass-card p-6 border-white/10 flex flex-col h-full bg-gradient-to-br from-black/40 to-black/20"
      >
        <h2
          class="text-lg font-semibold text-white mb-6 flex items-center gap-2"
        >
          <svg
            class="w-5 h-5 text-primary"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
            />
          </svg>
          Linked Accounts
        </h2>

        <div class="space-y-4">
          <!-- Discord -->
          <div
            class="p-4 rounded-xl border border-white/5 bg-black/20 hover:bg-black/40 transition-colors"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <div class="text-[#5865F2] bg-[#5865F2]/10 p-2 rounded-lg">
                  <svg
                    class="w-6 h-6"
                    fill="currentColor"
                    viewBox="0 0 127 96"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M107.7,8.07A105.15,105.15,0,0,0,81.47,0a72.06,72.06,0,0,0-3.36,6.83A97.68,97.68,0,0,0,49,6.83,72.37,72.37,0,0,0,45.64,0,105.89,105.89,0,0,0,19.39,8.09C2.79,32.65-1.71,56.6.54,80.21h0A105.73,105.73,0,0,0,32.71,96.36,77.11,77.11,0,0,0,39.6,85.25a68.42,68.42,0,0,1-10.85-5.18c.91-.66,1.8-1.34,2.66-2a75.57,75.57,0,0,0,64.32,0c.87.71,1.76,1.39,2.66,2a68.68,68.68,0,0,1-10.87,5.19,77,77,0,0,0,6.89,11.1A105.25,105.25,0,0,0,126.6,80.22c2.91-23.29-1.55-47.57-18.9-72.15ZM42.45,65.69C36.18,65.69,31,60,31,53s5-12.74,11.43-12.74S54,46,53.89,53,48.84,65.69,42.45,65.69Zm42.24,0C78.41,65.69,73.25,60,73.25,53s5-12.74,11.44-12.74S96.23,46,96.12,53,91.08,65.69,84.69,65.69Z"
                    />
                  </svg>
                </div>
                <div>
                  <div class="font-medium text-white">Discord</div>
                  <div class="text-xs text-muted-foreground">
                    Identity & Communication
                  </div>
                </div>
              </div>
              <span
                v-if="user.discordId"
                class="text-xs font-medium text-green-400 border border-green-500/20 bg-green-500/10 px-2.5 py-1 rounded-full flex items-center gap-1.5"
              >
                <svg
                  class="w-3 h-3"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
                Connected
              </span>
              <span
                v-else
                class="text-xs font-medium text-muted-foreground border border-white/10 bg-white/5 px-2.5 py-1 rounded-full"
                >Not Connected</span
              >
            </div>

            <a
              v-if="!user.discordId"
              :href="discordUrl"
              class="block w-full text-center py-2 rounded-lg bg-[#5865F2] hover:bg-[#4752c4] text-white text-sm font-semibold transition-colors shadow-lg shadow-[#5865F2]/20"
            >
              Connect Discord
            </a>
            <button
              v-else
              disabled
              class="w-full py-2 rounded-lg bg-white/5 border border-white/10 text-muted-foreground text-sm font-medium cursor-not-allowed flex items-center justify-center gap-2"
            >
              <svg
                class="w-4 h-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 13l4 4L19 7"
                />
              </svg>
              Account Linked
            </button>
          </div>

          <!-- Battle.net -->
          <div
            class="p-4 rounded-xl border border-white/5 bg-black/20 hover:bg-black/40 transition-colors"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <div class="bg-[#148EFF]/10 p-2 rounded-lg text-[#148EFF]">
                  <BattlenetIcon class="w-6 h-6" />
                </div>
                <div>
                  <div class="font-medium text-white">Battle.net</div>
                  <div class="text-xs text-muted-foreground">
                    Game Data & Imports
                  </div>
                </div>
              </div>
              <span
                v-if="user.battlenetId"
                class="text-xs font-medium text-green-400 border border-green-500/20 bg-green-500/10 px-2.5 py-1 rounded-full flex items-center gap-1.5"
              >
                <svg
                  class="w-3 h-3"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
                Connected
              </span>
              <span
                v-else
                class="text-xs font-medium text-muted-foreground border border-white/10 bg-white/5 px-2.5 py-1 rounded-full"
                >Not Connected</span
              >
            </div>

            <a
              v-if="!user.battlenetId"
              :href="battlenetUrl"
              class="block w-full text-center py-2 rounded-lg bg-[#148EFF] hover:bg-[#0070dd] text-white text-sm font-semibold transition-colors shadow-lg shadow-[#148EFF]/20"
            >
              Connect Battle.net
            </a>
            <button
              v-else
              disabled
              class="w-full py-2 rounded-lg bg-white/5 border border-white/10 text-muted-foreground text-sm font-medium cursor-not-allowed flex items-center justify-center gap-2"
            >
              <svg
                class="w-4 h-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 13l4 4L19 7"
                />
              </svg>
              Account Linked
            </button>
          </div>
        </div>
      </div>
      <!-- User Characters Card -->
      <div
        class="glass-card p-6 border-white/10 flex flex-col h-full bg-gradient-to-br from-black/40 to-black/20 lg:col-span-2"
      >
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-semibold text-white flex items-center gap-2">
            <svg
              class="w-5 h-5 text-primary"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
              />
            </svg>
            My Characters
          </h2>
          <div class="flex items-center gap-3">
            <button
              @click="handleFixLinkages"
              :disabled="fixingLinkages || !user?.battlenetId"
              class="text-xs text-amber-400 hover:text-amber-300 transition-colors flex items-center gap-1 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <svg
                v-if="fixingLinkages"
                class="animate-spin w-3 h-3"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  class="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  stroke-width="4"
                ></circle>
                <path
                  class="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                ></path>
              </svg>
              <svg
                v-else
                class="w-3 h-3"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
                />
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
              </svg>
              {{ fixingLinkages ? "Fixing..." : "Fix Links" }}
            </button>
            <a
              v-if="user?.battlenetId"
              :href="battlenetUrl"
              class="text-xs text-primary hover:text-primary/80 transition-colors flex items-center gap-1"
            >
              <svg
                class="w-3 h-3"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                />
              </svg>
              Refresh Characters
            </a>
          </div>
        </div>

        <!-- Linkage Fix Result -->
        <div
          v-if="linkageResult"
          class="mb-4 p-4 rounded-lg border"
          :class="
            (linkageResult.issues?.length || 0) > 0
              ? 'bg-amber-500/10 border-amber-500/20'
              : 'bg-green-500/10 border-green-500/20'
          "
        >
          <div class="flex items-start gap-3">
            <div
              :class="
                (linkageResult.issues?.length || 0) > 0
                  ? 'text-amber-400'
                  : 'text-green-400'
              "
            >
              <svg
                v-if="(linkageResult.issues?.length || 0) === 0"
                class="w-5 h-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <svg
                v-else
                class="w-5 h-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                />
              </svg>
            </div>
            <div class="flex-1">
              <p
                class="text-sm font-medium"
                :class="
                  (linkageResult.issues?.length || 0) > 0
                    ? 'text-amber-400'
                    : 'text-green-400'
                "
              >
                {{ linkageResult.summary }}
              </p>
              <div class="mt-2 text-xs text-muted-foreground space-y-1">
                <p v-if="linkageResult.orphanedMappingsRemoved > 0">
                  Removed {{ linkageResult.orphanedMappingsRemoved }} orphaned
                  link(s)
                </p>
                <p v-if="linkageResult.charactersAutoLinked > 0">
                  Auto-linked
                  {{ linkageResult.charactersAutoLinked }} character(s) to guild
                  roster
                </p>
                <p v-if="linkageResult.preferencesFixed">
                  Preferences were repaired
                </p>
                <p v-if="linkageResult.primaryCharacterSet">
                  Primary character was set
                </p>
              </div>
              <ul
                v-if="(linkageResult.issues?.length || 0) > 0"
                class="mt-2 text-xs text-amber-300 list-disc list-inside"
              >
                <li v-for="issue in linkageResult.issues" :key="issue">
                  {{ issue }}
                </li>
              </ul>
            </div>
            <button
              @click="linkageResult = null"
              class="text-muted-foreground hover:text-white transition-colors"
            >
              <svg
                class="w-4 h-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        </div>

        <!-- Linkage Fix Error -->
        <div
          v-if="linkageError"
          class="mb-4 p-4 rounded-lg bg-red-500/10 border border-red-500/20"
        >
          <div class="flex items-center gap-3">
            <svg
              class="w-5 h-5 text-red-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p class="text-sm text-red-400 flex-1">{{ linkageError }}</p>
            <button
              @click="linkageError = null"
              class="text-muted-foreground hover:text-white transition-colors"
            >
              <svg
                class="w-4 h-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        </div>

        <div
          v-if="loadingCharacters"
          class="text-center py-8 text-muted-foreground"
        >
          <svg
            class="animate-spin h-5 w-5 mx-auto mb-2 text-primary"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              class="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="4"
            ></circle>
            <path
              class="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
          Loading characters...
        </div>

        <div
          v-else-if="characters.length === 0"
          class="text-center py-8 text-muted-foreground border border-dashed border-white/10 rounded-xl bg-white/5"
        >
          <div v-if="!user?.battlenetId">
            Link your Battle.net account to see your characters.
          </div>
          <div v-else>
            No max-level characters found on your Battle.net account.
          </div>
        </div>

        <div
          v-else
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"
        >
          <div
            v-for="char in characters"
            :key="char.id"
            class="p-3 rounded-lg bg-white/5 border border-white/10 flex items-center gap-3 hover:bg-white/10 transition-colors"
          >
            <div
              class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold border border-white/20"
              :class="getClassColor(char.className)"
            >
              {{ char.level }}
            </div>
            <div class="overflow-hidden">
              <div
                class="font-medium text-white truncate"
                :class="getClassColorText(char.className)"
              >
                {{ char.name }}
              </div>
              <div class="text-xs text-muted-foreground truncate">
                {{ char.realm }} ({{ char.faction }})
              </div>
              <div
                class="text-[10px] text-muted-foreground/60 uppercase tracking-widest"
              >
                {{ char.className }} - {{ char.race }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
