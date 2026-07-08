<template>
  <section class="style-view content-hub-page market-page-shell">
    <header class="page-hero hub-hero">
      <div class="hero-copy">
        <p class="eyebrow">Style Packages</p>
        <h1>{{ currentView === 'market' ? '风格市场' : '我的风格包' }}</h1>
        <p class="page-subtitle">
          先把风格包做成内容浏览页，让封面、版本、评价和投稿都沿着同一条浏览路径展开。
        </p>
      </div>
      <div class="hero-actions">
        <el-button
          plain
          @click="$emit('go-related', currentView === 'market' ? 'my-styles' : 'style-market')"
        >
          {{ currentView === 'market' ? '进入我的风格包' : '返回风格市场' }}
        </el-button>
        <el-button :icon="Refresh" :loading="styleLoading" @click="$emit('refresh')">刷新</el-button>
        <el-button v-if="currentView === 'workspace'" type="primary" @click="openCreateDialog">
          新建风格包
        </el-button>
      </div>
    </header>

    <section class="soft-panel market-toolbar-panel">
      <div class="market-toolbar-grid style-toolbar-grid">
        <div class="toolbar-search-slot">
          <el-input
            v-model="keyword"
            :prefix-icon="Search"
            placeholder="搜索名称、描述或提示词模板"
            clearable
            class="market-search-input"
          />
        </div>
        <div class="toolbar-filter-slot">
          <el-select v-model="priceBand" clearable placeholder="价格区间" class="toolbar-select">
            <el-option label="免费或低价" value="low" />
            <el-option label="中等价位" value="mid" />
            <el-option label="高价" value="high" />
          </el-select>
          <el-select v-model="localStatus" clearable placeholder="状态" class="toolbar-select">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
          <el-select v-model="sortMode" placeholder="排序" class="toolbar-select">
            <el-option label="最新优先" value="latest" />
            <el-option label="评分优先" value="rating" />
            <el-option label="兑换量优先" value="access" />
            <el-option label="价格优先" value="price" />
          </el-select>
        </div>
      </div>
      <div class="hub-summary-row market-summary-row">
        <div>
          <h2>{{ currentView === 'market' ? '浏览风格市场' : '管理你的风格资产' }}</h2>
          <p>{{ summaryText }}</p>
        </div>
        <span class="pane-counter">{{ visiblePacks.length }}</span>
      </div>
    </section>

    <section class="hub-card-grid style-plaza-grid clean-style-grid market-card-grid">
      <article
        v-for="pack in visiblePacks"
        :key="pack.id"
        class="style-plaza-card clean-style-card"
        @click="openPackDrawer(pack)"
      >
        <div class="style-plaza-cover-wrap">
          <img v-if="pack.coverImageUrl" :src="pack.coverImageUrl" :alt="pack.name" class="style-plaza-cover" />
          <div v-else class="style-plaza-cover fallback">
            <Collection />
          </div>
          <span class="status-badge floating" :class="statusBadgeClass(pack.status)">{{ statusText(pack.status) }}</span>
        </div>
        <div class="style-plaza-body">
          <div class="style-plaza-title-row">
            <h3>{{ pack.name }}</h3>
            <span class="style-price">{{ Number(pack.pricePoints || 0) }} pts</span>
          </div>
          <p>{{ pack.description || pack.promptTemplate || '暂未补充简介。' }}</p>
          <div class="style-stat-strip large">
            <span v-for="item in styleStatsItems(pack)" :key="item.label"><strong>{{ item.value }}</strong>{{ item.label }}</span>
          </div>
        </div>
      </article>

      <div v-if="!visiblePacks.length" class="detail-empty-state hub-empty-state">
        <strong>{{ currentView === 'market' ? '没有匹配的风格包' : '你还没有创建风格包' }}</strong>
        <span>这里已经给搜索、筛选和详情扩展留出了位置，后面可以继续补更细的分类和后端查询。</span>
      </div>
    </section>

    <el-drawer
      v-model="packDrawerVisible"
      size="min(860px, 95vw)"
      class="hub-drawer"
      :title="activePack ? activePack.name : '风格包详情'"
      destroy-on-close
    >
      <template v-if="activePack">
        <div class="hub-drawer-body">
          <section class="soft-panel embedded-panel">
            <div class="drawer-topline">
              <div>
                <p class="eyebrow">Pack Detail</p>
                <h2>{{ activePack.name }}</h2>
              </div>
              <div class="drawer-top-badges">
                <span class="status-badge" :class="statusBadgeClass(activePack.status)">{{ statusText(activePack.status) }}</span>
                <span class="style-price prominent">{{ Number(activePack.pricePoints || 0) }} pts</span>
              </div>
            </div>
            <div class="style-drawer-hero">
              <img v-if="activePack.coverImageUrl" :src="activePack.coverImageUrl" :alt="activePack.name" class="detail-cover" />
              <div class="style-drawer-copy">
                <p class="detail-lead">{{ activePack.description || '这个风格包暂时还没有补充详细介绍。' }}</p>
                <div class="style-stat-strip large compact-gap">
                  <span v-for="item in styleStatsItems(activePack)" :key="item.label"><strong>{{ item.value }}</strong>{{ item.label }}</span>
                </div>
              </div>
            </div>
            <div class="detail-actions">
              <template v-if="currentView === 'market'">
                <el-button v-if="activePack.accessible" type="primary" @click="$emit('apply-pack', activePack)">
                  应用到工作台
                </el-button>
                <el-button v-else type="primary" :loading="styleLoading" @click="$emit('exchange-pack', activePack)">
                  兑换风格包
                </el-button>
                <el-button v-if="activePack.accessible" @click="openSubmissionDialog(activePack)">投稿作品</el-button>
                <el-button v-if="activePack.accessible && !activePack.owner" @click="openReviewDialog(activePack)">写评价</el-button>
              </template>
              <template v-else>
                <el-button @click="openEditDialog(activePack)">编辑风格包</el-button>
                <el-button v-if="activePack.status !== 'PUBLISHED'" type="primary" @click="$emit('publish-pack', activePack)">
                  发布
                </el-button>
                <el-button v-else @click="$emit('archive-pack', activePack)">下架</el-button>
                <el-button @click="openOwnerOps(activePack)">查看投稿</el-button>
              </template>
            </div>
            <section class="drawer-copy-block">
              <h3>正向模板</h3>
              <p>{{ activePack.promptTemplate || '暂未设置正向模板。' }}</p>
            </section>
            <section v-if="activePack.negativePromptTemplate" class="drawer-copy-block">
              <h3>反向模板</h3>
              <p>{{ activePack.negativePromptTemplate }}</p>
            </section>
          </section>

          <div class="detail-tab-grid">
            <section class="soft-panel embedded-panel">
              <div class="section-title-row lower">
                <h2>版本记录</h2>
                <el-button size="small" @click="reloadPackDetail(activePack)">刷新</el-button>
              </div>
              <div class="submission-list review-list compact-submission-list">
                <article v-for="version in stylePackageVersions" :key="version.id" class="style-review-row">
                  <div class="version-head">
                    <strong>v{{ version.versionNumber }} / {{ version.name }}</strong>
                    <span>{{ formatDate(version.createdAt) }}</span>
                  </div>
                  <p>{{ version.description || version.promptTemplate || '这个版本主要用于保留模板快照。' }}</p>
                  <span>{{ version.changeNote || '暂无更新说明' }}</span>
                </article>
                <div v-if="!stylePackageVersions.length" class="quiet-empty compact-empty">还没有版本记录。</div>
              </div>
            </section>

            <section class="soft-panel embedded-panel">
              <div class="section-title-row lower">
                <h2>评价列表</h2>
                <el-button size="small" @click="openReviewDialog(activePack)">我要评价</el-button>
              </div>
              <div class="submission-list review-list compact-submission-list">
                <article v-for="review in stylePackageReviews" :key="review.id" class="style-review-row">
                  <div>
                    <el-rate :model-value="review.rating" disabled size="small" />
                    <span>{{ formatDate(review.updatedAt || review.createdAt) }}</span>
                  </div>
                  <p>{{ review.comment || '这条评价没有文字内容。' }}</p>
                </article>
                <div v-if="!stylePackageReviews.length" class="quiet-empty compact-empty">还没有评价。</div>
              </div>
            </section>
          </div>

          <section v-if="currentView === 'workspace'" class="soft-panel embedded-panel">
            <div class="section-title-row lower">
              <h2>收到的投稿</h2>
              <el-button size="small" @click="openOwnerOps(activePack)">进入审核面板</el-button>
            </div>
            <div class="submission-list compact-submission-list">
              <article v-for="submission in styleSubmissions.slice(0, 4)" :key="submission.id" class="submission-row reviewable">
                <img v-if="submission.artworkImageUrl" :src="submission.artworkImageUrl" :alt="submission.artworkTitle" />
                <div>
                  <strong>{{ submission.artworkTitle }}</strong>
                  <span>{{ statusLabel(submission.status) }}</span>
                </div>
              </article>
              <div v-if="!styleSubmissions.length" class="quiet-empty compact-empty">这个风格包还没有收到投稿。</div>
            </div>
          </section>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="packEditorVisible" width="min(780px, 95vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Style Editor</p>
          <h2>{{ styleForm.id ? '编辑风格包' : '新建风格包' }}</h2>
        </div>
      </template>
      <el-form label-position="top">
        <el-form-item label="风格包名称">
          <el-input :model-value="styleForm.name" placeholder="例如：电影感人像包" @update:model-value="$emit('patch-style-form', { name: $event })" />
        </el-form-item>
        <el-form-item label="封面图 URL">
          <el-input :model-value="styleForm.coverImageUrl" placeholder="可以填写上传地址或外部图片链接" @update:model-value="$emit('patch-style-form', { coverImageUrl: $event })" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input :model-value="styleForm.description" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" @update:model-value="$emit('patch-style-form', { description: $event })" />
        </el-form-item>
        <el-form-item label="正向模板">
          <el-input :model-value="styleForm.promptTemplate" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" resize="none" @update:model-value="$emit('patch-style-form', { promptTemplate: $event })" />
        </el-form-item>
        <el-form-item label="反向模板">
          <el-input :model-value="styleForm.negativePromptTemplate" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" @update:model-value="$emit('patch-style-form', { negativePromptTemplate: $event })" />
        </el-form-item>
        <el-form-item label="兑换价格">
          <el-input-number :model-value="styleForm.pricePoints" :min="0" :step="1" controls-position="right" @update:model-value="$emit('patch-style-form', { pricePoints: $event })" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="detail-actions">
          <el-button @click="packEditorVisible = false">取消</el-button>
          <el-button v-if="styleForm.id" @click="$emit('reset-form')">重置</el-button>
          <el-button type="primary" :loading="styleLoading" @click="submitPackEditor">保存风格包</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="packSubmissionVisible" width="min(620px, 94vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Pack Submission</p>
          <h2>{{ activePack ? `向 ${activePack.name} 投稿作品` : '投稿作品' }}</h2>
        </div>
      </template>
      <div class="soft-panel embedded-panel submission-helper-panel">
        <p>这里先保留风格包投稿入口，后面可以继续补作品预览、选择器和投稿历史。</p>
      </div>
      <template #footer>
        <div class="detail-actions">
          <el-button @click="packSubmissionVisible = false">关闭</el-button>
          <el-button type="primary" @click="$emit('prepare-submission', activePack)">继续投稿流程</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="packReviewVisible" width="min(640px, 94vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Pack Review</p>
          <h2>{{ activePack ? `评价 ${activePack.name}` : '撰写评价' }}</h2>
        </div>
      </template>
      <el-form label-position="top">
        <el-form-item label="评分">
          <el-rate :model-value="styleReviewForm.rating" @update:model-value="$emit('patch-review-form', { rating: $event })" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input :model-value="styleReviewForm.comment" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" resize="none" @update:model-value="$emit('patch-review-form', { comment: $event })" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="detail-actions">
          <el-button @click="packReviewVisible = false">取消</el-button>
          <el-button type="primary" :loading="styleLoading" @click="submitReviewAndClose">提交评价</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="ownerOpsVisible" width="min(840px, 96vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Owner Review</p>
          <h2>{{ activePack ? `审核 ${activePack.name} 的投稿` : '投稿审核' }}</h2>
        </div>
      </template>
      <div class="submission-list compact-submission-list">
        <article v-for="submission in styleSubmissions" :key="submission.id" class="submission-row reviewable submission-review-card">
          <img v-if="submission.artworkImageUrl" :src="submission.artworkImageUrl" :alt="submission.artworkTitle" />
          <div>
            <strong>{{ submission.artworkTitle }}</strong>
            <span>{{ statusLabel(submission.status) }}</span>
          </div>
          <div v-if="submission.status === 'PENDING'" class="submission-actions">
            <el-button size="small" type="primary" @click="$emit('review-submission', { submission, status: 'APPROVED' })">通过</el-button>
            <el-button size="small" @click="$emit('review-submission', { submission, status: 'REJECTED' })">拒绝</el-button>
          </div>
        </article>
        <div v-if="!styleSubmissions.length" class="quiet-empty compact-empty">这个风格包还没有投稿记录。</div>
      </div>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { Collection, Refresh, Search } from '@element-plus/icons-vue'

