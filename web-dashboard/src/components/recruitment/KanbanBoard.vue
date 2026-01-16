<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { 
    recruitmentApi, 
    type RecruitmentApplication, 
    RecruitmentStatus 
} from '@/api/recruitment';
import { useGuildContextStore } from '@/stores/guildContext';

const guildStore = useGuildContextStore();
const applications = ref<RecruitmentApplication[]>([]);
const loading = ref(false);

const columns = [
    { id: RecruitmentStatus.PENDING, label: 'Applied', color: 'border-blue-500/50' },
    { id: RecruitmentStatus.SCREENED, label: 'Screened', color: 'border-purple-500/50' },
    { id: RecruitmentStatus.INTERVIEW, label: 'Interview', color: 'border-yellow-500/50' },
    { id: RecruitmentStatus.TRIAL, label: 'Trial', color: 'border-orange-500/50' },
    { id: RecruitmentStatus.ACCEPTED, label: 'Roster', color: 'border-green-500/50' },
    { id: RecruitmentStatus.REJECTED, label: 'Rejected', color: 'border-red-500/50' },
];

const getApplicationsByStatus = (status: string) => 
    applications.value.filter(app => app.status === status);

onMounted(async () => {
    if (guildStore.currentGuildId) {
        loading.value = true;
        try {
            applications.value = await recruitmentApi.getApplications(guildStore.currentGuildId);
        } finally {
            loading.value = false;
        }
    }
});

// Drag and Drop Logic
const draggedItem = ref<RecruitmentApplication | null>(null);

function onDragStart(event: DragEvent, app: RecruitmentApplication) {
    draggedItem.value = app;
    if (event.dataTransfer) {
        event.dataTransfer.effectAllowed = 'move';
        event.dataTransfer.setData('text/plain', app.id);
    }
}

async function onDrop(event: DragEvent, status: string) {
    event.preventDefault();
    const appId = event.dataTransfer?.getData('text/plain');
    if (appId && draggedItem.value && draggedItem.value.id === appId) {
        // Update local state optimistically
        const oldStatus = draggedItem.value.status;
        draggedItem.value.status = status as any; // Cast to enum

        // Call API
        try {
            await recruitmentApi.updateStatus(appId, status as any, "Officer"); // Mock reviewer
        } catch (e) {
            // Revert
            draggedItem.value.status = oldStatus;
            console.error("Failed to update status", e);
        }
    }
    draggedItem.value = null;
}
</script>

<template>
    <div class="h-full overflow-x-auto pb-4">
        <div class="flex gap-4 h-full min-w-[1200px]">
            <div 
                v-for="col in columns" 
                :key="col.id"
                class="flex-1 min-w-[280px] flex flex-col glass-card border-t-4 bg-black/20"
                :class="col.color"
                @dragover.prevent
                @dragenter.prevent
                @drop="onDrop($event, col.id)"
            >
                <!-- Column Header -->
                <div class="p-4 border-b border-white/5 flex justify-between items-center bg-white/5">
                    <h3 class="font-bold text-white text-sm uppercase tracking-wider">{{ col.label }}</h3>
                    <span class="text-xs font-mono text-muted-foreground bg-black/40 px-2 py-1 rounded">
                        {{ getApplicationsByStatus(col.id).length }}
                    </span>
                </div>

                <!-- Column Content -->
                <div class="flex-1 p-3 space-y-3 overflow-y-auto">
                    <div 
                        v-for="app in getApplicationsByStatus(col.id)"
                        :key="app.id"
                        draggable="true"
                        @dragstart="onDragStart($event, app)"
                        class="p-4 rounded-lg bg-[#1a1a1a] border border-white/5 hover:border-primary/50 hover:bg-[#252525] transition-all cursor-move shadow-sm group relative"
                    >
                        <div class="flex justify-between items-start mb-2">
                             <div class="font-bold text-white group-hover:text-primary transition-colors">
                                {{ app.applicant.character.name }}
                             </div>
                             <div class="text-xs text-muted-foreground bg-black/40 px-1.5 py-0.5 rounded">
                                {{ app.applicant.character.itemLevel.toFixed(0) }}
                             </div>
                        </div>
                        
                        <div class="text-xs text-muted-foreground mb-3">
                            {{ app.applicant.character.specialization }} {{ app.applicant.character.characterClass }}
                        </div>

                        <div class="flex items-center justify-between pt-2 border-t border-white/5 text-[10px] text-muted-foreground uppercase tracking-wider">
                            <span>{{ app.applicant.character.realm }}</span>
                            <span v-if="app.applicant.character.scores.raiderIoScore" class="text-orange-400 font-medium">
                                {{ app.applicant.character.scores.raiderIoScore }} IO
                            </span>
                        </div>
                    </div>

                    <!-- Empty State -->
                    <div 
                        v-if="getApplicationsByStatus(col.id).length === 0" 
                        class="h-24 border-2 border-dashed border-white/5 rounded-lg flex items-center justify-center text-muted-foreground text-xs"
                    >
                        Drop Here
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
