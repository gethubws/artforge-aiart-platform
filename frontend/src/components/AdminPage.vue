<template>
  <section class="admin-view admin-console">
    <div class="admin-console-topline">
      <div>
        <h1>管理后台</h1>
        <span>ArtForge 平台运营与内容治理</span>
      </div>
      <el-tooltip content="刷新当前后台数据" placement="bottom">
        <el-button
          :icon="Refresh"
          circle
          :loading="auditLoading || tagAdminLoading || adminDashboardLoading || adminUserLoading"
          @click="$emit('refresh')"
        />
      </el-tooltip>
    </div>

    <div class="admin-console-layout">
      <nav class="admin-section-nav" aria-label="管理员栏目">
        <button
          v-for="item in sections"
          :key="item.key"
          type="button"
          :class="{ active: activeSection === item.key }"
          @click="selectSection(item.key)"
        >
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
          <b v-if="item.badge">{{ item.badge }}</b>
        </button>
      </nav>

      <div class="admin-section-content">
        <AdminOverviewPanel
          v-if="activeSection === 'overview'"
          :dashboard="adminDashboard"
          :tag-analytics="tagAnalytics"
          :pending-audits="pendingAudits"
          :loading="adminDashboardLoading"
          :format-metric-value="formatMetricValue"
          :format-date="formatDate"
          :status-label="statusLabel"
          @open-section="selectSection"
        />

        <AdminUsersPanel
          v-else-if="activeSection === 'users'"
          :current-user="currentUser"
          :user-page="adminUserPage"
          :query="adminUserQuery"
          :loading="adminUserLoading"
          :format-date="formatDate"
          :format-points="formatPoints"
          @search="$emit('search-admin-users')"
          @page-change="$emit('change-admin-user-page', $event)"
          @save-user="$emit('save-admin-user', $event)"
        />

        <AdminAuditPanel
          v-else-if="activeSection === 'audit'"
          :loading="auditLoading"
          :pending-audits="pendingAudits"
          :my-audits="myAudits"
          :format-date="formatDate"
          :status-label="statusLabel"
          @review="$emit('review-content-audit', $event)"
        />

        <AdminOperationsPanel
          v-else-if="activeSection === 'operations'"
          :operation-page="adminOperationPage"
          :query="adminOperationQuery"
          :loading="adminOperationLoading"
          :format-date="formatDate"
          @search="$emit('search-admin-operations')"
          @page-change="$emit('change-admin-operation-page', $event)"
        />

        <AdminTagsPanel
          v-else-if="activeSection === 'tags'"
          :tag-admin-loading="tagAdminLoading"
          :tag-analytics="tagAnalytics"
          :category-form="categoryForm"
          :tag-form="tagForm"
          :admin-tag-tree="adminTagTree"
          :flat-tags="flatTags"
          :preview-manager-visible="previewManagerVisible"
          :active-tag-detail="activeTagDetail"
          :preview-form="previewForm"
          :tag-preview-loading="tagPreviewLoading"
          @save-tag-category="$emit('save-tag-category')"
          @reset-tag-form="$emit('reset-tag-form')"
          @save-tag-item="$emit('save-tag-item')"
          @edit-tag-item="$emit('edit-tag-item', $event)"
          @disable-tag-item="$emit('disable-tag-item', $event)"
          @open-tag-gallery="$emit('open-tag-gallery', $event)"
          @close-tag-gallery="$emit('close-tag-gallery')"
          @reset-preview-form="$emit('reset-preview-form')"
          @edit-tag-preview="$emit('edit-tag-preview', $event)"
          @save-tag-preview="$emit('save-tag-preview', $event)"
          @set-tag-preview-cover="$emit('set-tag-preview-cover', $event)"
          @delete-tag-preview="$emit('delete-tag-preview', $event)"
          @move-tag-preview="$emit('move-tag-preview', $event)"
        />

        <AdminContentPanel
          v-else
          :dashboard="adminDashboard"
          :status-count-text="statusCountText"
          :status-label="statusLabel"
          :point-reason-label="pointReasonLabel"
          :format-points="formatPoints"
          @navigate="$emit('navigate-platform', $event)"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { CollectionTag, DataAnalysis, Document, Grid, Refresh, Stamp, User } from '@element-plus/icons-vue'
import AdminAuditPanel from './admin/AdminAuditPanel.vue'
import AdminContentPanel from './admin/AdminContentPanel.vue'
import AdminOperationsPanel from './admin/AdminOperationsPanel.vue'
import AdminOverviewPanel from './admin/AdminOverviewPanel.vue'
import AdminTagsPanel from './admin/AdminTagsPanel.vue'
import AdminUsersPanel from './admin/AdminUsersPanel.vue'

const props = defineProps({
  currentUser: { type: Object, default: null },
  auditLoading: { type: Boolean, default: false },
  tagAdminLoading: { type: Boolean, default: false },
  adminDashboardLoading: { type: Boolean, default: false },
  adminDashboard: { type: Object, default: null },
  tagAnalytics: { type: Object, default: () => ({ topTags: [], topCombinations: [], searchKeywords: [] }) },
  adminUserLoading: { type: Boolean, default: false },
  adminUserPage: { type: Object, default: () => ({ items: [], page: 1, size: 20, total: 0, pages: 0 }) },
  adminUserQuery: { type: Object, required: true },
  adminOperationLoading: { type: Boolean, default: false },
  adminOperationPage: { type: Object, default: () => ({ items: [], page: 1, size: 20, total: 0, pages: 0 }) },
  adminOperationQuery: { type: Object, required: true },
  categoryForm: { type: Object, required: true },
  tagForm: { type: Object, required: true },
  adminTagTree: { type: Array, default: () => [] },
  flatTags: { type: Array, default: () => [] },
  previewManagerVisible: { type: Boolean, default: false },
  activeTagDetail: { type: Object, default: null },
  previewForm: { type: Object, required: true },
  tagPreviewLoading: { type: Boolean, default: false },
  pendingAudits: { type: Array, default: () => [] },
  myAudits: { type: Array, default: () => [] },
  formatMetricValue: { type: Function, required: true },
  statusCountText: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  pointReasonLabel: { type: Function, required: true },
  formatPoints: { type: Function, required: true },
  statusLabel: { type: Function, required: true }
})

const emit = defineEmits([
  'refresh', 'load-admin-users', 'search-admin-users', 'change-admin-user-page', 'save-admin-user',
  'load-admin-operations', 'search-admin-operations', 'change-admin-operation-page', 'navigate-platform',
  'save-tag-category', 'reset-tag-form', 'save-tag-item', 'edit-tag-item', 'disable-tag-item', 'open-tag-gallery',
  'close-tag-gallery', 'reset-preview-form', 'edit-tag-preview', 'save-tag-preview', 'set-tag-preview-cover',
  'delete-tag-preview', 'move-tag-preview', 'review-content-audit'
])

const activeSection = ref('overview')
const sections = computed(() => [
  { key: 'overview', label: '总览', icon: DataAnalysis },
  { key: 'users', label: '用户管理', icon: User },
  { key: 'audit', label: '内容审核', icon: Stamp, badge: props.pendingAudits.length || 0 },
  { key: 'operations', label: '操作日志', icon: Document },
  { key: 'tags', label: '标签图库', icon: CollectionTag },
  { key: 'content', label: '平台内容', icon: Grid }
])

function selectSection(section) {
  activeSection.value = section
  if (section === 'users') emit('load-admin-users')
  if (section === 'operations') emit('load-admin-operations')
}
</script>
