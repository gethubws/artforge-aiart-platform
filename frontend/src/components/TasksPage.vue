<template>
  <section
    class="tasks-view content-hub-page market-page-shell density-page"
    :class="`density-${density}`"
    v-loading="taskLoading"
  >
    <header class="page-hero hub-hero">
      <div class="hero-copy">
        <p class="eyebrow">Task Hub</p>
        <h1>{{ isMarketView ? '任务市场' : '我的任务' }}</h1>
        <p class="page-subtitle">
          先浏览任务卡片，再按需打开详情、投稿、审核或编辑面板，让主页面保持清爽，
          也为后续搜索、分类和市场规则留出空间。
        </p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="$emit('go-related', isMarketView ? 'my-tasks' : 'task-market')">
          {{ isMarketView ? '进入我的任务' : '返回任务市场' }}
        </el-button>
        <el-button :icon="Refresh" :loading="taskLoading" @click="emitRefresh">
          刷新
        </el-button>
        <el-button v-if="!isMarketView" type="primary" @click="openCreateDialog">发布任务</el-button>
      </div>
    </header>

    <section class="soft-panel market-toolbar-panel">
      <div class="market-toolbar-grid task-toolbar-grid">
        <div class="toolbar-search-slot">
          <el-input
            v-model="keyword"
            :prefix-icon="Search"
            placeholder="搜索任务标题、简介或创作要求"
            clearable
            class="market-search-input"
          />
        </div>
        <div class="toolbar-filter-slot task-filter-slot">
          <el-select v-model="localStatus" clearable placeholder="状态" class="toolbar-select">
            <template v-if="isMarketView">
              <el-option label="进行中" value="PUBLISHED" />
              <el-option label="已关闭" value="CLOSED" />
            </template>
            <template v-else>
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="已关闭" value="CLOSED" />
            </template>
          </el-select>
          <el-select v-model="taskTier" clearable placeholder="等级" class="toolbar-select">
            <el-option label="高预算" value="premium" />
            <el-option label="重点征集" value="featured" />
            <el-option label="标准任务" value="standard" />
          </el-select>
          <el-select v-model="sortMode" placeholder="排序" class="toolbar-select">
            <el-option label="最近更新" value="latest" />
            <el-option label="预算优先" value="budget" />
            <el-option label="投稿数优先" value="submissions" />
            <el-option label="截止时间优先" value="deadline" />
          </el-select>
        </div>
      </div>

      <div class="hub-summary-row market-summary-row">
        <div>
          <h2>{{ isMarketView ? '浏览任务广场' : '管理你的任务资产' }}</h2>
          <p>{{ summaryText }}</p>
        </div>
        <div class="market-summary-tools">
          <span class="pane-counter">{{ pagerSummary }}</span>
          <div class="display-density-control">
            <span>展示密度</span>
            <el-segmented v-model="density" :options="densityOptions" size="small" />
          </div>
        </div>
      </div>
    </section>

    <section class="hub-card-grid task-card-grid clean-task-grid market-card-grid">
      <article
        v-for="task in visibleTasks"
        :key="task.id"
        class="task-plaza-card"
        :class="[`task-tone-${taskTone(task)}`, { mine: !isMarketView }]"
        @click="openTaskDrawer(task)"
      >
        <div class="task-plaza-head">
          <div class="task-head-badges">
            <span class="status-badge" :class="statusBadgeClass(task.status)">
              {{ taskStatusText(task.status) }}
            </span>
            <span class="task-tier-chip">{{ taskToneLabel(task) }}</span>
          </div>
          <div class="card-quick-actions">
            <button
              type="button"
              class="card-icon-action"
              :class="{ active: props.isFavoriteTarget?.('TASK', task) }"
              @click.stop="$emit('toggle-favorite-target', { type: 'TASK', target: task })"
            >
              <Star />
            </button>
            <button
              type="button"
              class="card-icon-action"
              :class="{ active: props.isSubscribedTarget?.('TASK', task) }"
              @click.stop="$emit('toggle-subscription-target', { type: 'TASK', target: task })"
            >
              <Bell />
            </button>
          </div>
        </div>

        <div class="task-plaza-main">
          <h3>{{ task.title }}</h3>
          <p>{{ task.description || task.requirementsText || '发布者暂未补充更多任务说明。' }}</p>
        </div>

        <div class="task-plaza-metrics">
          <span>
            <strong>{{ formatPoints(task.budgetPoints) }}</strong>
            <em>积分</em>
          </span>
          <span>
            <strong>{{ task.submissionCount || 0 }}</strong>
            <em>投稿</em>
          </span>
          <span>
            <strong>{{ deadlineShort(task.deadline) }}</strong>
            <em>截止</em>
          </span>
        </div>
      </article>

      <div v-if="!visibleTasks.length" class="detail-empty-state hub-empty-state">
        <strong>{{ isMarketView ? '没有匹配的任务' : '你还没有创建任务' }}</strong>
        <span>试试调整搜索条件，或者切换到上一页看看；这里已经为更细的市场规则留好了位置。</span>
      </div>
    </section>

    <section class="soft-panel pager-panel">
      <div class="pager-copy">
        <strong>{{ isMarketView ? '任务列表分页' : '任务资产分页' }}</strong>
        <span>共 {{ Number(queryState.total || 0) }} 个任务，当前第 {{ currentPage }} 页。</span>
      </div>
      <div class="pager-actions">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="Number(queryState.total || 0)"
          @current-change="changePage"
        />
      </div>
    </section>

    <el-drawer
      v-model="taskDrawerVisible"
      size="min(780px, 94vw)"
      class="hub-drawer"
      :title="activeTask ? activeTask.title : '任务详情'"
      destroy-on-close
    >
      <template v-if="activeTask">
        <div class="hub-drawer-body">
          <section class="soft-panel embedded-panel">
            <div class="drawer-topline">
              <div>
                <p class="eyebrow">Task Detail</p>
                <h2>{{ activeTask.title }}</h2>
              </div>
              <div class="drawer-top-badges">
                <span class="status-badge" :class="statusBadgeClass(activeTask.status)">
                  {{ taskStatusText(activeTask.status) }}
                </span>
                <span class="task-tier-chip strong">{{ taskToneLabel(activeTask) }}</span>
              </div>
            </div>

            <p class="detail-lead">{{ activeTask.description || '发布者暂未补充任务简介。' }}</p>

            <div class="task-meta-grid">
              <div>
                <span>预算积分</span>
                <strong>{{ formatPoints(activeTask.budgetPoints) }} pts</strong>
              </div>
              <div>
                <span>投稿数量</span>
                <strong>{{ activeTask.submissionCount || 0 }}</strong>
              </div>
              <div>
                <span>任务状态</span>
                <strong>{{ taskStatusText(activeTask.status) }}</strong>
              </div>
              <div>
                <span>截止时间</span>
                <strong>{{ formatDeadline(activeTask.deadline) }}</strong>
              </div>
            </div>

            <section class="drawer-copy-block">
              <h3>创作要求</h3>
              <p>{{ activeTask.requirementsText || '暂未填写更多创作要求。' }}</p>
            </section>

            <div class="detail-actions" v-if="!isMarketView">
              <el-button @click="$emit('toggle-favorite-target', { type: 'TASK', target: activeTask })">
                {{ props.isFavoriteTarget?.('TASK', activeTask) ? '取消收藏' : '收藏任务' }}
              </el-button>
              <el-button @click="$emit('toggle-subscription-target', { type: 'TASK', target: activeTask })">
                {{ props.isSubscribedTarget?.('TASK', activeTask) ? '取消订阅' : '订阅动态' }}
              </el-button>
              <el-button @click="openEditDialog(activeTask)">编辑任务</el-button>
              <el-button
                v-if="activeTask.status !== 'PUBLISHED'"
                type="primary"
                @click="$emit('publish-task-item', activeTask)"
              >
                发布任务
              </el-button>
              <el-button v-else @click="$emit('close-task-item', activeTask)">关闭任务</el-button>
              <el-button @click="openSubmissionReview(activeTask)">查看投稿</el-button>
            </div>
            <div class="detail-actions" v-else>
              <el-button @click="$emit('toggle-favorite-target', { type: 'TASK', target: activeTask })">
                {{ props.isFavoriteTarget?.('TASK', activeTask) ? '取消收藏' : '收藏任务' }}
              </el-button>
              <el-button @click="$emit('toggle-subscription-target', { type: 'TASK', target: activeTask })">
                {{ props.isSubscribedTarget?.('TASK', activeTask) ? '取消订阅' : '订阅动态' }}
              </el-button>
              <el-button type="primary" @click="openSubmitDialog(activeTask)">我要投稿</el-button>
            </div>
          </section>

          <section v-if="isMarketView" class="soft-panel embedded-panel">
            <div class="section-title-row lower">
              <h2>我的投稿记录</h2>
            </div>
            <div class="submission-list compact-submission-list">
              <article v-for="submission in filteredMyTaskSubmissions" :key="submission.id" class="submission-row">
                <img
                  v-if="submission.artworkImageUrl"
                  :src="submission.artworkImageUrl"
                  :alt="submission.artworkTitle"
                />
                <div>
                  <strong>{{ submission.artworkTitle }}</strong>
                  <span>{{ statusLabel(submission.status) }} / {{ formatPoints(submission.rewardPoints) }} pts</span>
                  <p v-if="submission.reviewComment" class="submission-review-copy">
                    审核备注：{{ submission.reviewComment }}
                  </p>
                </div>
              </article>
              <div v-if="!filteredMyTaskSubmissions.length" class="quiet-empty compact-empty">
                你还没有向这个任务投稿。
              </div>
            </div>
          </section>

          <section v-else class="soft-panel embedded-panel">
            <div class="section-title-row lower">
              <h2>投稿管理</h2>
              <el-button size="small" @click="openSubmissionReview(activeTask)">进入审核面板</el-button>
            </div>
            <div class="submission-list compact-submission-list">
              <article
                v-for="submission in selectedTaskSubmissions.slice(0, 4)"
                :key="submission.id"
                class="submission-row reviewable"
              >
                <img
                  v-if="submission.artworkImageUrl"
                  :src="submission.artworkImageUrl"
                  :alt="submission.artworkTitle"
                />
                <div>
                  <strong>{{ submission.artworkTitle }}</strong>
                  <span>{{ statusLabel(submission.status) }} / {{ formatPoints(submission.rewardPoints) }} pts</span>
                  <p v-if="submission.reviewComment" class="submission-review-copy">
                    审核备注：{{ submission.reviewComment }}
                  </p>
                </div>
              </article>
              <div v-if="!selectedTaskSubmissions.length" class="quiet-empty compact-empty">
                这个任务还没有收到投稿。
              </div>
            </div>
          </section>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="taskEditorVisible" width="min(760px, 94vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Task Editor</p>
          <h2>{{ taskForm.id ? '编辑任务' : '新建任务' }}</h2>
        </div>
      </template>

      <el-form label-position="top">
        <el-form-item label="任务标题">
          <el-input :model-value="taskForm.title" @update:model-value="$emit('patch-task-form', { title: $event })" />
        </el-form-item>
        <el-form-item label="任务概述">
          <el-input
            :model-value="taskForm.description"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4 }"
            resize="none"
            @update:model-value="$emit('patch-task-form', { description: $event })"
          />
        </el-form-item>
        <el-form-item label="创作要求">
          <el-input
            :model-value="taskForm.requirementsText"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 8 }"
            resize="none"
            @update:model-value="$emit('patch-task-form', { requirementsText: $event })"
          />
        </el-form-item>

        <div class="detail-tab-grid">
          <el-form-item label="预算积分">
            <el-input-number
              :model-value="taskForm.budgetPoints"
              :min="0"
              :step="5"
              controls-position="right"
              @update:model-value="$emit('patch-task-form', { budgetPoints: $event })"
            />
          </el-form-item>
          <el-form-item label="截止时间">
            <el-date-picker
              :model-value="taskForm.deadline"
              type="datetime"
              placeholder="可不设置"
              value-format="YYYY-MM-DDTHH:mm:ss"
              @update:model-value="$emit('patch-task-form', { deadline: $event })"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="detail-actions">
          <el-button @click="taskEditorVisible = false">取消</el-button>
          <el-button v-if="taskForm.id" @click="$emit('reset-task-form')">重置</el-button>
          <el-button type="primary" :loading="taskLoading" @click="submitTaskEditor">保存任务</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="taskSubmitVisible" width="min(620px, 94vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Task Submission</p>
          <h2>{{ activeTask ? `向 ${activeTask.title} 投稿` : '提交作品' }}</h2>
        </div>
      </template>

      <el-form label-position="top">
        <el-form-item label="选择作品">
          <el-select
            :model-value="submissionForm.artworkId"
            filterable
            placeholder="从作品库中选择一个作品"
            @update:model-value="$emit('patch-submission-form', { artworkId: $event, taskId: activeTask?.id })"
          >
            <el-option v-for="artwork in myArtworks" :key="artwork.id" :label="artwork.title" :value="artwork.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="投稿说明">
          <el-input
            :model-value="submissionForm.note"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 5 }"
            resize="none"
            @update:model-value="$emit('patch-submission-form', { note: $event, taskId: activeTask?.id })"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="detail-actions">
          <el-button @click="taskSubmitVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!currentUser" :loading="taskLoading" @click="submitTaskWorkAndClose">
            提交作品
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="taskReviewVisible" width="min(820px, 96vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Task Review</p>
          <h2>{{ activeTask ? `审核 ${activeTask.title} 的投稿` : '投稿审核' }}</h2>
        </div>
      </template>

      <div class="submission-list compact-submission-list">
        <article
          v-for="submission in selectedTaskSubmissions"
          :key="submission.id"
          class="submission-row reviewable submission-review-card"
        >
          <img v-if="submission.artworkImageUrl" :src="submission.artworkImageUrl" :alt="submission.artworkTitle" />
          <div>
            <strong>{{ submission.artworkTitle }}</strong>
            <span>{{ statusLabel(submission.status) }} / {{ formatPoints(submission.rewardPoints) }} pts</span>
            <p v-if="submission.note" class="submission-note-copy">{{ submission.note }}</p>
            <p v-if="submission.reviewComment" class="submission-review-copy">审核备注：{{ submission.reviewComment }}</p>
          </div>
          <div v-if="submission.status === 'PENDING'" class="submission-actions">
            <el-button size="small" @click="$emit('prepare-task-review', submission)">填写审核</el-button>
            <el-button size="small" type="primary" @click="$emit('review-submission', { submission, status: 'APPROVED' })">
              通过
            </el-button>
            <el-button size="small" @click="$emit('review-submission', { submission, status: 'REJECTED' })">
              拒绝
            </el-button>
          </div>
        </article>
        <div v-if="!selectedTaskSubmissions.length" class="quiet-empty compact-empty">这个任务还没有投稿记录。</div>
      </div>

      <section v-if="taskReviewForm.submissionId" class="soft-panel embedded-panel inline-review-form review-config-panel">
        <div class="section-title-row lower">
          <h2>审核设置</h2>
        </div>
        <el-form label-position="top">
          <el-form-item label="奖励积分">
            <el-input-number
              :model-value="taskReviewForm.rewardPoints"
              :min="0"
              :step="5"
              controls-position="right"
              @update:model-value="$emit('patch-task-review-form', { rewardPoints: $event })"
            />
          </el-form-item>
          <el-form-item label="审核反馈">
            <el-input
              :model-value="taskReviewForm.comment"
              type="textarea"
              :autosize="{ minRows: 2, maxRows: 4 }"
              resize="none"
              @update:model-value="$emit('patch-task-review-form', { comment: $event })"
            />
          </el-form-item>
        </el-form>
      </section>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { Bell, Refresh, Search, Star } from '@element-plus/icons-vue'
