<template>
  <section class="soft-panel prompt-panel prompt-builder-panel">
    <div class="panel-title-row compact">
      <div>
        <p class="eyebrow">Prompt</p>
        <h2>提示词</h2>
      </div>
      <div class="prompt-panel-actions">
        <el-tooltip content="刷新标签" placement="top">
          <el-button :icon="Refresh" circle @click="$emit('reload-tags')" />
        </el-tooltip>
        <el-tooltip content="展开提示词工作台" placement="top">
          <el-button :icon="FullScreen" circle @click="$emit('open-expanded')" />
        </el-tooltip>
      </div>
    </div>

    <div class="prompt-field-block">
      <div class="prompt-field-label">
        <strong>画面描述</strong>
        <span>自由输入</span>
      </div>
      <el-input
        :model-value="freeText"
        type="textarea"
        :autosize="{ minRows: 4, maxRows: 6 }"
        resize="none"
        placeholder="描述画面主体、场景、氛围与细节..."
        @update:model-value="$emit('update:freeText', $event)"
        @blur="$emit('compose')"
      />
    </div>

    <div v-if="selectedTagObjects.length" class="prompt-selected-block">
      <div class="prompt-field-label">
        <strong>已选标签</strong>
        <span>{{ selectedTagObjects.length }} 个</span>
      </div>
      <div class="selected-tag-strip">
        <button
          v-for="tag in selectedTagObjects.slice(0, 6)"
          :key="tag.id"
          class="selected-tag-pill"
          type="button"
          @click="handleTagClick(tag)"
        >
          <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
        </button>
        <span v-if="selectedTagObjects.length > 6" class="selected-tag-more">+{{ selectedTagObjects.length - 6 }}</span>
      </div>
    </div>

    <div class="prompt-tag-library">
      <div class="prompt-library-head">
        <strong>标签库</strong>
        <span>{{ totalTagCount }} 个标签</span>
      </div>

      <div class="prompt-category-tabs" role="tablist" aria-label="提示词分类">
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

      <div class="compact-tag-grid">
        <button
          v-for="tag in activeTags"
          :key="tag.id"
          class="prompt-tag-option"
          :class="{
            active: selectedTags.includes(tag.id),
            focused: focusedTag?.id === tag.id
          }"
          type="button"
          :aria-pressed="selectedTags.includes(tag.id)"
          @click="handleTagClick(tag)"
        >
          <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
          <el-icon v-if="selectedTags.includes(tag.id)" class="prompt-tag-check"><Check /></el-icon>
        </button>
        <div v-if="activeCategory?.loading" class="prompt-empty-state">正在加载标签...</div>
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
    </div>

    <TagPreviewGallery v-if="focusedTag" :tag="focusedTag" :category-name="categoryLabel(focusedCategory)" />

    <div class="prompt-field-block">
      <div class="prompt-field-label">
        <strong>反向描述</strong>
        <span>排除内容</span>
      </div>
      <el-input
        :model-value="negativeText"
        type="textarea"
        :autosize="{ minRows: 2, maxRows: 4 }"
        resize="none"
        placeholder="例如：低质量、模糊、文字、水印..."
        @update:model-value="$emit('update:negativeText', $event)"
        @blur="$emit('compose')"
      />
    </div>

    <el-button :icon="MagicStick" class="wide-button secondary-action" @click="$emit('compose')">
      更新提示词
    </el-button>

    <div v-if="generationMode === 'img2img'" class="reference-uploader">
      <div class="reference-head">
        <span>参考图</span>
        <button v-if="initImagePreview" class="text-button" type="button" @click="$emit('clear-init-image')">清除</button>
      </div>
      <label class="upload-dropzone" :class="{ filled: initImagePreview }">
        <input type="file" accept="image/*" @change="$emit('init-image-change', $event)" />
        <img v-if="initImagePreview" :src="initImagePreview" alt="参考图" />
        <span v-else>选择图片</span>
      </label>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { Check, FullScreen, MagicStick, Refresh } from '@element-plus/icons-vue'
import BilingualTagLabel from './BilingualTagLabel.vue'
import TagPreviewGallery from './TagPreviewGallery.vue'

const props = defineProps({
  freeText: { type: String, default: '' },
  negativeText: { type: String, default: '' },
  tagTree: { type: Array, default: () => [] },
  tagOptions: { type: Array, default: () => [] },
  selectedCategoryId: { type: [String, Number], default: null },
  selectedTags: { type: Array, default: () => [] },
  generationMode: { type: String, default: 'txt2img' },
  initImagePreview: { type: String, default: '' }
})

const emit = defineEmits([
  'update:freeText',
  'update:negativeText',
  'reload-tags',
  'open-expanded',
  'compose',
  'toggle-tag',
  'load-category',
  'load-more-tags',
  'category-change',
  'clear-init-image',
  'init-image-change'
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
const flatTags = computed(() => props.tagTree.flatMap((category) => category.tags || []))
const totalTagCount = computed(() => props.tagTree.reduce((total, category) => total + Number(category.tagCount ?? category.tags?.length ?? 0), 0))
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
  const categoryId = focusedTag.value?.categoryId
  return props.tagTree.find((category) => category.id === categoryId)
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
  if (!(category.tags || []).some((tag) => tag.id === focusedTagId.value)) {
    focusedTagId.value = category.tags?.[0]?.id || null
  }
  emit('load-category', category.id)
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
</script>