const props = defineProps({
  styleForm: { type: Object, required: true },
  styleLoading: { type: Boolean, default: false },
  myStylePackages: { type: Array, default: () => [] },
  marketStylePackages: { type: Array, default: () => [] },
  stylePackageVersions: { type: Array, default: () => [] },
  stylePackageReviews: { type: Array, default: () => [] },
  styleSubmissions: { type: Array, default: () => [] },
  activeStylePackageName: { type: String, default: '' },
  styleReviewForm: { type: Object, required: true },
  styleStatsItems: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  initialView: { type: String, default: 'market' },
  allowMarket: { type: Boolean, default: true },
  allowWorkspace: { type: Boolean, default: true }
})

const emit = defineEmits([
  'go-related',
  'refresh',
  'reset-form',
  'patch-style-form',
  'save',
  'edit-pack',
  'apply-pack',
  'publish-pack',
  'archive-pack',
  'load-versions',
  'load-submissions',
  'load-artworks',
  'exchange-pack',
  'prepare-submission',
  'prepare-review',
  'load-reviews',
  'patch-review-form',
  'submit-review',
  'review-submission'
])

const currentView = ref(props.initialView)
const keyword = ref('')
const priceBand = ref('')
const localStatus = ref('')
const sortMode = ref('latest')
const activePackId = ref(null)
const packDrawerVisible = ref(false)
const packEditorVisible = ref(false)
const packSubmissionVisible = ref(false)
const packReviewVisible = ref(false)
const ownerOpsVisible = ref(false)

