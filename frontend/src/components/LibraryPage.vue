<template>
  <section class="library-view">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Gallery</p>
        <h1>作品库</h1>
      </div>
      <div class="library-tools">
        <el-input :model-value="libraryKeyword" :prefix-icon="Search" placeholder="搜索标题或提示词" clearable @update:model-value="$emit('update:libraryKeyword', $event)" />
        <el-select :model-value="libraryTagFilter" multiple clearable filterable collapse-tags collapse-tags-tooltip placeholder="标签筛选" @update:model-value="$emit('update:libraryTagFilter', $event)">
          <el-option v-for="tag in selectableTags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
        <el-select :model-value="libraryVisibilityFilter" clearable placeholder="可见性" @update:model-value="$emit('update:libraryVisibilityFilter', $event)">
          <el-option label="私有" value="PRIVATE" />
          <el-option label="公开" value="PUBLIC" />
        </el-select>
        <el-select :model-value="libraryStatusFilter" clearable placeholder="状态" @update:model-value="$emit('update:libraryStatusFilter', $event)">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="审核中" value="PENDING_AUDIT" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
        <el-button :icon="Refresh" @click="$emit('refresh')">刷新</el-button>
      </div>
    </div>

    <div class="library-batch-bar">
      <div class="batch-selection-copy">
        <strong>{{ selectedArtworkCount }}</strong>
        <span>已选择</span>
      </div>
      <div class="batch-actions">
        <el-button size="small" @click="$emit('toggle-all-filtered')">{{ allFilteredArtworksSelected ? '取消全选' : '选择当前' }}</el-button>
        <el-button size="small" :disabled="!selectedArtworkCount" @click="$emit('clear-selection')">清空</el-button>
        <el-button size="small" type="primary" :loading="libraryBatchLoading" :disabled="!selectedArtworkCount" @click="$emit('publish-selected')">申请公开</el-button>
        <el-button size="small" :loading="libraryBatchLoading" :disabled="!selectedArtworkCount" @click="$emit('make-private-selected')">设为私有</el-button>
        <el-button size="small" :loading="libraryBatchLoading" :disabled="!selectedArtworkCount" @click="$emit('archive-selected')">归档</el-button>
      </div>
    </div>

    <div class="gallery-grid">
      <article v-for="artwork in filteredArtworks" :key="artwork.id" class="art-card" :class="{ selected: isArtworkSelected(artwork) }" @click="$emit('open-detail', artwork)">
        <button class="art-select-toggle" :class="{ active: isArtworkSelected(artwork) }" type="button" @click.stop="$emit('toggle-selection', artwork)">
          <Check />
        </button>
        <img :src="artwork.imageUrl" :alt="artwork.title" />
        <div class="art-card-body">
          <div class="art-title-row">
            <strong>{{ artwork.title }}</strong>
            <Star />
          </div>
          <p>{{ artwork.promptText }}</p>
          <div v-if="artwork.tags?.length" class="art-tag-strip">
            <span v-for="tag in artwork.tags.slice(0, 4)" :key="tag.id">{{ tag.name }}</span>
          </div>
          <div class="art-card-footer">
            <span>{{ formatDate(artwork.createdAt) }} · {{ statusLabel(artwork.status) }}</span>
            <el-button v-if="artwork.visibility !== 'PUBLIC' && artwork.status !== 'PENDING_AUDIT'" size="small" @click.stop="$emit('request-publish', artwork)">申请公开</el-button>
          </div>
        </div>
      </article>
      <div v-if="!filteredArtworks.length" class="gallery-empty">暂无作品</div>
    </div>
  </section>
</template>

<script setup>
import { Check, Refresh, Search, Star } from '@element-plus/icons-vue'

defineProps({
  libraryKeyword: { type: String, default: '' },
  libraryTagFilter: { type: Array, default: () => [] },
  libraryVisibilityFilter: { type: String, default: '' },
  libraryStatusFilter: { type: String, default: '' },
  selectableTags: { type: Array, default: () => [] },
  selectedArtworkCount: { type: Number, default: 0 },
  allFilteredArtworksSelected: { type: Boolean, default: false },
  libraryBatchLoading: { type: Boolean, default: false },
  filteredArtworks: { type: Array, default: () => [] },
  isArtworkSelected: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  statusLabel: { type: Function, required: true }
})

defineEmits([
  'update:libraryKeyword',
  'update:libraryTagFilter',
  'update:libraryVisibilityFilter',
  'update:libraryStatusFilter',
  'refresh',
  'toggle-all-filtered',
  'clear-selection',
  'publish-selected',
  'make-private-selected',
  'archive-selected',
  'open-detail',
  'toggle-selection',
  'request-publish'
])
</script>
