<template>
  <article v-if="tag" class="tag-preview-card" :class="{ large }">
    <div class="tag-preview-gallery">
      <div class="tag-preview-media" :class="{ large }">
        <img
          v-if="activePreview?.imageUrl"
          :src="activePreview.imageUrl"
          :alt="activePreview.titleZh || resolvedTag.displayNameZh || resolvedTag.name"
          loading="lazy"
        />
        <div v-else class="tag-preview-empty">{{ loading ? '正在加载预览...' : '预览图待补充' }}</div>
        <template v-if="previews.length > 1">
          <button
            class="tag-preview-nav previous"
            type="button"
            aria-label="上一张预览"
            title="上一张预览"
            @click="shiftPreview(-1)"
          >
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <button
            class="tag-preview-nav next"
            type="button"
            aria-label="下一张预览"
            title="下一张预览"
            @click="shiftPreview(1)"
          >
            <el-icon><ArrowRight /></el-icon>
          </button>
        </template>
      </div>
      <div v-if="previews.length > 1" class="tag-preview-thumbnails" aria-label="标签预览图库">
        <button
          v-for="(preview, index) in previews"
          :key="preview.id || `${preview.imageUrl}-${index}`"
          class="tag-preview-thumbnail"
          :class="{ active: index === activeIndex }"
          type="button"
          :title="preview.titleZh || sceneLabel(preview.sceneKey)"
          @click="activeIndex = index"
        >
          <img :src="preview.imageUrl" :alt="preview.titleZh || sceneLabel(preview.sceneKey)" loading="lazy" />
        </button>
      </div>
    </div>

    <div class="tag-preview-meta">
      <div class="tag-preview-title">
        <strong>
          <BilingualTagLabel :name="resolvedTag.name" :display-name-zh="resolvedTag.displayNameZh" />
        </strong>
        <span>{{ categoryName || detail?.categoryName || '标签' }}</span>
      </div>
      <p>{{ resolvedTag.descriptionZh || '暂未补充中文说明。' }}</p>
      <p v-if="activePreview?.titleZh" class="tag-preview-caption">
        {{ activePreview.titleZh }}<span v-if="activePreview.sceneKey"> · {{ sceneLabel(activePreview.sceneKey) }}</span>
      </p>
      <dl class="tag-prompt-impact">
        <div v-if="resolvedTag.promptText">
          <dt>正向词</dt>
          <dd>{{ resolvedTag.promptText }}</dd>
        </div>
        <div v-if="resolvedTag.negativePromptText">
          <dt>反向词</dt>
          <dd>{{ resolvedTag.negativePromptText }}</dd>
        </div>
        <div v-if="large && resolvedTag.weight">
          <dt>权重</dt>
          <dd>{{ resolvedTag.weight }}</dd>
        </div>
      </dl>
    </div>
  </article>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getTagDetail } from '../api/tags'
import BilingualTagLabel from './BilingualTagLabel.vue'

const detailCache = new Map()

const props = defineProps({
  tag: { type: Object, default: null },
  categoryName: { type: String, default: '' },
  large: { type: Boolean, default: false }
})

const detail = ref(null)
const loading = ref(false)
const activeIndex = ref(0)
const resolvedTag = computed(() => detail.value?.tag || props.tag || {})
const previews = computed(() => {
  if (detail.value?.previews?.length) return detail.value.previews
  if (resolvedTag.value.previewImageUrl) {
    return [{ imageUrl: resolvedTag.value.previewImageUrl, previewType: 'COVER', sceneKey: 'GENERAL', titleZh: '封面预览', cover: true }]
  }
  return []
})
const activePreview = computed(() => previews.value[activeIndex.value] || previews.value[0] || null)

const sceneNames = {
  GENERAL: '综合示例',
  CHARACTER: '人物示例',
  LANDSCAPE: '风景示例',
  OBJECT: '物品示例',
  NIGHT: '夜景示例',
  CLOSE_UP: '近景示例'
}

function sceneLabel(sceneKey) {
  return sceneNames[sceneKey] || '效果示例'
}

function shiftPreview(offset) {
  if (previews.value.length < 2) return
  activeIndex.value = (activeIndex.value + offset + previews.value.length) % previews.value.length
}

async function loadDetail(tagId) {
  if (!tagId) {
    detail.value = null
    return
  }
  activeIndex.value = 0
  if (detailCache.has(tagId)) {
    detail.value = detailCache.get(tagId)
    return
  }
  loading.value = true
  try {
    const result = await getTagDetail(tagId)
    detailCache.set(tagId, result)
    detail.value = result
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

watch(() => props.tag?.id, loadDetail, { immediate: true })
</script>
