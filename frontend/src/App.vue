<template>
  <div class="app-shell">
    <AppTopbar
      :active-view="activeView"
      :current-user="currentUser"
      @navigate="activateView"
      @refresh-provider="loadProvider"
      @refresh-artworks="refreshContent"
      @logout="logout"
    />

    <main class="app-main">
      <WorkbenchPage
        v-if="activeView === 'workbench'"
        :generation-mode="generationMode"
        :free-text="freeText"
        :negative-text="negativeText"
        :tag-tree="tagTree"
        :selected-tags="selectedTags"
        :init-image-preview="initImagePreview"
        :provider-state-class="providerStateClass"
        :generation-form="generationForm"
        :models="models"
        :vaes="vaes"
        :loras="loras"
        :selected-loras="selectedLoras"
        :lora-weight="loraWeight"
        :samplers="samplers"
        :upscalers="upscalers"
        :advanced-settings-open="advancedSettingsOpen"
        :override-settings-json="overrideSettingsJson"
        :extra-payload-json="extraPayloadJson"
        :provider-summary="providerSummary"
        :generation-presets="generationPresets"
        :preset-name="presetName"
        :current-user="currentUser"
        :generating="generating"
        :provider-status="providerStatus"
        :preview-image="previewImage"
        :gallery-items="myArtworks"
        :composed-prompt="composedPrompt"
        :composed-negative="composedNegative"
        :generation-queue-rows="generationQueueRows"
        :quick-sizes="quickSizes"
        :resource-name="resourceName"
        :lora-label="loraLabel"
        :is-live-job="isLiveJob"
        :status-label="statusLabel"
        :status-percent="statusPercent"
        :progress-status="progressStatus"
        @update:generation-mode="generationMode = $event"
        @update:free-text="freeText = $event"
        @update:negative-text="negativeText = $event"
        @reload-tags="loadTags"
        @open-prompt-builder="promptBuilderVisible = true"
        @compose="composePrompt"
        @toggle-tag="toggleTag"
        @clear-init-image="clearInitImage"
        @init-image-change="handleInitImageChange"
        @patch-generation-form="patchGenerationForm"
        @update:selected-loras="selectedLoras = $event"
        @update:lora-weight="loraWeight = $event"
        @apply-size="applySize"
        @toggle-advanced-settings="advancedSettingsOpen = !advancedSettingsOpen"
        @update:override-settings-json="overrideSettingsJson = $event"
        @update:extra-payload-json="extraPayloadJson = $event"
        @update:preset-name="presetName = $event"
        @save-preset="savePreset"
        @apply-preset="applyPreset"
        @delete-preset="deletePreset"
        @generate="generate"
        @refresh-artworks="refreshContent"
        @open-preview="previewVisible = true"
        @download-current="downloadCurrent"
        @select-artwork="openArtworkDetail"
        @cancel-tracked-job="cancelTrackedJob"
        @restore-job="restoreJob"
      />

      <LibraryPage
        v-else-if="activeView === 'library'"
        :library-keyword="libraryKeyword"
        :library-tag-filter="libraryTagFilter"
        :library-visibility-filter="libraryVisibilityFilter"
        :library-status-filter="libraryStatusFilter"
        :selectable-tags="flatTags"
        :selected-artwork-count="selectedArtworkIds.size"
        :all-filtered-artworks-selected="allFilteredArtworksSelected"
        :library-batch-loading="libraryBatchLoading"
        :filtered-artworks="filteredArtworks"
        :is-artwork-selected="isArtworkSelected"
        :format-date="formatDate"
        :status-label="statusLabel"
        @update:library-keyword="libraryKeyword = $event"
        @update:library-tag-filter="libraryTagFilter = $event"
        @update:library-visibility-filter="libraryVisibilityFilter = $event"
        @update:library-status-filter="libraryStatusFilter = $event"
        @refresh="loadMyArtworks"
        @toggle-all-filtered="toggleAllFiltered"
        @clear-selection="clearArtworkSelection"
        @publish-selected="publishSelected"
        @make-private-selected="makePrivateSelected"
        @archive-selected="archiveSelected"
        @open-detail="openArtworkDetail"
        @toggle-selection="toggleArtworkSelection"
        @request-publish="requestPublish"
      />

      <CommunityPage
        v-else-if="activeView === 'community'"
        :community-tag-filter="communityTagFilter"
        :selectable-tags="flatTags"
        :public-artworks="filteredPublicArtworks"
        :format-date="formatDate"
        @update:community-tag-filter="communityTagFilter = $event"
        @refresh="loadCommunity"
        @open-detail="openArtworkDetail"
      />

      <StyleMarketPage
        v-else-if="activeView === 'style-market'"
        :style-form="styleForm"
        :style-loading="styleLoading"
        :market-style-packages="marketStylePackages"
        :style-package-versions="stylePackageVersions"
        :style-package-reviews="stylePackageReviews"
        :active-style-package-name="activeStylePackageName"
        :style-review-form="styleReviewForm"
        :style-stats-items="styleStatsItems"
        :format-date="formatDate"
        :status-label="statusLabel"
        @refresh="loadStylePackages"
        @reset-form="resetStyleForm"
        @patch-style-form="patchStyleForm"
        @save="saveStylePackage"
        @edit-pack="editStylePackage"
        @apply-pack="applyStylePackage"
        @publish-pack="publishStylePackageItem"
        @archive-pack="archiveStylePackageItem"
        @load-versions="loadStyleVersions"
        @load-submissions="loadStyleSubmissions"
        @load-artworks="loadStyleArtworks"
        @exchange-pack="exchangeStylePackageItem"
        @prepare-submission="prepareStyleSubmission"
        @prepare-review="prepareStyleReview"
        @load-reviews="loadStyleReviews"
        @patch-review-form="patchStyleReviewForm"
        @submit-review="submitStyleReview"
        @review-submission="reviewStyleSubmissionItem"
        @go-related="activateView"
      />

      <MyStylePackagesPage
        v-else-if="activeView === 'my-styles'"
        :style-form="styleForm"
        :style-loading="styleLoading"
        :my-style-packages="myStylePackages"
        :style-package-versions="stylePackageVersions"
        :style-package-reviews="stylePackageReviews"
        :style-submissions="styleSubmissions"
        :active-style-package-name="activeStylePackageName"
        :style-review-form="styleReviewForm"
        :style-stats-items="styleStatsItems"
        :format-date="formatDate"
        :status-label="statusLabel"
        @refresh="loadStylePackages"
        @reset-form="resetStyleForm"
        @patch-style-form="patchStyleForm"
        @save="saveStylePackage"
        @edit-pack="editStylePackage"
        @apply-pack="applyStylePackage"
        @publish-pack="publishStylePackageItem"
        @archive-pack="archiveStylePackageItem"
        @load-versions="loadStyleVersions"
        @load-submissions="loadStyleSubmissions"
        @load-artworks="loadStyleArtworks"
        @exchange-pack="exchangeStylePackageItem"
        @prepare-submission="prepareStyleSubmission"
        @prepare-review="prepareStyleReview"
        @load-reviews="loadStyleReviews"
        @patch-review-form="patchStyleReviewForm"
        @submit-review="submitStyleReview"
        @review-submission="reviewStyleSubmissionItem"
        @go-related="activateView"
      />

      <TaskMarketPage
        v-else-if="activeView === 'task-market'"
        :task-loading="taskLoading"
        :task-form="taskForm"
        :task-market="taskMarket"
        :task-market-status-filter="taskMarketStatusFilter"
        :submission-form="submissionForm"
        :my-artworks="myArtworks"
        :current-user="currentUser"
        :my-task-submissions="myTaskSubmissions"
        :selected-task-submissions="selectedTaskSubmissions"
        :task-review-form="taskReviewForm"
        :format-points="formatPoints"
        :format-deadline="formatDeadline"
        :status-label="statusLabel"
        @refresh="loadTasks"
        @reset-task-form="resetTaskForm"
        @patch-task-form="patchTaskForm"
        @save-task="saveTask"
        @update:taskMarketStatusFilter="taskMarketStatusFilter = $event"
        @prepare-submission="prepareTaskSubmission"
        @patch-submission-form="patchSubmissionForm"
        @submit-task-work="submitTaskWork"
        @update:myTaskStatusFilter="myTaskStatusFilter = $event"
        @edit-task="editTask"
        @publish-task-item="publishTaskItem"
        @close-task-item="closeTaskItem"
        @load-task-submissions="loadTaskSubmissions"
        @prepare-task-review="prepareTaskReview"
        @review-submission="reviewTaskSubmissionItem"
        @patch-task-review-form="patchTaskReviewForm"
        @go-related="activateView"
      />

      <MyTasksPage
        v-else-if="activeView === 'my-tasks'"
        :task-loading="taskLoading"
        :task-form="taskForm"
        :task-market-status-filter="taskMarketStatusFilter"
        :submission-form="submissionForm"
        :my-artworks="myArtworks"
        :current-user="currentUser"
        :my-task-submissions="myTaskSubmissions"
        :my-tasks="myTasks"
        :my-task-status-filter="myTaskStatusFilter"
        :selected-task-submissions="selectedTaskSubmissions"
        :task-review-form="taskReviewForm"
        :format-points="formatPoints"
        :format-deadline="formatDeadline"
        :status-label="statusLabel"
        @refresh="loadTasks"
        @reset-task-form="resetTaskForm"
        @patch-task-form="patchTaskForm"
        @save-task="saveTask"
        @update:taskMarketStatusFilter="taskMarketStatusFilter = $event"
        @prepare-submission="prepareTaskSubmission"
        @patch-submission-form="patchSubmissionForm"
        @submit-task-work="submitTaskWork"
        @update:myTaskStatusFilter="myTaskStatusFilter = $event"
        @edit-task="editTask"
        @publish-task-item="publishTaskItem"
        @close-task-item="closeTaskItem"
        @load-task-submissions="loadTaskSubmissions"
        @prepare-task-review="prepareTaskReview"
        @review-submission="reviewTaskSubmissionItem"
        @patch-task-review-form="patchTaskReviewForm"
        @go-related="activateView"
      />

      <ModelsPage
        v-else-if="activeView === 'models'"
        :model-resource-keyword="modelResourceKeyword"
        :favorite-resources="favoriteResources"
        :filtered-models="filteredModels"
        :filtered-loras="filteredLoras"
        :filtered-vaes="filteredVaes"
        :filtered-upscalers="filteredUpscalers"
        :lora-trigger-drafts="loraTriggerDrafts"
        :resource-type-label="resourceTypeLabel"
        :resource-name="resourceName"
        :is-favorite-resource="isFavoriteResource"
        :lora-label="loraLabel"
        :lora-trigger-key="loraTriggerKey"
        :suggested-lora-trigger="suggestedLoraTrigger"
        @update:model-resource-keyword="modelResourceKeyword = $event"
        @refresh-provider="loadProvider"
        @apply-favorite-resource="applyFavoriteResource"
        @toggle-favorite-resource="toggleFavoriteResource"
        @update:lora-trigger-draft="updateLoraTriggerDraft"
        @save-lora-trigger="saveLoraTrigger"
        @apply-model-resource="applyModelResource"
        @add-lora-resource="addLoraResource"
        @apply-vae-resource="applyVaeResource"
        @apply-upscaler-resource="applyUpscalerResource"
      />

      <AdminPage
        v-else-if="activeView === 'admin'"
        :audit-loading="auditLoading"
        :tag-admin-loading="tagAdminLoading"
        :admin-dashboard-loading="adminDashboardLoading"
        :admin-dashboard="adminDashboard"
        :category-form="categoryForm"
        :tag-form="tagForm"
        :admin-tag-tree="adminTagTree"
        :flat-tags="flatTags"
        :pending-audits="pendingAudits"
        :my-audits="myAudits"
        :format-metric-value="formatMetricValue"
        :status-count-text="statusCountText"
        :format-date="formatDate"
        :point-reason-label="pointReasonLabel"
        :format-points="formatPoints"
        :status-label="statusLabel"
        @refresh="refreshAdminData"
        @save-tag-category="saveTagCategory"
        @reset-tag-form="resetTagForm"
        @save-tag-item="saveTagItem"
        @edit-tag-item="editTagItem"
        @disable-tag-item="disableTagItem"
        @review-content-audit="reviewContentAudit"
      />

      <AccountPage
        v-else
        :current-user="currentUser"
        :user-initial="userInitial"
        :point-account="pointAccount"
        :point-loading="pointLoading"
        :format-points="formatPoints"
        :format-signed-points="formatSignedPoints"
        :point-reason-label="pointReasonLabel"
        @claim-daily-points="claimDaily"
        @refresh-points="loadPointAccount"
        @logout="logout"
      />
    </main>

    <PromptBuilderExpanded
      :visible="promptBuilderVisible"
      :free-text="freeText"
      :negative-text="negativeText"
      :composed-prompt="composedPrompt"
      :composed-negative="composedNegative"
      :tag-tree="tagTree"
      :selected-tags="selectedTags"
      @close="promptBuilderVisible = false"
      @compose="composePrompt"
      @toggle-tag="toggleTag"
      @update:free-text="freeText = $event"
      @update:negative-text="negativeText = $event"
    />

    <el-dialog v-model="previewVisible" width="min(1200px, 96vw)" title="作品预览">
      <div class="art-preview-dialog">
        <img v-if="previewImage" :src="previewImage" alt="preview" class="art-preview-image" />
      </div>
    </el-dialog>

    <el-drawer v-model="detailVisible" size="min(760px, 94vw)" title="作品详情">
      <template v-if="artworkDetail">
        <div class="hub-drawer-body">
          <section class="soft-panel embedded-panel">
            <div class="drawer-topline">
              <div>
                <p class="eyebrow">Artwork</p>
                <h2>{{ artworkDetail.title }}</h2>
              </div>
              <div class="drawer-top-badges">
                <span class="status-badge" :class="(artworkDetail.status || '').toLowerCase()">{{ statusLabel(artworkDetail.status) }}</span>
                <span class="task-tier-chip strong">{{ visibilityLabel(artworkDetail.visibility) }}</span>
              </div>
            </div>
            <img v-if="artworkDetail.imageUrl" :src="artworkDetail.imageUrl" :alt="artworkDetail.title" class="detail-cover" />
            <el-form label-position="top" class="detail-form-block">
              <el-form-item label="标题">
                <el-input v-model="artworkDetail.title" />
              </el-form-item>
              <el-form-item label="正向 Prompt">
                <el-input v-model="artworkDetail.promptText" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" resize="none" />
              </el-form-item>
              <el-form-item label="反向 Prompt">
                <el-input v-model="artworkDetail.negativePromptText" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" />
              </el-form-item>
            </el-form>
            <div class="detail-actions">
              <el-button @click="reuseArtworkPrompt">复用到工作台</el-button>
              <el-button @click="archiveArtworkDetail">归档</el-button>
              <el-button type="primary" @click="saveArtworkDetail">保存修改</el-button>
            </div>
          </section>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import AppTopbar from './components/AppTopbar.vue'
