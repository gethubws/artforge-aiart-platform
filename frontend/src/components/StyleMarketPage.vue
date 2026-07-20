<template>
  <StylePackagesPage
    :style-form="styleForm"
    :style-loading="styleLoading"
    :all-tags="allTags"
    :owned-artworks="ownedArtworks"
    :query-state="queryState"
    :my-style-packages="[]"
    :market-style-packages="marketStylePackages"
    :my-style-package-submissions="myStylePackageSubmissions"
    :style-package-versions="stylePackageVersions"
    :style-package-reviews="stylePackageReviews"
    :style-submissions="[]"
    :active-style-package-name="activeStylePackageName"
    :style-submission-form="styleSubmissionForm"
    :style-review-form="styleReviewForm"
    :style-stats-items="styleStatsItems"
    :format-date="formatDate"
    :status-label="statusLabel"
    :is-favorite-target="isFavoriteTarget"
    :is-subscribed-target="isSubscribedTarget"
    :focus-target-id="focusTargetId"
    :focus-target-stamp="focusTargetStamp"
    initial-view="market"
    :allow-workspace="false"
    @refresh="$emit('refresh')"
    @reset-form="$emit('reset-form')"
    @patch-style-form="$emit('patch-style-form', $event)"
    @save="$emit('save')"
    @edit-pack="$emit('edit-pack', $event)"
    @apply-pack="$emit('apply-pack', $event)"
    @publish-pack="$emit('publish-pack', $event)"
    @archive-pack="$emit('archive-pack', $event)"
    @load-versions="$emit('load-versions', $event)"
    @load-submissions="$emit('load-submissions', $event)"
    @load-artworks="$emit('load-artworks', $event)"
    @exchange-pack="$emit('exchange-pack', $event)"
    @prepare-submission="$emit('prepare-submission', $event)"
    @patch-submission-form="$emit('patch-submission-form', $event)"
    @submit-submission="$emit('submit-submission')"
    @prepare-review="$emit('prepare-review', $event)"
    @load-reviews="$emit('load-reviews', $event)"
    @patch-review-form="$emit('patch-review-form', $event)"
    @submit-review="$emit('submit-review')"
    @review-submission="$emit('review-submission', $event)"
    @toggle-favorite-target="$emit('toggle-favorite-target', $event)"
    @toggle-subscription-target="$emit('toggle-subscription-target', $event)"
  />
</template>

<script setup>
import StylePackagesPage from './StylePackagesPage.vue'

defineProps({
  styleForm: { type: Object, required: true },
  styleLoading: { type: Boolean, default: false },
  allTags: { type: Array, default: () => [] },
  ownedArtworks: { type: Array, default: () => [] },
  queryState: { type: Object, required: true },
  marketStylePackages: { type: Array, default: () => [] },
  myStylePackageSubmissions: { type: Array, default: () => [] },
  stylePackageVersions: { type: Array, default: () => [] },
  stylePackageReviews: { type: Array, default: () => [] },
  activeStylePackageName: { type: String, default: '' },
  styleSubmissionForm: { type: Object, required: true },
  styleReviewForm: { type: Object, required: true },
  styleStatsItems: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  isFavoriteTarget: { type: Function, default: null },
  isSubscribedTarget: { type: Function, default: null },
  focusTargetId: { type: Number, default: null },
  focusTargetStamp: { type: Number, default: 0 }
})

defineEmits([
  'refresh',
  'reset-form',
  'patch-style-form',
  'save',
  'edit-pack',
  'apply-pack',
  'publish-pack',
  'archive-pack',
  'load-versions',
  'load-submissions',
  'load-artworks',
  'exchange-pack',
  'prepare-submission',
  'patch-submission-form',
  'submit-submission',
  'prepare-review',
  'load-reviews',
  'patch-review-form',
  'submit-review',
  'review-submission',
  'toggle-favorite-target',
  'toggle-subscription-target'
])
</script>
