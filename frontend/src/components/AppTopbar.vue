<template>
  <header class="topbar">
    <div class="brand-block">
      <div class="brand-mark">
        <BrushFilled />
      </div>
      <div>
        <div class="brand-name">ArtForge</div>
        <div class="brand-subtitle">AI 创作平台</div>
      </div>
    </div>

    <nav class="top-tabs" aria-label="Primary navigation">
      <button
        v-for="item in visibleTabs"
        :key="item.name"
        class="top-tab"
        :class="{ active: activeView === item.name }"
        type="button"
        @click="$emit('navigate', item.name)"
      >
        {{ item.label }}
      </button>
    </nav>

    <div class="top-actions">
      <el-tooltip content="刷新模型资源" placement="bottom">
        <el-button :icon="Connection" circle @click="$emit('refresh-provider')" />
      </el-tooltip>
      <el-tooltip content="刷新作品数据" placement="bottom">
        <el-button :icon="Refresh" circle @click="$emit('refresh-artworks')" />
      </el-tooltip>
      <el-tooltip content="通知中心" placement="bottom">
        <el-badge :value="unreadNotificationCount" :hidden="!unreadNotificationCount" :max="99">
          <el-button :icon="Bell" circle :disabled="!currentUser" @click="$emit('open-notifications')" />
        </el-badge>
      </el-tooltip>

      <el-dropdown trigger="click" placement="bottom-end">
        <button class="user-pill" :class="{ guest: !currentUser }" type="button">
          <span class="avatar-dot">{{ userInitial }}</span>
          <span>{{ currentUser?.displayName || currentUser?.username || '未登录' }}</span>
          <small v-if="currentUser">{{ roleLabel }}</small>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="$emit('navigate', 'account')">个人中心</el-dropdown-item>
            <el-dropdown-item @click="$emit('navigate', 'library')">作品库</el-dropdown-item>
            <el-dropdown-item @click="$emit('navigate', 'my-styles')">我的风格包</el-dropdown-item>
            <el-dropdown-item @click="$emit('navigate', 'my-tasks')">我的任务</el-dropdown-item>
            <el-dropdown-item @click="$emit('navigate', 'models')">模型资源</el-dropdown-item>
            <el-dropdown-item v-if="isAdmin" @click="$emit('navigate', 'admin')">后台审核</el-dropdown-item>
            <el-dropdown-item divided @click="$emit('logout')">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { Bell, BrushFilled, Connection, Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  activeView: { type: String, required: true },
  currentUser: { type: Object, default: null },
  unreadNotificationCount: { type: Number, default: 0 }
})

defineEmits(['navigate', 'refresh-provider', 'refresh-artworks', 'logout', 'open-notifications'])

const tabs = [
  { name: 'workbench', label: '工作台' },
  { name: 'community', label: '广场' },
  { name: 'style-market', label: '风格市场' },
  { name: 'task-market', label: '任务市场' },
  { name: 'models', label: '模型资源' }
]

const visibleTabs = computed(() => tabs)
const isAdmin = computed(() => props.currentUser?.role === 'ADMIN')
const roleLabel = computed(() => (props.currentUser?.role === 'ADMIN' ? '管理员' : '用户'))
const userInitial = computed(() => {
  const name = props.currentUser?.displayName || props.currentUser?.username || 'A'
  return name.slice(0, 1).toUpperCase()
})
</script>
