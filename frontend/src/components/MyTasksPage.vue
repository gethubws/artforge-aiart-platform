<template>
  <TasksPage
    :task-loading="taskLoading"
    :task-form="taskForm"
    :task-market="[]"
    :task-market-status-filter="taskMarketStatusFilter"
    :submission-form="submissionForm"
    :my-artworks="myArtworks"
    :current-user="currentUser"
    :my-task-submissions="myTaskSubmissions"
    :my-tasks="myTasks"
    :my-task-status-filter="myTaskStatusFilter"
    :selected-task-submissions="selectedTaskSubmissions"
    :task-review-form="taskReviewForm"
    :format-points="formatPoints"
    :format-deadline="formatDeadline"
    :status-label="statusLabel"
    initial-view="workspace"
    :allow-market="false"
    @refresh="$emit('refresh')"
    @reset-task-form="$emit('reset-task-form')"
    @patch-task-form="$emit('patch-task-form', $event)"
    @save-task="$emit('save-task')"
    @update:taskMarketStatusFilter="$emit('update:taskMarketStatusFilter', $event)"
    @prepare-submission="$emit('prepare-submission', $event)"
    @patch-submission-form="$emit('patch-submission-form', $event)"
    @submit-task-work="$emit('submit-task-work')"
    @update:myTaskStatusFilter="$emit('update:myTaskStatusFilter', $event)"
    @edit-task="$emit('edit-task', $event)"
    @publish-task-item="$emit('publish-task-item', $event)"
    @close-task-item="$emit('close-task-item', $event)"
    @load-task-submissions="$emit('load-task-submissions', $event)"
    @prepare-task-review="$emit('prepare-task-review', $event)"
    @review-submission="$emit('review-submission', $event)"
    @patch-task-review-form="$emit('patch-task-review-form', $event)"
  />
</template>

<script setup>
import TasksPage from './TasksPage.vue'

defineProps({
  taskLoading: { type: Boolean, default: false },
  taskForm: { type: Object, required: true },
  taskMarketStatusFilter: { type: String, default: 'PUBLISHED' },
  submissionForm: { type: Object, required: true },
  myArtworks: { type: Array, default: () => [] },
  currentUser: { type: Object, default: null },
  myTaskSubmissions: { type: Array, default: () => [] },
  myTasks: { type: Array, default: () => [] },
  myTaskStatusFilter: { type: String, default: '' },
  selectedTaskSubmissions: { type: Array, default: () => [] },
  taskReviewForm: { type: Object, required: true },
  formatPoints: { type: Function, required: true },
  formatDeadline: { type: Function, required: true },
  statusLabel: { type: Function, required: true }
})

defineEmits([
  'refresh',
  'reset-task-form',
  'patch-task-form',
  'save-task',
  'update:taskMarketStatusFilter',
  'prepare-submission',
  'patch-submission-form',
  'submit-task-work',
  'update:myTaskStatusFilter',
  'edit-task',
  'publish-task-item',
  'close-task-item',
  'load-task-submissions',
  'prepare-task-review',
  'review-submission',
  'patch-task-review-form'
])
</script>
