<template>
  <el-dialog
    :model-value="visible"
    width="min(1240px, 96vw)"
    class="prompt-builder-dialog"
    align-center
    @close="$emit('close')"
  >
    <template #header>
      <div class="prompt-dialog-head">
        <div>
          <p class="eyebrow">Prompt Builder</p>
          <h2>提示词工作台</h2>
        </div>
        <span>{{ selectedTagObjects.length }} 个标签已选</span>
      </div>
    </template>

    <div class="prompt-dialog-layout">
      <section class="prompt-dialog-editor">
        <div class="prompt-field-block">
          <div class="prompt-field-label">
            <strong>画面描述</strong>
            <span>自由输入</span>
          </div>
          <el-input
            :model-value="freeText"
            type="textarea"
            :autosize="{ minRows: 5, maxRows: 8 }"
            resize="none"
            placeholder="输入角色设定、场景叙述和画面细节..."
            @update:model-value="$emit('update:freeText', $event)"
          />
        </div>

        <div class="prompt-field-block">
          <div class="prompt-field-label">
            <strong>反向描述</strong>
            <span>排除内容</span>
          </div>
          <el-input
            :model-value="negativeText"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 5 }"
            resize="none"
            placeholder="例如：blurry, low quality, text..."
            @update:model-value="$emit('update:negativeText', $event)"
          />
        </div>

        <div class="prompt-result-box">
          <strong>当前正向提示词</strong>
          <p>{{ composedPrompt || '还没有拼接结果。' }}</p>
        </div>

        <div class="prompt-result-box secondary">
          <strong>当前反向提示词</strong>
          <p>{{ composedNegative || '还没有反向提示词。' }}</p>
        </div>
      </section>

      <section class="prompt-dialog-library">
        <div class="prompt-dialog-selected">
          <div class="prompt-library-head">
            <strong>已选标签</strong>
            <span>{{ selectedTagObjects.length }} 个</span>
          </div>
          <div class="selected-tag-strip expanded">
            <button
              v-for="tag in selectedTagObjects"
              :key="tag.id"
              class="selected-tag-pill"
              type="button"
              @click="handleTagClick(tag)"
            >
              <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
            </button>
            <span v-if="!selectedTagObjects.length" class="selected-tag-empty">还没有选择标签</span>
          </div>
        </div>

        <div class="prompt-category-tabs expanded" role="tablist" aria-label="提示词分类">
          <button
            v-for="category in tagTree"
            :key="category.id"
            class="prompt-category-tab"
            :class="{ active: category.id === activeCategoryId }"
            type="button"
            role="tab"
            :aria-selected="category.id === activeCategoryId"
            @click="selectCategory(category)"
          >
            <span>{{ categoryLabel(category) }}</span>
            <small>{{ category.tagCount ?? category.tags?.length ?? 0 }}</small>
          </button>
        </div>

        <div class="prompt-library-search">
          <el-input
            v-model="searchKeyword"
            :prefix-icon="Search"
            clearable
            placeholder="搜索当前分类的中英文名称或提示词"
            @keyup.enter="searchActiveCategory"
            @clear="searchActiveCategory"
          />
          <el-button :icon="Search" :loading="activeCategory?.loading" @click="searchActiveCategory">搜索</el-button>
        </div>

        <div class="prompt-active-category-head">
          <div>
            <strong>{{ categoryLabel(activeCategory) }}</strong>
            <span>{{ activeCategory?.name }}</span>
          </div>
          <span>{{ activeCategory?.total ?? activeCategory?.tagCount ?? activeTags.length }} 个标签</span>
        </div>

        <div class="prompt-dialog-tag-grid">
          <button
            v-for="tag in activeTags"
            :key="tag.id"
            class="prompt-tag-card"
            :class="{
              active: selectedTags.includes(tag.id),
              focused: focusedTag?.id === tag.id
            }"
            type="button"
            :aria-pressed="selectedTags.includes(tag.id)"
            @click="handleTagClick(tag)"
          >
            <span class="prompt-tag-card-media">
              <img v-if="tag.previewImageUrl" :src="tag.previewImageUrl" :alt="tag.displayNameZh || tag.name" loading="lazy" />
            </span>
            <span class="prompt-tag-card-copy">
              <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
              <small>{{ tag.descriptionZh || tag.promptText }}</small>
            </span>
            <el-icon v-if="selectedTags.includes(tag.id)" class="prompt-tag-check"><Check /></el-icon>
          </button>
          <div v-if="activeCategory?.loading && !activeTags.length" class="prompt-empty-state">正在加载标签...</div>
          <div v-else-if="!activeTags.length" class="prompt-empty-state">当前分类暂无可用标签</div>
        </div>
        <el-button
          v-if="activeCategory?.hasNext"
          class="prompt-load-more"
          text
          :loading="activeCategory.loading"
          @click="$emit('load-more-tags', activeCategory.id)"
        >
          加载更多标签
        </el-button>
      </section>

      <aside class="prompt-dialog-preview">
        <TagPreviewGallery
          v-if="focusedTag"
          :tag="focusedTag"
          :category-name="categoryLabel(focusedCategory)"
          large
        />
      </aside>
    </div>

    <template #footer>
      <div class="prompt-dialog-footer">
        <el-button @click="$emit('close')">收起</el-button>
        <el-button type="primary" :icon="MagicStick" @click="$emit('compose')">更新拼接结果</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { Check, MagicStick, Search } from '@element-plus/icons-vue'