import WorkbenchPage from './components/WorkbenchPage.vue'
import LibraryPage from './components/LibraryPage.vue'
import CommunityPage from './components/CommunityPage.vue'
import StyleMarketPage from './components/StyleMarketPage.vue'
import MyStylePackagesPage from './components/MyStylePackagesPage.vue'
import TaskMarketPage from './components/TaskMarketPage.vue'
import MyTasksPage from './components/MyTasksPage.vue'
import ModelsPage from './components/ModelsPage.vue'
import AdminPage from './components/AdminPage.vue'
import AccountPage from './components/AccountPage.vue'
import PromptBuilderExpanded from './components/PromptBuilderExpanded.vue'
import { useAuthStore } from './stores/auth'
import {
  cancelGenerationJob,
  getMyGenerationJobs,
  getProviderLoras,
  getProviderModels,
  getProviderOptions,
  getProviderSamplers,
  getProviderUpscalers,
  getProviderVaes,
  queueImg2Img,
  queueTxt2Img
} from './api/generation'
import {
  archiveArtwork,
  bulkArchiveArtworks,
  bulkRequestArtworkPublish,
  bulkUpdateArtworkVisibility,
  getArtworkDetail,
  getMyArtworks,
  getPublicArtworks,
  requestArtworkPublish,
  updateArtwork
} from './api/artworks'
import { buildPrompt, createTag, createTagCategory, deactivateTag, getAdminTagTree, getTagTree, updateTag } from './api/tags'
import {
  archiveStylePackage,
  createStylePackage,
  exchangeStylePackage,
  getMarketStylePackages,
  getMyStylePackages,
  getStylePackageArtworks,
  getStylePackageReviews,
  getStylePackageSubmissions,
  getStylePackageVersions,
  publishStylePackage,
  reviewStylePackageSubmission,
  saveStylePackageReview,
  updateStylePackage
} from './api/stylePackages'
import {
  closeTask,
  createTask,
  getMyTaskSubmissions,
  getMyTasks,
  getTaskMarket,
  getTaskSubmissions,
  publishTask,
  reviewTaskSubmission,
  submitTaskArtwork,
  updateTask
} from './api/tasks'
import { claimDailyPoints, getPointAccount } from './api/points'
import { getAdminDashboard } from './api/admin'
import { getMyAudits, getPendingAudits, reviewAudit } from './api/audits'

