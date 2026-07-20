<template>
  <section class="library-view content-hub-page market-page-shell">
    <header class="page-hero hub-hero">
      <div class="hero-copy">
        <p class="eyebrow">Gallery</p>
        <h1>作品库</h1>
        <p class="page-subtitle">
          这里沉淀你自己的作品资产。筛选、批量管理、详情编辑和公开申请都围绕同一套作品库工作流展开。
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
            placeholder="搜索标题或提示词"
            clearable
            class="market-search-input"
          />
        </div>
        <div class="toolbar-filter-slot library-filter-slot">
          <el-select v-model="selectedTags" multiple clearable filterable collapse-tags collapse-tags-tooltip placeholder="标签筛选" class="toolbar-select">
            <el-option v-for="tag in selectableTags" :key="tag.id" :label="tagLabel(tag)" :value="tag.id" />
          </el-select>
          <el-select v-model="visibility" clearable placeholder="可见性" class="toolbar-select">
            <el-option label="私有" value="PRIVATE" />
            <el-option label="公开" value="PUBLIC" />
          </el-select>
          <el-select v-model="status" clearable placeholder="状态" class="toolbar-select">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="审核中" value="PENDING_AUDIT" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </div>
      </div>
    </section>

    <section class="library-batch-bar">
      <div class="batch-selection-copy">
        <strong>{{ selectedArtworkCount }}</strong>
        <span>已选择</span>
      </div>
      <div class="batch-actions">
        <el-button size="small" @click="$emit('toggle-all-filtered')">
          {{ allFilteredArtworksSelected ? '取消当前选择' : '选择当前结果' }}
        </el-button>
        <el-button size="small" :disabled="!selectedArtworkCount" @click="$emit('clear-selection')">清空</el-button>
        <el-button size="small" type="primary" :loading="libraryBatchLoading" :disabled="!selectedArtworkCount" @click="$emit('publish-selected')">
          申请公开
        </el-button>
        <el-button size="small" :loading="libraryBatchLoading" :disabled="!selectedArtworkCount" @click="$emit('make-private-selected')">
          设为私有
        </el-button>
        <el-button size="small" :loading="libraryBatchLoading" :disabled="!selectedArtworkCount" @click="$emit('archive-selected')">
          归档
        </el-button>
      </div>
    </section>

    <section class="gallery-grid">
      <article
        v-for="artwork in artworks"
        :key="artwork.id"
        class="art-card"
        :class="{ selected: isArtworkSelected(artwork) }"
        @click="$emit('open-detail', artwork)"
      >
        <button class="art-select-toggle" :class="{ active: isArtworkSelected(artwork) }" type="button" @click.stop="$emit('toggle-selection', artwork)">
          <Check />
        </button>
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
          <div class="art-title-row">
            <strong>{{ artwork.title }}</strong>
            <Star />
          </div>
          <p>{{ artwork.promptText }}</p>
          <div v-if="artwork.tags?.length" class="art-tag-strip">
            <span v-for="tag in artwork.tags.slice(0, 4)" :key="tag.id">
              <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
            </span>
          </div>
          <div class="art-card-footer">
            <span>{{ formatDate(artwork.createdAt) }} · {{ statusLabel(artwork.status) }}</span>
            <el-button
              v-if="artwork.visibility !== 'PUBLIC' && artwork.status !== 'PENDING_AUDIT'"
              size="small"
              @click.stop="$emit('request-publish', artwork)"
            >
              申请公开
            </el-button>
          </div>
        </div>
      </article>
      <div v-if="!artworks.length" class="gallery-empty">暂无作品</div>
    </section>

    <section class="soft-panel pager-panel">
      <div class="pager-copy">
        <strong>作品库分页</strong>
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
import { Check, Refresh, Search, Star } from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import BilingualTagLabel from './BilingualTagLabel.vue'

const props = defineProps({
  queryState: { type: Object, required: true },
  selectableTags: { type: Array, default: () => [] },
  selectedArtworkCount: { type: Number, default: 0 },
  allFilteredArtworksSelected: { type: Boolean, default: false },
  libraryBatchLoading: { type: Boolean, default: false },
  artworks: { type: Array, default: () => [] },
  isArtworkSelected: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  isFavoriteTarget: { type: Function, default: null }
})

const emit = defineEmits([
  'refresh',
  'toggle-all-filtered',
  'clear-selection',
  'publish-selected',
  'make-private-selected',
  'archive-selected',
  'open-detail',
  'toggle-selection',
  'request-publish',
  'toggle-favorite-target'
])

const keyword = ref(props.queryState.keyword || '')
const selectedTags = ref([...(props.queryState.tagIds || [])])
const visibility = ref(props.queryState.visibility || '')
const status = ref(props.queryState.status || '')
const currentPage = ref(props.queryState.page || 1)
const pageSize = ref(props.queryState.size || 12)
let queryTimer = null
let lastEmittedQueryKey = ''

function queryKey(query) {
  return JSON.stringify({
    keyword: query.keyword || '',
    tagIds: [...(query.tagIds || [])].map(Number).sort((a, b) => a - b),
    visibility: query.visibility || '',
    status: query.status || '',
    page: Number(query.page) || 1,
    size: Number(query.size) || 12
  })
}

function emitRefresh(query) {
  lastEmittedQueryKey = queryKey(query)
  emit('refresh', query)
}

function sameTags(left = [], right = []) {
  if (left.length !== right.length) return false
  return left.every((value, index) => Number(value) === Number(right[index]))
}

function tagLabel(tag) {
  return tag?.displayNameZh ? `${tag.displayNameZh} / ${tag.name}` : tag?.name || ''
}

function currentQuery() {
  return {
    keyword: keyword.value,
    tagIds: selectedTags.value,
    visibility: visibility.value,
    status: status.value,
    page: currentPage.value,
    size: pageSize.value
  }
}

function goPrevPage() {
  if (currentPage.value <= 1) return
  currentPage.value -= 1
  emitRefresh(currentQuery())
}

function goNextPage() {
  if (!props.queryState.hasNext) return
  currentPage.value += 1
  emitRefresh(currentQuery())
}

function updatePageSize(value) {
  pageSize.value = Number(value) || 12
  currentPage.value = 1
  emitRefresh(currentQuery())
}

watch(
  () => props.queryState,
  (value) => {
    const nextKeyword = value?.keyword || ''
    const nextTags = [...(value?.tagIds || [])]
    const nextVisibility = value?.visibility || ''
    const nextStatus = value?.status || ''
    const nextPage = value?.page || 1
    const nextSize = value?.size || 12
    if (keyword.value !== nextKeyword) keyword.value = nextKeyword
    if (!sameTags(selectedTags.value, nextTags)) selectedTags.value = nextTags
    if (visibility.value !== nextVisibility) visibility.value = nextVisibility
    if (status.value !== nextStatus) status.value = nextStatus
    if (currentPage.value !== nextPage) currentPage.value = nextPage
    if (pageSize.value !== nextSize) pageSize.value = nextSize
    lastEmittedQueryKey = queryKey(value || {})
  },
  { deep: true }
)

watch([keyword, selectedTags, visibility, status], () => {
  window.clearTimeout(queryTimer)
  queryTimer = window.setTimeout(() => {
    currentPage.value = 1
    const query = currentQuery()
    if (queryKey(query) === lastEmittedQueryKey) return
    emitRefresh(query)
  }, 260)
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