const sourcePacks = computed(() => (currentView.value === 'market' ? props.marketStylePackages : props.myStylePackages))

const visiblePacks = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  const list = sourcePacks.value.filter((pack) => {
    const text = `${pack.name || ''} ${pack.description || ''} ${pack.promptTemplate || ''}`.toLowerCase()
    const matchesKeyword = !q || text.includes(q)
    const matchesStatus = !localStatus.value || (pack.status || '') === localStatus.value
    const price = Number(pack.pricePoints || 0)
    const matchesPrice = !priceBand.value
      || (priceBand.value === 'low' && price <= 30)
      || (priceBand.value === 'mid' && price > 30 && price <= 120)
      || (priceBand.value === 'high' && price > 120)
    return matchesKeyword && matchesStatus && matchesPrice
  })

  return [...list].sort((a, b) => {
    const aStats = a.stats || {}
    const bStats = b.stats || {}
    if (sortMode.value === 'rating') return Number(bStats.averageRating || 0) - Number(aStats.averageRating || 0)
    if (sortMode.value === 'access') return Number(bStats.accessCount || 0) - Number(aStats.accessCount || 0)
    if (sortMode.value === 'price') return Number(b.pricePoints || 0) - Number(a.pricePoints || 0)
    const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0
    const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0
    return bTime - aTime
  })
})

