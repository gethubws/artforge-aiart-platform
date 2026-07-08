<template>
  <section class="workbench-view">
    <aside class="control-panel">
      <section class="soft-panel workbench-block">
        <div class="panel-title-row compact">
          <div>
            <p class="eyebrow">Generation Mode</p>
            <h2>创作方式</h2>
          </div>
        </div>
        <div class="mode-switch clean-mode-switch">
          <button
            class="mode-button"
            :class="{ active: generationMode === 'txt2img' }"
            type="button"
            @click="$emit('update:generationMode', 'txt2img')"
          >
            文生图
          </button>
          <button
            class="mode-button"
            :class="{ active: generationMode === 'img2img' }"
            type="button"
            @click="$emit('update:generationMode', 'img2img')"
          >
            图生图
          </button>
        </div>
      </section>

      <section class="soft-panel workbench-block">
        <div class="panel-title-row compact">
          <div>
            <p class="eyebrow">Prompt Builder</p>
            <h2>提示词编排</h2>
          </div>
          <el-button text @click="$emit('open-prompt-builder')">展开</el-button>
        </div>

        <PromptBuilderPanel
          :free-text="freeText"
          :negative-text="negativeText"
          :tag-tree="tagTree"
          :selected-tags="selectedTags"
          :generation-mode="generationMode"
          :init-image-preview="initImagePreview"
          @update:free-text="$emit('update:freeText', $event)"
          @update:negative-text="$emit('update:negativeText', $event)"
          @reload-tags="$emit('reload-tags')"
          @open-expanded="$emit('open-prompt-builder')"
          @compose="$emit('compose')"
          @toggle-tag="$emit('toggle-tag', $event)"
          @clear-init-image="$emit('clear-init-image')"
          @init-image-change="$emit('init-image-change', $event)"
        />
      </section>

      <section class="soft-panel settings-panel workbench-block">
        <div class="panel-title-row compact">
          <div>
            <p class="eyebrow">Forge</p>
            <h2>生成参数</h2>
          </div>
          <span class="status-dot" :class="providerStateClass"></span>
        </div>

        <el-form label-position="top">
          <el-form-item label="作品标题">
            <el-input :model-value="generationForm.title" @update:model-value="$emit('patch-generation-form', { title: $event })" />
          </el-form-item>

          <el-form-item label="模型">
            <el-select
              :model-value="generationForm.model"
              filterable
              clearable
              placeholder="选择模型"
              @update:model-value="$emit('patch-generation-form', { model: $event })"
            >
              <el-option
                v-for="model in models"
                :key="model.title || model.model_name"
                :label="model.title || model.model_name"
                :value="model.title || model.model_name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="VAE">
            <el-select
              :model-value="generationForm.vae"
              filterable
              clearable
              placeholder="自动"
              @update:model-value="$emit('patch-generation-form', { vae: $event })"
            >
              <el-option
                v-for="vae in vaes"
                :key="resourceName(vae)"
                :label="resourceName(vae)"
                :value="resourceName(vae)"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="LoRA">
            <el-select
              :model-value="selectedLoras"
              multiple
              filterable
              clearable
              collapse-tags
              collapse-tags-tooltip
              placeholder="选择 LoRA"
              @update:model-value="$emit('update:selectedLoras', $event)"
            >
              <el-option
                v-for="lora in loras"
                :key="resourceName(lora)"
                :label="loraLabel(lora)"
                :value="resourceName(lora)"
              />
            </el-select>
          </el-form-item>

          <div v-if="selectedLoras.length" class="slider-field lora-weight">
            <div class="field-line"><span>LoRA 权重</span><strong>{{ loraWeight }}</strong></div>
            <el-slider :model-value="loraWeight" :min="0" :max="2" :step="0.05" @update:model-value="$emit('update:loraWeight', $event)" />
          </div>

          <el-form-item label="采样器">
            <el-select
              :model-value="generationForm.samplerName"
              filterable
              placeholder="选择采样器"
              @update:model-value="$emit('patch-generation-form', { samplerName: $event })"
            >
              <el-option v-for="sampler in samplers" :key="sampler.name" :label="sampler.name" :value="sampler.name" />
            </el-select>
          </el-form-item>

          <div class="size-presets">
            <button
              v-for="size in quickSizes"
              :key="size.label"
              class="size-chip"
              type="button"
              :class="{ active: generationForm.width === size.width && generationForm.height === size.height }"
              @click="$emit('apply-size', size)"
            >
              {{ size.label }}
            </button>
          </div>

          <div class="number-grid">
            <el-form-item label="宽度">
              <el-input-number :model-value="generationForm.width" :min="256" :max="2048" :step="64" controls-position="right" @update:model-value="$emit('patch-generation-form', { width: $event })" />
            </el-form-item>
            <el-form-item label="高度">
              <el-input-number :model-value="generationForm.height" :min="256" :max="2048" :step="64" controls-position="right" @update:model-value="$emit('patch-generation-form', { height: $event })" />
            </el-form-item>
          </div>

          <div class="slider-field">
            <div class="field-line"><span>采样步数</span><strong>{{ generationForm.steps }}</strong></div>
            <el-slider :model-value="generationForm.steps" :min="1" :max="80" @update:model-value="$emit('patch-generation-form', { steps: $event })" />
          </div>

          <div class="slider-field">
            <div class="field-line"><span>CFG Scale</span><strong>{{ generationForm.cfgScale }}</strong></div>
            <el-slider :model-value="generationForm.cfgScale" :min="1" :max="20" :step="0.5" @update:model-value="$emit('patch-generation-form', { cfgScale: $event })" />
          </div>

          <div class="number-grid advanced-grid">
            <el-form-item label="Seed">
              <el-input-number :model-value="generationForm.seed" :min="-1" controls-position="right" @update:model-value="$emit('patch-generation-form', { seed: $event })" />
            </el-form-item>
            <el-form-item label="Clip Skip">
              <el-input-number :model-value="generationForm.clipSkip" :min="1" :max="12" controls-position="right" @update:model-value="$emit('patch-generation-form', { clipSkip: $event })" />
            </el-form-item>
            <el-form-item label="单批数量">
              <el-input-number :model-value="generationForm.batchSize" :min="1" :max="4" controls-position="right" @update:model-value="$emit('patch-generation-form', { batchSize: $event })" />
            </el-form-item>
            <el-form-item label="批次数量">
              <el-input-number :model-value="generationForm.batchCount" :min="1" :max="4" controls-position="right" @update:model-value="$emit('patch-generation-form', { batchCount: $event })" />
            </el-form-item>
          </div>

          <div class="switch-grid">
            <div class="switch-line compact-switch">
              <span>面部修复</span>
              <el-switch :model-value="generationForm.restoreFaces" @update:model-value="$emit('patch-generation-form', { restoreFaces: $event })" />
            </div>
            <div class="switch-line compact-switch">
              <span>平铺</span>
              <el-switch :model-value="generationForm.tiling" @update:model-value="$emit('patch-generation-form', { tiling: $event })" />
            </div>
          </div>

          <div class="switch-line highres-switch">
            <span>高清修复</span>
            <el-switch :model-value="generationForm.enableHr" @update:model-value="$emit('patch-generation-form', { enableHr: $event })" />
          </div>

          <div v-if="generationForm.enableHr" class="hr-settings">
            <el-form-item label="放大器">
              <el-select :model-value="generationForm.hrUpscaler" filterable placeholder="选择放大器" @update:model-value="$emit('patch-generation-form', { hrUpscaler: $event })">
                <el-option v-for="upscaler in upscalers" :key="upscaler.name" :label="upscaler.name" :value="upscaler.name" />
              </el-select>
            </el-form-item>
            <div class="number-grid tucked">
              <el-form-item label="重绘幅度">
                <el-input-number :model-value="generationForm.denoisingStrength" :min="0" :max="1" :step="0.05" controls-position="right" @update:model-value="$emit('patch-generation-form', { denoisingStrength: $event })" />
              </el-form-item>
              <el-form-item label="放大倍率">
                <el-input-number :model-value="generationForm.hrScale" :min="1" :max="4" :step="0.1" controls-position="right" @update:model-value="$emit('patch-generation-form', { hrScale: $event })" />
              </el-form-item>
              <el-form-item label="二次步数">
                <el-input-number :model-value="generationForm.hrSecondPassSteps" :min="0" :max="150" controls-position="right" @update:model-value="$emit('patch-generation-form', { hrSecondPassSteps: $event })" />
              </el-form-item>
            </div>
          </div>

          <div class="advanced-payload-box">
            <button class="advanced-toggle" type="button" @click="$emit('toggle-advanced-settings')">
              <span>Forge 高级参数</span>
              <strong>{{ advancedSettingsOpen ? '收起' : '展开' }}</strong>
            </button>
            <div v-if="advancedSettingsOpen" class="advanced-payload-fields">
              <el-form-item label="Override Settings JSON">
                <el-input :model-value="overrideSettingsJson" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" resize="none" placeholder='例如: {"eta_noise_seed_delta":31337}' @update:model-value="$emit('update:overrideSettingsJson', $event)" />
              </el-form-item>
              <el-form-item label="扩展 Payload JSON">
                <el-input :model-value="extraPayloadJson" type="textarea" :autosize="{ minRows: 5, maxRows: 10 }" resize="none" placeholder='例如: {"alwayson_scripts":{"ADetailer":{"args":[{"ad_model":"face_yolov8n.pt"}]}}}' @update:model-value="$emit('update:extraPayloadJson', $event)" />
              </el-form-item>
            </div>
          </div>

          <div class="resource-hint">
            <span>{{ providerSummary }}</span>
          </div>

          <div class="preset-box">
            <div class="field-line"><span>创作预设</span><strong>{{ generationPresets.length }}</strong></div>
            <div class="preset-save-row">
              <el-input :model-value="presetName" placeholder="预设名称" @update:model-value="$emit('update:presetName', $event)" />
              <el-button @click="$emit('save-preset')">保存</el-button>
            </div>
            <div v-if="generationPresets.length" class="preset-list">
              <div v-for="preset in generationPresets" :key="preset.id" class="preset-row">
                <button type="button" @click="$emit('apply-preset', preset)">{{ preset.name }}</button>
                <el-button size="small" text @click="$emit('delete-preset', preset.id)">删除</el-button>
              </div>
            </div>
          </div>

          <el-button
            type="primary"
            :disabled="!currentUser"
            :loading="generating"
            :icon="VideoPlay"
            class="wide-button generate-button"
            @click="$emit('generate')"
          >
            开始生成
          </el-button>
        </el-form>
      </section>
    </aside>

    <section class="studio-panel">
      <div class="studio-header">
        <div>
          <p class="eyebrow">Workspace</p>
          <h1>创作工作台</h1>
          <p class="provider-line">{{ providerStatus }}</p>
        </div>
        <div class="stage-actions">
          <el-tooltip content="刷新作品库" placement="bottom">
            <el-button :icon="FolderOpened" circle @click="$emit('refresh-artworks')" />
          </el-tooltip>
          <el-tooltip content="查看大图" placement="bottom">
            <el-button :icon="View" circle :disabled="!previewImage" @click="$emit('open-preview')" />
          </el-tooltip>
          <el-tooltip content="下载图片" placement="bottom">
            <el-button :icon="Download" circle :disabled="!previewImage" @click="$emit('download-current')" />
          </el-tooltip>
        </div>
      </div>

      <div class="image-stage clean-stage">
        <img v-if="previewImage" :src="previewImage" alt="Generated artwork" />
        <div v-else class="empty-stage">
          <PictureFilled />
          <span>生成完成后会在这里显示预览</span>
        </div>
      </div>

      <div v-if="galleryItems.length" class="thumbnail-strip">
        <button
          v-for="item in galleryItems.slice(0, 7)"
          :key="item.id"
          type="button"
          class="thumb-button"
          :class="{ active: previewImage === item.imageUrl }"
          @click="$emit('select-artwork', item)"
        >
          <img :src="item.imageUrl" :alt="item.title" />
        </button>
      </div>

      <div class="prompt-preview-grid">
        <article class="prompt-preview">
          <div class="preview-label">正向 Prompt</div>
          <p>{{ composedPrompt || '等待拼接结果' }}</p>
        </article>
        <article class="prompt-preview negative">
          <div class="preview-label">反向 Prompt</div>
          <p>{{ composedNegative || '等待拼接结果' }}</p>
        </article>
      </div>

      <section class="queue-panel">
        <div class="section-title-row">
          <h2>生成队列</h2>
          <button type="button" class="text-button" @click="$emit('refresh-artworks')">刷新</button>
        </div>
        <div class="queue-list">
          <article
            v-for="item in generationQueueRows"
            :key="item.key"
            class="queue-row"
            :class="{ active: isLiveJob(item), failed: item.status === 'FAILED' }"
          >
            <img v-if="item.imageUrl" class="queue-thumb" :src="item.imageUrl" :alt="item.title" />
            <div v-else class="queue-thumb pending"><VideoPause /></div>
            <div class="queue-main">
              <strong>{{ item.title }}</strong>
              <span>{{ item.modeLabel }} · {{ item.meta }} · {{ statusLabel(item.status) }}</span>
              <el-progress :percentage="statusPercent(item.status)" :status="progressStatus(item.status)" :show-text="false" />
            </div>
            <div class="queue-actions">
              <el-tooltip v-if="isLiveJob(item)" content="取消任务" placement="top">
                <el-button :icon="Close" circle @click="$emit('cancel-tracked-job', item)" />
              </el-tooltip>
              <el-tooltip v-if="item.imageUrl" content="查看预览" placement="top">
                <el-button :icon="View" circle @click="$emit('select-artwork', item)" />
              </el-tooltip>
              <el-tooltip v-if="item.paramsJson" content="复用参数" placement="top">
                <el-button :icon="MagicStick" circle @click="$emit('restore-job', item)" />
              </el-tooltip>
            </div>
          </article>
          <div v-if="!generationQueueRows.length" class="quiet-empty">暂无生成任务</div>
        </div>
      </section>
    </section>
  </section>
