<script setup lang="ts">
import { ref } from 'vue';
import KanbanBoard from '@/components/recruitment/KanbanBoard.vue';
import SmartWizard from '@/components/recruitment/SmartWizard.vue';

const activeTab = ref<'kanban' | 'scout'>('kanban');
</script>

<template>
    <div class="h-full flex flex-col space-y-6 p-8">
        <div class="flex justify-between items-center">
            <div>
                <h1 class="text-3xl font-bold text-white tracking-tight mb-2">Recruitment</h1>
                <p class="text-muted-foreground">Manage applications and scout for new talent.</p>
            </div>
            
            <div class="glass-card p-1 flex gap-1 bg-black/40">
                <button 
                    class="px-4 py-2 rounded-lg text-sm font-medium transition-all"
                    :class="activeTab === 'kanban' ? 'bg-primary text-white shadow-lg shadow-primary/20' : 'text-muted-foreground hover:text-white hover:bg-white/5'"
                    @click="activeTab = 'kanban'"
                >
                    Pipeline Board
                </button>
                <button 
                    class="px-4 py-2 rounded-lg text-sm font-medium transition-all"
                    :class="activeTab === 'scout' ? 'bg-primary text-white shadow-lg shadow-primary/20' : 'text-muted-foreground hover:text-white hover:bg-white/5'"
                    @click="activeTab = 'scout'"
                >
                    Smart Scout
                </button>
            </div>
        </div>

        <div class="flex-1 min-h-0">
            <KanbanBoard v-if="activeTab === 'kanban'" />
             <!-- Constrain width for the wizard -->
            <div v-else class="max-w-4xl mx-auto">
                 <SmartWizard />
            </div>
        </div>
    </div>
</template>
