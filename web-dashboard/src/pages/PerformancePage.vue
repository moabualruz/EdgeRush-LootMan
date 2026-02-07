<script setup lang="ts">
import { computed } from "vue";
import { useQuery } from "@tanstack/vue-query";
import { performanceApi } from "@/api/performance";
import { flpsApi } from "@/api/flps";
import { formatDate, formatRelativeTime } from "@/utils/date";
import { useAuthStore } from "@/stores/auth";
import { useGuildContextStore } from "@/stores/guildContext";
import SkeletonCard from "@/components/SkeletonCard.vue";
import SkeletonTable from "@/components/SkeletonTable.vue";
import { LineChart, ProgressBar, DonutChart } from "@/components/charts";

const authStore = useAuthStore();
const guildContextStore = useGuildContextStore();
const guildId = computed(
  () => guildContextStore.currentGuildId || authStore.user?.guildId,
);

// Performance data query
const {
  data: performanceData,
  isLoading: perfLoading,
  error: perfError,
} = useQuery({
  queryKey: ["myPerformance", guildId],
  queryFn: () => performanceApi.getMyPerformance(guildId.value!),
  enabled: computed(() => !!guildId.value),
});

// FLPS data for MAS breakdown
const { data: flpsData, isLoading: flpsLoading } = useQuery({
  queryKey: ["myFlps", guildId],
  queryFn: () => flpsApi.getMyFlps(guildId.value!),
  enabled: computed(() => !!guildId.value),
});

// Warcraft Logs reports (when we have raiderId)
const { data: wclReports, isLoading: wclLoading } = useQuery({
  queryKey: ["wclReports", guildId, flpsData.value?.raiderId],
  queryFn: () =>
    performanceApi.getWarcraftLogsReports(
      guildId.value!,
      flpsData.value!.raiderId,
      20,
    ),
  enabled: computed(() => !!guildId.value && !!flpsData.value?.raiderId),
});

// Performance trend for LineChart component
const trendChartData = computed(() => {
  if (!performanceData.value?.performanceTrend) return [];
  return performanceData.value.performanceTrend
    .slice(-30)
    .map((point, index) => ({
      x:
        index % 5 === 0
          ? new Date(point.date).toLocaleDateString("en-US", {
              month: "short",
              day: "numeric",
            })
          : "",
      y: point.dpa,
    }));
});

// RMS breakdown for donut chart
const rmsBreakdown = computed(() => {
  if (!flpsData.value?.rms) return [];
  return [
    { label: "ACS", value: flpsData.value.rms.acs * 100, color: "#3b82f6" },
    { label: "MAS", value: flpsData.value.rms.mas * 100, color: "#22c55e" },
    { label: "EPS", value: flpsData.value.rms.eps * 100, color: "#a855f7" },
  ];
});

// Helper functions
function getPercentileColor(percentile: number): string {
  if (percentile >= 95) return "text-orange-400"; // Legendary
  if (percentile >= 75) return "text-purple-400"; // Epic
  if (percentile >= 50) return "text-blue-400"; // Rare
  if (percentile >= 25) return "text-green-400"; // Uncommon
  return "text-gray-400"; // Common
}

function getPercentileBg(percentile: number): string {
  if (percentile >= 95) return "bg-orange-900/30";
  if (percentile >= 75) return "bg-purple-900/30";
  if (percentile >= 50) return "bg-blue-900/30";
  if (percentile >= 25) return "bg-green-900/30";
  return "bg-gray-800/30";
}

function getDifficultyColor(difficulty: string): string {
  switch (difficulty.toLowerCase()) {
    case "mythic":
      return "text-purple-400";
    case "heroic":
      return "text-orange-400";
    case "normal":
      return "text-green-400";
    default:
      return "text-gray-400";
  }
}

