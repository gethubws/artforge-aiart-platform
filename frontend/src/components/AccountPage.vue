<template>
  <section class="account-shell">
    <header class="page-hero hub-hero">
      <div class="hero-copy">
        <p class="eyebrow">Account Hub</p>
        <h1>{{ currentUser?.displayName || '个人中心' }}</h1>
        <p class="page-subtitle">
          把通知、收藏、订阅、积分和投稿记录收进同一个工作台，协作消息也会在这里沉淀下来。
        </p>
      </div>
      <div class="hero-actions" v-if="currentUser">
        <el-button :icon="Refresh" :loading="pointLoading" @click="$emit('refresh-points')">刷新数据</el-button>
        <el-button type="primary" :loading="pointLoading" @click="$emit('claim-daily-points')">领取每日积分</el-button>
        <el-button :icon="SwitchButton" @click="$emit('logout')">退出登录</el-button>
      </div>
    </header>

    <section v-if="currentUser" class="account-overview-grid">
      <aside class="soft-panel account-profile-panel">
        <div class="account-profile-head">
          <div class="account-avatar">{{ userInitial }}</div>
          <div>
            <h2>{{ currentUser.displayName || currentUser.username }}</h2>
            <p>{{ currentUser.username }} · {{ roleText }}</p>
          </div>
        </div>

        <div class="account-stat-grid">
          <div class="account-stat-card">
            <span>可用积分</span>
            <strong>{{ formatPoints(pointAccount?.balance) }}</strong>
          </div>
          <div class="account-stat-card">
            <span>冻结积分</span>
            <strong>{{ formatPoints(pointAccount?.frozenBalance) }}</strong>
          </div>
          <div class="account-stat-card">
            <span>未读通知</span>
            <strong>{{ unreadNotificationCount }}</strong>
          </div>
          <div class="account-stat-card">
            <span>协作订阅</span>
            <strong>{{ subscriptionItems.length }}</strong>
          </div>
        </div>

        <div class="account-kpi-grid">
          <div class="mini-kpi">
            <span>我的任务</span>
            <strong>{{ myTasks.length }}</strong>
          </div>
          <div class="mini-kpi">
            <span>我的风格包</span>
            <strong>{{ myStylePackages.length }}</strong>
          </div>
          <div class="mini-kpi">
            <span>任务投稿</span>
            <strong>{{ myTaskSubmissions.length }}</strong>
          </div>
          <div class="mini-kpi">
            <span>风格投稿</span>
            <strong>{{ myStylePackageSubmissions.length }}</strong>
          </div>
        </div>
      </aside>

      <section class="soft-panel account-shortcuts-panel">
        <div class="section-title-row">
          <div>
            <p class="eyebrow">Quick Access</p>
            <h2>常用入口</h2>
          </div>
        </div>

        <div class="quick-link-grid">
          <button type="button" class="quick-link-card" @click="$emit('go-view', 'library')">
            <strong>作品库</strong>
            <span>继续整理自己的作品资产</span>
          </button>
          <button type="button" class="quick-link-card" @click="$emit('go-view', 'community')">
            <strong>作品广场</strong>
            <span>查看公开作品与热门风格</span>
          </button>
          <button type="button" class="quick-link-card" @click="$emit('go-view', 'my-tasks')">
            <strong>我的任务</strong>
            <span>管理你发布的任务与投稿审核</span>
          </button>
          <button type="button" class="quick-link-card" @click="$emit('go-view', 'my-styles')">
            <strong>我的风格包</strong>
            <span>维护风格包内容、协作与收录作品</span>
          </button>
          <button type="button" class="quick-link-card" @click="$emit('go-view', 'task-market')">
            <strong>任务市场</strong>
            <span>浏览任务并继续投稿</span>
          </button>
          <button type="button" class="quick-link-card" @click="$emit('go-view', 'style-market')">
            <strong>风格市场</strong>
            <span>查看风格成果集合与更新</span>
          </button>
          <button
            v-if="isAdmin"
            type="button"
            class="quick-link-card emphasis"
            @click="$emit('go-view', 'admin')"
          >
            <strong>后台审核</strong>
            <span>管理员入口与标签维护</span>
          </button>
        </div>
      </section>
    </section>

    <section v-if="currentUser" class="soft-panel account-tabs-panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="`通知 ${unreadNotificationCount ? `(${unreadNotificationCount})` : ''}`" name="notifications">
          <div class="tab-toolbar">
            <div>
              <h2>通知与协作</h2>
              <p>任务投稿、风格包协作、审核结果和订阅动态都会汇总到这里。</p>
            </div>
            <el-button
              :icon="Bell"
              :disabled="!unreadNotificationCount"
              @click="$emit('mark-all-notifications-read')"
            >
              全部已读
            </el-button>
          </div>

          <div class="notification-list">
            <article
              v-for="item in notifications"
              :key="item.id"
              class="notification-card"
              :class="{ unread: !item.read }"
            >
              <div class="notification-main">
                <div class="notification-title-row">
                  <strong>{{ item.title }}</strong>
                  <span class="notification-type">{{ notificationTypeLabel(item.type) }}</span>
                </div>
                <p>{{ item.content || '暂无更多说明。' }}</p>
                <div class="notification-meta">
                  <span>{{ formatDate(item.createdAt) }}</span>
                  <span v-if="item.read">已读</span>
                  <span v-else>未读</span>
                </div>
              </div>
              <div class="notification-actions">
                <el-button size="small" @click="$emit('open-target', item)">
                  前往相关内容
                </el-button>
                <el-button
                  v-if="!item.read"
                  size="small"
                  type="primary"
                  @click="$emit('mark-notification-read', item)"
                >
                  标记已读
                </el-button>
              </div>
            </article>

            <div v-if="!notifications.length" class="quiet-empty compact-empty">
              暂时还没有通知，后续任务投稿、风格协作和审核动态会出现在这里。
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`收藏 ${favoriteItems.length ? `(${favoriteItems.length})` : ''}`" name="favorites">
          <div class="tab-toolbar">
            <div>
              <h2>我的收藏</h2>
              <p>把值得回看的任务、风格包和作品先放进这里，后面继续细化筛选也很顺手。</p>
            </div>
          </div>

          <div class="engagement-grid">
            <article v-for="item in favoriteItems" :key="favoriteKey(item)" class="engagement-card">
              <img v-if="item.coverImageUrl" :src="item.coverImageUrl" :alt="item.title" class="engagement-cover" />
              <div v-else class="engagement-cover fallback">{{ targetTypeLabel(item.targetType) }}</div>
              <div class="engagement-copy">
                <div class="engagement-head">
                  <strong>{{ item.title }}</strong>
                  <span class="status-badge" :class="(item.status || '').toLowerCase()">{{ statusLabel(item.status) }}</span>
                </div>
                <p>{{ item.summary || '暂无简介。' }}</p>
                <div class="engagement-meta">
                  <span>{{ targetTypeLabel(item.targetType) }}</span>
                  <span>{{ item.ownerName || '未知作者' }}</span>
                  <span>{{ item.metaText || formatDate(item.savedAt) }}</span>
                </div>
              </div>
              <div class="engagement-actions">
                <el-button size="small" @click="$emit('open-target', item)">前往</el-button>
                <el-button size="small" @click="$emit('toggle-favorite', { type: item.targetType, target: item })">
                  取消收藏
                </el-button>
              </div>
            </article>

            <div v-if="!favoriteItems.length" class="quiet-empty compact-empty">
              你还没有收藏内容。后面在任务、风格包或作品详情里点一下就会出现在这里。
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`订阅 ${subscriptionItems.length ? `(${subscriptionItems.length})` : ''}`" name="subscriptions">
          <div class="tab-toolbar">
            <div>
              <h2>我的订阅</h2>
              <p>适合盯任务进展、风格包协作和版本更新。通知会自动回流到上面的通知中心。</p>
            </div>
          </div>

          <div class="engagement-grid">
            <article v-for="item in subscriptionItems" :key="favoriteKey(item)" class="engagement-card">
              <img v-if="item.coverImageUrl" :src="item.coverImageUrl" :alt="item.title" class="engagement-cover" />
              <div v-else class="engagement-cover fallback">{{ targetTypeLabel(item.targetType) }}</div>
              <div class="engagement-copy">
                <div class="engagement-head">
                  <strong>{{ item.title }}</strong>
                  <span class="status-badge" :class="(item.status || '').toLowerCase()">{{ statusLabel(item.status) }}</span>
                </div>
                <p>{{ item.summary || '暂无简介。' }}</p>
                <div class="engagement-meta">
                  <span>{{ targetTypeLabel(item.targetType) }}</span>
                  <span>{{ item.ownerName || '未知作者' }}</span>
                  <span>{{ item.metaText || formatDate(item.savedAt) }}</span>
                </div>
              </div>
              <div class="engagement-actions">
                <el-button size="small" @click="$emit('open-target', item)">前往</el-button>
                <el-button size="small" @click="$emit('toggle-subscription', { type: item.targetType, target: item })">
                  取消订阅
                </el-button>
              </div>
            </article>

            <div v-if="!subscriptionItems.length" class="quiet-empty compact-empty">
              你还没有订阅内容。建议先从任务和风格包开始。
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="投稿记录" name="activity">
          <div class="account-activity-grid">
            <section class="account-submission-panel">
              <div class="section-title-row lower">
                <h2>我的任务投稿</h2>
                <span class="pane-counter">{{ myTaskSubmissions.length }}</span>
              </div>
              <div class="submission-feed">
                <article
                  v-for="submission in myTaskSubmissions.slice(0, 6)"
                  :key="`task-${submission.id}`"
                  class="submission-feed-card"
                >
                  <img v-if="submission.artworkImageUrl" :src="submission.artworkImageUrl" :alt="submission.artworkTitle" />
                  <div>
                    <strong>{{ submission.artworkTitle }}</strong>
                    <span>{{ statusLabel(submission.status) }} · {{ formatDate(submission.createdAt) }}</span>
                    <p v-if="submission.reviewComment" class="submission-review-copy">
                      审核备注：{{ submission.reviewComment }}
                    </p>
                  </div>
                </article>
                <div v-if="!myTaskSubmissions.length" class="quiet-empty compact-empty">还没有任务投稿记录。</div>
              </div>
            </section>

            <section class="account-submission-panel">
              <div class="section-title-row lower">
                <h2>我的风格投稿</h2>
                <span class="pane-counter">{{ myStylePackageSubmissions.length }}</span>
              </div>
              <div class="submission-feed">
                <article
                  v-for="submission in myStylePackageSubmissions.slice(0, 6)"
                  :key="`style-${submission.id}`"
                  class="submission-feed-card"
                >
                  <img v-if="submission.artworkImageUrl" :src="submission.artworkImageUrl" :alt="submission.artworkTitle" />
                  <div>
                    <strong>{{ submission.artworkTitle }}</strong>
                    <span>{{ statusLabel(submission.status) }} · {{ formatDate(submission.createdAt) }}</span>
                    <p v-if="submission.reviewComment" class="submission-review-copy">
                      审核备注：{{ submission.reviewComment }}
                    </p>
                  </div>
                </article>
                <div v-if="!myStylePackageSubmissions.length" class="quiet-empty compact-empty">还没有风格投稿记录。</div>
              </div>
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="积分流水" name="points">
          <div class="account-points-layout">
            <div class="point-summary account-point-summary">
              <div>
                <span>可用积分</span>
                <strong>{{ formatPoints(pointAccount?.balance) }}</strong>
              </div>
              <div>
                <span>冻结积分</span>
                <strong>{{ formatPoints(pointAccount?.frozenBalance) }}</strong>
              </div>
            </div>

            <div class="transaction-list account-transaction-list">
              <div v-for="item in pointAccount?.transactions || []" :key="item.id" class="transaction-row">
                <div>
                  <span>{{ pointReasonLabel(item.reason) }}</span>
                  <small>{{ formatDate(item.createdAt) }}</small>
                </div>
                <strong :class="item.direction === 'OUT' ? 'out' : 'in'">
                  {{ formatSignedPoints(item.amount) }}
                </strong>
              </div>
              <div v-if="!(pointAccount?.transactions || []).length" class="quiet-empty compact-empty">
                暂无积分流水。
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <section v-else class="soft-panel account-empty-panel">
      <div class="account-avatar">{{ userInitial }}</div>
      <h2>请先登录</h2>
      <p>登录后就能查看通知、收藏、订阅、积分和个人资产入口。</p>
    </section>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Bell, Refresh, SwitchButton } from '@element-plus/icons-vue'