const activePack = computed(() => {
  return visiblePacks.value.find((pack) => pack.id === activePackId.value)
    || sourcePacks.value.find((pack) => pack.id === activePackId.value)
    || null
})

const summaryText = computed(() => {
  if (currentView.value === 'market') {
    return '先把风格包做成真正可浏览的内容页，后面再继续补分类、联动搜索和更完整的市场规则。'
  }
  return '你的风格资产集中放在这里，编辑、发布和投稿审核都改成点击后再处理，不再挤在主页面。'
})

const statusText = (status) => {
  const labels = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ARCHIVED: '已归档',
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return labels[status] || status || '未设置'
}

const statusBadgeClass = (status) => {
  if (status === 'PUBLISHED') return 'published'
  if (status === 'ARCHIVED') return 'archived'
  return (status || '').toLowerCase()
}

const reloadPackDetail = (pack) => {
  emit('load-versions', pack)
  emit('load-reviews', pack)
  emit('load-artworks', pack)
}

const openPackDrawer = (pack) => {
  activePackId.value = pack.id
  packDrawerVisible.value = true
  reloadPackDetail(pack)
  if (currentView.value === 'workspace') {
    emit('load-submissions', pack)
  }
}

const openCreateDialog = () => {
  activePackId.value = null
  emit('reset-form')
  packEditorVisible.value = true
}

const openEditDialog = (pack) => {
  emit('edit-pack', pack)
  packEditorVisible.value = true
}

const submitPackEditor = async () => {
  await emit('save')
  packEditorVisible.value = false
}

const openSubmissionDialog = (pack) => {
  activePackId.value = pack.id
  emit('prepare-submission', pack)
  packSubmissionVisible.value = true
}

const openReviewDialog = (pack) => {
  activePackId.value = pack.id
  emit('prepare-review', pack)
  packReviewVisible.value = true
}

const submitReviewAndClose = async () => {
  await emit('submit-review')
  packReviewVisible.value = false
}

const openOwnerOps = (pack) => {
  activePackId.value = pack.id
  emit('load-submissions', pack)
  ownerOpsVisible.value = true
}

watch(
  () => props.initialView,
  (value) => {
    currentView.value = value
  }
)

watch(currentView, () => {
  keyword.value = ''
  priceBand.value = ''
  localStatus.value = ''
})
</script>