function formatMetric(value: number): string {
  if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`;
  if (value >= 1000) return `${(value / 1000).toFixed(1)}K`;
  return value.toFixed(1);
}

function getMasColor(mas: number): string {
  if (mas >= 0.9) return "text-green-400";
  if (mas >= 0.7) return "text-yellow-400";
  return "text-red-400";
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Performance Metrics</h1>
      <div v-if="performanceData" class="text-sm text-gray-400">
        {{ performanceData.characterName }}
      </div>
    </div>

    <!-- Loading state with skeletons -->
    <div v-if="perfLoading || flpsLoading" class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <SkeletonCard :lines="4" />
        <SkeletonCard :lines="4" />
      </div>
      <SkeletonCard :lines="3" />
      <SkeletonTable :rows="5" :columns="6" />
    </div>

    <!-- Error state -->
    <div v-else-if="perfError" class="alert alert-error">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        class="h-5 w-5"
      >
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
      <div>
        <h5 class="alert-title">Error Loading Performance</h5>
        <div class="alert-description">
          Failed to load performance data. Please try again later.
        </div>
      </div>
    </div>

    <!-- Content -->
    <div v-else-if="performanceData && flpsData" class="space-y-6">
      <!-- MAS Breakdown Card -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">
          Mechanical Adherence Score (MAS)
        </h2>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- MAS Score -->
          <div class="flex items-center justify-center">
            <div class="text-center">
              <div
                :class="[
                  'text-5xl font-bold',
                  getMasColor(flpsData.rms?.mas || 0),
                ]"
              >
                {{ ((flpsData.rms?.mas || 0) * 100).toFixed(0) }}%
              </div>
              <div class="text-gray-400 mt-2">Overall MAS</div>
            </div>
          </div>

          <!-- MAS Components -->
          <div class="space-y-4">
            <div>
              <ProgressBar
                :value="performanceData.dpa || 0"
                :max="(performanceData.specAverage || 0) * 1.2"
                label="Damage Per Active (DPA)"
                :show-percentage="false"
                color="#3b82f6"
              />
              <div class="text-xs text-gray-500 mt-1">
                Spec average: {{ formatMetric(performanceData.specAverage) }}
              </div>
            </div>

            <ProgressBar
              :value="performanceData.adt * 100"
              :max="100"
              label="Active Damage Time (ADT)"
              color="#22c55e"
            />
          </div>
        </div>
      </div>

      <!-- RMS Breakdown Card -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">
          Raider Merit Score (RMS) Breakdown
        </h2>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="bg-gray-800/50 rounded-lg p-4 text-center">
            <div class="text-3xl font-bold text-blue-400">
              {{ ((flpsData.rms?.acs || 0) * 100).toFixed(0) }}%
            </div>
            <div class="text-sm text-gray-400 mt-1">ACS (Attendance)</div>
          </div>
          <div class="bg-gray-800/50 rounded-lg p-4 text-center">
            <div
              :class="[
                'text-3xl font-bold',
                getMasColor(flpsData.rms?.mas || 0),
              ]"
            >
              {{ ((flpsData.rms?.mas || 0) * 100).toFixed(0) }}%
            </div>
            <div class="text-sm text-gray-400 mt-1">MAS (Mechanical)</div>
          </div>
          <div class="bg-gray-800/50 rounded-lg p-4 text-center">
            <div class="text-3xl font-bold text-purple-400">
              {{ ((flpsData.rms?.eps || 0) * 100).toFixed(0) }}%
            </div>
            <div class="text-sm text-gray-400 mt-1">EPS (Equipment)</div>
          </div>
        </div>

        <div class="mt-4 p-3 bg-gray-800/30 rounded-lg">
          <div class="flex justify-between items-center">
            <span class="text-gray-400">Combined RMS</span>
            <span class="text-xl font-bold text-primary-400">
              {{ ((flpsData.rms?.value || 0) * 100).toFixed(1) }}%
            </span>
          </div>
        </div>
      </div>

      <!-- Performance Trend Chart -->
      <div v-if="trendChartData.length > 1" class="card">
        <h2 class="text-lg font-semibold mb-4">
          Performance Trend (Last 30 Days)
        </h2>

        <LineChart
          :data="trendChartData"
          :height="200"
          :show-area="true"
          :show-points="true"
          :show-grid="true"
          line-color="#3b82f6"
          area-color="rgba(59, 130, 246, 0.2)"
          point-color="#60a5fa"
        />

        <div class="text-xs text-gray-500 mt-2 text-center">DPA over time</div>
      </div>

      <!-- Warcraft Logs Reports -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Recent Warcraft Logs Reports</h2>

        <div v-if="wclLoading" class="py-4">
          <SkeletonTable :rows="5" :columns="6" :show-header="false" />
        </div>

        <div v-else-if="wclReports?.reports?.length" class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-800/50">
              <tr>
                <th
                  class="text-left px-4 py-3 text-sm font-medium text-gray-400"
                >
                  Encounter
                </th>
                <th
                  class="text-left px-4 py-3 text-sm font-medium text-gray-400"
                >
                  Difficulty
                </th>
                <th
                  class="text-right px-4 py-3 text-sm font-medium text-gray-400"
                >
                  DPS/HPS
                </th>
                <th
                  class="text-right px-4 py-3 text-sm font-medium text-gray-400"
                >
                  Percentile
                </th>
                <th
                  class="text-right px-4 py-3 text-sm font-medium text-gray-400"
                >
                  Deaths
                </th>
                <th
                  class="text-left px-4 py-3 text-sm font-medium text-gray-400"
                >
                  Date
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-700">
              <tr
                v-for="report in wclReports.reports"
                :key="`${report.reportId}-${report.encounterId}`"
                :class="[
                  'hover:bg-gray-800/30 transition-colors',
                  getPercentileBg(report.percentile),
                ]"
              >
                <td class="px-4 py-3">
                  <span class="font-medium">{{ report.encounterName }}</span>
                </td>
                <td class="px-4 py-3">
                  <span :class="getDifficultyColor(report.difficulty)">
                    {{ report.difficulty }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right font-mono">
                  {{ formatMetric(report.dps || report.hps || 0) }}
                </td>
                <td class="px-4 py-3 text-right">
                  <span
                    :class="[
                      'font-bold',
                      getPercentileColor(report.percentile),
                    ]"
                  >
                    {{ report.percentile }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right">
                  <span
                    :class="
                      report.deaths > 0 ? 'text-red-400' : 'text-green-400'
                    "
                  >
                    {{ report.deaths }}
                  </span>
                </td>
                <td class="px-4 py-3 text-sm text-gray-400">
                  {{ formatDate(report.date) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="text-center py-8 text-gray-400">
          No Warcraft Logs reports found.
        </div>
      </div>

      <!-- Percentile Legend -->
      <div class="card">
        <h3 class="text-sm font-semibold mb-3">Percentile Legend</h3>
        <div class="flex flex-wrap gap-4 text-sm">
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-orange-400"></span>
            <span class="text-gray-400">95%+ (Legendary)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-purple-400"></span>
            <span class="text-gray-400">75-94% (Epic)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-blue-400"></span>
            <span class="text-gray-400">50-74% (Rare)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-green-400"></span>
            <span class="text-gray-400">25-49% (Uncommon)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-gray-400"></span>
            <span class="text-gray-400">&lt;25% (Common)</span>
          </div>
        </div>
      </div>

      <!-- Last Updated -->
      <div class="text-sm text-gray-500 text-center">
        Last updated: {{ formatRelativeTime(performanceData.lastUpdated) }}
      </div>
    </div>
  </div>
</template>