import { useDisplayDensity } from '../composables/useDisplayDensity'

const props = defineProps({
  taskLoading: { type: Boolean, default: false },
  taskForm: { type: Object, required: true },
  taskMarket: { type: Array, default: () => [] },
  queryState: { type: Object, required: true },
  submissionForm: { type: Object, required: true },
  myArtworks: { type: Array, default: () => [] },
  currentUser: { type: Object, default: null },
  myTaskSubmissions: { type: Array, default: () => [] },
  myTasks: { type: Array, default: () => [] },
  selectedTaskSubmissions: { type: Array, default: () => [] },
  taskReviewForm: { type: Object, required: true },
  formatPoints: { type: Function, required: true },
  formatDeadline: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  isFavoriteTarget: { type: Function, default: null },
  isSubscribedTarget: { type: Function, default: null },
  focusTargetId: { type: Number, default: null },
  focusTargetStamp: { type: Number, default: 0 },
  initialView: { type: String, default: 'market' },
  allowMarket: { type: Boolean, default: true },
  allowWorkspace: { type: Boolean, default: true }
})

const emit = defineEmits([
  'go-related',
  'refresh',
  'reset-task-form',
  'patch-task-form',
  'save-task',
  'prepare-submission',
  'patch-submission-form',
  'submit-task-work',
  'edit-task',
  'publish-task-item',
  'close-task-item',
  'load-task-submissions',
  'prepare-task-review',
  'review-submission',
  'patch-task-review-form',
  'toggle-favorite-target',
  'toggle-subscription-target'
])

