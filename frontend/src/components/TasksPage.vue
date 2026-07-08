<template>
  <section class="tasks-view content-hub-page">
    <div class="page-heading hub-heading">
      <div>
        <p class="eyebrow">Task Hub</p>
        <h1>{{ currentView === 'market' ? '任务市场' : '我的任务' }}</h1>
        <p class="page-subtitle">
          先看任务卡片，再按需打开详情、投稿、审核或编辑面板，让主页面保持清爽。
        </p>
      </div>
      <div class="hub-header-actions">
        <el-button
          plain
          @click="$emit('go-related', currentView === 'market' ? 'my-tasks' : 'task-market')"
        >
          {{ currentView === 'market' ? '进入我的任务' : '返回任务市场' }}
        </el-button>
        <el-button :icon="Refresh" :loading="taskLoading" @click="$emit('refresh')">刷新</el-button>
        <el-button v-if="currentView === 'workspace'" type="primary" @click="openCreateDialog">
          发布任务
        </el-button>
      </div>
    </div>

    <section class="soft-panel hub-toolbar-panel clean-toolbar-panel">
      <div class="filter-row responsive-filter-row compact-market-filters">
        <el-input
          v-model="keyword"
          :prefix-icon="Search"
          placeholder="搜索任务标题、简介或要求"
          clearable
          class="page-search-input"
        />
        <el-select v-model="localStatus" clearable placeholder="状态" class="toolbar-select">
          <el-option label="进行中" value="PUBLISHED" />
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-select v-model="taskTier" clearable placeholder="等级" class="toolbar-select">
          <el-option label="高预算" value="premium" />
          <el-option label="重点征集" value="featured" />
          <el-option label="标准任务" value="standard" />
        </el-select>
        <el-select v-model="sortMode" placeholder="排序" class="toolbar-select">
          <el-option label="最新优先" value="latest" />
          <el-option label="预算优先" value="budget" />
          <el-option label="投稿数优先" value="submissions" />
          <el-option label="截止时间优先" value="deadline" />
        </el-select>
      </div>
      <div class="hub-summary-row compact-summary-row">
        <div>
          <h2>{{ currentView === 'market' ? '浏览任务广场' : '管理你的任务资产' }}</h2>
          <p>{{ summaryText }}</p>
        </div>
        <span class="pane-counter">{{ visibleTasks.length }}</span>
      </div>
    </section>

    <section class="hub-card-grid task-card-grid clean-task-grid">
      <article
        v-for="task in visibleTasks"
        :key="task.id"
        class="task-plaza-card"
        :class="[taskTone(task), { mine: currentView === 'workspace' }]"
        @click="openTaskDrawer(task)"
      >
        <div class="task-plaza-head">
          <span class="status-badge" :class="statusBadgeClass(task.status)">{{ statusText(task.status) }}</span>
          <span class="task-tier-chip">{{ taskToneLabel(task) }}</span>
        </div>
        <div class="task-plaza-main">
          <h3>{{ task.title }}</h3>
          <p>{{ task.description || task.requirementsText || '发布者暂未补充更多描述。' }}</p>
        </div>
        <div class="task-plaza-metrics">
          <span><strong>{{ formatPoints(task.budgetPoints) }}</strong><em>积分</em></span>
          <span><strong>{{ task.submissionCount || 0 }}</strong><em>投稿</em></span>
          <span><strong>{{ deadlineShort(task.deadline) }}</strong><em>截止</em></span>
        </div>
      </article>

      <div v-if="!visibleTasks.length" class="detail-empty-state hub-empty-state">
        <strong>{{ currentView === 'market' ? '没有匹配的任务' : '你还没有创建任务' }}</strong>
        <span>这里已经为搜索、筛选和详情面板预留好了空间，后面可以继续补充分类和市场规则。</span>
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
                  {{ statusText(activeTask.status) }}
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
                <strong>{{ statusText(activeTask.status) }}</strong>
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
            <div class="detail-actions" v-if="currentView === 'workspace'">
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
              <el-button type="primary" @click="openSubmitDialog(activeTask)">我要投稿</el-button>
            </div>
          </section>

          <section v-if="currentView === 'market'" class="soft-panel embedded-panel">
            <div class="section-title-row lower">
              <h2>我的投稿记录</h2>
            </div>
            <div class="submission-list compact-submission-list">
              <article
                v-for="submission in filteredMyTaskSubmissions"
                :key="submission.id"
                class="submission-row"
              >
                <img
                  v-if="submission.artworkImageUrl"
                  :src="submission.artworkImageUrl"
                  :alt="submission.artworkTitle"
                />
                <div>
                  <strong>{{ submission.artworkTitle }}</strong>
                  <span>{{ statusLabel(submission.status) }} / {{ formatPoints(submission.rewardPoints) }} pts</span>
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
          <img
            v-if="submission.artworkImageUrl"
            :src="submission.artworkImageUrl"
            :alt="submission.artworkTitle"
          />
          <div>
            <strong>{{ submission.artworkTitle }}</strong>
            <span>{{ statusLabel(submission.status) }} / {{ formatPoints(submission.rewardPoints) }} pts</span>
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
        <div v-if="!selectedTaskSubmissions.length" class="quiet-empty compact-empty">
          这个任务还没有投稿记录。
        </div>
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
import { Refresh, Search } from '@element-plus/icons-vue'

