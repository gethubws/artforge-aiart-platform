<template>
  <span class="bilingual-tag-label">
    <span class="bilingual-tag-main">{{ displayNameZh || name }}</span>
    <span v-if="showEnglish" class="bilingual-tag-sub">{{ name }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, default: '' },
  displayNameZh: { type: String, default: '' }
})

const showEnglish = computed(() => {
  return Boolean(props.name) && Boolean(props.displayNameZh) && props.displayNameZh !== props.name
})
</script>
