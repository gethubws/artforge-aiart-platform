<template>
  <section class="admin-section-view" v-loading="loading">
    <div class="admin-section-heading">
      <div>
        <h1>内容审核</h1>
        <p>处理作品公开申请并追踪审核结果。</p>
      </div>
      <span class="admin-count-label warning">待处理 {{ pendingAudits.length }}</span>
    </div>

    <div class="admin-audit-grid">
      <article class="admin-data-panel admin-pending-audit-panel">
        <div class="admin-panel-head"><div><h2>待审核队列</h2><span>按提交时间排序</span></div></div>
        <div class="admin-review-list">
          <article v-for="audit in pendingAudits" :key="audit.id" class="admin-review-row">
            <img v-if="audit.artworkImageUrl" :src="audit.artworkImageUrl" :alt="audit.artworkTitle" />
            <div class="admin-review-copy">
              <strong>{{ audit.artworkTitle || audit.contentType }}</strong>
              <span>{{ audit.contentType }} · {{ formatDate(audit.createdAt) }}</span>
            </div>
            <div class="admin-review-actions">
              <el-button type="primary" size="small" :icon="Check" @click="$emit('review', { audit, status: 'APPROVED' })">通过</el-button>
              <el-button size="small" :icon="Close" @click="$emit('review', { audit, status: 'REJECTED' })">拒绝</el-button>
            </div>
          </article>
          <div v-if="!pendingAudits.length" class="quiet-empty">当前没有待审核内容</div>
        </div>
      </article>

      <article class="admin-data-panel admin-audit-history-panel">
        <div class="admin-panel-head"><div><h2>我的公开申请</h2><span>{{ myAudits.length }} 条记录</span></div></div>
        <el-table :data="myAudits" class="admin-table" empty-text="暂无公开申请">
          <el-table-column label="内容" min-width="150">
            <template #default="scope">{{ scope.row.artworkTitle || scope.row.contentType }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope"><span class="admin-status-chip">{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="时间" width="160">
            <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </article>
    </div>
  </section>
</template>

<script setup>
import { Check, Close } from '@element-plus/icons-vue'

defineProps({
  loading: { type: Boolean, default: false },
  pendingAudits: { type: Array, default: () => [] },
  myAudits: { type: Array, default: () => [] },
  formatDate: { type: Function, required: true },
  statusLabel: { type: Function, required: true }
})

defineEmits(['review'])
</script>
