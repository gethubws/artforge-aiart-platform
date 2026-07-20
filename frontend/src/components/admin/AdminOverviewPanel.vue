<template>
  <section class="admin-section-view" v-loading="loading">
    <div class="admin-section-heading">
      <div>
        <h1>运营总览</h1>
        <p>平台创作、内容供给与审核队列的实时状态。</p>
      </div>
      <span class="admin-count-label">最近 7 日</span>
    </div>

    <div class="admin-kpi-grid">
      <article v-for="metric in visibleMetrics" :key="metric.key" class="admin-kpi" :class="metric.tone">
        <div class="admin-kpi-icon"><component :is="metricIcon(metric.key)" /></div>
        <div>
          <span>{{ metric.label }}</span>
          <strong>{{ formatMetricValue(metric) }}<small>{{ metric.unit }}</small></strong>
        </div>
      </article>
    </div>

    <div class="admin-overview-grid">
      <article class="admin-data-panel admin-trend-panel">
        <div class="admin-panel-head">
          <div><h2>近 7 日创作趋势</h2><span>生成任务与新增作品</span></div>
        </div>
        <AdminChart :option="trendOption" height="300px" aria-label="近七日生成任务和新增作品趋势" />
      </article>

      <article class="admin-data-panel admin-status-panel">
        <div class="admin-panel-head">
          <div><h2>生成任务状态</h2><span>近 90 日</span></div>
        </div>
        <AdminChart :option="generationStatusOption" height="300px" aria-label="生成任务状态分布" />
      </article>

      <article class="admin-data-panel admin-tag-chart-panel">
        <div class="admin-panel-head">
          <div><h2>热门标签</h2><span>按提示词拼接次数</span></div>
        </div>
        <AdminChart :option="tagUsageOption" height="320px" aria-label="热门标签使用次数排行" />
      </article>

      <article class="admin-data-panel admin-queue-panel">
        <div class="admin-panel-head">
          <div><h2>运营队列</h2><span>优先处理</span></div>
        </div>
        <div class="admin-queue-list">
          <button v-for="audit in pendingAudits.slice(0, 4)" :key="audit.id" type="button" @click="$emit('open-section', 'audit')">
            <span class="queue-dot warning"></span>
            <span><strong>{{ audit.artworkTitle || audit.contentType }}</strong><small>等待内容审核</small></span>
            <ArrowRight />
          </button>
          <button v-for="item in zeroResultKeywords" :key="item.keyword" type="button" @click="$emit('open-section', 'tags')">
            <span class="queue-dot muted"></span>
            <span><strong>{{ item.keyword }}</strong><small>零结果搜索 {{ item.zeroResultCount }} 次</small></span>
            <ArrowRight />
          </button>
          <div v-if="!pendingAudits.length && !zeroResultKeywords.length" class="quiet-empty compact-empty">当前没有待处理事项</div>
        </div>
      </article>
    </div>

    <article class="admin-data-panel admin-activity-panel">
      <div class="admin-panel-head">
        <div><h2>近期平台活动</h2><span>最近 30 日</span></div>
      </div>
      <el-table :data="dashboard?.recentActivities || []" class="admin-table" empty-text="暂无平台活动">
        <el-table-column prop="type" label="类型" width="90" />
        <el-table-column prop="title" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="scope"><span class="admin-status-chip">{{ statusLabel(scope.row.status) }}</span></template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </article>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import {
  ArrowRight,
  CircleCheckFilled,
  CollectionTag,
  MagicStick,
  PictureFilled,
  UserFilled,
  WarningFilled
} from '@element-plus/icons-vue'
import AdminChart from './AdminChart.vue'

const props = defineProps({
  dashboard: { type: Object, default: null },
  tagAnalytics: { type: Object, default: () => ({ topTags: [], searchKeywords: [] }) },
  pendingAudits: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  formatMetricValue: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  statusLabel: { type: Function, required: true }
})

defineEmits(['open-section'])

const metricKeys = ['users', 'artworks', 'generations', 'successRate', 'pendingAudits', 'stylePackages']
const visibleMetrics = computed(() => {
  const metrics = props.dashboard?.metrics || []
  return metricKeys.map((key) => metrics.find((item) => item.key === key)).filter(Boolean)
})

const zeroResultKeywords = computed(() => (props.tagAnalytics.searchKeywords || [])
  .filter((item) => Number(item.zeroResultCount || 0) > 0)
  .slice(0, Math.max(0, 6 - props.pendingAudits.slice(0, 4).length)))

const chartText = '#59635d'
const chartLine = '#e6ebe7'
const chartGreen = '#557f63'
const chartBlue = '#6f9ab3'
const chartAmber = '#d7a33f'

const trendOption = computed(() => ({
  color: [chartGreen, chartBlue],
  tooltip: { trigger: 'axis' },
  legend: { top: 0, right: 0, textStyle: { color: chartText } },
  grid: { left: 42, right: 18, top: 42, bottom: 30 },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: (props.dashboard?.dailyGenerations || []).map((item) => shortDate(item.date)),
    axisLine: { lineStyle: { color: chartLine } },
    axisTick: { show: false },
    axisLabel: { color: chartText }
  },
  yAxis: {
    type: 'value', minInterval: 1,
    splitLine: { lineStyle: { color: chartLine } },
    axisLabel: { color: chartText }
  },
  series: [
    { name: '生成任务', type: 'line', smooth: true, symbolSize: 7, lineStyle: { width: 3 }, areaStyle: { opacity: 0.08 }, data: (props.dashboard?.dailyGenerations || []).map((item) => item.count) },
    { name: '新增作品', type: 'line', smooth: true, symbolSize: 7, lineStyle: { width: 3 }, data: (props.dashboard?.dailyArtworks || []).map((item) => item.count) }
  ]
}))

const generationStatusOption = computed(() => ({
  color: [chartGreen, chartBlue, chartAmber, '#c96a66', '#9aa39d'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, left: 'center', textStyle: { color: chartText } },
  series: [{
    type: 'pie',
    radius: ['50%', '72%'],
    center: ['50%', '43%'],
    padAngle: 2,
    itemStyle: { borderRadius: 4 },
    label: { formatter: '{b}\n{c}', color: chartText },
    data: (props.dashboard?.generationStatus || []).map((item) => ({ name: props.statusLabel(item.status), value: item.count }))
  }]
}))

const tagUsageOption = computed(() => {
  const tags = (props.tagAnalytics.topTags || []).slice(0, 8).reverse()
  return {
    color: [chartGreen],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 92, right: 24, top: 10, bottom: 20 },
    xAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: chartLine } }, axisLabel: { color: chartText } },
    yAxis: { type: 'category', data: tags.map((tag) => tag.displayNameZh || tag.name), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartText, width: 78, overflow: 'truncate' } },
    series: [{ type: 'bar', data: tags.map((tag) => tag.usageCount), barWidth: 12, itemStyle: { borderRadius: [0, 5, 5, 0] } }]
  }
})

function metricIcon(key) {
  return {
    users: UserFilled,
    artworks: PictureFilled,
    generations: MagicStick,
    successRate: CircleCheckFilled,
    pendingAudits: WarningFilled,
    stylePackages: CollectionTag
  }[key] || CircleCheckFilled
}

function shortDate(value) {
  if (!value) return '-'
  const parts = String(value).split('-')
  return parts.length === 3 ? `${parts[1]}/${parts[2]}` : value
}
</script>