const props = defineProps({
  currentUser: { type: Object, default: null },
  userInitial: { type: String, default: 'A' },
  pointAccount: { type: Object, default: null },
  pointLoading: { type: Boolean, default: false },
  formatPoints: { type: Function, required: true },
  formatSignedPoints: { type: Function, required: true },
  pointReasonLabel: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  myTaskSubmissions: { type: Array, default: () => [] },
  myStylePackageSubmissions: { type: Array, default: () => [] },
  myTasks: { type: Array, default: () => [] },
  myStylePackages: { type: Array, default: () => [] },
  notifications: { type: Array, default: () => [] },
  unreadNotificationCount: { type: Number, default: 0 },
  favoriteItems: { type: Array, default: () => [] },
  subscriptionItems: { type: Array, default: () => [] }
})

defineEmits([
  'claim-daily-points',
  'refresh-points',
  'logout',
  'go-view',
  'mark-notification-read',
  'mark-all-notifications-read',
  'open-target',
  'toggle-favorite',
  'toggle-subscription'
])

const activeTab = ref('notifications')

const isAdmin = computed(() => props.currentUser?.role === 'ADMIN')
const roleText = computed(() => {
  const map = {
    ADMIN: '管理员',
    USER: '普通用户'
  }
  return map[props.currentUser?.role] || props.currentUser?.role || '未登录'
})

