<template>
  <StylePackagesPage
    :style-form="styleForm"
    :style-loading="styleLoading"
    :my-style-packages="[]"
    :market-style-packages="marketStylePackages"
    :style-package-versions="stylePackageVersions"
    :style-package-reviews="stylePackageReviews"
    :style-submissions="[]"
    :active-style-package-name="activeStylePackageName"
    :style-review-form="styleReviewForm"
    :style-stats-items="styleStatsItems"
    :format-date="formatDate"
    :status-label="statusLabel"
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
    @prepare-review="$emit('prepare-review', $event)"
    @load-reviews="$emit('load-reviews', $event)"
    @patch-review-form="$emit('patch-review-form', $event)"
    @submit-review="$emit('submit-review')"
    @review-submission="$emit('review-submission', $event)"
  />
</template>

<script setup>
import StylePackagesPage from './StylePackagesPage.vue'

defineProps({
  styleForm: { type: Object, required: true },
  styleLoading: { type: Boolean, default: false },
  marketStylePackages: { type: Array, default: () => [] },
  stylePackageVersions: { type: Array, default: () => [] },
  stylePackageReviews: { type: Array, default: () => [] },
  activeStylePackageName: { type: String, default: '' },
  styleReviewForm: { type: Object, required: true },
  styleStatsItems: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  statusLabel: { type: Function, required: true }
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
  'prepare-review',
  'load-reviews',
  'patch-review-form',
  'submit-review',
  'review-submission'
])
</script>
