<template>
  <section class="admin-section-view" v-loading="loading">
    <div class="admin-section-heading">
      <div>
        <h1>用户管理</h1>
        <p>查看账号状态、平台资产与角色权限。</p>
      </div>
      <span class="admin-count-label">共 {{ userPage.total || 0 }} 个账号</span>
    </div>

    <div class="admin-filter-bar">
      <el-input
        v-model="query.keyword"
        clearable
        :prefix-icon="Search"
        placeholder="搜索用户名或昵称"
        @keyup.enter="$emit('search')"
        @clear="$emit('search')"
      />
      <el-select v-model="query.role" clearable placeholder="全部角色" @change="$emit('search')">
        <el-option label="普通用户" value="USER" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="全部状态" @change="$emit('search')">
        <el-option label="正常" value="ACTIVE" />
        <el-option label="已停用" value="SUSPENDED" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="$emit('search')">查询</el-button>
    </div>

    <article class="admin-data-panel admin-users-table-panel">
      <el-table :data="userPage.items || []" class="admin-table" empty-text="没有符合条件的用户">
        <el-table-column label="用户" min-width="210" fixed="left">
          <template #default="scope">
            <div class="admin-user-cell">
              <el-avatar :src="scope.row.avatarUrl" :size="34">{{ userInitial(scope.row) }}</el-avatar>
              <span><strong>{{ scope.row.displayName }}</strong><small>@{{ scope.row.username }}</small></span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="110">
          <template #default="scope"><span class="admin-role-chip" :class="scope.row.role.toLowerCase()">{{ roleLabel(scope.row.role) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="scope"><span class="admin-status-chip" :class="scope.row.status.toLowerCase()">{{ userStatusLabel(scope.row.status) }}</span></template>
        </el-table-column>
        <el-table-column label="积分" width="100" align="right">
          <template #default="scope">{{ formatPoints(scope.row.pointBalance) }}</template>
        </el-table-column>
        <el-table-column label="平台资产" min-width="240">
          <template #default="scope">
            <div class="admin-asset-counts">
              <span>作品 <b>{{ scope.row.artworkCount }}</b></span>
              <span>生成 <b>{{ scope.row.generationCount }}</b></span>
              <span>风格 <b>{{ scope.row.stylePackageCount }}</b></span>
              <span>任务 <b>{{ scope.row.taskCount }}</b></span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="92" fixed="right" align="right">
          <template #default="scope">
            <el-tooltip content="编辑用户" placement="top">
              <el-button :icon="Edit" circle size="small" @click="openEditor(scope.row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination-row">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="userPage.page || 1"
          :page-size="userPage.size || 20"
          :total="userPage.total || 0"
          @current-change="$emit('page-change', $event)"
        />
      </div>
    </article>

    <el-drawer v-model="editorVisible" size="min(480px, 94vw)" class="admin-user-drawer" destroy-on-close>
      <template #header>
        <div class="admin-drawer-heading">
          <h2>编辑用户</h2>
          <span>@{{ activeUser?.username }}</span>
        </div>
      </template>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="显示名称"><el-input v-model="editorForm.displayName" maxlength="80" /></el-form-item>
        <el-form-item label="账号角色">
          <el-select v-model="editorForm.role" :disabled="isCurrentUser">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-radio-group v-model="editorForm.status" :disabled="isCurrentUser">
            <el-radio-button value="ACTIVE">正常</el-radio-button>
            <el-radio-button value="SUSPENDED">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <div class="admin-user-summary">
          <span><small>积分余额</small><strong>{{ formatPoints(activeUser?.pointBalance) }}</strong></span>
          <span><small>作品</small><strong>{{ activeUser?.artworkCount || 0 }}</strong></span>
          <span><small>生成</small><strong>{{ activeUser?.generationCount || 0 }}</strong></span>
        </div>
        <el-alert v-if="isCurrentUser" type="info" :closable="false" show-icon title="当前登录账号只能修改显示名称" />
      </el-form>
      <template #footer>
        <div class="admin-drawer-footer">
          <el-button @click="editorVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="saveUser">保存修改</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { Edit, Search } from '@element-plus/icons-vue'

const props = defineProps({
  currentUser: { type: Object, default: null },
  userPage: { type: Object, default: () => ({ items: [], page: 1, size: 20, total: 0, pages: 0 }) },
  query: { type: Object, required: true },
  loading: { type: Boolean, default: false },
  formatDate: { type: Function, required: true },
  formatPoints: { type: Function, required: true }
})

const emit = defineEmits(['search', 'page-change', 'save-user'])

const editorVisible = ref(false)
const activeUser = ref(null)
const editorForm = reactive({ displayName: '', role: 'USER', status: 'ACTIVE' })
const isCurrentUser = computed(() => activeUser.value?.id === props.currentUser?.id)

function openEditor(user) {
  activeUser.value = user
  Object.assign(editorForm, {
    displayName: user.displayName || '',
    role: user.role || 'USER',
    status: user.status || 'ACTIVE'
  })
  editorVisible.value = true
}

function saveUser() {
  if (!activeUser.value) return
  emit('save-user', {
    id: activeUser.value.id,
    payload: { ...editorForm },
    done: () => { editorVisible.value = false }
  })
}

function userInitial(user) {
  return (user.displayName || user.username || 'U').slice(0, 1).toUpperCase()
}

function roleLabel(role) {
  return role === 'ADMIN' ? '管理员' : '普通用户'
}

function userStatusLabel(status) {
  return status === 'ACTIVE' ? '正常' : '已停用'
}
</script>
