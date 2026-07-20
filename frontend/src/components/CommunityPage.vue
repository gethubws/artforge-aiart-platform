<template>
  <section class="community-view content-hub-page market-page-shell">
    <header class="page-hero hub-hero">
      <div class="hero-copy">
        <p class="eyebrow">Community</p>
        <h1>作品广场</h1>
        <p class="page-subtitle">
          这里展示公开作品。你可以按关键词和标签浏览近期公开内容，把广场当成平台对外展示面的内容流。
        </p>
      </div>
      <div class="hero-actions">
        <el-button :icon="Refresh" @click="$emit('refresh', currentQuery())">刷新</el-button>
      </div>
    </header>

    <section class="soft-panel market-toolbar-panel">
      <div class="market-toolbar-grid">
        <div class="toolbar-search-slot">
          <el-input
            v-model="keyword"
            :prefix-icon="Search"
            placeholder="搜索公开作品标题或提示词"
            clearable
            class="market-search-input"
          />
        </div>
        <div class="toolbar-filter-slot community-filter-slot">
          <el-select v-model="selectedTags" multiple clearable filterable collapse-tags collapse-tags-tooltip placeholder="标签筛选" class="toolbar-select">
            <el-option v-for="tag in selectableTags" :key="tag.id" :label="tagLabel(tag)" :value="tag.id" />
          </el-select>
        </div>
      </div>
    </section>

    <section class="gallery-grid">
      <article v-for="artwork in publicArtworks" :key="artwork.id" class="art-card" @click="$emit('open-detail', artwork)">
        <button
          type="button"
          class="art-favorite-toggle"
          :class="{ active: props.isFavoriteTarget?.('ARTWORK', artwork) }"
          @click.stop="$emit('toggle-favorite-target', { type: 'ARTWORK', target: artwork })"
        >
          <Star />
        </button>
        <img :src="artwork.imageUrl" :alt="artwork.title" />
        <div class="art-card-body">
          <div class="art-title-row"><strong>{{ artwork.title }}</strong><Star /></div>
          <p>{{ artwork.promptText }}</p>
          <div v-if="artwork.tags?.length" class="art-tag-strip">
            <span v-for="tag in artwork.tags.slice(0, 4)" :key="tag.id">
              <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
            </span>
          </div>
          <span>{{ formatDate(artwork.createdAt) }}</span>
        </div>
      </article>
      <div v-if="!publicArtworks.length" class="gallery-empty">暂无公开作品</div>
    </section>

    <section class="soft-panel pager-panel">
      <div class="pager-copy">
        <strong>广场分页</strong>
        <span>当前第 {{ currentPage }} 页，每页 {{ pageSize }} 项。</span>
      </div>
      <div class="pager-actions">
        <el-select :model-value="pageSize" class="pager-size-select" @update:model-value="updatePageSize">
          <el-option label="每页 8 项" :value="8" />
          <el-option label="每页 12 项" :value="12" />
          <el-option label="每页 16 项" :value="16" />
        </el-select>
        <el-button :disabled="currentPage <= 1" @click="goPrevPage">上一页</el-button>
        <span class="pager-index">第 {{ currentPage }} 页</span>
        <el-button :disabled="!queryState.hasNext" @click="goNextPage">下一页</el-button>
      </div>
    </section>
  </section>
</template>

<script setup>
import { Refresh, Search, Star } from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import BilingualTagLabel from './BilingualTagLabel.vue'

const props = defineProps({
  queryState: { type: Object, required: true },
  selectableTags: { type: Array, default: () => [] },
  publicArtworks: { type: Array, default: () => [] },
  formatDate: { type: Function, required: true },
  isFavoriteTarget: { type: Function, default: null }
})

const emit = defineEmits(['refresh', 'open-detail', 'toggle-favorite-target'])

const keyword = ref(props.queryState.keyword || '')
const selectedTags = ref([...(props.queryState.tagIds || [])])
const currentPage = ref(props.queryState.page || 1)
const pageSize = ref(props.queryState.size || 12)

function tagLabel(tag) {
  return tag?.displayNameZh ? `${tag.displayNameZh} / ${tag.name}` : tag?.name || ''
}

function currentQuery() {
  return {
    keyword: keyword.value,
    tagIds: selectedTags.value,
    page: currentPage.value,
    size: pageSize.value
  }
}

function goPrevPage() {
  if (currentPage.value <= 1) return
  currentPage.value -= 1
  emit('refresh', currentQuery())
}

function goNextPage() {
  if (!props.queryState.hasNext) return
  currentPage.value += 1
  emit('refresh', currentQuery())
}

function updatePageSize(value) {
  pageSize.value = Number(value) || 12
  currentPage.value = 1
  emit('refresh', currentQuery())
}

watch(
  () => props.queryState,
  (value) => {
    keyword.value = value?.keyword || ''
    selectedTags.value = [...(value?.tagIds || [])]
    currentPage.value = value?.page || 1
    pageSize.value = value?.size || 12
  },
  { deep: true }
)

watch([keyword, selectedTags], () => {
  currentPage.value = 1
  emit('refresh', currentQuery())
}, { deep: true })
</script>

<style scoped>
.art-favorite-toggle {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 36px;
  height: 36px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.82);
  color: #64748b;
  cursor: pointer;
  z-index: 2;
  transition: color 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.art-favorite-toggle.active {
  color: #b45309;
  border-color: rgba(245, 158, 11, 0.3);
  background: rgba(255, 251, 235, 0.96);
}
</style>