</template>

<script setup>
import { Close, Download, FolderOpened, MagicStick, PictureFilled, VideoPause, VideoPlay, View } from '@element-plus/icons-vue'
import PromptBuilderPanel from './PromptBuilderPanel.vue'

defineProps({
  generationMode: { type: String, required: true },
  freeText: { type: String, default: '' },
  negativeText: { type: String, default: '' },
  tagTree: { type: Array, default: () => [] },
  selectedTags: { type: Array, default: () => [] },
  initImagePreview: { type: String, default: '' },
  providerStateClass: { type: String, default: '' },
  generationForm: { type: Object, required: true },
  models: { type: Array, default: () => [] },
  vaes: { type: Array, default: () => [] },
  loras: { type: Array, default: () => [] },
  selectedLoras: { type: Array, default: () => [] },
  loraWeight: { type: Number, default: 0.75 },
  samplers: { type: Array, default: () => [] },
  upscalers: { type: Array, default: () => [] },
  advancedSettingsOpen: { type: Boolean, default: false },
  overrideSettingsJson: { type: String, default: '' },
  extraPayloadJson: { type: String, default: '' },
  providerSummary: { type: String, default: '' },
  generationPresets: { type: Array, default: () => [] },
  presetName: { type: String, default: '' },
  currentUser: { type: Object, default: null },
  generating: { type: Boolean, default: false },
  providerStatus: { type: String, default: '' },
  previewImage: { type: String, default: '' },
  galleryItems: { type: Array, default: () => [] },
  composedPrompt: { type: String, default: '' },
  composedNegative: { type: String, default: '' },
  generationQueueRows: { type: Array, default: () => [] },
  quickSizes: { type: Array, default: () => [] },
  resourceName: { type: Function, required: true },
  loraLabel: { type: Function, required: true },
  isLiveJob: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  statusPercent: { type: Function, required: true },
  progressStatus: { type: Function, required: true }
})

defineEmits([
  'update:generationMode',
  'update:freeText',
  'update:negativeText',
  'reload-tags',
  'open-prompt-builder',
  'compose',
  'toggle-tag',
  'clear-init-image',
  'init-image-change',
  'patch-generation-form',
  'update:selectedLoras',
  'update:loraWeight',
  'apply-size',
  'toggle-advanced-settings',
  'update:overrideSettingsJson',
  'update:extraPayloadJson',
  'update:presetName',
  'save-preset',
  'apply-preset',
  'delete-preset',
  'generate',
  'refresh-artworks',
  'open-preview',
  'download-current',
  'select-artwork',
  'cancel-tracked-job',
  'restore-job'
])
</script>
