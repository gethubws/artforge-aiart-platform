<template>
  <div class="app-shell">
    <AppTopbar
      :active-view="activeView"
      :current-user="currentUser"
      :unread-notification-count="unreadNotificationCount"
      @navigate="activateView"
      @refresh-provider="loadProvider"
      @refresh-artworks="refreshContent"
      @logout="logout"
      @open-notifications="openNotificationsCenter"
    />

    <main class="app-main">
      <WorkbenchPage
        v-if="activeView === 'workbench'"
        :generation-mode="generationMode"
        :free-text="freeText"
        :negative-text="negativeText"
        :tag-tree="tagTree"
        :tag-options="tagOptions"
        :selected-category-id="promptCategoryId"
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
        @load-tag-category="loadTagCategory"
        @load-more-tags="loadMoreTags"
        @prompt-category-change="promptCategoryId = $event"
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
        :query-state="libraryQuery"
        :selectable-tags="flatTags"
        :selected-artwork-count="selectedArtworkIds.size"
        :all-filtered-artworks-selected="allFilteredArtworksSelected"
        :library-batch-loading="libraryBatchLoading"
        :artworks="myArtworks"
        :is-artwork-selected="isArtworkSelected"
        :format-date="formatDate"
        :status-label="statusLabel"
        :is-favorite-target="isFavoriteTarget"
        @refresh="loadMyArtworks($event)"
        @toggle-all-filtered="toggleAllFiltered"
        @clear-selection="clearArtworkSelection"
        @publish-selected="publishSelected"
        @make-private-selected="makePrivateSelected"
        @archive-selected="archiveSelected"
        @open-detail="openArtworkDetail"
        @toggle-selection="toggleArtworkSelection"
        @request-publish="requestPublish"
        @toggle-favorite-target="toggleFavoriteTarget"
      />

      <CommunityPage
        v-else-if="activeView === 'community'"
        :query-state="communityQuery"
        :selectable-tags="flatTags"
        :public-artworks="publicArtworks"
        :format-date="formatDate"
        :is-favorite-target="isFavoriteTarget"
        @refresh="loadCommunity($event)"
        @open-detail="openArtworkDetail"
        @toggle-favorite-target="toggleFavoriteTarget"
      />

      <StyleMarketPage
        v-else-if="activeView === 'style-market'"
        :style-form="styleForm"
        :style-loading="styleLoading"
        :all-tags="flatTags"
        :owned-artworks="myArtworks"
        :query-state="styleMarketQuery"
        :market-style-packages="marketStylePackages"
        :my-style-package-submissions="myStylePackageSubmissions"
        :style-package-versions="stylePackageVersions"
        :style-package-reviews="stylePackageReviews"
        :active-style-package-name="activeStylePackageName"
        :style-submission-form="styleSubmissionForm"
        :style-review-form="styleReviewForm"
        :style-stats-items="styleStatsItems"
        :format-date="formatDate"
        :status-label="statusLabel"
        :is-favorite-target="isFavoriteTarget"
        :is-subscribed-target="isSubscribedTarget"
        :focus-target-id="engagementFocus.targetType === 'STYLE_PACKAGE' ? engagementFocus.targetId : null"
        :focus-target-stamp="engagementFocus.stamp"
        @refresh="loadStylePackages({ scope: 'market', query: $event })"
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
        @patch-submission-form="patchStyleSubmissionForm"
        @submit-submission="submitStyleSubmission"
        @prepare-review="prepareStyleReview"
        @load-reviews="loadStyleReviews"
        @patch-review-form="patchStyleReviewForm"
        @submit-review="submitStyleReview"
        @review-submission="reviewStyleSubmissionItem"
        @go-related="activateView"
        @toggle-favorite-target="toggleFavoriteTarget"
        @toggle-subscription-target="toggleSubscriptionTarget"
      />

      <MyStylePackagesPage
        v-else-if="activeView === 'my-styles'"
        :style-form="styleForm"
        :style-loading="styleLoading"
        :all-tags="flatTags"
        :owned-artworks="myArtworks"
        :query-state="myStyleQuery"
        :my-style-packages="myStylePackages"
        :my-style-package-submissions="myStylePackageSubmissions"
        :style-package-versions="stylePackageVersions"
        :style-package-reviews="stylePackageReviews"
        :style-submissions="styleSubmissions"
        :active-style-package-name="activeStylePackageName"
        :style-submission-form="styleSubmissionForm"
        :style-review-form="styleReviewForm"
        :style-stats-items="styleStatsItems"
        :format-date="formatDate"
        :status-label="statusLabel"
        :is-favorite-target="isFavoriteTarget"
        :is-subscribed-target="isSubscribedTarget"
        :focus-target-id="engagementFocus.targetType === 'STYLE_PACKAGE' ? engagementFocus.targetId : null"
        :focus-target-stamp="engagementFocus.stamp"
        @refresh="loadStylePackages({ scope: 'my', query: $event })"
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
        @patch-submission-form="patchStyleSubmissionForm"
        @submit-submission="submitStyleSubmission"
        @prepare-review="prepareStyleReview"
        @load-reviews="loadStyleReviews"
        @patch-review-form="patchStyleReviewForm"
        @submit-review="submitStyleReview"
        @review-submission="reviewStyleSubmissionItem"
        @go-related="activateView"
        @toggle-favorite-target="toggleFavoriteTarget"
        @toggle-subscription-target="toggleSubscriptionTarget"
      />

      <TaskMarketPage
        v-else-if="activeView === 'task-market'"
        :task-loading="taskLoading"
        :task-form="taskForm"
        :task-market="taskMarket"
        :query-state="taskMarketQuery"
        :submission-form="submissionForm"
        :my-artworks="myArtworks"
        :current-user="currentUser"
        :my-task-submissions="myTaskSubmissions"
        :selected-task-submissions="selectedTaskSubmissions"
        :task-review-form="taskReviewForm"
        :format-points="formatPoints"
        :format-deadline="formatDeadline"
        :status-label="statusLabel"
        :is-favorite-target="isFavoriteTarget"
        :is-subscribed-target="isSubscribedTarget"
        :focus-target-id="engagementFocus.targetType === 'TASK' ? engagementFocus.targetId : null"
        :focus-target-stamp="engagementFocus.stamp"
        @refresh="loadTasks({ scope: 'market', query: $event })"
        @reset-task-form="resetTaskForm"
        @patch-task-form="patchTaskForm"
        @save-task="saveTask"
        @prepare-submission="prepareTaskSubmission"
        @patch-submission-form="patchSubmissionForm"
        @submit-task-work="submitTaskWork"
        @edit-task="editTask"
        @publish-task-item="publishTaskItem"
        @close-task-item="closeTaskItem"
        @load-task-submissions="loadTaskSubmissions"
        @prepare-task-review="prepareTaskReview"
        @review-submission="reviewTaskSubmissionItem"
        @patch-task-review-form="patchTaskReviewForm"
        @go-related="activateView"
        @toggle-favorite-target="toggleFavoriteTarget"
        @toggle-subscription-target="toggleSubscriptionTarget"
      />

      <MyTasksPage
        v-else-if="activeView === 'my-tasks'"
        :task-loading="taskLoading"
        :task-form="taskForm"
        :query-state="myTaskQuery"
        :submission-form="submissionForm"
        :my-artworks="myArtworks"
        :current-user="currentUser"
        :my-task-submissions="myTaskSubmissions"
        :my-tasks="myTasks"
        :selected-task-submissions="selectedTaskSubmissions"
        :task-review-form="taskReviewForm"
        :format-points="formatPoints"
        :format-deadline="formatDeadline"
        :status-label="statusLabel"
        :is-favorite-target="isFavoriteTarget"
        :is-subscribed-target="isSubscribedTarget"
        :focus-target-id="engagementFocus.targetType === 'TASK' ? engagementFocus.targetId : null"
        :focus-target-stamp="engagementFocus.stamp"
        @refresh="loadTasks({ scope: 'my', query: $event })"
        @reset-task-form="resetTaskForm"
        @patch-task-form="patchTaskForm"
        @save-task="saveTask"
        @prepare-submission="prepareTaskSubmission"
        @patch-submission-form="patchSubmissionForm"
        @submit-task-work="submitTaskWork"
        @edit-task="editTask"
        @publish-task-item="publishTaskItem"
        @close-task-item="closeTaskItem"
        @load-task-submissions="loadTaskSubmissions"
        @prepare-task-review="prepareTaskReview"
        @review-submission="reviewTaskSubmissionItem"
        @patch-task-review-form="patchTaskReviewForm"
        @go-related="activateView"
        @toggle-favorite-target="toggleFavoriteTarget"
        @toggle-subscription-target="toggleSubscriptionTarget"
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
        :format-date="formatDate"
        :status-label="statusLabel"
        :my-task-submissions="myTaskSubmissions"
        :my-style-package-submissions="myStylePackageSubmissions"
        :my-tasks="myTasks"
        :my-style-packages="myStylePackages"
        :notifications="notifications"
        :unread-notification-count="unreadNotificationCount"
        :favorite-items="favoriteItems"
        :subscription-items="subscriptionItems"
        @claim-daily-points="claimDaily"
        @refresh-points="loadPointAccount"
        @logout="logout"
        @go-view="activateView"
        @mark-notification-read="markNotificationReadItem"
        @mark-all-notifications-read="markAllNotificationsReadItems"
        @open-target="openEngagementTarget"
        @toggle-favorite="toggleFavoriteTarget"
        @toggle-subscription="toggleSubscriptionTarget"
      />
    </main>

    <PromptBuilderExpanded
      :visible="promptBuilderVisible"
      :free-text="freeText"
      :negative-text="negativeText"
      :composed-prompt="composedPrompt"
      :composed-negative="composedNegative"
      :tag-tree="tagTree"
      :tag-options="tagOptions"
      :selected-category-id="promptCategoryId"
      :selected-tags="selectedTags"
      @close="promptBuilderVisible = false"
      @compose="composePrompt"
      @toggle-tag="toggleTag"
      @load-category="loadTagCategory"
      @load-more-tags="loadMoreTags"
      @search-tags="searchTags"
      @category-change="promptCategoryId = $event"
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
                <el-input v-model="artworkDetail.negativePrompt" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" />
              </el-form-item>
              <el-form-item label="作品标签">
                <el-select
                  v-model="artworkDetailTagIds"
                  multiple
                  filterable
                  clearable
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="选择作品标签"
                >
                  <el-option
                    v-for="tag in flatTags"
                    :key="tag.id"
                    :label="tag.displayNameZh ? `${tag.displayNameZh} / ${tag.name}` : tag.name"
                    :value="tag.id"
                  />
                </el-select>
              </el-form-item>
            </el-form>
            <div class="detail-actions">
              <el-button @click="toggleFavoriteTarget({ type: 'ARTWORK', target: artworkDetail })">
                {{ isFavoriteTarget('ARTWORK', artworkDetail) ? '取消收藏' : '收藏作品' }}
              </el-button>
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
import {
  buildPrompt,
  createTag,
  createTagCategory,
  deactivateTag,
  getAdminTagTree,
  getTagCategories,
  getTagOptions,
  getTags,
  updateTag
} from './api/tags'
import {
  archiveStylePackage,
  createStylePackage,
  exchangeStylePackage,
  getMarketStylePackages,
  getMyStylePackages,
  getMyStylePackageSubmissions,
  getStylePackageArtworks,
  getStylePackageDetail,
  getStylePackageReviews,
  getStylePackageSubmissions,
  getStylePackageVersions,
  publishStylePackage,
  reviewStylePackageSubmission,
  saveStylePackageReview,
  submitStylePackageArtwork,
  updateStylePackage
} from './api/stylePackages'
import {
  closeTask,
  createTask,
  getTaskDetail,
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
import {
  getFavoriteTargets,
  getNotifications,
  getSubscriptionTargets,
  markAllNotificationsRead,
  markNotificationRead,
  toggleFavoriteTarget as toggleFavoriteTargetApi,
  toggleSubscriptionTarget as toggleSubscriptionTargetApi
} from './api/engagement'

const router = useRouter()
const auth = useAuthStore()

const currentUser = computed(() => auth.currentUser.value)
const activeView = ref('workbench')
const promptBuilderVisible = ref(false)
const previewVisible = ref(false)
const detailVisible = ref(false)
const artworkDetail = ref(null)
const artworkDetailTagIds = ref([])

const generationMode = ref('txt2img')
const freeText = ref('')
const negativeText = ref('')
const composedPrompt = ref('')
const composedNegative = ref('')
const tagTree = ref([])
const tagOptions = ref([])
const promptCategoryId = ref(null)
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

const libraryQuery = reactive({ keyword: '', tagIds: [], visibility: '', status: '', page: 1, size: 12, hasNext: false })
const communityQuery = reactive({ keyword: '', tagIds: [], page: 1, size: 12, hasNext: false })
const myArtworks = ref([])
const publicArtworks = ref([])
const selectedArtworkIds = ref(new Set())
const libraryBatchLoading = ref(false)

const styleLoading = ref(false)
const myStylePackages = ref([])
const marketStylePackages = ref([])
const styleMarketQuery = reactive({ keyword: '', tagId: null, status: '', sort: 'latest', page: 1, size: 8, hasNext: false })
const myStyleQuery = reactive({ keyword: '', tagId: null, status: '', sort: 'latest', page: 1, size: 8, hasNext: false })
const stylePackageVersions = ref([])
const stylePackageReviews = ref([])
const styleSubmissions = ref([])
const myStylePackageSubmissions = ref([])
const activeStylePackageName = ref('')
const styleForm = reactive({
  id: null,
  name: '',
  coverImageUrl: '',
  description: '',
  styleStatement: '',
  promptGuide: '',
  negativePromptGuide: '',
  featuredArtworkId: null,
  tagNames: [],
  collaborators: [],
  pricePoints: 0
})
const styleSubmissionForm = reactive({
  artworkId: null,
  note: ''
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
const taskMarketQuery = reactive({ keyword: '', status: 'PUBLISHED', tier: '', sort: 'latest', page: 1, size: 9, hasNext: false })
const myTaskQuery = reactive({ keyword: '', status: '', tier: '', sort: 'latest', page: 1, size: 9, hasNext: false })
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

const notifications = ref([])
const unreadNotificationCount = ref(0)
const favoriteItems = ref([])
const subscriptionItems = ref([])
const engagementFocus = reactive({ targetType: '', targetId: null, stamp: 0 })

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
  displayNameZh: '',
  descriptionZh: '',
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

const loadedPromptTags = computed(() => tagTree.value.flatMap((category) => category.tags || []))
const flatTags = computed(() => tagOptions.value)
const previewImage = computed(() => artworkDetail.value?.imageUrl || myArtworks.value[0]?.imageUrl || '')

const allFilteredArtworksSelected = computed(() => {
  if (!myArtworks.value.length) return false
  return myArtworks.value.every((artwork) => selectedArtworkIds.value.has(artwork.id))
})

const filteredModels = computed(() => filterResources(models.value))
const filteredLoras = computed(() => filterResources(loras.value))
const filteredVaes = computed(() => filterResources(vaes.value))
const filteredUpscalers = computed(() => filterResources(upscalers.value))
const favoriteTargetKeys = computed(() => new Set((favoriteItems.value || []).map((item) => targetKey(item.targetType, item.targetId))))
const subscriptionTargetKeys = computed(() => new Set((subscriptionItems.value || []).map((item) => targetKey(item.targetType, item.targetId))))

function filterResources(list) {
  const keyword = modelResourceKeyword.value.trim().toLowerCase()
  if (!keyword) return list
  return list.filter((item) => resourceName(item).toLowerCase().includes(keyword))
}

function targetKey(type, targetId) {
  return `${String(type || '').toUpperCase()}:${targetId}`
}

function resolveTargetId(target) {
  return target?.targetId || target?.id || target?.artworkId || target?.relatedId || null
}

function upsertById(list, item) {
  if (!item?.id) return list
  const index = list.findIndex((entry) => entry.id === item.id)
  if (index === -1) {
    return [item, ...list]
  }
  const next = [...list]
  next[index] = { ...next[index], ...item }
  return next
}

function injectTaskTarget(target, view) {
  if (!target?.id) return
  if (view === 'my-tasks') {
    myTasks.value = upsertById(myTasks.value, target)
    return
  }
  taskMarket.value = upsertById(taskMarket.value, target)
}

function injectStyleTarget(target, view) {
  if (!target?.id) return
  if (view === 'my-styles') {
    myStylePackages.value = upsertById(myStylePackages.value, target)
    return
  }
  marketStylePackages.value = upsertById(marketStylePackages.value, target)
}

function isFavoriteTarget(type, target) {
  const targetId = resolveTargetId(target)
  if (!targetId) return false
  return favoriteTargetKeys.value.has(targetKey(type, targetId))
}

function isSubscribedTarget(type, target) {
  const targetId = resolveTargetId(target)
  if (!targetId) return false
  return subscriptionTargetKeys.value.has(targetKey(type, targetId))
}

function activateView(view) {
  if (view === 'admin' && currentUser.value?.role !== 'ADMIN') {
    ElMessage.warning('当前账号没有后台权限')
    return
  }
  activeView.value = view
}

function focusEngagementTarget(type, targetId) {
  engagementFocus.targetType = String(type || '').toUpperCase()
  engagementFocus.targetId = targetId || null
  engagementFocus.stamp += 1
}

function prepareEngagementView(view) {
  if (view === 'task-market') {
    Object.assign(taskMarketQuery, { keyword: '', status: 'PUBLISHED', tier: '', sort: 'latest', page: 1 })
  } else if (view === 'my-tasks') {
    Object.assign(myTaskQuery, { keyword: '', status: '', tier: '', sort: 'latest', page: 1 })
  } else if (view === 'style-market') {
    Object.assign(styleMarketQuery, { keyword: '', tagId: null, status: '', sort: 'latest', page: 1 })
  } else if (view === 'my-styles') {
    Object.assign(myStyleQuery, { keyword: '', tagId: null, status: '', sort: 'latest', page: 1 })
  } else if (view === 'community') {
    Object.assign(communityQuery, { keyword: '', tagIds: [], page: 1 })
  } else if (view === 'library') {
    Object.assign(libraryQuery, { keyword: '', tagIds: [], visibility: '', status: '', page: 1 })
  }
}

async function loadViewForEngagement(view) {
  if (view === 'task-market') {
    await loadTasks({ scope: 'market', query: taskMarketQuery })
  } else if (view === 'my-tasks') {
    await loadTasks({ scope: 'my', query: myTaskQuery })
  } else if (view === 'style-market') {
    await loadStylePackages({ scope: 'market', query: styleMarketQuery })
  } else if (view === 'my-styles') {
    await loadStylePackages({ scope: 'my', query: myStyleQuery })
  } else if (view === 'community') {
    await loadCommunity(communityQuery)
  } else if (view === 'library') {
    await loadMyArtworks(libraryQuery)
  }
}

function patchGenerationForm(patch) {
  Object.assign(generationForm, patch)
}

function patchStyleForm(patch) {
  Object.assign(styleForm, patch)
}

function patchStyleSubmissionForm(patch) {
  Object.assign(styleSubmissionForm, patch)
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
      tagIds: selectedTags.value
    })
    composedPrompt.value = result.prompt || ''
    composedNegative.value = result.negativePrompt || ''
  } catch {
    composedPrompt.value = [freeText.value, ...selectedTagObjects().map((tag) => tag.promptText).filter(Boolean)].filter(Boolean).join(', ')
    composedNegative.value = [negativeText.value, ...selectedTagObjects().map((tag) => tag.negativePromptText).filter(Boolean)].filter(Boolean).join(', ')
  }
}