const props = defineProps({
  taskLoading: { type: Boolean, default: false },
  taskForm: { type: Object, required: true },
  taskMarket: { type: Array, default: () => [] },
  taskMarketStatusFilter: { type: String, default: 'PUBLISHED' },
  submissionForm: { type: Object, required: true },
  myArtworks: { type: Array, default: () => [] },
  currentUser: { type: Object, default: null },
  myTaskSubmissions: { type: Array, default: () => [] },
  myTasks: { type: Array, default: () => [] },
  myTaskStatusFilter: { type: String, default: '' },
  selectedTaskSubmissions: { type: Array, default: () => [] },
  taskReviewForm: { type: Object, required: true },
  formatPoints: { type: Function, required: true },
  formatDeadline: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
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
  'update:taskMarketStatusFilter',
  'prepare-submission',
  'patch-submission-form',
  'submit-task-work',
  'update:myTaskStatusFilter',
  'edit-task',
  'publish-task-item',
  'close-task-item',
  'load-task-submissions',
  'prepare-task-review',
  'review-submission',
  'patch-task-review-form'
])

const currentView = ref(props.initialView)
const keyword = ref('')
const localStatus = ref('')
const taskTier = ref('')
const sortMode = ref('latest')
const taskDrawerVisible = ref(false)
const taskEditorVisible = ref(false)
const taskSubmitVisible = ref(false)
const taskReviewVisible = ref(false)
const activeTaskId = ref(null)

const sourceTasks = computed(() => (currentView.value === 'market' ? props.taskMarket : props.myTasks))

const visibleTasks = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  const status = localStatus.value
  const tier = taskTier.value
  const list = sourceTasks.value.filter((task) => {
    const text = `${task.title || ''} ${task.description || ''} ${task.requirementsText || ''}`.toLowerCase()
    const matchesKeyword = !q || text.includes(q)
    const matchesStatus = !status || (task.status || '') === status
    const matchesTier = !tier || taskTone(task) === `task-tone-${tier}`
    return matchesKeyword && matchesStatus && matchesTier
  })

  return [...list].sort((a, b) => {
    if (sortMode.value === 'budget') return Number(b.budgetPoints || 0) - Number(a.budgetPoints || 0)
    if (sortMode.value === 'submissions') return Number(b.submissionCount || 0) - Number(a.submissionCount || 0)
    if (sortMode.value === 'deadline') {
      const aTime = a.deadline ? new Date(a.deadline).getTime() : Number.MAX_SAFE_INTEGER
      const bTime = b.deadline ? new Date(b.deadline).getTime() : Number.MAX_SAFE_INTEGER
      return aTime - bTime
    }
    const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0
    const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0
    return bTime - aTime
  })
})

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
  if (currentView.value === 'market') {
    return '把任务做成真正可浏览的内容广场，详情、投稿和审核都收进点击后的面板里。'
  }
  return '这里集中管理你发布过的任务，创建、编辑和投稿审核都改成按需打开，不再长期占满页面。'
})

const statusText = (status) => {
  const labels = {
    DRAFT: '草稿',
    PUBLISHED: '进行中',
    CLOSED: '已关闭',
    PENDING: '待处理',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return labels[status] || status || '未设置'
}

const statusBadgeClass = (status) => {
  if (status === 'PUBLISHED') return 'published'
  if (status === 'CLOSED') return 'archived'
  return (status || '').toLowerCase()
}

const taskTone = (task) => {
  const budget = Number(task?.budgetPoints || 0)
  if ((task?.status || '') === 'CLOSED') return 'task-tone-muted'
  if (budget >= 200) return 'task-tone-premium'
  if (budget >= 80) return 'task-tone-featured'
  return 'task-tone-standard'
}

const taskToneLabel = (task) => {
  const budget = Number(task?.budgetPoints || 0)
  if ((task?.status || '') === 'CLOSED') return '已结束'
  if (budget >= 200) return '高预算'
  if (budget >= 80) return '重点征集'
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
  if (currentView.value === 'workspace') {
    emit('load-task-submissions', task)
  }
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

const submitTaskEditor = async () => {
  await emit('save-task')
  taskEditorVisible.value = false
}

const openSubmitDialog = (task) => {
  activeTaskId.value = task.id
  emit('prepare-submission', task)
  taskSubmitVisible.value = true
}

const submitTaskWorkAndClose = async () => {
  await emit('submit-task-work')
  taskSubmitVisible.value = false
}

const openSubmissionReview = (task) => {
  activeTaskId.value = task.id
  emit('load-task-submissions', task)
  taskReviewVisible.value = true
}

watch(
  () => props.initialView,
  (value) => {
    currentView.value = value
  }
)

watch(currentView, (value) => {
  keyword.value = ''
  localStatus.value = ''
  taskTier.value = ''
  if (value === 'market') {
    emit('update:taskMarketStatusFilter', 'PUBLISHED')
  }
})
</script>