const currentView = ref(props.initialView)
const keyword = ref(props.queryState.keyword || '')
const localStatus = ref(props.queryState.status || '')
const taskTier = ref(props.queryState.tier || '')
const sortMode = ref(props.queryState.sort || 'latest')
const currentPage = ref(props.queryState.page || 1)
const pageSize = ref(props.queryState.size || 12)
const { density, densityOptions } = useDisplayDensity()
const taskDrawerVisible = ref(false)
const taskEditorVisible = ref(false)
const taskSubmitVisible = ref(false)
const taskReviewVisible = ref(false)
const activeTaskId = ref(null)
let queryTimer = null
let lastEmittedQueryKey = ''

const isMarketView = computed(() => currentView.value === 'market')
const sourceTasks = computed(() => (isMarketView.value ? props.taskMarket : props.myTasks))
const visibleTasks = computed(() => sourceTasks.value || [])

const activeTask = computed(() => {
  return visibleTasks.value.find((task) => task.id === activeTaskId.value)
    || sourceTasks.value.find((task) => task.id === activeTaskId.value)
    || null
})

const filteredMyTaskSubmissions = computed(() => {
  if (!activeTask.value) return []
  return props.myTaskSubmissions.filter((submission) => submission.taskId === activeTask.value.id)
})

const summaryText = computed(() => {
  return isMarketView.value
    ? '把任务做成真正可浏览的内容广场，详情、投稿和动作都放进点击后的面板里。'
    : '在这里集中管理你发布过的任务，创建、编辑和投稿审核都改成按需打开。'
})