const router = useRouter()
const auth = useAuthStore()

const currentUser = computed(() => auth.currentUser.value)
const activeView = ref('workbench')
const promptBuilderVisible = ref(false)
const previewVisible = ref(false)
const detailVisible = ref(false)
const artworkDetail = ref(null)

const generationMode = ref('txt2img')
const freeText = ref('')
const negativeText = ref('')
const composedPrompt = ref('')
const composedNegative = ref('')
const tagTree = ref([])
const selectedTags = ref([])
const initImagePreview = ref('')
const initImageBase64 = ref('')
const providerStatus = ref('Forge 未检测')
const providerSummary = ref('等待连接 Forge 资源')
const providerStateClass = ref('offline')
const models = ref([])
const samplers = ref([])
const loras = ref([])
const vaes = ref([])
const upscalers = ref([])
const selectedLoras = ref([])
const loraWeight = ref(0.75)
const overrideSettingsJson = ref('')
const extraPayloadJson = ref('')
const advancedSettingsOpen = ref(false)
const generating = ref(false)
const presetName = ref('')
const generationPresets = ref([])
const generationQueueRows = ref([])

const quickSizes = [
  { label: '768 x 1024', width: 768, height: 1024 },
  { label: '832 x 1216', width: 832, height: 1216 },
  { label: '1024 x 1024', width: 1024, height: 1024 },
  { label: '1216 x 832', width: 1216, height: 832 }
]

const generationForm = reactive({
  title: '',
  model: '',
  vae: '',
  samplerName: 'DPM++ 2M Karras',
  width: 1024,
  height: 1024,
  steps: 28,
  cfgScale: 7,
  seed: -1,
  clipSkip: 1,
  batchSize: 1,
  batchCount: 1,
  restoreFaces: false,
  tiling: false,
  enableHr: false,
  hrUpscaler: '',
  denoisingStrength: 0.35,
  hrScale: 1.5,
  hrSecondPassSteps: 12
})

const libraryKeyword = ref('')
const libraryTagFilter = ref([])
const libraryVisibilityFilter = ref('')
const libraryStatusFilter = ref('')
const communityTagFilter = ref([])
const myArtworks = ref([])
const publicArtworks = ref([])
const selectedArtworkIds = ref(new Set())
const libraryBatchLoading = ref(false)

