<template>
  <section class="admin-section-view">
    <div class="admin-section-heading">
      <div>
        <h1>平台内容</h1>
        <p>查看作品、风格包、任务与模型资源的运营状态。</p>
      </div>
    </div>

    <div class="admin-content-list">
      <article v-for="item in contentItems" :key="item.key" class="admin-content-row">
        <div class="admin-content-icon"><component :is="item.icon" /></div>
        <div class="admin-content-main">
          <strong>{{ item.title }}</strong>
          <span>{{ item.summary }}</span>
        </div>
        <div class="admin-content-statuses">
          <span v-for="status in item.statuses" :key="status.status">
            {{ statusLabel(status.status) }} <b>{{ status.count }}</b>
          </span>
        </div>
        <el-button :icon="ArrowRight" @click="$emit('navigate', item.target)">打开</el-button>
      </article>
    </div>

    <div class="admin-content-bottom-grid">
      <article class="admin-data-panel">
        <div class="admin-panel-head"><div><h2>积分流向</h2><span>近 30 日</span></div></div>
        <div class="admin-flow-table">
          <span v-for="flow in dashboard?.pointFlows || []" :key="flow.reason">
            <strong>{{ pointReasonLabel(flow.reason) }}</strong>
            <em>流入 {{ formatPoints(flow.totalIn) }}</em>
            <em>流出 {{ formatPoints(flow.totalOut) }}</em>
            <b>{{ flow.count }} 笔</b>
          </span>
          <div v-if="!dashboard?.pointFlows?.length" class="quiet-empty compact-empty">暂无积分流动</div>
        </div>
      </article>

      <article class="admin-data-panel">
        <div class="admin-panel-head"><div><h2>状态观察</h2><span>近 90 日</span></div></div>
        <div class="admin-state-groups">
          <div><strong>生成</strong><span>{{ statusCountText(dashboard?.generationStatus) }}</span></div>
          <div><strong>审核</strong><span>{{ statusCountText(dashboard?.auditStatus) }}</span></div>
          <div><strong>任务</strong><span>{{ statusCountText(dashboard?.taskStatus) }}</span></div>
          <div><strong>风格包</strong><span>{{ statusCountText(dashboard?.styleStatus) }}</span></div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowRight, CollectionTag, Cpu, List, PictureFilled } from '@element-plus/icons-vue'

const props = defineProps({
  dashboard: { type: Object, default: null },
  statusCountText: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  pointReasonLabel: { type: Function, required: true },
  formatPoints: { type: Function, required: true }
})

defineEmits(['navigate'])

const contentItems = computed(() => [
  {
    key: 'artworks', title: '作品内容', icon: PictureFilled, target: 'community',
    summary: `${metric('artworks')} 张作品，${metric('publicArtworks')} 张已公开`,
    statuses: props.dashboard?.auditStatus || []
  },
  {
    key: 'styles', title: '风格包', icon: CollectionTag, target: 'style-market',
    summary: `${metric('stylePackages')} 个已发布风格包`,
    statuses: props.dashboard?.styleStatus || []
  },
  {
    key: 'tasks', title: '任务市场', icon: List, target: 'task-market',
    summary: `${metric('tasks')} 个进行中任务`,
    statuses: props.dashboard?.taskStatus || []
  },
  {
    key: 'models', title: '模型资源', icon: Cpu, target: 'models',
    summary: '检查 Forge 模型、LoRA、VAE 与放大器资源',
    statuses: []
  }
])

function metric(key) {
  return props.dashboard?.metrics?.find((item) => item.key === key)?.value || 0
}
</script>