function selectedTagObjects() {
  return loadedPromptTags.value.filter((tag) => selectedTags.value.includes(tag.id))
}

function toggleTag(tagId) {
  if (selectedTags.value.includes(tagId)) {
    selectedTags.value = selectedTags.value.filter((id) => id !== tagId)
  } else {
    selectedTags.value = [...selectedTags.value, tagId]
  }
  composePrompt()
}

function parseSettingsDraft(text, label) {
  if (!text?.trim()) return undefined
  try {
    const value = JSON.parse(text)
    if (!value || Array.isArray(value) || typeof value !== 'object') {
      throw new Error()
    }
    return value
  } catch {
    throw new Error(`${label} 必须是合法的 JSON 对象`)
  }
}

function buildGenerationPayload() {
  const payload = {
    title: generationForm.title || `作品 ${new Date().toLocaleString('zh-CN')}`,
    prompt: composedPrompt.value || freeText.value,
    negativePrompt: composedNegative.value || negativeText.value,
    freeText: freeText.value,
    negativeText: negativeText.value,
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
    overrideSettings: parseSettingsDraft(overrideSettingsJson.value, 'Override Settings'),
    extraPayload: parseSettingsDraft(extraPayloadJson.value, 'Extra Payload'),
    tagIds: [...selectedTags.value]
  }
  if (generationMode.value === 'img2img' && initImageBase64.value) {
    payload.initImages = [initImageBase64.value]
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

function settingsDraft(value) {
  if (!value || Array.isArray(value) || typeof value !== 'object') return ''
  return JSON.stringify(value, null, 2)
}

function applyGenerationContext(params = {}, fallbackTagIds = []) {
  generationForm.title = params.title || generationForm.title
  generationForm.model = params.model || generationForm.model
  generationForm.vae = params.vae || generationForm.vae
  generationForm.samplerName = params.samplerName || generationForm.samplerName
  generationForm.width = params.width || generationForm.width
  generationForm.height = params.height || generationForm.height
  generationForm.steps = params.steps || generationForm.steps
  generationForm.cfgScale = params.cfgScale || generationForm.cfgScale
  generationForm.seed = params.seed ?? generationForm.seed
  generationForm.clipSkip = params.clipSkip || generationForm.clipSkip
  generationForm.batchSize = params.batchSize || generationForm.batchSize
  generationForm.batchCount = params.batchCount || generationForm.batchCount
  generationForm.restoreFaces = Boolean(params.restoreFaces)
  generationForm.tiling = Boolean(params.tiling)
  generationForm.enableHr = Boolean(params.enableHr)
  generationForm.hrUpscaler = params.hrUpscaler || generationForm.hrUpscaler
  generationForm.denoisingStrength = params.denoisingStrength ?? generationForm.denoisingStrength
  generationForm.hrScale = params.hrScale ?? generationForm.hrScale
  generationForm.hrSecondPassSteps = params.hrSecondPassSteps ?? generationForm.hrSecondPassSteps

  freeText.value = params.freeText ?? params.prompt ?? freeText.value
  negativeText.value = params.negativeText ?? params.negativePrompt ?? negativeText.value
  composedPrompt.value = params.prompt || freeText.value
  composedNegative.value = params.negativePrompt || negativeText.value
  selectedTags.value = [...new Set(params.tagIds || params.selectedTagIds || fallbackTagIds)]
    .filter((id) => id != null)
    .map((id) => String(id))
  selectedLoras.value = (params.loras || []).map((item) => item.name).filter(Boolean)
  if (params.loras?.[0]?.weight != null) loraWeight.value = params.loras[0].weight
  overrideSettingsJson.value = settingsDraft(params.overrideSettings)
    || (typeof params.overrideSettingsJson === 'string' ? params.overrideSettingsJson : '')
  extraPayloadJson.value = settingsDraft(params.extraPayload)
    || (typeof params.extraPayloadJson === 'string' ? params.extraPayloadJson : '')
}

function restoreJob(item) {
  if (!item?.paramsJson) return
  try {
    applyGenerationContext(JSON.parse(item.paramsJson))
    ElMessage.success('已恢复提示词、标签和生成参数')
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
    STYLE_PACKAGE_EXCHANGE: '风格包兑换',
    STYLE_PACKAGE_SALE: '风格包售出',
    MANUAL: '人工调整'
  }
  return labels[reason] || reason || '积分变动'
}

function resourceName(item) {
  return item?.title || item?.model_name || item?.name || item?.filename || '未命名资源'
}

function loraLabel(item) {
  const trigger = suggestedLoraTrigger(item)
  return trigger ? `${resourceName(item)} 路 ${trigger}` : resourceName(item)
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
    const [categories, options] = await Promise.all([getTagCategories(), getTagOptions()])
    tagOptions.value = options || []
    tagTree.value = (categories || []).map((category) => ({
      ...category,
      tags: category.popularTags || [],
      page: 0,
      size: 8,
      total: category.tagCount || 0,
      hasNext: Number(category.tagCount || 0) > Number(category.popularTags?.length || 0),
      loaded: false,
      loading: false,
      keyword: ''
    }))
    if (tagTree.value[0]) {
      promptCategoryId.value = tagTree.value[0].id
      await loadTagCategory(tagTree.value[0].id, { reset: true })
    }
  } catch {
    tagTree.value = []
    tagOptions.value = []
  }
}

async function loadTagCategory(categoryId, options = {}) {
  const category = tagTree.value.find((item) => item.id === categoryId)
  if (!category || category.loading) return
  const keyword = options.keyword ?? category.keyword ?? ''
  const reset = Boolean(options.reset) || keyword !== (category.keyword || '')
  const append = Boolean(options.append)
  if (category.loaded && !reset && !append) return
  const nextPage = reset || !append ? 1 : Math.max(1, Number(category.page || 0) + 1)
  patchTagCategory(categoryId, { loading: true })
  try {
    const result = await getTags({ categoryId, keyword: keyword || undefined, page: nextPage, size: category.size || 12 })
    const currentCategory = tagTree.value.find((item) => item.id === categoryId)
    const currentTags = reset ? [] : (currentCategory?.tags || [])
    const merged = [...currentTags]
    for (const tag of result?.items || []) {
      const index = merged.findIndex((item) => item.id === tag.id)
      if (index >= 0) merged[index] = tag
      else merged.push(tag)
    }
    patchTagCategory(categoryId, {
      tags: merged,
      page: result?.page || nextPage,
      total: result?.total ?? merged.length,
      hasNext: Boolean(result?.hasNext),
      loaded: true,
      loading: false,
      keyword
    })
  } catch (error) {
    patchTagCategory(categoryId, { loading: false })
    ElMessage.error(error?.message || '标签加载失败')
  }
}

function patchTagCategory(categoryId, patch) {
  tagTree.value = tagTree.value.map((category) => category.id === categoryId ? { ...category, ...patch } : category)
}

function loadMoreTags(categoryId) {
  return loadTagCategory(categoryId, { append: true })
}

function searchTags({ categoryId, keyword }) {
  return loadTagCategory(categoryId, { keyword, reset: true })
}

function normalizeArtworkQuery(query = {}) {
  return {
    keyword: query.keyword || '',
    tagIds: Array.isArray(query.tagIds) ? query.tagIds.filter(Boolean) : [],
    visibility: query.visibility || '',
    status: query.status || '',
    page: Math.max(1, Number(query.page) || 1),
    size: Math.min(24, Math.max(6, Number(query.size) || 12)),
    hasNext: Boolean(query.hasNext)
  }
}

function artworkQueryParams(query, includeVisibility = true) {
  const normalized = normalizeArtworkQuery(query)
  return {
    keyword: normalized.keyword.trim() || undefined,
    tagIds: normalized.tagIds.length ? normalized.tagIds.join(',') : undefined,
    visibility: includeVisibility ? normalized.visibility || undefined : undefined,
    status: includeVisibility ? normalized.status || undefined : undefined,
    page: normalized.page,
    size: normalized.size
  }
}

async function loadMyArtworks(query) {
  try {
    if (query) Object.assign(libraryQuery, normalizeArtworkQuery(query))
    myArtworks.value = (await getMyArtworks(artworkQueryParams(libraryQuery, true))) || []
    libraryQuery.hasNext = myArtworks.value.length >= libraryQuery.size
  } catch {
    myArtworks.value = []
    libraryQuery.hasNext = false
  }
}

async function loadCommunity(query) {
  try {
    if (query) Object.assign(communityQuery, normalizeArtworkQuery(query))
    publicArtworks.value = (await getPublicArtworks(artworkQueryParams(communityQuery, false))) || []
    communityQuery.hasNext = publicArtworks.value.length >= communityQuery.size
  } catch {
    publicArtworks.value = []
    communityQuery.hasNext = false
  }
}

function normalizeStyleQuery(query = {}) {
  return {
    keyword: query.keyword || '',
    tagId: query.tagId ?? null,
    status: query.status || '',
    sort: query.sort || 'latest',
    page: Math.max(1, Number(query.page) || 1),
    size: Math.min(24, Math.max(4, Number(query.size) || 8)),
    hasNext: Boolean(query.hasNext)
  }
}

function styleQueryParams(query) {
  const normalized = normalizeStyleQuery(query)
  return {
    keyword: normalized.keyword.trim() || undefined,
    tagId: normalized.tagId || undefined,
    status: normalized.status || undefined,
    sort: normalized.sort || undefined,
    page: normalized.page,
    size: normalized.size
  }
}

async function loadStylePackages(options = {}) {
  styleLoading.value = true
  try {
    const scope = options.scope || 'all'
    if (scope === 'market' && options.query) Object.assign(styleMarketQuery, normalizeStyleQuery(options.query))
    if (scope === 'my' && options.query) Object.assign(myStyleQuery, normalizeStyleQuery(options.query))

    if (scope === 'market') {
      marketStylePackages.value = (await getMarketStylePackages(styleQueryParams(styleMarketQuery))) || []
      styleMarketQuery.hasNext = marketStylePackages.value.length >= styleMarketQuery.size
      return
    }

    if (scope === 'my') {
      myStylePackages.value = (await getMyStylePackages(styleQueryParams(myStyleQuery))) || []
      myStyleQuery.hasNext = myStylePackages.value.length >= myStyleQuery.size
      return
    }

    const [mine, market] = await Promise.all([
      getMyStylePackages(styleQueryParams(myStyleQuery)),
      getMarketStylePackages(styleQueryParams(styleMarketQuery))
    ])
    myStylePackages.value = mine || []
    marketStylePackages.value = market || []
    myStyleQuery.hasNext = myStylePackages.value.length >= myStyleQuery.size
    styleMarketQuery.hasNext = marketStylePackages.value.length >= styleMarketQuery.size
  } finally {
    styleLoading.value = false
  }
}

async function loadMyStyleSubmissions() {
  try {
    myStylePackageSubmissions.value = (await getMyStylePackageSubmissions()) || []
  } catch {
    myStylePackageSubmissions.value = []
  }
}

function normalizeTaskQuery(query = {}) {
  return {
    keyword: query.keyword || '',
    status: query.status || '',
    tier: query.tier || '',
    sort: query.sort || 'latest',
    page: Math.max(1, Number(query.page) || 1),
    size: Math.min(24, Math.max(4, Number(query.size) || 9)),
    hasNext: Boolean(query.hasNext)
  }
}

function taskQueryParams(query) {
  const normalized = normalizeTaskQuery(query)
  return {
    keyword: normalized.keyword.trim() || undefined,
    status: normalized.status || undefined,
    tier: normalized.tier || undefined,
    sort: normalized.sort || undefined,
    page: normalized.page,
    size: normalized.size
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
  const artworks = ((await getStylePackageArtworks(pack.id)) || []).map((item) => ({
    id: item.artworkId || item.id,
    title: item.artworkTitle || item.title || '作品',
    imageUrl: item.artworkImageUrl || item.imageUrl || '',
    status: item.status || 'APPROVED',
    visibility: item.visibility || 'PUBLIC'
  }))
  const patchCollection = (items) => items.map((item) => {
    if (item.id !== pack.id) return item
    return {
      ...item,
      artworks,
      stats: {
        ...(item.stats || {}),
        approvedArtworkCount: artworks.length
      }
    }
  })
  myStylePackages.value = patchCollection(myStylePackages.value)
  marketStylePackages.value = patchCollection(marketStylePackages.value)
}

function resetStyleForm() {
  Object.assign(styleForm, {
    id: null,
    name: '',
    coverImageUrl: '',
    description: '',
    styleStatement: '',
    promptGuide: '',
    negativePromptGuide: '',
    featuredArtworkId: null,
    tagNames: [],
    collaborators: [],
    pricePoints: 0
  })
}

function editStylePackage(pack) {
  Object.assign(styleForm, {
    id: pack.id,
    name: pack.name || '',
    coverImageUrl: pack.coverImageUrl || '',
    description: pack.description || '',
    styleStatement: pack.styleStatement || '',
    promptGuide: pack.promptGuide || '',
    negativePromptGuide: pack.negativePromptGuide || '',
    featuredArtworkId: pack.featuredArtworkId || null,
    tagNames: (pack.tags || []).map((tag) => tag.name).filter(Boolean),
    collaborators: (pack.collaborators || []).map((user) => ({
      userId: user.userId,
      role: user.role || 'CONTRIBUTOR'
    })),
    pricePoints: pack.pricePoints || 0
  })
}

async function saveStylePackage() {
  styleLoading.value = true
  try {
    const payload = {
      name: styleForm.name,
      coverImageUrl: styleForm.coverImageUrl,
      description: styleForm.description,
      styleStatement: styleForm.styleStatement,
      promptGuide: styleForm.promptGuide,
      negativePromptGuide: styleForm.negativePromptGuide,
      featuredArtworkId: styleForm.featuredArtworkId,
      pricePoints: styleForm.pricePoints,
      tagNames: styleForm.tagNames,
      collaborators: styleForm.collaborators
    }
    if (styleForm.id) {
      await updateStylePackage(styleForm.id, payload)
    } else {
      await createStylePackage(payload)
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
  freeText.value = [freeText.value, pack.promptGuide || pack.styleStatement].filter(Boolean).join(', ')
  negativeText.value = [negativeText.value, pack.negativePromptGuide].filter(Boolean).join(', ')
  selectedTags.value = [...new Set([
    ...selectedTags.value,
    ...(pack.tags || []).map((tag) => tag.id).filter(Boolean)
  ])]
  composePrompt()
  activeView.value = 'workbench'
  ElMessage.success('已将风格包提示词和标签应用到工作台')
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
    ElMessage.success('风格包已归档')
    await loadStylePackages()
  } catch (error) {
    ElMessage.error(error?.message || '归档失败')
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
  Object.assign(styleSubmissionForm, {
    artworkId: null,
    note: ''
  })
}

function prepareStyleReview(pack) {
  styleReviewContext.value = pack
  patchStyleReviewForm({ rating: 5, comment: '' })
}

async function submitStyleSubmission() {
  if (!styleSubmissionContext.value?.id || !styleSubmissionForm.artworkId) {
    ElMessage.warning('请先选择要投稿的作品')
    return
  }
  styleLoading.value = true
  try {
    await submitStylePackageArtwork(styleSubmissionContext.value.id, {
      artworkId: styleSubmissionForm.artworkId,
      note: styleSubmissionForm.note
    })
    ElMessage.success('风格包投稿已提交')
    Object.assign(styleSubmissionForm, { artworkId: null, note: '' })
    await loadMyStyleSubmissions()
    if (styleSubmissionContext.value?.owner) {
      await loadStyleSubmissions(styleSubmissionContext.value)
    }
  } catch (error) {
    ElMessage.error(error?.message || '提交投稿失败')
  } finally {
    styleLoading.value = false
  }
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

async function reviewStyleSubmissionItem({ submission, status, comment }) {
  try {
    await reviewStylePackageSubmission(submission.id, { status, comment })
    ElMessage.success('投稿审核已更新')
    if (styleSubmissionContext.value) {
      await loadStyleSubmissions(styleSubmissionContext.value)
      await loadStyleArtworks(styleSubmissionContext.value)
    }
    await loadMyStyleSubmissions()
    await loadStylePackages()
  } catch (error) {
    ElMessage.error(error?.message || '审核失败')
  }
}

async function loadTasks(options = {}) {
  taskLoading.value = true
  try {
    const scope = options.scope || 'all'
    if (scope === 'market' && options.query) Object.assign(taskMarketQuery, normalizeTaskQuery(options.query))
    if (scope === 'my' && options.query) Object.assign(myTaskQuery, normalizeTaskQuery(options.query))

    if (scope === 'market') {
      const [market, mineSubs] = await Promise.all([
        getTaskMarket(taskQueryParams(taskMarketQuery)),
        getMyTaskSubmissions()
      ])
      taskMarket.value = market || []
      myTaskSubmissions.value = mineSubs || []
      taskMarketQuery.hasNext = taskMarket.value.length >= taskMarketQuery.size
      return
    }

    if (scope === 'my') {
      myTasks.value = (await getMyTasks(taskQueryParams(myTaskQuery))) || []
      myTaskQuery.hasNext = myTasks.value.length >= myTaskQuery.size
      return
    }

    const [market, mine, mineSubs] = await Promise.all([
      getTaskMarket(taskQueryParams(taskMarketQuery)),
      getMyTasks(taskQueryParams(myTaskQuery)),
      getMyTaskSubmissions()
    ])
    taskMarket.value = market || []
    myTasks.value = mine || []
    myTaskSubmissions.value = mineSubs || []
    taskMarketQuery.hasNext = taskMarket.value.length >= taskMarketQuery.size
    myTaskQuery.hasNext = myTasks.value.length >= myTaskQuery.size
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

async function loadNotifications() {
  if (!currentUser.value) {
    notifications.value = []
    unreadNotificationCount.value = 0
    return
  }
  try {
    const feed = await getNotifications({ page: 1, size: 40 })
    notifications.value = feed?.items || []
    unreadNotificationCount.value = Number(feed?.unreadCount || 0)
  } catch {
    notifications.value = []
    unreadNotificationCount.value = 0
  }
}

async function loadFavoriteItems() {
  if (!currentUser.value) {
    favoriteItems.value = []
    return
  }
  try {
    favoriteItems.value = (await getFavoriteTargets()) || []
  } catch {
    favoriteItems.value = []
  }
}

async function loadSubscriptionItems() {
  if (!currentUser.value) {
    subscriptionItems.value = []
    return
  }
  try {
    subscriptionItems.value = (await getSubscriptionTargets()) || []
  } catch {
    subscriptionItems.value = []
  }
}

async function loadEngagementData() {
  await Promise.all([loadNotifications(), loadFavoriteItems(), loadSubscriptionItems()])
}

async function openNotificationsCenter() {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    return
  }
  await loadNotifications()
  activateView('account')
}

async function toggleFavoriteTarget({ type, target }) {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    return
  }
  const targetId = resolveTargetId(target)
  if (!type || !targetId) {
    ElMessage.warning('无法识别要收藏的内容')
    return
  }
  try {
    const result = await toggleFavoriteTargetApi({ targetType: type, targetId })
    await loadFavoriteItems()
    ElMessage.success(result?.active ? '已加入收藏' : '已取消收藏')
  } catch (error) {
    ElMessage.error(error?.message || '收藏操作失败')
  }
}

async function toggleSubscriptionTarget({ type, target }) {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    return
  }
  const targetId = resolveTargetId(target)
  if (!type || !targetId) {
    ElMessage.warning('无法识别要订阅的内容')
    return
  }
  try {
    const result = await toggleSubscriptionTargetApi({ targetType: type, targetId })
    await Promise.all([loadSubscriptionItems(), loadNotifications()])
    ElMessage.success(result?.active ? '已开启订阅' : '已取消订阅')
  } catch (error) {
    ElMessage.error(error?.message || '订阅操作失败')
  }
}

async function markNotificationReadItem(notification) {
  if (!notification?.id) return
  try {
    await markNotificationRead(notification.id)
    notifications.value = notifications.value.map((item) =>
      item.id === notification.id ? { ...item, read: true } : item
    )
    unreadNotificationCount.value = Math.max(0, unreadNotificationCount.value - 1)
  } catch (error) {
    ElMessage.error(error?.message || '标记已读失败')
  }
}

async function markAllNotificationsReadItems() {
  try {
    await markAllNotificationsRead()
    notifications.value = notifications.value.map((item) => ({ ...item, read: true }))
    unreadNotificationCount.value = 0
    ElMessage.success('通知已全部标记为已读')
  } catch (error) {
    ElMessage.error(error?.message || '批量已读失败')
  }
}

async function openEngagementTarget(item) {
  const targetView = item?.routeView || 'account'
  const targetType = item?.targetType || item?.relatedType
  const targetId = item?.targetId || item?.relatedId
  prepareEngagementView(targetView)
  await loadViewForEngagement(targetView)
  if (targetType === 'TASK' && targetId) {
    try {
      injectTaskTarget(await getTaskDetail(targetId), targetView)
    } catch {
      // Keep the page usable even if the direct detail fetch fails.
    }
  }
  if (targetType === 'STYLE_PACKAGE' && targetId) {
    try {
      injectStyleTarget(await getStylePackageDetail(targetId), targetView)
    } catch {
      // Keep the page usable even if the direct detail fetch fails.
    }
  }
  activateView(targetView)
  if (targetType === 'ARTWORK' && targetId) {
    openArtworkDetail({ id: targetId, title: item?.title || '作品' })
    return
  }
  if (targetId) {
    focusEngagementTarget(targetType, targetId)
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
  const entries = Array.isArray(data)
    ? data.map((item) => [item?.status, item?.count])
    : Object.entries(data || {})
  return entries
    .filter(([key]) => key)
    .map(([key, value]) => `${statusLabel(key)} ${Number(value || 0)}`)
    .join(' · ') || '暂无数据'
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
  Object.assign(tagForm, {
    id: null,
    categoryId: null,
    name: '',
    displayNameZh: '',
    descriptionZh: '',
    promptText: '',
    negativePromptText: '',
    previewImageUrl: '',
    weight: 1,
    visibility: 'PUBLIC'
  })
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
    displayNameZh: tag.displayNameZh || '',
    descriptionZh: tag.descriptionZh || '',
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
    { label: '收录', value: stats.approvedArtworkCount || pack?.artworks?.length || 0 },
    { label: '协作', value: stats.collaboratorCount || 0 },
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
  selectedArtworkIds.value = new Set(myArtworks.value.map((artwork) => artwork.id))
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
  artworkDetailTagIds.value = (artworkDetail.value?.tags || []).map((tag) => tag.id).filter(Boolean)
}

async function saveArtworkDetail() {
  if (!artworkDetail.value?.id) return
  try {
    artworkDetail.value = await updateArtwork(artworkDetail.value.id, {
      title: artworkDetail.value.title,
      promptText: artworkDetail.value.promptText || '',
      negativePrompt: artworkDetail.value.negativePrompt || '',
      visibility: artworkDetail.value.visibility,
      tagIds: artworkDetailTagIds.value
    })
    artworkDetailTagIds.value = (artworkDetail.value?.tags || []).map((tag) => tag.id).filter(Boolean)
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
  let params = {
    prompt: artworkDetail.value.promptText || '',
    negativePrompt: artworkDetail.value.negativePrompt || ''
  }
  try {
    const metadata = JSON.parse(artworkDetail.value.generationParamsJson || '{}')
    const restored = metadata.request || metadata
    if (restored && Object.keys(restored).length) {
      params = { ...params, ...restored }
    }
  } catch {
    // Older artworks may not have structured generation metadata.
  }
  applyGenerationContext(params, artworkDetailTagIds.value)
  activeView.value = 'workbench'
  detailVisible.value = false
  ElMessage.success('已恢复作品的提示词、标签和生成参数')
}

function downloadCurrent() {
  if (!previewImage.value) return
  window.open(previewImage.value, '_blank', 'noopener')
}

async function refreshContent() {
  const jobs = [loadMyArtworks(), loadCommunity(), refreshJobs()]
  if (currentUser.value) {
    jobs.push(loadNotifications())
  }
  await Promise.all(jobs)
}

async function logout() {
  auth.logoutUser()
  notifications.value = []
  unreadNotificationCount.value = 0
  favoriteItems.value = []
  subscriptionItems.value = []
  ElMessage.success('已退出登录')
  await router.push({ name: 'auth' })
}

onMounted(async () => {
  await auth.hydrateAuth()
  await Promise.all([loadTags(), loadProvider()])
  if (currentUser.value) {
    await Promise.all([
      refreshContent(),
      loadStylePackages(),
      loadMyStyleSubmissions(),
      loadTasks(),
      loadPointAccount(),
      loadEngagementData()
    ])
    if (currentUser.value.role === 'ADMIN') {
      await refreshAdminData()
    }
  }
})
</script>