const styleLoading = ref(false)
const myStylePackages = ref([])
const marketStylePackages = ref([])
const stylePackageVersions = ref([])
const stylePackageReviews = ref([])
const styleSubmissions = ref([])
const activeStylePackageName = ref('')
const styleForm = reactive({
  id: null,
  name: '',
  coverImageUrl: '',
  description: '',
  promptTemplate: '',
  negativePromptTemplate: '',
  pricePoints: 0
})
const styleReviewForm = reactive({
  rating: 5,
  comment: ''
})
const styleSubmissionContext = ref(null)
const styleReviewContext = ref(null)

const taskLoading = ref(false)
const taskMarket = ref([])
const myTasks = ref([])
const myTaskSubmissions = ref([])
const selectedTaskSubmissions = ref([])
const taskMarketStatusFilter = ref('PUBLISHED')
const myTaskStatusFilter = ref('')
const taskForm = reactive({
  id: null,
  title: '',
  description: '',
  requirementsText: '',
  budgetPoints: 0,
  deadline: ''
})
const submissionForm = reactive({
  taskId: null,
  artworkId: null,
  note: ''
})
const taskReviewForm = reactive({
  submissionId: null,
  rewardPoints: 0,
  comment: ''
})

const pointLoading = ref(false)
const pointAccount = ref({ balance: 0, frozenBalance: 0, transactions: [] })

const modelResourceKeyword = ref('')
const favoriteResources = ref([])
const favoriteResourceMap = ref({})
const loraTriggerDrafts = ref({})

const auditLoading = ref(false)
const tagAdminLoading = ref(false)
const adminDashboardLoading = ref(false)
const adminDashboard = ref(null)
const adminTagTree = ref([])
const pendingAudits = ref([])
const myAudits = ref([])
const categoryForm = reactive({ name: '', slug: '', sortOrder: 0 })
const tagForm = reactive({
  id: null,
  categoryId: null,
  name: '',
  promptText: '',
  negativePromptText: '',
  previewImageUrl: '',
  weight: 1,
  visibility: 'PUBLIC'
})

const userInitial = computed(() => {
  const name = currentUser.value?.displayName || currentUser.value?.username || 'A'
  return name.slice(0, 1).toUpperCase()
})

const flatTags = computed(() => tagTree.value.flatMap((category) => category.tags || []))
const previewImage = computed(() => artworkDetail.value?.imageUrl || myArtworks.value[0]?.imageUrl || '')

const filteredArtworks = computed(() => {
  return myArtworks.value.filter((artwork) => {
    const keyword = libraryKeyword.value.trim().toLowerCase()
    const keywordHit = !keyword || `${artwork.title || ''} ${artwork.promptText || ''}`.toLowerCase().includes(keyword)
    const tagsHit = !libraryTagFilter.value.length || libraryTagFilter.value.every((id) => (artwork.tags || []).some((tag) => tag.id === id))
    const visibilityHit = !libraryVisibilityFilter.value || artwork.visibility === libraryVisibilityFilter.value
    const statusHit = !libraryStatusFilter.value || artwork.status === libraryStatusFilter.value
    return keywordHit && tagsHit && visibilityHit && statusHit
  })
})

const filteredPublicArtworks = computed(() => {
  return publicArtworks.value.filter((artwork) => {
    return !communityTagFilter.value.length || communityTagFilter.value.every((id) => (artwork.tags || []).some((tag) => tag.id === id))
  })
})

const allFilteredArtworksSelected = computed(() => {
  if (!filteredArtworks.value.length) return false
  return filteredArtworks.value.every((artwork) => selectedArtworkIds.value.has(artwork.id))
})

const filteredModels = computed(() => filterResources(models.value))
const filteredLoras = computed(() => filterResources(loras.value))
const filteredVaes = computed(() => filterResources(vaes.value))
const filteredUpscalers = computed(() => filterResources(upscalers.value))

function filterResources(list) {
  const keyword = modelResourceKeyword.value.trim().toLowerCase()
  if (!keyword) return list
  return list.filter((item) => resourceName(item).toLowerCase().includes(keyword))
}

function activateView(view) {
  if (view === 'admin' && currentUser.value?.role !== 'ADMIN') {
    ElMessage.warning('当前账号没有后台权限')
    return
  }
  activeView.value = view
}

function patchGenerationForm(patch) {
  Object.assign(generationForm, patch)
}

function patchStyleForm(patch) {
  Object.assign(styleForm, patch)
}

function patchTaskForm(patch) {
  Object.assign(taskForm, patch)
}

function patchSubmissionForm(patch) {
  Object.assign(submissionForm, patch)
}

function patchTaskReviewForm(patch) {
  Object.assign(taskReviewForm, patch)
}

function patchStyleReviewForm(patch) {
  Object.assign(styleReviewForm, patch)
}

function applySize(size) {
  generationForm.width = size.width
  generationForm.height = size.height
}

function clearInitImage() {
  initImagePreview.value = ''
  initImageBase64.value = ''
}

function handleInitImageChange(file) {
  if (!file?.raw) return
  const reader = new FileReader()
  reader.onload = () => {
    initImagePreview.value = String(reader.result || '')
    initImageBase64.value = initImagePreview.value.split(',')[1] || ''
  }
  reader.readAsDataURL(file.raw)
}

async function composePrompt() {
  try {
    const result = await buildPrompt({
      freeText: freeText.value,
      negativeText: negativeText.value,
      selectedTagIds: selectedTags.value
    })
    composedPrompt.value = result.promptText || ''
    composedNegative.value = result.negativePromptText || ''
  } catch {
    composedPrompt.value = [freeText.value, ...selectedTagObjects().map((tag) => tag.promptText).filter(Boolean)].filter(Boolean).join(', ')
    composedNegative.value = [negativeText.value, ...selectedTagObjects().map((tag) => tag.negativePromptText).filter(Boolean)].filter(Boolean).join(', ')
  }
}

function selectedTagObjects() {
  return flatTags.value.filter((tag) => selectedTags.value.includes(tag.id))
}

function toggleTag(tagId) {
  if (selectedTags.value.includes(tagId)) {
    selectedTags.value = selectedTags.value.filter((id) => id !== tagId)
  } else {
    selectedTags.value = [...selectedTags.value, tagId]
  }
  composePrompt()
}

