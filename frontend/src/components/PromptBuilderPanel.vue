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
        <el-tooltip content="展开编辑器" placement="top">
          <el-button :icon="FullScreen" circle @click="$emit('open-expanded')" />
        </el-tooltip>
      </div>
    </div>

    <el-input
      :model-value="freeText"
      type="textarea"
      :autosize="{ minRows: 4, maxRows: 6 }"
      resize="none"
      placeholder="画面主体、风格、镜头、光线、材质..."
      @update:model-value="$emit('update:freeText', $event)"
      @blur="$emit('compose')"
    />

    <div class="selected-tag-strip" v-if="selectedTagObjects.length">
      <button
        v-for="tag in selectedTagObjects.slice(0, 6)"
        :key="tag.id"
        class="selected-tag-pill"
        type="button"
        @click="$emit('toggle-tag', tag.id)"
      >
        {{ tag.name }}
      </button>
      <span v-if="selectedTagObjects.length > 6" class="selected-tag-more">+{{ selectedTagObjects.length - 6 }}</span>
    </div>

    <div class="tag-scroll compact-scroll">
      <div v-for="category in tagTree" :key="category.id" class="tag-section">
        <div class="tag-category">{{ category.name }}</div>
        <div class="tag-list">
          <button
            v-for="tag in category.tags"
            :key="tag.id"
            class="tag-chip"
            :class="{ active: selectedTags.includes(tag.id) }"
            type="button"
            @click="$emit('toggle-tag', tag.id)"
          >
            {{ tag.name }}
          </button>
        </div>
      </div>
    </div>

    <div class="tag-preview-card" v-if="focusedTag">
      <div class="tag-preview-media">
        <img v-if="focusedTag.previewImageUrl" :src="focusedTag.previewImageUrl" :alt="focusedTag.name" />
        <div v-else class="tag-preview-empty">预览待补充</div>
      </div>
      <div class="tag-preview-meta">
        <strong>{{ focusedTag.name }}</strong>
        <p>{{ focusedTag.promptText }}</p>
      </div>
    </div>

    <el-input
      :model-value="negativeText"
      type="textarea"
      :autosize="{ minRows: 2, maxRows: 4 }"
      resize="none"
      placeholder="低质量、文字、水印、畸形..."
      @update:model-value="$emit('update:negativeText', $event)"
      @blur="$emit('compose')"
    />

    <el-button :icon="MagicStick" class="wide-button secondary-action" @click="$emit('compose')">
      拼接提示词
    </el-button>

    <div v-if="generationMode === 'img2img'" class="reference-uploader">
      <div class="reference-head">
        <span>参考图</span>
        <button v-if="initImagePreview" class="text-button" type="button" @click="$emit('clear-init-image')">清除</button>
      </div>
      <label class="upload-dropzone" :class="{ filled: initImagePreview }">
        <input type="file" accept="image/*" @change="$emit('init-image-change', $event)" />
        <img v-if="initImagePreview" :src="initImagePreview" alt="Reference image" />
        <span v-else>选择图片</span>
      </label>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { FullScreen, MagicStick, Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  freeText: { type: String, default: '' },
  negativeText: { type: String, default: '' },
  tagTree: { type: Array, default: () => [] },
  selectedTags: { type: Array, default: () => [] },
  generationMode: { type: String, default: 'txt2img' },
  initImagePreview: { type: String, default: '' }
})

defineEmits([
  'update:freeText',
  'update:negativeText',
  'reload-tags',
  'open-expanded',
  'compose',
  'toggle-tag',
  'clear-init-image',
  'init-image-change'
])

const selectedTagObjects = computed(() => {
  return props.tagTree.flatMap((category) => category.tags || []).filter((tag) => props.selectedTags.includes(tag.id))
})

const focusedTag = computed(() => selectedTagObjects.value[0] || null)
</script>
