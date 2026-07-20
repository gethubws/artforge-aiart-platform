<template>
  <el-dialog :model-value="visible" width="min(1080px, 94vw)" class="prompt-builder-dialog" align-center @close="$emit('close')">
    <template #header>
      <div class="prompt-dialog-head">
        <div>
          <p class="eyebrow">Prompt Builder</p>
          <h2>提示词工作台</h2>
        </div>
      </div>
    </template>

    <div class="prompt-dialog-layout">
      <section class="prompt-dialog-editor">
        <el-input
          :model-value="freeText"
          type="textarea"
          :autosize="{ minRows: 5, maxRows: 8 }"
          resize="none"
          placeholder="输入你自己的描述、角色设定、场景叙述..."
          @update:model-value="$emit('update:freeText', $event)"
        />

        <el-input
          :model-value="negativeText"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 5 }"
          resize="none"
          placeholder="输入负面词，例如 blurry, low quality, text..."
          @update:model-value="$emit('update:negativeText', $event)"
        />

        <div class="prompt-result-box">
          <strong>当前正向提示词</strong>
          <p>{{ composedPrompt || '还没有拼接结果' }}</p>
        </div>

        <div class="prompt-result-box secondary">
          <strong>当前反向提示词</strong>
          <p>{{ composedNegative || '还没有负面提示词' }}</p>
        </div>
      </section>

      <section class="prompt-dialog-library">
        <div class="prompt-dialog-selected">
          <strong>已选标签</strong>
          <div class="selected-tag-strip expanded">
            <button
              v-for="tag in selectedTagObjects"
              :key="tag.id"
              class="selected-tag-pill"
              type="button"
              @click="$emit('toggle-tag', tag.id)"
            >
              <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
            </button>
            <span v-if="!selectedTagObjects.length" class="selected-tag-empty">还没有选择标签</span>
          </div>
        </div>

        <div class="prompt-dialog-categories">
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
                <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
              </button>
            </div>
          </div>
        </div>
      </section>

      <aside class="prompt-dialog-preview">
        <div class="tag-preview-card large" v-if="focusedTag">
          <div class="tag-preview-media large">
            <img v-if="focusedTag.previewImageUrl" :src="focusedTag.previewImageUrl" :alt="focusedTag.name" />
            <div v-else class="tag-preview-empty">暂无静态预览图</div>
          </div>
          <div class="tag-preview-meta">
            <strong><BilingualTagLabel :name="focusedTag.name" :display-name-zh="focusedTag.displayNameZh" /></strong>
            <p>{{ focusedTag.descriptionZh || focusedTag.promptText }}</p>
            <span v-if="focusedTag.negativePromptText">负面词: {{ focusedTag.negativePromptText }}</span>
          </div>
        </div>
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
import { computed } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'
import BilingualTagLabel from './BilingualTagLabel.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  freeText: { type: String, default: '' },
  negativeText: { type: String, default: '' },
  composedPrompt: { type: String, default: '' },
  composedNegative: { type: String, default: '' },
  tagTree: { type: Array, default: () => [] },
  selectedTags: { type: Array, default: () => [] }
})

defineEmits(['close', 'compose', 'toggle-tag', 'update:freeText', 'update:negativeText'])

const selectedTagObjects = computed(() => {
  return props.tagTree.flatMap((category) => category.tags || []).filter((tag) => props.selectedTags.includes(tag.id))
})

const focusedTag = computed(() => selectedTagObjects.value[0] || null)
</script>