import BilingualTagLabel from './BilingualTagLabel.vue'
import TagPreviewGallery from './TagPreviewGallery.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  freeText: { type: String, default: '' },
  negativeText: { type: String, default: '' },
  composedPrompt: { type: String, default: '' },
  composedNegative: { type: String, default: '' },
  tagTree: { type: Array, default: () => [] },
  tagOptions: { type: Array, default: () => [] },
  selectedCategoryId: { type: [String, Number], default: null },
  selectedTags: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'close',
  'compose',
  'toggle-tag',
  'update:freeText',
  'update:negativeText',
  'load-category',
  'load-more-tags',
  'search-tags',
  'category-change'
])

const categoryNames = {
  style: '风格',
  subject: '主体',
  lighting: '光线',
  composition: '构图',
  quality: '质量',
  color: '色彩',
  mood: '氛围'
}

const activeCategoryId = ref(null)
const focusedTagId = ref(null)
const searchKeyword = ref('')
const flatTags = computed(() => props.tagTree.flatMap((category) => category.tags || []))
const selectedTagObjects = computed(() => props.selectedTags.map((tagId) => {
  return flatTags.value.find((tag) => tag.id === tagId) || props.tagOptions.find((tag) => tag.id === tagId)
}).filter(Boolean))
const activeCategory = computed(() => props.tagTree.find((category) => category.id === activeCategoryId.value) || props.tagTree[0] || null)
const activeTags = computed(() => activeCategory.value?.tags || [])
const focusedTag = computed(() => {
  return flatTags.value.find((tag) => tag.id === focusedTagId.value)
    || selectedTagObjects.value[0]
    || activeTags.value[0]
    || null
})
const focusedCategory = computed(() => {
  const option = props.tagOptions.find((tag) => tag.id === focusedTag.value?.id)
  return props.tagTree.find((category) => category.id === option?.categoryId)
    || props.tagTree.find((category) => (category.tags || []).some((tag) => tag.id === focusedTag.value?.id))
    || activeCategory.value
})

function categoryLabel(category) {
  if (!category) return ''
  return categoryNames[category.slug] || category.name
}

function selectCategory(category) {
  activeCategoryId.value = category.id
  emit('category-change', category.id)
  searchKeyword.value = category.keyword || ''
  if (!(category.tags || []).some((tag) => tag.id === focusedTagId.value)) {
    focusedTagId.value = category.tags?.[0]?.id || null
  }
  emit('load-category', category.id)
}

function searchActiveCategory() {
  if (!activeCategory.value) return
  emit('search-tags', { categoryId: activeCategory.value.id, keyword: searchKeyword.value.trim() })
}

function handleTagClick(tag) {
  focusedTagId.value = tag.id
  const category = props.tagTree.find((item) => item.id === tag.categoryId)
    || props.tagTree.find((item) => (item.tags || []).some((candidate) => candidate.id === tag.id))
  if (category) {
    activeCategoryId.value = category.id
    emit('category-change', category.id)
  }
  emit('toggle-tag', tag.id)
}

watch(
  () => props.selectedCategoryId,
  (categoryId) => {
    if (props.tagTree.some((category) => category.id === categoryId)) {
      activeCategoryId.value = categoryId
      const category = props.tagTree.find((item) => item.id === categoryId)
      searchKeyword.value = category?.keyword || ''
    }
  },
  { immediate: true }
)

watch(
  () => props.tagTree,
  () => {
    if (!props.tagTree.some((category) => category.id === activeCategoryId.value)) {
      const selectedCategory = props.tagTree.find((category) => (category.tags || []).some((tag) => props.selectedTags.includes(tag.id)))
      activeCategoryId.value = selectedCategory?.id || props.tagTree[0]?.id || null
    }
    if (!flatTags.value.some((tag) => tag.id === focusedTagId.value)) {
      focusedTagId.value = selectedTagObjects.value[0]?.id || activeTags.value[0]?.id || null
    }
  },
  { immediate: true, deep: true }
)

watch(
  () => props.visible,
  (visible) => {
    if (!visible) return
    if (props.tagTree.some((category) => category.id === props.selectedCategoryId)) {
      activeCategoryId.value = props.selectedCategoryId
    }
    const nextTag = selectedTagObjects.value[0] || activeTags.value[0] || flatTags.value[0] || null
    focusedTagId.value = nextTag?.id || null
    const category = props.tagTree.find((item) => (item.tags || []).some((tag) => tag.id === nextTag?.id))
    if (category) activeCategoryId.value = category.id
    searchKeyword.value = category?.keyword || ''
  }
)
</script>