function favoriteKey(item) {
  return `${item.targetType}:${item.targetId}`
}

function targetTypeLabel(type) {
  const map = {
    ARTWORK: '作品',
    STYLE_PACKAGE: '风格包',
    TASK: '任务'
  }
  return map[type] || type || '内容'
}

function notificationTypeLabel(type) {
  const map = {
    TASK_UPDATED: '任务更新',
    TASK_PUBLISHED: '任务发布',
    TASK_CLOSED: '任务关闭',
    TASK_SUBMISSION_CREATED: '任务投稿',
    TASK_SUBMISSION_REVIEWED: '任务审核',
    STYLE_UPDATED: '风格更新',
    STYLE_PUBLISHED: '风格发布',
    STYLE_ARCHIVED: '风格归档',
    STYLE_COLLABORATOR_ADDED: '协作邀请',
    STYLE_REVIEW_CREATED: '风格评价',
    STYLE_SUBMISSION_CREATED: '风格投稿',
    STYLE_SUBMISSION_REVIEWED: '风格审核'
  }
  return map[type] || '平台通知'
}
</script>

<style scoped>
.account-shell {
  display: grid;
  gap: 20px;
}

.account-overview-grid {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 20px;
}

.account-profile-panel,
.account-shortcuts-panel,
.account-tabs-panel,
.account-empty-panel {
  display: grid;
  gap: 18px;
}