const pagerSummary = computed(() => `${Number(props.queryState.total || 0)} 项 / 第 ${currentPage.value} 页`)

const currentQuery = () => ({
  keyword: keyword.value,
  status: localStatus.value,
  tier: taskTier.value,
  sort: sortMode.value,
  page: currentPage.value,
  size: pageSize.value
})

const queryKey = (query) => JSON.stringify({
  keyword: query?.keyword || '',
  status: query?.status || '',
  tier: query?.tier || '',
  sort: query?.sort || 'latest',
  page: Number(query?.page) || 1,
  size: Number(query?.size) || 12
})

const emitRefresh = () => {
  const query = currentQuery()
  lastEmittedQueryKey = queryKey(query)
  emit('refresh', query)
}

const taskStatusText = (status) => {
  const labels = {
    DRAFT: '草稿',
    PUBLISHED: isMarketView.value ? '进行中' : '已发布',
    CLOSED: '已关闭'
  }
  return labels[status] || props.statusLabel(status)
}

const statusBadgeClass = (status) => {
  if (status === 'PUBLISHED') return 'published'
  if (status === 'CLOSED') return 'archived'
  return (status || '').toLowerCase()
}

const taskTone = (task) => {
  if ((task?.status || '') === 'CLOSED') return 'muted'
  const budget = Number(task?.budgetPoints || 0)
  if (budget >= 200) return 'premium'
  if (budget >= 80) return 'featured'
  return 'standard'
}

