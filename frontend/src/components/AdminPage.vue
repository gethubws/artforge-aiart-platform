<template>
  <section class="admin-view">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Operations</p>
        <h1>后台运营</h1>
      </div>
      <div class="library-tools">
        <el-button :icon="Refresh" :loading="auditLoading || tagAdminLoading || adminDashboardLoading" @click="$emit('refresh')">
          刷新
        </el-button>
      </div>
    </div>

    <section v-if="adminDashboard" class="admin-dashboard">
      <div class="dashboard-metrics">
        <article v-for="metric in adminDashboard.metrics" :key="metric.key" class="dashboard-metric" :class="metric.tone">
          <span>{{ metric.label }}</span>
          <strong>{{ formatMetricValue(metric) }}</strong>
          <em>{{ metric.unit }}</em>
        </article>
      </div>

      <div class="dashboard-grid">
        <article class="dashboard-panel"><h2>生成状态</h2><p>{{ statusCountText(adminDashboard.generationStatus) }}</p></article>
        <article class="dashboard-panel"><h2>审核队列</h2><p>{{ statusCountText(adminDashboard.auditStatus) }}</p></article>
        <article class="dashboard-panel"><h2>任务状态</h2><p>{{ statusCountText(adminDashboard.taskStatus) }}</p></article>
        <article class="dashboard-panel"><h2>风格包状态</h2><p>{{ statusCountText(adminDashboard.styleStatus) }}</p></article>
      </div>

      <div class="dashboard-grid wide">
        <article class="dashboard-panel">
          <h2>近 7 天生成</h2>
          <div class="daily-bars">
            <span v-for="item in adminDashboard.dailyGenerations" :key="item.date">
              <i :style="{ height: `${Math.max(8, Math.min(72, item.count * 12))}px` }"></i>
              <b>{{ item.count }}</b>
              <em>{{ formatDate(item.date) }}</em>
            </span>
          </div>
        </article>

        <article class="dashboard-panel">
          <h2>近 7 天作品</h2>
          <div class="daily-bars">
            <span v-for="item in adminDashboard.dailyArtworks" :key="item.date">
              <i :style="{ height: `${Math.max(8, Math.min(72, item.count * 12))}px` }"></i>
              <b>{{ item.count }}</b>
              <em>{{ formatDate(item.date) }}</em>
            </span>
          </div>
        </article>

        <article class="dashboard-panel">
          <h2>积分流向</h2>
          <div class="flow-list">
            <span v-for="flow in adminDashboard.pointFlows" :key="flow.reason">
              <strong>{{ pointReasonLabel(flow.reason) }}</strong>
              <em>入 {{ formatPoints(flow.totalIn) }} / 出 {{ formatPoints(flow.totalOut) }}</em>
            </span>
            <p v-if="!adminDashboard.pointFlows.length">暂无积分流动</p>
          </div>
        </article>

        <article class="dashboard-panel">
          <h2>近期活动</h2>
          <div class="activity-list">
            <span v-for="activity in adminDashboard.recentActivities" :key="`${activity.type}-${activity.createdAt}-${activity.title}`">
              <strong>{{ activity.type }}</strong>
              <em>{{ activity.title }}</em>
              <b>{{ statusLabel(activity.status) }}</b>
            </span>
          </div>
        </article>
      </div>
    </section>

    <div class="admin-ops-layout">
      <section class="soft-panel admin-tool-panel">
        <div class="section-title-row"><h2>标签分类</h2></div>
        <el-form label-position="top">
          <el-form-item label="分类名称"><el-input v-model="categoryForm.name" placeholder="镜头 / 光线 / 材质" /></el-form-item>
          <el-form-item label="Slug"><el-input v-model="categoryForm.slug" placeholder="camera / light / material" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="categoryForm.sortOrder" :min="0" :step="10" controls-position="right" /></el-form-item>
          <el-button type="primary" class="wide-button" :loading="tagAdminLoading" @click="$emit('save-tag-category')">创建分类</el-button>
        </el-form>
      </section>

      <section class="soft-panel admin-tool-panel">
        <div class="section-title-row">
          <h2>{{ tagForm.id ? '编辑标签' : '新建标签' }}</h2>
          <el-button v-if="tagForm.id" size="small" @click="$emit('reset-tag-form')">清空</el-button>
        </div>
        <el-form label-position="top">
          <el-form-item label="所属分类">
            <el-select v-model="tagForm.categoryId" filterable placeholder="选择分类">
              <el-option v-for="category in adminTagTree" :key="category.id" :label="category.name" :value="category.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="标签名称"><el-input v-model="tagForm.name" placeholder="柔和逆光" /></el-form-item>
          <el-form-item label="正向提示词"><el-input v-model="tagForm.promptText" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" resize="none" /></el-form-item>
          <el-form-item label="反向提示词"><el-input v-model="tagForm.negativePromptText" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" /></el-form-item>
          <el-form-item label="预览图 URL"><el-input v-model="tagForm.previewImageUrl" placeholder="https://... 或 /images/tags/..." /></el-form-item>
          <div v-if="tagForm.previewImageUrl" class="tag-preview-box">
            <img :src="tagForm.previewImageUrl" alt="标签预览图" />
          </div>
          <div class="tag-form-grid">
            <el-form-item label="权重"><el-input-number v-model="tagForm.weight" :min="0.1" :max="3" :step="0.05" controls-position="right" /></el-form-item>
            <el-form-item label="可见性"><el-select v-model="tagForm.visibility"><el-option label="公开" value="PUBLIC" /><el-option label="私有" value="PRIVATE" /></el-select></el-form-item>
          </div>
          <el-button type="primary" class="wide-button" :loading="tagAdminLoading" @click="$emit('save-tag-item')">保存标签</el-button>
        </el-form>
      </section>

      <section class="soft-panel admin-tag-list-panel">
        <div class="section-title-row"><h2>标签知识库</h2><span>{{ flatTags.length }} 个标签</span></div>
        <div class="admin-tag-groups">
          <div v-for="category in adminTagTree" :key="category.id" class="admin-tag-group">
            <div class="admin-tag-group-head"><strong>{{ category.name }}</strong><span>{{ category.slug }}</span></div>
            <article v-for="tag in category.tags" :key="tag.id" class="admin-tag-row">
              <div class="admin-tag-copy">
                <strong>{{ tag.name }}</strong>
                <span>{{ tag.promptText }}</span>
                <div v-if="tag.previewImageUrl" class="mini-preview-chip">含预览图</div>
              </div>
              <div class="submission-actions">
                <el-button size="small" @click="$emit('edit-tag-item', { category, tag })">编辑</el-button>
                <el-button size="small" @click="$emit('disable-tag-item', tag)">停用</el-button>
              </div>
            </article>
            <div v-if="!category.tags.length" class="quiet-empty compact-empty">暂无标签</div>
          </div>
        </div>
      </section>
    </div>

    <div class="audit-layout">
      <section class="audit-column">
        <div class="section-title-row"><h2>待审核作品</h2></div>
        <div class="submission-list">
          <article v-for="audit in pendingAudits" :key="audit.id" class="submission-row reviewable">
            <img v-if="audit.artworkImageUrl" :src="audit.artworkImageUrl" :alt="audit.artworkTitle" />
            <div>
              <strong>{{ audit.artworkTitle || audit.contentType }}</strong>
              <span>{{ statusLabel(audit.status) }} · {{ formatDate(audit.createdAt) }}</span>
            </div>
            <div class="submission-actions">
              <el-button size="small" type="primary" @click="$emit('review-content-audit', { audit, status: 'APPROVED' })">通过</el-button>
              <el-button size="small" @click="$emit('review-content-audit', { audit, status: 'REJECTED' })">拒绝</el-button>
            </div>
          </article>
          <div v-if="!pendingAudits.length" class="quiet-empty">暂无待审核内容</div>
        </div>
      </section>

      <section class="audit-column">
        <div class="section-title-row"><h2>我的公开申请</h2></div>
        <div class="submission-list">
          <article v-for="audit in myAudits" :key="audit.id" class="submission-row">
            <img v-if="audit.artworkImageUrl" :src="audit.artworkImageUrl" :alt="audit.artworkTitle" />
            <div>
              <strong>{{ audit.artworkTitle || audit.contentType }}</strong>
              <span>{{ statusLabel(audit.status) }} · {{ formatDate(audit.createdAt) }}</span>
            </div>
          </article>
          <div v-if="!myAudits.length" class="quiet-empty">暂无公开申请</div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'

defineProps({
  auditLoading: { type: Boolean, default: false },
  tagAdminLoading: { type: Boolean, default: false },
  adminDashboardLoading: { type: Boolean, default: false },
  adminDashboard: { type: Object, default: null },
  categoryForm: { type: Object, required: true },
  tagForm: { type: Object, required: true },
  adminTagTree: { type: Array, default: () => [] },
  flatTags: { type: Array, default: () => [] },
  pendingAudits: { type: Array, default: () => [] },
  myAudits: { type: Array, default: () => [] },
  formatMetricValue: { type: Function, required: true },
  statusCountText: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  pointReasonLabel: { type: Function, required: true },
  formatPoints: { type: Function, required: true },
  statusLabel: { type: Function, required: true }
})

defineEmits([
  'refresh',
  'save-tag-category',
  'reset-tag-form',
  'save-tag-item',
  'edit-tag-item',
  'disable-tag-item',
  'review-content-audit'
])
</script>