.account-profile-head {
  display: flex;
  align-items: center;
  gap: 16px;
}

.account-avatar {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.24), rgba(45, 212, 191, 0.26));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.55);
}

.account-stat-grid,
.account-kpi-grid,
.quick-link-grid,
.account-activity-grid,
.engagement-grid {
  display: grid;
  gap: 14px;
}

.account-stat-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.account-kpi-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.account-stat-card,
.mini-kpi,
.quick-link-card,
.engagement-card,
.notification-card {
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.78);
  border-radius: 18px;
}

.account-stat-card,
.mini-kpi {
  padding: 16px;
  display: grid;
  gap: 8px;
}

.account-stat-card span,
.mini-kpi span {
  color: #64748b;
  font-size: 13px;
}

.account-stat-card strong,
.mini-kpi strong {
  font-size: 22px;
  color: #0f172a;
}

.quick-link-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.quick-link-card {
  padding: 18px;
  text-align: left;
  display: grid;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.quick-link-card strong {
  font-size: 16px;
  color: #0f172a;
}

.quick-link-card span {
  color: #64748b;
  line-height: 1.6;
}

.quick-link-card:hover {
  transform: translateY(-1px);
  border-color: rgba(99, 102, 241, 0.32);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.08);
}

.quick-link-card.emphasis {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12), rgba(14, 165, 233, 0.1));
}

