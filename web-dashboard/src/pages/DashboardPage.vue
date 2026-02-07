<script setup lang="ts">
import { ref, computed } from "vue";
import { useQuery } from "@tanstack/vue-query";
import { flpsApi } from "@/api/flps";
import { lootApi } from "@/api/loot";
import { useAuthStore } from "@/stores/auth";
import { useGuildContextStore } from "@/stores/guildContext";
import ScoreCard from "@/components/ScoreCard.vue";
import ScoreBreakdown from "@/components/ScoreBreakdown.vue";
import FlpsVisualization from "@/components/FlpsVisualization.vue";
import RecentLoot from "@/components/RecentLoot.vue";
import SkeletonCard from "@/components/SkeletonCard.vue";
import SkeletonProfile from "@/components/SkeletonProfile.vue";

const authStore = useAuthStore();
const guildContextStore = useGuildContextStore();
const guildId = computed(
  () => guildContextStore.currentGuildId || authStore.user?.guildId,
);

// View mode for FLPS breakdown
const detailedView = ref(false);

const {
  data: flpsData,
  isLoading: flpsLoading,
  error: flpsError,
} = useQuery({
  queryKey: ["myFlps", guildId],
  queryFn: () => flpsApi.getMyFlps(guildId.value!),
  enabled: computed(() => !!guildId.value),
});

const { data: lootData, isLoading: lootLoading } = useQuery({
  queryKey: ["myLootHistory", guildId],
  queryFn: () => lootApi.getMyLootHistory(guildId.value!, 5),
  enabled: computed(() => !!guildId.value),
});

const scoreColor = computed(() => {
  if (!flpsData.value) return "text-muted-foreground";
  const score = flpsData.value.flps;
  if (score >= 0.8) return "text-green-400 drop-shadow-md";
  if (score >= 0.5) return "text-yellow-400 drop-shadow-md";
  return "text-red-400 drop-shadow-md";
});

const hasRequiredData = computed(() => {
  return (
    flpsData.value &&
    typeof flpsData.value.flps === "number" &&
    !!flpsData.value.rms &&
    !!flpsData.value.ipi
  );
});
</script>

<template>
  <div class="space-y-8">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold tracking-tight text-white mb-2 text-glow">
          Mission Control
        </h1>
        <p class="text-muted-foreground">
          Your performance command center for
          {{ guildContextStore.activeGuild?.guildName || "your guild" }}
        </p>
      </div>
      <div v-if="flpsData" class="flex gap-2">
        <span
          class="px-3 py-1 rounded-full text-xs font-medium border border-border bg-black/40 text-muted-foreground"
        >
          Updated: Today
        </span>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="flpsLoading && guildId" class="space-y-6">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <SkeletonProfile :stats-count="2" />
        <div class="lg:col-span-2">
          <SkeletonCard :lines="6" />
        </div>
      </div>
      <SkeletonCard :lines="4" />
    </div>

    <!-- No Guild state -->
    <div
      v-else-if="!guildId"
      class="glass-card p-8 text-center border-primary/20"
    >
      <div class="inline-flex p-4 rounded-full bg-primary/10 mb-4">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-8 h-8 text-primary"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
          />
        </svg>
      </div>
      <h2 class="text-xl font-bold text-white mb-2">Join a Guild</h2>
      <p class="text-muted-foreground max-w-md mx-auto mb-6">
        You are not currently a member of any guild. Join a guild to start
        tracking your performance and loot eligibility.
      </p>
      <button
        class="bg-primary hover:bg-primary/90 text-white px-6 py-2 rounded-lg font-medium transition-colors"
      >
        Find a Guild
      </button>
    </div>

    <!-- Error state -->
    <div
      v-else-if="flpsError"
      class="glass-card p-6 border-destructive/50 bg-destructive/5"
    >
      <div class="flex items-center gap-3 text-destructive-foreground">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-6 h-6"
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
        <p class="font-medium">
          Failed to load performance data. The sync service might be offline.
        </p>
      </div>
    </div>

    <!-- Main Content -->
    <div v-else-if="hasRequiredData" class="space-y-8 animate-fade-in-up">
      <!-- Eligibility Alert -->
      <div
        v-if="!flpsData!.eligible && flpsData!.ineligibilityReasons?.length"
        class="glass-card p-4 border-yellow-500/30 bg-yellow-500/5 flex gap-4 items-start"
      >
        <div class="p-2 rounded-md bg-yellow-500/10 text-yellow-500 shrink-0">
          <svg
            xmlns="http://www.w3.org/2000/svg"
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
        <div>
          <h3 class="font-semibold text-yellow-400 mb-1">
            Loot Eligibility Warning
          </h3>
          <ul
            class="text-sm text-yellow-200/80 space-y-1 list-disc list-inside"
          >
            <li v-for="reason in flpsData!.ineligibilityReasons" :key="reason">
              {{ reason }}
            </li>
          </ul>
        </div>
      </div>

      <!-- Performance Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Main Score Card -->
        <div class="glass-card lg:col-span-1 relative overflow-hidden group">
          <div
            class="absolute inset-0 bg-gradient-to-br from-primary/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"
          ></div>
          <ScoreCard
            :score="flpsData!.flps"
            :rank="flpsData!.rank"
            :eligible="flpsData!.eligible"
            :character-name="flpsData!.characterName"
            :character-class="flpsData!.characterClass"
          />
        </div>

        <!-- Breakdown Card -->
        <div class="glass-card lg:col-span-2 p-6 flex flex-col">
          <div class="flex items-center justify-between mb-6">
            <h2
              class="text-lg font-semibold text-white flex items-center gap-2"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="w-5 h-5 text-primary"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                />
              </svg>
              Score Breakdown
            </h2>
            <button
              @click="detailedView = !detailedView"
              class="text-xs font-medium text-primary hover:text-white transition-colors border border-primary/30 rounded-md px-3 py-1.5 hover:bg-primary/20"
            >
              {{ detailedView ? "Show Simple View" : "Show Detailed Metrics" }}
            </button>
          </div>

          <div class="flex-1">
            <ScoreBreakdown
              v-if="!detailedView"
              :rms="flpsData!.rms"
              :ipi="flpsData!.ipi"
              :rdf="flpsData!.rdf"
            />
            <FlpsVisualization
              v-else
              :flps="flpsData!.flps"
              :rms="flpsData!.rms"
              :ipi="flpsData!.ipi"
              :rdf="flpsData!.rdf"
            />
          </div>
        </div>
      </div>

      <!-- Recent Loot -->
      <div class="glass-card p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-semibold text-white flex items-center gap-2">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="w-5 h-5 text-purple-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
              />
            </svg>
            Recent Loot History
          </h2>
          <RouterLink
            to="/history"
            class="text-xs text-muted-foreground hover:text-white transition-colors"
            >View All</RouterLink
          >
        </div>

        <div v-if="lootLoading">
          <SkeletonCard :lines="3" :show-header="false" />
        </div>
        <RecentLoot v-else-if="lootData" :awards="lootData.awards" />
        <p v-else class="text-muted-foreground text-center py-8">
          No loot history recording in the last 30 days.
        </p>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="glass-card p-8 text-center border-primary/20">
      <div class="inline-flex p-4 rounded-full bg-primary/10 mb-4">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-8 h-8 text-primary"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
          />
        </svg>
      </div>
      <h2 class="text-xl font-bold text-white mb-2">No Performance Data</h2>
      <p class="text-muted-foreground max-w-md mx-auto">
        We couldn't find any performance data for your character. This usually
        means you haven't participated in any logged raids yet.
      </p>
    </div>
  </div>
</template>
