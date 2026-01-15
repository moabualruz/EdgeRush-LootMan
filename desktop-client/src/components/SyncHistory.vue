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
  <div class="sync-history">
    <div class="header">
      <h2 class="title">Sync History</h2>
      <span class="count">{{ history.length }} syncs</span>
    </div>

    <div v-if="history.length === 0" class="empty">
      <p>No sync history yet</p>
      <p class="hint">Syncs will appear here after your first sync</p>
    </div>

    <div v-else class="history-list">
      <div
        v-for="record in history"
        :key="record.id"
        class="history-item"
        :class="{ error: !record.success }"
      >
        <div class="item-header">
          <div class="status-badge" :class="{ success: record.success, error: !record.success }">
            {{ record.success ? "Success" : "Failed" }}
          </div>
          <div class="timestamp">
            <span class="date">{{ formatDate(record.timestamp) }}</span>
            <span class="time">{{ formatTime(record.timestamp) }}</span>
          </div>
        </div>

        <div class="item-details">
          <div class="detail">
            <span class="detail-label">Characters</span>
            <span class="detail-value">{{ record.characters_synced }}</span>
          </div>
          <div class="detail">
            <span class="detail-label">Gear Items</span>
            <span class="detail-value">{{ record.gear_items_synced }}</span>
          </div>
          <div class="detail">
            <span class="detail-label">FLPS Updated</span>
            <span class="detail-value">{{ record.flps_updated ? "Yes" : "No" }}</span>
          </div>
        </div>

        <div v-if="record.message" class="item-message">
          {{ record.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sync-history {
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.count {
  font-size: 14px;
  color: var(--text-secondary);
}

.empty {
  text-align: center;
  padding: 40px 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.empty p {
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.empty .hint {
  font-size: 14px;
  opacity: 0.7;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 16px;
  border-left: 4px solid var(--success);
}

.history-item.error {
  border-left-color: var(--error);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.success {
  background: rgba(74, 222, 128, 0.2);
  color: var(--success);
}

.status-badge.error {
  background: rgba(239, 68, 68, 0.2);
  color: var(--error);
}

.timestamp {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.date {
  font-size: 14px;
  color: var(--text-primary);
}

.time {
  font-size: 12px;
  color: var(--text-secondary);
}

.item-details {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid var(--bg-tertiary);
}

.detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
}

.item-message {
  margin-top: 12px;
  padding: 10px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 6px;
  font-size: 13px;
  color: var(--error);
}
</style>
