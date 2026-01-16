<script setup lang="ts">
import { ref, onMounted } from "vue";

interface SyncRecord {
  id: number;
  timestamp: Date;
  success: boolean;
  characters_synced: number;
  gear_items_synced: number;
  flps_updated: boolean;
  message: string | null;
}

const history = ref<SyncRecord[]>([]);

// Mock data for now - would be loaded from Tauri backend
onMounted(() => {
  // In production, this would fetch from the backend
  history.value = [
    {
      id: 1,
      timestamp: new Date(Date.now() - 1000 * 60 * 5),
      success: true,
      characters_synced: 1,
      gear_items_synced: 16,
      flps_updated: true,
      message: null,
    },
    {
      id: 2,
      timestamp: new Date(Date.now() - 1000 * 60 * 30),
      success: true,
      characters_synced: 1,
      gear_items_synced: 16,
      flps_updated: true,
      message: null,
    },
    {
      id: 3,
      timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2),
      success: false,
      characters_synced: 0,
      gear_items_synced: 0,
      flps_updated: false,
      message: "API connection failed",
    },
  ];
});

function formatTime(date: Date): string {
  return date.toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatDate(date: Date): string {
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  if (date.toDateString() === today.toDateString()) {
    return "Today";
  } else if (date.toDateString() === yesterday.toDateString()) {
    return "Yesterday";
  } else {
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
    });
  }
}
</script>

<template>
  <div class="space-y-6 pb-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-white tracking-tight">Sync Logs</h2>
        <p class="text-muted-foreground">Historical record of all sync operations</p>
      </div>
      <div class="px-3 py-1 rounded-full bg-white/5 border border-white/10 text-xs font-medium text-muted-foreground">
        {{ history.length }} Records
      </div>
    </div>

    <div v-if="history.length === 0" class="flex flex-col items-center justify-center p-12 glass-card border-white/5 text-center">
      <div class="w-12 h-12 rounded-full bg-muted flex items-center justify-center mb-4">
        <svg class="w-6 h-6 text-muted-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
      </div>
      <h3 class="text-lg font-medium text-white">No sync history yet</h3>
      <p class="text-muted-foreground mt-1">Syncs will appear here after your first operation</p>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="record in history"
        :key="record.id"
        class="glass-card p-0 border-white/5 overflow-hidden transition-all hover:bg-white/5"
      >
        <div class="p-4 border-l-4" :class="record.success ? 'border-l-green-500' : 'border-l-destructive'">
          <div class="flex items-start justify-between mb-4">
             <div class="flex items-center gap-3">
               <span 
                class="px-2.5 py-0.5 rounded-full text-xs font-bold uppercase tracking-wider"
                :class="record.success ? 'bg-green-500/10 text-green-500' : 'bg-destructive/10 text-destructive'"
              >
                {{ record.success ? "Success" : "Failed" }}
              </span>
              <span v-if="record.message" class="text-sm text-destructive">{{ record.message }}</span>
             </div>
             
             <div class="text-right">
               <div class="text-sm font-medium text-white">{{ formatDate(record.timestamp) }}</div>
               <div class="text-xs text-muted-foreground">{{ formatTime(record.timestamp) }}</div>
             </div>
          </div>

          <div class="grid grid-cols-3 gap-4 pt-4 border-t border-white/5">
            <div>
              <div class="text-[10px] uppercase tracking-wider text-muted-foreground mb-1">Characters</div>
              <div class="text-sm font-medium text-white">{{ record.characters_synced }}</div>
            </div>
            <div>
              <div class="text-[10px] uppercase tracking-wider text-muted-foreground mb-1">Gear Items</div>
              <div class="text-sm font-medium text-white">{{ record.gear_items_synced }}</div>
            </div>
            <div>
              <div class="text-[10px] uppercase tracking-wider text-muted-foreground mb-1">FLPS Updated</div>
              <div class="text-sm font-medium" :class="record.flps_updated ? 'text-green-400' : 'text-muted-foreground'">{{ record.flps_updated ? "Yes" : "No" }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
