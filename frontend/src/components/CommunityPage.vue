<template>
  <section class="community-view">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Community</p>
        <h1>作品广场</h1>
      </div>
      <div class="library-tools">
        <el-select :model-value="communityTagFilter" multiple clearable filterable collapse-tags collapse-tags-tooltip placeholder="标签筛选" @update:model-value="$emit('update:communityTagFilter', $event)">
          <el-option v-for="tag in selectableTags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
        <el-button :icon="Refresh" @click="$emit('refresh')">刷新</el-button>
      </div>
    </div>

    <div class="gallery-grid">
      <article v-for="artwork in publicArtworks" :key="artwork.id" class="art-card" @click="$emit('open-detail', artwork)">
        <img :src="artwork.imageUrl" :alt="artwork.title" />
        <div class="art-card-body">
          <div class="art-title-row"><strong>{{ artwork.title }}</strong><Star /></div>
          <p>{{ artwork.promptText }}</p>
          <div v-if="artwork.tags?.length" class="art-tag-strip"><span v-for="tag in artwork.tags.slice(0, 4)" :key="tag.id">{{ tag.name }}</span></div>
          <span>{{ formatDate(artwork.createdAt) }}</span>
        </div>
      </article>
      <div v-if="!publicArtworks.length" class="gallery-empty">暂无公开作品</div>
    </div>
  </section>
</template>

<script setup>
import { Refresh, Star } from '@element-plus/icons-vue'

defineProps({
  communityTagFilter: { type: Array, default: () => [] },
  selectableTags: { type: Array, default: () => [] },
  publicArtworks: { type: Array, default: () => [] },
  formatDate: { type: Function, required: true }
})

defineEmits(['update:communityTagFilter', 'refresh', 'open-detail'])
</script>
