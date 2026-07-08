<template>
  <section class="account-view">
    <div class="account-card soft-panel">
      <div class="account-avatar">{{ userInitial }}</div>
      <h1>{{ currentUser?.displayName || '未登录' }}</h1>
      <p v-if="currentUser">{{ currentUser.username }} · {{ currentUser.role }}</p>
      <p v-else>请先登录后查看积分与账户信息。</p>

      <div v-if="currentUser" class="point-summary">
        <div>
          <span>可用积分</span>
          <strong>{{ formatPoints(pointAccount?.balance) }}</strong>
        </div>
        <div>
          <span>冻结积分</span>
          <strong>{{ formatPoints(pointAccount?.frozenBalance) }}</strong>
        </div>
      </div>

      <div v-if="currentUser" class="account-actions">
        <el-button type="primary" :loading="pointLoading" @click="$emit('claim-daily-points')">每日领取</el-button>
        <el-button :icon="Refresh" :loading="pointLoading" @click="$emit('refresh-points')">刷新积分</el-button>
      </div>

      <div v-if="currentUser" class="transaction-list">
        <div v-for="item in pointAccount?.transactions || []" :key="item.id" class="transaction-row">
          <span>{{ pointReasonLabel(item.reason) }}</span>
          <strong :class="item.direction === 'OUT' ? 'out' : 'in'">{{ formatSignedPoints(item.amount) }}</strong>
        </div>
        <div v-if="!(pointAccount?.transactions || []).length" class="quiet-empty compact-empty">暂无积分流水</div>
      </div>

      <el-button v-if="currentUser" :icon="SwitchButton" @click="$emit('logout')">退出登录</el-button>
    </div>
  </section>
</template>

<script setup>
import { Refresh, SwitchButton } from '@element-plus/icons-vue'

defineProps({
  currentUser: { type: Object, default: null },
  userInitial: { type: String, default: 'A' },
  pointAccount: { type: Object, default: null },
  pointLoading: { type: Boolean, default: false },
  formatPoints: { type: Function, required: true },
  formatSignedPoints: { type: Function, required: true },
  pointReasonLabel: { type: Function, required: true }
})

defineEmits(['claim-daily-points', 'refresh-points', 'logout'])
</script>