const taskToneLabel = (task) => {
  const tone = taskTone(task)
  if (tone === 'muted') return '已结束'
  if (tone === 'premium') return '高预算'
  if (tone === 'featured') return '重点征集'
  return '标准任务'
}

const deadlineShort = (value) => {
  if (!value) return '长期'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '长期'
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const openTaskDrawer = (task) => {
  activeTaskId.value = task.id
  taskDrawerVisible.value = true
  if (!isMarketView.value) emit('load-task-submissions', task)
}

const openCreateDialog = () => {
  activeTaskId.value = null
  emit('reset-task-form')
  taskEditorVisible.value = true
}

const openEditDialog = (task) => {
  emit('edit-task', task)
  taskEditorVisible.value = true
}

const submitTaskEditor = () => {
  emit('save-task')
  taskEditorVisible.value = false
}

const openSubmitDialog = (task) => {
  activeTaskId.value = task.id
  emit('prepare-submission', task)
  taskSubmitVisible.value = true
}

const submitTaskWorkAndClose = () => {
  emit('submit-task-work')
  taskSubmitVisible.value = false
}

const openSubmissionReview = (task) => {
  activeTaskId.value = task.id
  emit('load-task-submissions', task)
  taskReviewVisible.value = true
}

const changePage = (value) => {
  currentPage.value = Math.max(1, Number(value) || 1)
  emitRefresh()
}

watch(
  () => props.initialView,
  (value) => {
    currentView.value = value
  }
)

watch(
  () => props.queryState,
  (value) => {
    keyword.value = value?.keyword || ''
    localStatus.value = value?.status || ''
    taskTier.value = value?.tier || ''
    sortMode.value = value?.sort || 'latest'
    currentPage.value = value?.page || 1
    pageSize.value = value?.size || 12
    lastEmittedQueryKey = queryKey(value || {})
  },
  { deep: true }
)

watch([keyword, localStatus, taskTier, sortMode], () => {
  window.clearTimeout(queryTimer)
  queryTimer = window.setTimeout(() => {
    currentPage.value = 1
    const query = currentQuery()
    if (queryKey(query) === lastEmittedQueryKey) return
    emitRefresh()
  }, 260)
})

watch(
  () => [props.focusTargetId, props.focusTargetStamp, sourceTasks.value.length],
  () => {
    if (!props.focusTargetId) return
    const matched = sourceTasks.value.find((task) => task.id === props.focusTargetId)
    if (!matched) return
    openTaskDrawer(matched)
  },
  { immediate: true }
)
</script>

<style scoped>
.task-plaza-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.task-head-badges,
.card-quick-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-icon-action {
  width: 34px;
  height: 34px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.78);
  color: #64748b;
  cursor: pointer;
  transition: color 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.card-icon-action.active {
  color: #0f766e;
  border-color: rgba(16, 185, 129, 0.26);
  background: rgba(236, 253, 245, 0.95);
}
</style>