function buildGenerationPayload() {
  const payload = {
    title: generationForm.title || `作品 ${new Date().toLocaleString('zh-CN')}`,
    prompt: composedPrompt.value || freeText.value,
    negativePrompt: composedNegative.value || negativeText.value,
    model: generationForm.model || undefined,
    vae: generationForm.vae || undefined,
    samplerName: generationForm.samplerName,
    width: generationForm.width,
    height: generationForm.height,
    steps: generationForm.steps,
    cfgScale: generationForm.cfgScale,
    seed: generationForm.seed,
    clipSkip: generationForm.clipSkip,
    batchSize: generationForm.batchSize,
    batchCount: generationForm.batchCount,
    restoreFaces: generationForm.restoreFaces,
    tiling: generationForm.tiling,
    enableHr: generationForm.enableHr,
    hrUpscaler: generationForm.hrUpscaler || undefined,
    denoisingStrength: generationForm.denoisingStrength,
    hrScale: generationForm.hrScale,
    hrSecondPassSteps: generationForm.hrSecondPassSteps,
    loras: selectedLoras.value.map((name) => ({ name, weight: loraWeight.value })),
    overrideSettingsJson: overrideSettingsJson.value || undefined,
    extraPayloadJson: extraPayloadJson.value || undefined,
    selectedTagIds: selectedTags.value
  }
  if (generationMode.value === 'img2img' && initImageBase64.value) {
    payload.initImageBase64 = initImageBase64.value
  }
  return payload
}

async function generate() {
  generating.value = true
  try {
    await composePrompt()
    const payload = buildGenerationPayload()
    const job = generationMode.value === 'img2img' ? await queueImg2Img(payload) : await queueTxt2Img(payload)
    ElMessage.success('生成任务已提交')
    if (job?.jobId || job?.id) {
      generationQueueRows.value = [{
        key: job.jobId || job.id,
        id: job.jobId || job.id,
        title: payload.title,
        modeLabel: generationMode.value === 'img2img' ? '图生图' : '文生图',
        meta: `${generationForm.width}x${generationForm.height}`,
        status: 'PENDING',
        imageUrl: '',
        paramsJson: JSON.stringify(payload)
      }, ...generationQueueRows.value]
      await refreshJobs()
    }
    await refreshContent()
  } catch (error) {
    ElMessage.error(error?.message || '生成失败')
  } finally {
    generating.value = false
  }
}

async function refreshJobs() {
  try {
    const jobs = await getMyGenerationJobs()
    generationQueueRows.value = (jobs || []).map((job) => ({
      key: job.id,
      id: job.id,
      title: job.title || '生成任务',
      modeLabel: job.mode === 'IMG2IMG' ? '图生图' : '文生图',
      meta: `${job.width || generationForm.width}x${job.height || generationForm.height}`,
      status: job.status,
      imageUrl: job.imageUrl,
      paramsJson: job.paramsJson
    }))
  } catch {
    generationQueueRows.value = generationQueueRows.value
  }
}

async function cancelTrackedJob(item) {
  try {
    await cancelGenerationJob(item.id)
    ElMessage.success('已取消生成任务')
    await refreshJobs()
  } catch (error) {
    ElMessage.error(error?.message || '取消失败')
  }
}

function restoreJob(item) {
  if (!item?.paramsJson) return
  try {
    const params = JSON.parse(item.paramsJson)
    generationForm.title = params.title || generationForm.title
    generationForm.width = params.width || generationForm.width
    generationForm.height = params.height || generationForm.height
    generationForm.steps = params.steps || generationForm.steps
    generationForm.cfgScale = params.cfgScale || generationForm.cfgScale
    freeText.value = params.prompt || freeText.value
    negativeText.value = params.negativePrompt || negativeText.value
    composedPrompt.value = params.prompt || composedPrompt.value
    composedNegative.value = params.negativePrompt || composedNegative.value
    selectedLoras.value = (params.loras || []).map((item) => item.name)
    if (params.loras?.[0]?.weight) loraWeight.value = params.loras[0].weight
    ElMessage.success('已恢复任务参数')
  } catch {
    ElMessage.warning('这条任务没有可恢复的参数')
  }
}

function isLiveJob(item) {
  return ['PENDING', 'RUNNING', 'QUEUED'].includes(item?.status)
}

