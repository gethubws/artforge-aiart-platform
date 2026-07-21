<template>
  <section class="admin-section-view" v-loading="loading">
    <div class="admin-section-heading">
      <div>
        <h1>操作日志</h1>
        <p>追踪管理员对用户、内容审核和标签图库执行的关键修改。</p>
      </div>
      <span class="admin-count-label">共 {{ operationPage.total || 0 }} 条记录</span>
    </div>

    <div class="admin-filter-bar">
      <el-select v-model="query.action" clearable placeholder="全部动作" @change="$emit('search')">
        <el-option v-for="item in actionOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.targetType" clearable placeholder="全部对象" @change="$emit('search')">
        <el-option label="用户" value="USER" />
        <el-option label="内容审核" value="CONTENT_AUDIT" />
        <el-option label="标签分类" value="TAG_CATEGORY" />
        <el-option label="提示词标签" value="TAG" />
        <el-option label="标签预览图" value="TAG_PREVIEW" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="$emit('search')">查询</el-button>
    </div>

    <article class="admin-data-panel admin-users-table-panel">
      <el-table :data="operationPage.items || []" class="admin-table" empty-text="暂无管理员操作记录">
        <el-table-column label="管理员" min-width="150" fixed="left">
          <template #default="scope">
            <div class="admin-operation-user">
              <strong>{{ scope.row.operatorName }}</strong>
              <small>#{{ scope.row.operatorId }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="动作" min-width="170">
          <template #default="scope">
            <span class="admin-operation-action">{{ actionLabel(scope.row.action) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="对象" min-width="140">
          <template #default="scope">
            {{ targetLabel(scope.row.targetType) }}<span v-if="scope.row.targetId"> #{{ scope.row.targetId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="说明" prop="summary" min-width="220" show-overflow-tooltip />
        <el-table-column label="请求" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <code class="admin-operation-route">{{ scope.row.requestMethod }} {{ scope.row.requestPath }}</code>
          </template>
        </el-table-column>
        <el-table-column label="来源 IP" prop="ipAddress" width="145" />
        <el-table-column label="时间" width="170">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination-row">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="operationPage.page || 1"
          :page-size="operationPage.size || 20"
          :total="operationPage.total || 0"
          @current-change="$emit('page-change', $event)"
        />
      </div>
    </article>
  </section>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue'

defineProps({
  operationPage: { type: Object, default: () => ({ items: [], page: 1, size: 20, total: 0, pages: 0 }) },
  query: { type: Object, required: true },
  loading: { type: Boolean, default: false },
  formatDate: { type: Function, required: true }
})

defineEmits(['search', 'page-change'])

const actionOptions = [
  { value: 'USER_UPDATE', label: '更新用户' },
  { value: 'CONTENT_AUDIT_REVIEW', label: '内容审核' },
  { value: 'TAG_CATEGORY_CREATE', label: '创建标签分类' },
  { value: 'TAG_CREATE', label: '创建标签' },
  { value: 'TAG_UPDATE', label: '更新标签' },
  { value: 'TAG_DEACTIVATE', label: '停用标签' },
  { value: 'TAG_PREVIEW_CREATE', label: '上传预览图' },
  { value: 'TAG_PREVIEW_UPDATE', label: '更新预览图' },
  { value: 'TAG_PREVIEW_REPLACE', label: '替换预览图' },
  { value: 'TAG_PREVIEW_DELETE', label: '删除预览图' },
  { value: 'TAG_PREVIEW_REORDER', label: '调整预览图顺序' }
]

function actionLabel(action) {
  return actionOptions.find((item) => item.value === action)?.label || action || '未知操作'
}

function targetLabel(targetType) {
  return {
    USER: '用户',
    CONTENT_AUDIT: '内容审核',
    TAG_CATEGORY: '标签分类',
    TAG: '提示词标签',
    TAG_PREVIEW: '标签预览图'
  }[targetType] || targetType || '平台对象'
}
</script>
