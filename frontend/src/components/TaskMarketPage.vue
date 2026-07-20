<template>
  <TasksPage
    :task-loading="taskLoading"
    :task-form="taskForm"
    :task-market="taskMarket"
    :query-state="queryState"
    :submission-form="submissionForm"
    :my-artworks="myArtworks"
    :current-user="currentUser"
    :my-task-submissions="myTaskSubmissions"
    :my-tasks="[]"
    :selected-task-submissions="selectedTaskSubmissions"
    :task-review-form="taskReviewForm"
    :format-points="formatPoints"
    :format-deadline="formatDeadline"
    :status-label="statusLabel"
    :is-favorite-target="isFavoriteTarget"
    :is-subscribed-target="isSubscribedTarget"
    :focus-target-id="focusTargetId"
    :focus-target-stamp="focusTargetStamp"
    initial-view="market"
    :allow-workspace="false"
    @refresh="$emit('refresh', $event)"
    @reset-task-form="$emit('reset-task-form')"
    @patch-task-form="$emit('patch-task-form', $event)"
    @save-task="$emit('save-task')"
    @prepare-submission="$emit('prepare-submission', $event)"
    @patch-submission-form="$emit('patch-submission-form', $event)"
    @submit-task-work="$emit('submit-task-work')"
    @edit-task="$emit('edit-task', $event)"
    @publish-task-item="$emit('publish-task-item', $event)"
    @close-task-item="$emit('close-task-item', $event)"
    @load-task-submissions="$emit('load-task-submissions', $event)"
    @prepare-task-review="$emit('prepare-task-review', $event)"
    @review-submission="$emit('review-submission', $event)"
    @patch-task-review-form="$emit('patch-task-review-form', $event)"
    @go-related="$emit('go-related', $event)"
    @toggle-favorite-target="$emit('toggle-favorite-target', $event)"
    @toggle-subscription-target="$emit('toggle-subscription-target', $event)"
  />
</template>

<script setup>
import TasksPage from './TasksPage.vue'

defineProps({
  taskLoading: { type: Boolean, default: false },
  taskForm: { type: Object, required: true },
  taskMarket: { type: Array, default: () => [] },
  queryState: { type: Object, required: true },
  submissionForm: { type: Object, required: true },
  myArtworks: { type: Array, default: () => [] },
  currentUser: { type: Object, default: null },
  myTaskSubmissions: { type: Array, default: () => [] },
  selectedTaskSubmissions: { type: Array, default: () => [] },
  taskReviewForm: { type: Object, required: true },
  formatPoints: { type: Function, required: true },
  formatDeadline: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  isFavoriteTarget: { type: Function, default: null },
  isSubscribedTarget: { type: Function, default: null },
  focusTargetId: { type: Number, default: null },
  focusTargetStamp: { type: Number, default: 0 }
})

defineEmits([
  'refresh',
  'reset-task-form',
  'patch-task-form',
  'save-task',
  'prepare-submission',
  'patch-submission-form',
  'submit-task-work',
  'edit-task',
  'publish-task-item',
  'close-task-item',
  'load-task-submissions',
  'prepare-task-review',
  'review-submission',
  'patch-task-review-form',
  'go-related',
  'toggle-favorite-target',
  'toggle-subscription-target'
])
</script>