function statusLabel(status) {
  const labels = {
    ACTIVE: '正常',
    ARCHIVED: '已归档',
    PENDING_AUDIT: '待审核',
    PENDING: '待处理',
    RUNNING: '进行中',
    QUEUED: '排队中',
    SUCCESS: '已完成',
    COMPLETED: '已完成',
    FAILED: '失败',
    CANCELLED: '已取消',
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    CLOSED: '已关闭',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return labels[status] || status || '未设置'
}

function statusPercent(status) {
  const map = { PENDING: 20, QUEUED: 15, RUNNING: 65, SUCCESS: 100, COMPLETED: 100, FAILED: 100, CANCELLED: 100 }
  return map[status] || 0
}

function progressStatus(status) {
  if (status === 'FAILED') return 'exception'
  if (status === 'SUCCESS' || status === 'COMPLETED') return 'success'
  return ''
}

function visibilityLabel(visibility) {
  return visibility === 'PUBLIC' ? '公开' : '私有'
}

function formatDate(value) {
  if (!value) return '未记录'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '未记录'
  return date.toLocaleString('zh-CN')
}

function formatDeadline(value) {
  if (!value) return '长期有效'
  return formatDate(value)
}

function formatPoints(value) {
  return Number(value || 0)
}

function formatSignedPoints(value) {
  const amount = Number(value || 0)
  return amount > 0 ? `+${amount}` : `${amount}`
}

function pointReasonLabel(reason) {
  const labels = {
    DAILY_CLAIM: '每日领取',
    TASK_REWARD: '任务奖励',
    STYLE_EXCHANGE: '风格兑换',
    MANUAL: '人工调整'
  }
  return labels[reason] || reason || '积分变动'
}

function resourceName(item) {
  return item?.title || item?.model_name || item?.name || item?.filename || '未命名资源'
}

function loraLabel(item) {
  const trigger = suggestedLoraTrigger(item)
  return trigger ? `${resourceName(item)} · ${trigger}` : resourceName(item)
}

function resourceTypeLabel(type) {
  const labels = { model: '模型', lora: 'LoRA', vae: 'VAE', upscaler: '放大器' }
  return labels[type] || type
}

function loraTriggerKey(item) {
  return resourceName(item)
}

function suggestedLoraTrigger(item) {
  const key = loraTriggerKey(item)
  return loraTriggerDrafts.value[key] || item?.metadata?.ss_tag_frequency?.[0]?.tag || ''
}

function isFavoriteResource(type, resource) {
  const key = `${type}:${resourceName(resource)}`
  return Boolean(favoriteResourceMap.value[key])
}

function toggleFavoriteResource({ type, resource }) {
  const key = `${type}:${resourceName(resource)}`
  const next = { ...favoriteResourceMap.value }
  if (next[key]) {
    delete next[key]
  } else {
    next[key] = { key, type, name: resourceName(resource) }
  }
  favoriteResourceMap.value = next
  favoriteResources.value = Object.values(next)
}

function applyFavoriteResource(favorite) {
  if (favorite.type === 'model') generationForm.model = favorite.name
  if (favorite.type === 'vae') generationForm.vae = favorite.name
  if (favorite.type === 'upscaler') generationForm.hrUpscaler = favorite.name
  if (favorite.type === 'lora' && !selectedLoras.value.includes(favorite.name)) {
    selectedLoras.value = [...selectedLoras.value, favorite.name]
  }
}

function updateLoraTriggerDraft({ key, value }) {
  loraTriggerDrafts.value = { ...loraTriggerDrafts.value, [key]: value }
}

function saveLoraTrigger(lora) {
  const key = loraTriggerKey(lora)
  if (!loraTriggerDrafts.value[key]) {
    ElMessage.warning('请先填写触发词')
    return
  }
  ElMessage.success('已保存本地触发词草稿')
}

function applyModelResource(model) {
  generationForm.model = resourceName(model)
  ElMessage.success('已应用模型')
}

function addLoraResource(lora) {
  const name = resourceName(lora)
  if (!selectedLoras.value.includes(name)) {
    selectedLoras.value = [...selectedLoras.value, name]
  }
  ElMessage.success('已加入 LoRA')
}

function applyVaeResource(vae) {
  generationForm.vae = resourceName(vae)
  ElMessage.success('已应用 VAE')
}

function applyUpscalerResource(upscaler) {
  generationForm.hrUpscaler = resourceName(upscaler)
  generationForm.enableHr = true
  ElMessage.success('已设置高清修复放大器')
}

function savePreset() {
  if (!presetName.value.trim()) {
    ElMessage.warning('请先填写预设名称')
    return
  }
  generationPresets.value = [{
    id: `${Date.now()}`,
    name: presetName.value.trim(),
    payload: JSON.parse(JSON.stringify({ generationForm, freeText: freeText.value, negativeText: negativeText.value, selectedLoras: selectedLoras.value, loraWeight: loraWeight.value }))
  }, ...generationPresets.value]
  presetName.value = ''
  ElMessage.success('预设已保存')
}

function applyPreset(preset) {
  Object.assign(generationForm, preset.payload.generationForm)
  freeText.value = preset.payload.freeText
  negativeText.value = preset.payload.negativeText
  selectedLoras.value = preset.payload.selectedLoras
  loraWeight.value = preset.payload.loraWeight
  composePrompt()
}

function deletePreset(id) {
  generationPresets.value = generationPresets.value.filter((item) => item.id !== id)
}

async function loadProvider() {
  try {
    const [modelList, samplerList, options, loraList, vaeList, upscalerList] = await Promise.all([
      getProviderModels(),
      getProviderSamplers(),
      getProviderOptions(),
      getProviderLoras(),
      getProviderVaes(),
      getProviderUpscalers()
    ])
    models.value = modelList || []
    samplers.value = samplerList || []
    loras.value = loraList || []
    vaes.value = vaeList || []
    upscalers.value = upscalerList || []
    providerStateClass.value = 'online'
    providerStatus.value = 'Forge 已连接'
    providerSummary.value = `${models.value.length} 模型 · ${loras.value.length} LoRA · ${upscalers.value.length} 放大器`
    if (!generationForm.model && models.value[0]) generationForm.model = resourceName(models.value[0])
    if (!generationForm.samplerName && samplers.value[0]) generationForm.samplerName = samplers.value[0].name
    if (!generationForm.hrUpscaler && upscalers.value[0]) generationForm.hrUpscaler = resourceName(upscalers.value[0])
    if (!generationForm.vae && options?.sdVae) generationForm.vae = options.sdVae
  } catch (error) {
    providerStateClass.value = 'offline'
    providerStatus.value = 'Forge 连接失败'
    providerSummary.value = error?.message || '请检查 Forge API 是否可访问'
  }
}

async function loadTags() {
  try {
    tagTree.value = (await getTagTree()) || []
  } catch {
    tagTree.value = []
  }
}

async function loadMyArtworks() {
  try {
    myArtworks.value = (await getMyArtworks()) || []
  } catch {
    myArtworks.value = []
  }
}

async function loadCommunity() {
  try {
    publicArtworks.value = (await getPublicArtworks()) || []
  } catch {
    publicArtworks.value = []
  }
}

async function loadStylePackages() {
  styleLoading.value = true
  try {
    const [mine, market] = await Promise.all([getMyStylePackages(), getMarketStylePackages()])
    myStylePackages.value = mine || []
    marketStylePackages.value = market || []
  } finally {
    styleLoading.value = false
  }
}

async function loadStyleVersions(pack) {
  if (!pack?.id) return
  activeStylePackageName.value = pack.name || ''
  stylePackageVersions.value = (await getStylePackageVersions(pack.id)) || []
}

async function loadStyleReviews(pack) {
  if (!pack?.id) return
  stylePackageReviews.value = (await getStylePackageReviews(pack.id)) || []
}

async function loadStyleSubmissions(pack) {
  if (!pack?.id) return
  styleSubmissions.value = (await getStylePackageSubmissions(pack.id)) || []
}

async function loadStyleArtworks(pack) {
  if (!pack?.id) return
  await getStylePackageArtworks(pack.id)
}

function resetStyleForm() {
  Object.assign(styleForm, {
    id: null,
    name: '',
    coverImageUrl: '',
    description: '',
    promptTemplate: '',
    negativePromptTemplate: '',
    pricePoints: 0
  })
}

function editStylePackage(pack) {
  Object.assign(styleForm, {
    id: pack.id,
    name: pack.name || '',
    coverImageUrl: pack.coverImageUrl || '',
    description: pack.description || '',
    promptTemplate: pack.promptTemplate || '',
    negativePromptTemplate: pack.negativePromptTemplate || '',
    pricePoints: pack.pricePoints || 0
  })
}

async function saveStylePackage() {
  styleLoading.value = true
  try {
    if (styleForm.id) {
      await updateStylePackage(styleForm.id, styleForm)
    } else {
      await createStylePackage(styleForm)
    }
    ElMessage.success('风格包已保存')
    await loadStylePackages()
  } catch (error) {
    ElMessage.error(error?.message || '保存风格包失败')
  } finally {
    styleLoading.value = false
  }
}

function applyStylePackage(pack) {
  freeText.value = [freeText.value, pack.promptTemplate].filter(Boolean).join(', ')
  negativeText.value = [negativeText.value, pack.negativePromptTemplate].filter(Boolean).join(', ')
  composePrompt()
  activeView.value = 'workbench'
  ElMessage.success('已将风格包应用到工作台')
}

async function publishStylePackageItem(pack) {
  try {
    await publishStylePackage(pack.id)
    ElMessage.success('风格包已发布')
    await loadStylePackages()
  } catch (error) {
    ElMessage.error(error?.message || '发布失败')
  }
}

async function archiveStylePackageItem(pack) {
  try {
    await archiveStylePackage(pack.id)
    ElMessage.success('风格包已下架')
    await loadStylePackages()
  } catch (error) {
    ElMessage.error(error?.message || '下架失败')
  }
}

async function exchangeStylePackageItem(pack) {
  try {
    await exchangeStylePackage(pack.id)
    ElMessage.success('兑换成功')
    await loadStylePackages()
    await loadPointAccount()
  } catch (error) {
    ElMessage.error(error?.message || '兑换失败')
  }
}

function prepareStyleSubmission(pack) {
  styleSubmissionContext.value = pack
}

function prepareStyleReview(pack) {
  styleReviewContext.value = pack
  patchStyleReviewForm({ rating: 5, comment: '' })
}

async function submitStyleReview() {
  if (!styleReviewContext.value?.id) return
  try {
    await saveStylePackageReview(styleReviewContext.value.id, styleReviewForm)
    ElMessage.success('评价已提交')
    await loadStyleReviews(styleReviewContext.value)
  } catch (error) {
    ElMessage.error(error?.message || '提交评价失败')
  }
}

async function reviewStyleSubmissionItem({ submission, status }) {
  try {
    await reviewStylePackageSubmission(submission.id, { status })
    ElMessage.success('投稿审核已更新')
    if (styleSubmissionContext.value) await loadStyleSubmissions(styleSubmissionContext.value)
  } catch (error) {
    ElMessage.error(error?.message || '审核失败')
  }
}

async function loadTasks() {
  taskLoading.value = true
  try {
    const [market, mine, mineSubs] = await Promise.all([
      getTaskMarket({ status: taskMarketStatusFilter.value || undefined }),
      getMyTasks({ status: myTaskStatusFilter.value || undefined }),
      getMyTaskSubmissions()
    ])
    taskMarket.value = market || []
    myTasks.value = mine || []
    myTaskSubmissions.value = mineSubs || []
  } finally {
    taskLoading.value = false
  }
}

function resetTaskForm() {
  Object.assign(taskForm, { id: null, title: '', description: '', requirementsText: '', budgetPoints: 0, deadline: '' })
}

function editTask(task) {
  Object.assign(taskForm, {
    id: task.id,
    title: task.title || '',
    description: task.description || '',
    requirementsText: task.requirementsText || '',
    budgetPoints: task.budgetPoints || 0,
    deadline: task.deadline || ''
  })
}

async function saveTask() {
  taskLoading.value = true
  try {
    if (taskForm.id) {
      await updateTask(taskForm.id, taskForm)
    } else {
      await createTask(taskForm)
    }
    ElMessage.success('任务已保存')
    await loadTasks()
  } catch (error) {
    ElMessage.error(error?.message || '保存任务失败')
  } finally {
    taskLoading.value = false
  }
}

function prepareTaskSubmission(task) {
  Object.assign(submissionForm, { taskId: task.id, artworkId: null, note: '' })
}

async function submitTaskWork() {
  if (!submissionForm.taskId || !submissionForm.artworkId) {
    ElMessage.warning('请先选择投稿作品')
    return
  }
  try {
    await submitTaskArtwork(submissionForm.taskId, { artworkId: submissionForm.artworkId, note: submissionForm.note })
    ElMessage.success('投稿已提交')
    await loadTasks()
  } catch (error) {
    ElMessage.error(error?.message || '投稿失败')
  }
}

async function loadTaskSubmissions(task) {
  if (!task?.id) return
  selectedTaskSubmissions.value = (await getTaskSubmissions(task.id)) || []
}

function prepareTaskReview(submission) {
  Object.assign(taskReviewForm, {
    submissionId: submission.id,
    rewardPoints: submission.rewardPoints || 0,
    comment: submission.comment || ''
  })
}

async function publishTaskItem(task) {
  try {
    await publishTask(task.id)
    ElMessage.success('任务已发布')
    await loadTasks()
  } catch (error) {
    ElMessage.error(error?.message || '发布任务失败')
  }
}

async function closeTaskItem(task) {
  try {
    await closeTask(task.id)
    ElMessage.success('任务已关闭')
    await loadTasks()
  } catch (error) {
    ElMessage.error(error?.message || '关闭任务失败')
  }
}

async function reviewTaskSubmissionItem({ submission, status }) {
  try {
    await reviewTaskSubmission(submission.id, { status, rewardPoints: taskReviewForm.rewardPoints, comment: taskReviewForm.comment })
    ElMessage.success('任务投稿审核已更新')
    const task = myTasks.value.find((item) => item.id === submission.taskId)
    if (task) await loadTaskSubmissions(task)
    await loadTasks()
  } catch (error) {
    ElMessage.error(error?.message || '审核失败')
  }
}

async function loadPointAccount() {
  pointLoading.value = true
  try {
    pointAccount.value = (await getPointAccount()) || { balance: 0, frozenBalance: 0, transactions: [] }
  } finally {
    pointLoading.value = false
  }
}

async function claimDaily() {
  try {
    await claimDailyPoints()
    ElMessage.success('每日积分已领取')
    await loadPointAccount()
  } catch (error) {
    ElMessage.error(error?.message || '领取失败')
  }
}

async function refreshAdminData() {
  if (currentUser.value?.role !== 'ADMIN') return
  auditLoading.value = true
  adminDashboardLoading.value = true
  try {
    const [dashboard, pending, mine, adminTags] = await Promise.all([
      getAdminDashboard(),
      getPendingAudits(),
      getMyAudits(),
      getAdminTagTree()
    ])
    adminDashboard.value = normalizeDashboard(dashboard)
    pendingAudits.value = pending || []
    myAudits.value = mine || []
    adminTagTree.value = adminTags || []
  } finally {
    auditLoading.value = false
    adminDashboardLoading.value = false
  }
}

function normalizeDashboard(dashboard) {
  if (!dashboard) return null
  return {
    ...dashboard,
    metrics: dashboard.metrics || [],
    generationStatus: dashboard.generationStatus || {},
    auditStatus: dashboard.auditStatus || {},
    taskStatus: dashboard.taskStatus || {},
    styleStatus: dashboard.styleStatus || {},
    dailyGenerations: dashboard.dailyGenerations || [],
    dailyArtworks: dashboard.dailyArtworks || [],
    pointFlows: dashboard.pointFlows || [],
    recentActivities: dashboard.recentActivities || []
  }
}

function formatMetricValue(metric) {
  return metric?.value ?? 0
}

function statusCountText(data) {
  return Object.entries(data || {}).map(([key, value]) => `${statusLabel(key)} ${value}`).join(' · ') || '暂无数据'
}

async function saveTagCategory() {
  if (!categoryForm.name.trim() || !categoryForm.slug.trim()) {
    ElMessage.warning('请先填写分类名称和 slug')
    return
  }
  tagAdminLoading.value = true
  try {
    await createTagCategory(categoryForm)
    ElMessage.success('分类已创建')
    Object.assign(categoryForm, { name: '', slug: '', sortOrder: 0 })
    await refreshAdminData()
    await loadTags()
  } finally {
    tagAdminLoading.value = false
  }
}

function resetTagForm() {
  Object.assign(tagForm, { id: null, categoryId: null, name: '', promptText: '', negativePromptText: '', previewImageUrl: '', weight: 1, visibility: 'PUBLIC' })
}

async function saveTagItem() {
  tagAdminLoading.value = true
  try {
    if (tagForm.id) {
      await updateTag(tagForm.id, tagForm)
    } else {
      await createTag(tagForm)
    }
    ElMessage.success('标签已保存')
    resetTagForm()
    await refreshAdminData()
    await loadTags()
  } finally {
    tagAdminLoading.value = false
  }
}

function editTagItem({ category, tag }) {
  Object.assign(tagForm, {
    id: tag.id,
    categoryId: category.id,
    name: tag.name || '',
    promptText: tag.promptText || '',
    negativePromptText: tag.negativePromptText || '',
    previewImageUrl: tag.previewImageUrl || '',
    weight: tag.weight || 1,
    visibility: tag.visibility || 'PUBLIC'
  })
}

async function disableTagItem(tag) {
  try {
    await deactivateTag(tag.id)
    ElMessage.success('标签已停用')
    await refreshAdminData()
    await loadTags()
  } catch (error) {
    ElMessage.error(error?.message || '停用标签失败')
  }
}

async function reviewContentAudit({ audit, status }) {
  try {
    await reviewAudit(audit.id, { status })
    ElMessage.success('审核结果已提交')
    await refreshAdminData()
  } catch (error) {
    ElMessage.error(error?.message || '审核失败')
  }
}

function styleStatsItems(pack) {
  const stats = pack?.stats || {}
  return [
    { label: '评分', value: Number(stats.averageRating || 0).toFixed(1) },
    { label: '兑换', value: stats.accessCount || 0 },
    { label: '版本', value: stats.versionCount || 0 }
  ]
}

function toggleArtworkSelection(artwork) {
  const next = new Set(selectedArtworkIds.value)
  if (next.has(artwork.id)) next.delete(artwork.id)
  else next.add(artwork.id)
  selectedArtworkIds.value = next
}

function isArtworkSelected(artwork) {
  return selectedArtworkIds.value.has(artwork.id)
}

function clearArtworkSelection() {
  selectedArtworkIds.value = new Set()
}

function toggleAllFiltered() {
  if (allFilteredArtworksSelected.value) {
    clearArtworkSelection()
    return
  }
  selectedArtworkIds.value = new Set(filteredArtworks.value.map((artwork) => artwork.id))
}

async function publishSelected() {
  libraryBatchLoading.value = true
  try {
    await bulkRequestArtworkPublish([...selectedArtworkIds.value])
    ElMessage.success('已提交公开申请')
    await loadMyArtworks()
    clearArtworkSelection()
  } finally {
    libraryBatchLoading.value = false
  }
}

async function makePrivateSelected() {
  libraryBatchLoading.value = true
  try {
    await bulkUpdateArtworkVisibility([...selectedArtworkIds.value], 'PRIVATE')
    ElMessage.success('所选作品已设为私有')
    await loadMyArtworks()
    clearArtworkSelection()
  } finally {
    libraryBatchLoading.value = false
  }
}

async function archiveSelected() {
  libraryBatchLoading.value = true
  try {
    await bulkArchiveArtworks([...selectedArtworkIds.value])
    ElMessage.success('所选作品已归档')
    await loadMyArtworks()
    clearArtworkSelection()
  } finally {
    libraryBatchLoading.value = false
  }
}

async function requestPublish(artwork) {
  try {
    await requestArtworkPublish(artwork.id)
    ElMessage.success('已提交公开申请')
    await loadMyArtworks()
  } catch (error) {
    ElMessage.error(error?.message || '提交申请失败')
  }
}

async function openArtworkDetail(artwork) {
  detailVisible.value = true
  try {
    artworkDetail.value = await getArtworkDetail(artwork.id)
  } catch {
    artworkDetail.value = { ...artwork }
  }
}

async function saveArtworkDetail() {
  if (!artworkDetail.value?.id) return
  try {
    await updateArtwork(artworkDetail.value.id, artworkDetail.value)
    ElMessage.success('作品信息已更新')
    await refreshContent()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  }
}

async function archiveArtworkDetail() {
  if (!artworkDetail.value?.id) return
  try {
    await archiveArtwork(artworkDetail.value.id)
    ElMessage.success('作品已归档')
    detailVisible.value = false
    artworkDetail.value = null
    await loadMyArtworks()
  } catch (error) {
    ElMessage.error(error?.message || '归档失败')
  }
}

function reuseArtworkPrompt() {
  if (!artworkDetail.value) return
  freeText.value = artworkDetail.value.promptText || ''
  negativeText.value = artworkDetail.value.negativePromptText || ''
  composedPrompt.value = artworkDetail.value.promptText || ''
  composedNegative.value = artworkDetail.value.negativePromptText || ''
  activeView.value = 'workbench'
  detailVisible.value = false
}

function downloadCurrent() {
  if (!previewImage.value) return
  window.open(previewImage.value, '_blank', 'noopener')
}

async function refreshContent() {
  await Promise.all([loadMyArtworks(), loadCommunity(), refreshJobs()])
}

async function logout() {
  auth.logoutUser()
  ElMessage.success('已退出登录')
  await router.push({ name: 'auth' })
}

onMounted(async () => {
  await auth.hydrateAuth()
  await Promise.all([loadTags(), loadProvider()])
  if (currentUser.value) {
    await Promise.all([refreshContent(), loadStylePackages(), loadTasks(), loadPointAccount()])
    if (currentUser.value.role === 'ADMIN') {
      await refreshAdminData()
    }
  }
})
</script>
