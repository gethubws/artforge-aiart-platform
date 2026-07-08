<template>
  <section class="models-view">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Forge Assets</p>
        <h1>模型资源</h1>
      </div>
      <div class="library-tools">
        <el-input
          :model-value="modelResourceKeyword"
          :prefix-icon="Search"
          placeholder="搜索模型、LoRA、VAE"
          clearable
          @update:model-value="$emit('update:modelResourceKeyword', $event)"
        />
        <el-button :icon="Refresh" @click="$emit('refresh-provider')">刷新资源</el-button>
      </div>
    </div>

    <section v-if="favoriteResources.length" class="favorite-resource-strip">
      <button
        v-for="favorite in favoriteResources"
        :key="favorite.key"
        type="button"
        class="favorite-resource-pill"
        @click="$emit('apply-favorite-resource', favorite)"
      >
        <span>{{ resourceTypeLabel(favorite.type) }}</span>
        <strong>{{ favorite.name }}</strong>
      </button>
    </section>

    <div class="model-resource-layout">
      <section class="resource-column">
        <div class="section-title-row">
          <h2>Checkpoint</h2>
          <span>{{ filteredModels.length }}</span>
        </div>
        <div class="resource-grid">
          <article v-for="model in filteredModels" :key="resourceName(model)" class="resource-card">
            <button
              class="resource-favorite"
              :class="{ active: isFavoriteResource('model', model) }"
              type="button"
              @click="$emit('toggle-favorite-resource', { type: 'model', resource: model })"
            >
              <Star />
            </button>
            <strong>{{ resourceName(model) }}</strong>
            <span>{{ model.filename || model.hash || '模型' }}</span>
            <el-button size="small" type="primary" @click="$emit('apply-model-resource', model)">使用模型</el-button>
          </article>
          <div v-if="!filteredModels.length" class="quiet-empty compact-empty">暂无模型</div>
        </div>
      </section>

      <section class="resource-column">
        <div class="section-title-row">
          <h2>LoRA</h2>
          <span>{{ filteredLoras.length }}</span>
        </div>
        <div class="resource-grid">
          <article v-for="lora in filteredLoras" :key="resourceName(lora)" class="resource-card lora-resource-card">
            <button
              class="resource-favorite"
              :class="{ active: isFavoriteResource('lora', lora) }"
              type="button"
              @click="$emit('toggle-favorite-resource', { type: 'lora', resource: lora })"
            >
              <Star />
            </button>
            <strong>{{ loraLabel(lora) }}</strong>
            <span>{{ lora.path || lora.metadata?.ss_output_name || 'LoRA' }}</span>
            <div class="lora-trigger-editor">
              <el-input
                :model-value="loraTriggerDrafts[loraTriggerKey(lora)]"
                size="small"
                :placeholder="suggestedLoraTrigger(lora) || '触发词备注'"
                clearable
                @update:model-value="$emit('update:lora-trigger-draft', { key: loraTriggerKey(lora), value: $event })"
              />
              <el-button size="small" @click="$emit('save-lora-trigger', lora)">保存</el-button>
            </div>
            <el-button size="small" type="primary" @click="$emit('add-lora-resource', lora)">加入 LoRA</el-button>
          </article>
          <div v-if="!filteredLoras.length" class="quiet-empty compact-empty">暂无 LoRA</div>
        </div>
      </section>

      <section class="resource-column">
        <div class="section-title-row">
          <h2>VAE</h2>
          <span>{{ filteredVaes.length }}</span>
        </div>
        <div class="resource-grid">
          <article v-for="vae in filteredVaes" :key="resourceName(vae)" class="resource-card">
            <button
              class="resource-favorite"
              :class="{ active: isFavoriteResource('vae', vae) }"
              type="button"
              @click="$emit('toggle-favorite-resource', { type: 'vae', resource: vae })"
            >
              <Star />
            </button>
            <strong>{{ resourceName(vae) }}</strong>
            <span>{{ vae.filename || 'VAE' }}</span>
            <el-button size="small" @click="$emit('apply-vae-resource', vae)">使用 VAE</el-button>
          </article>
          <div v-if="!filteredVaes.length" class="quiet-empty compact-empty">暂无 VAE</div>
        </div>
      </section>

      <section class="resource-column">
        <div class="section-title-row">
          <h2>放大器</h2>
          <span>{{ filteredUpscalers.length }}</span>
        </div>
        <div class="resource-grid">
          <article v-for="upscaler in filteredUpscalers" :key="resourceName(upscaler)" class="resource-card">
            <button
              class="resource-favorite"
              :class="{ active: isFavoriteResource('upscaler', upscaler) }"
              type="button"
              @click="$emit('toggle-favorite-resource', { type: 'upscaler', resource: upscaler })"
            >
              <Star />
            </button>
            <strong>{{ resourceName(upscaler) }}</strong>
            <span>{{ upscaler.model_path || 'Upscaler' }}</span>
            <el-button size="small" @click="$emit('apply-upscaler-resource', upscaler)">用于高清修复</el-button>
          </article>
          <div v-if="!filteredUpscalers.length" class="quiet-empty compact-empty">暂无放大器</div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { Refresh, Search, Star } from '@element-plus/icons-vue'

defineProps({
  modelResourceKeyword: { type: String, default: '' },
  favoriteResources: { type: Array, default: () => [] },
  filteredModels: { type: Array, default: () => [] },
  filteredLoras: { type: Array, default: () => [] },
  filteredVaes: { type: Array, default: () => [] },
  filteredUpscalers: { type: Array, default: () => [] },
  loraTriggerDrafts: { type: Object, default: () => ({}) },
  resourceTypeLabel: { type: Function, required: true },
  resourceName: { type: Function, required: true },
  isFavoriteResource: { type: Function, required: true },
  loraLabel: { type: Function, required: true },
  loraTriggerKey: { type: Function, required: true },
  suggestedLoraTrigger: { type: Function, required: true }
})

defineEmits([
  'update:modelResourceKeyword',
  'refresh-provider',
  'apply-favorite-resource',
  'toggle-favorite-resource',
  'update:lora-trigger-draft',
  'save-lora-trigger',
  'apply-model-resource',
  'add-lora-resource',
  'apply-vae-resource',
  'apply-upscaler-resource'
])
</script>