.tab-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.tab-toolbar h2 {
  margin: 0;
  font-size: 22px;
}

.tab-toolbar p {
  margin: 6px 0 0;
  color: #64748b;
}

.notification-list,
.submission-feed,
.transaction-list {
  display: grid;
  gap: 12px;
}

.notification-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  padding: 18px;
}

.notification-card.unread {
  border-color: rgba(99, 102, 241, 0.34);
  box-shadow: 0 12px 28px rgba(99, 102, 241, 0.08);
}

.notification-main,
.engagement-copy {
  display: grid;
  gap: 10px;
}

.notification-title-row,
.engagement-head,
.engagement-meta,
.notification-meta,
.engagement-actions,
.notification-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.notification-type,
.engagement-meta,
.notification-meta {
  color: #64748b;
  font-size: 13px;
}

.engagement-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.engagement-card {
  padding: 16px;
  display: grid;
  grid-template-columns: 108px minmax(0, 1fr);
  gap: 16px;
}

.engagement-cover {
  width: 108px;
  height: 108px;
  object-fit: cover;
  border-radius: 14px;
  background: rgba(148, 163, 184, 0.12);
}

.engagement-cover.fallback {
  display: grid;
  place-items: center;
  color: #475569;
  font-weight: 600;
}

.engagement-copy p,
.notification-main p {
  margin: 0;
  color: #334155;
  line-height: 1.65;
}

.submission-feed-card {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 14px;
  padding: 14px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.74);
}

.submission-feed-card img {
  width: 88px;
  height: 88px;
  object-fit: cover;
  border-radius: 12px;
}

.submission-feed-card strong {
  display: block;
  margin-bottom: 6px;
}

.submission-feed-card span,
.submission-review-copy {
  color: #64748b;
}

.transaction-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}

.transaction-row:last-child {
  border-bottom: none;
}

.transaction-row small {
  display: block;
  margin-top: 4px;
  color: #94a3b8;
}

.transaction-row .in {
  color: #059669;
}

.transaction-row .out {
  color: #dc2626;
}

.account-points-layout {
  display: grid;
  gap: 18px;
}

.account-point-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.account-point-summary div {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.account-point-summary span {
  display: block;
  color: #64748b;
  margin-bottom: 10px;
}

.account-point-summary strong {
  font-size: 24px;
}

.account-empty-panel {
  place-items: center;
  text-align: center;
  padding: 48px 24px;
}

@media (max-width: 1180px) {
  .account-overview-grid,
  .quick-link-grid,
  .engagement-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 820px) {
  .notification-card,
  .engagement-card,
  .submission-feed-card {
    grid-template-columns: 1fr;
  }

  .account-stat-grid,
  .account-kpi-grid,
  .account-point-summary {
    grid-template-columns: 1fr;
  }

  .tab-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
