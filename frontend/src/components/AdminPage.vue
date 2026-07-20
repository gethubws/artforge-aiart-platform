<template>
  <section class="admin-view">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Operations</p>
        <h1>后台运营</h1>
      </div>
      <div class="library-tools">
        <el-button :icon="Refresh" :loading="auditLoading || tagAdminLoading || adminDashboardLoading" @click="$emit('refresh')">
          刷新
        </el-button>
      </div>
    </div>

    <section v-if="adminDashboard" class="admin-dashboard">
      <div class="dashboard-metrics">
        <article v-for="metric in adminDashboard.metrics" :key="metric.key" class="dashboard-metric" :class="metric.tone">
          <span>{{ metric.label }}</span>
          <strong>{{ formatMetricValue(metric) }}</strong>
          <em>{{ metric.unit }}</em>
        </article>
      </div>

      <div class="dashboard-grid">
        <article class="dashboard-panel"><h2>生成状态</h2><p>{{ statusCountText(adminDashboard.generationStatus) }}</p></article>
        <article class="dashboard-panel"><h2>审核队列</h2><p>{{ statusCountText(adminDashboard.auditStatus) }}</p></article>
        <article class="dashboard-panel"><h2>任务状态</h2><p>{{ statusCountText(adminDashboard.taskStatus) }}</p></article>
        <article class="dashboard-panel"><h2>风格包状态</h2><p>{{ statusCountText(adminDashboard.styleStatus) }}</p></article>
      </div>

      <div class="dashboard-grid wide">
        <article class="dashboard-panel">
          <h2>近 7 天生成</h2>
          <div class="daily-bars">
            <span v-for="item in adminDashboard.dailyGenerations" :key="item.date">
              <i :style="{ height: `${Math.max(8, Math.min(72, item.count * 12))}px` }"></i>
              <b>{{ item.count }}</b>
              <em>{{ formatDay(item.date) }}</em>
            </span>
          </div>
        </article>

        <article class="dashboard-panel">
          <h2>近 7 天作品</h2>
          <div class="daily-bars">
            <span v-for="item in adminDashboard.dailyArtworks" :key="item.date">
              <i :style="{ height: `${Math.max(8, Math.min(72, item.count * 12))}px` }"></i>
              <b>{{ item.count }}</b>
              <em>{{ formatDay(item.date) }}</em>
            </span>
          </div>
        </article>

        <article class="dashboard-panel">
          <h2>积分流向</h2>
          <div class="flow-list">
            <span v-for="flow in adminDashboard.pointFlows" :key="flow.reason">
              <strong>{{ pointReasonLabel(flow.reason) }}</strong>
              <em>入 {{ formatPoints(flow.totalIn) }} / 出 {{ formatPoints(flow.totalOut) }}</em>
            </span>
            <p v-if="!adminDashboard.pointFlows.length">暂无积分流动</p>
          </div>
        </article>

        <article class="dashboard-panel">
          <h2>近期活动</h2>
          <div class="activity-list">
            <span v-for="activity in adminDashboard.recentActivities" :key="`${activity.type}-${activity.createdAt}-${activity.title}`">
              <strong>{{ activity.type }}</strong>
              <em>{{ activity.title }}</em>
              <b>{{ statusLabel(activity.status) }}</b>
            </span>
          </div>
        </article>
      </div>
    </section>

    <section class="tag-analytics-grid">
      <article class="soft-panel tag-analytics-panel">
        <div class="section-title-row"><h2>热门标签</h2><span>按提示词使用次数</span></div>
        <div class="analytics-list">
          <span v-for="tag in tagAnalytics.topTags.slice(0, 10)" :key="tag.id">
            <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
            <strong>{{ tag.usageCount }}</strong>
          </span>
          <p v-if="!tagAnalytics.topTags.length" class="quiet-empty compact-empty">使用数据将在用户拼接标签后出现</p>
        </div>
      </article>

      <article class="soft-panel tag-analytics-panel">
        <div class="section-title-row"><h2>常用组合</h2><span>同时选择的标签</span></div>
        <div class="analytics-list combination-list">
          <span v-for="item in tagAnalytics.topCombinations.slice(0, 10)" :key="`${item.tagAId}-${item.tagBId}`">
            <em>{{ item.tagADisplayNameZh || item.tagAName }} + {{ item.tagBDisplayNameZh || item.tagBName }}</em>
            <strong>{{ item.usageCount }}</strong>
          </span>
          <p v-if="!tagAnalytics.topCombinations.length" class="quiet-empty compact-empty">组合数据将在用户同时选择多个标签后出现</p>
        </div>
      </article>

      <article class="soft-panel tag-analytics-panel">
        <div class="section-title-row"><h2>搜索观察</h2><span>零结果词优先处理</span></div>
        <div class="analytics-list search-analytics-list">
          <span v-for="item in tagAnalytics.searchKeywords.slice(0, 12)" :key="item.keyword" :class="{ warning: item.zeroResultCount > 0 }">
            <em>{{ item.keyword }}</em>
            <small>搜索 {{ item.searchCount }} · 零结果 {{ item.zeroResultCount }}</small>
          </span>
          <p v-if="!tagAnalytics.searchKeywords.length" class="quiet-empty compact-empty">用户搜索标签后会在这里形成统计</p>
        </div>
      </article>
    </section>

    <div class="admin-ops-layout">
      <section class="soft-panel admin-tool-panel">
        <div class="section-title-row"><h2>标签分类</h2></div>
        <el-form label-position="top">
          <el-form-item label="分类名称"><el-input v-model="categoryForm.name" placeholder="镜头 / 光线 / 材质" /></el-form-item>
          <el-form-item label="Slug"><el-input v-model="categoryForm.slug" placeholder="camera / light / material" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="categoryForm.sortOrder" :min="0" :step="10" controls-position="right" /></el-form-item>
          <el-button type="primary" class="wide-button" :loading="tagAdminLoading" @click="$emit('save-tag-category')">创建分类</el-button>
        </el-form>
      </section>

      <section class="soft-panel admin-tool-panel">
        <div class="section-title-row">
          <h2>{{ tagForm.id ? '编辑标签' : '新建标签' }}</h2>
          <el-button v-if="tagForm.id" size="small" @click="$emit('reset-tag-form')">清空</el-button>
        </div>
        <el-form label-position="top">
          <el-form-item label="所属分类">
            <el-select v-model="tagForm.categoryId" filterable placeholder="选择分类">
              <el-option v-for="category in adminTagTree" :key="category.id" :label="category.name" :value="category.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="英文标签名"><el-input v-model="tagForm.name" placeholder="cinematic / rim light / close-up" /></el-form-item>
          <el-form-item label="中文显示名"><el-input v-model="tagForm.displayNameZh" placeholder="电影感 / 轮廓光 / 近景" /></el-form-item>
          <el-form-item label="中文说明"><el-input v-model="tagForm.descriptionZh" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" /></el-form-item>
          <el-form-item label="正向提示词"><el-input v-model="tagForm.promptText" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" resize="none" /></el-form-item>
          <el-form-item label="反向提示词"><el-input v-model="tagForm.negativePromptText" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" /></el-form-item>
          <el-form-item label="预览图 URL"><el-input v-model="tagForm.previewImageUrl" placeholder="https://... 或 /images/tags/..." /></el-form-item>
          <div v-if="tagForm.previewImageUrl" class="tag-preview-box">
            <img :src="tagForm.previewImageUrl" alt="标签预览图" />
          </div>
          <div class="tag-form-grid">
            <el-form-item label="权重"><el-input-number v-model="tagForm.weight" :min="0.1" :max="3" :step="0.05" controls-position="right" /></el-form-item>
            <el-form-item label="可见性"><el-select v-model="tagForm.visibility"><el-option label="公开" value="PUBLIC" /><el-option label="私有" value="PRIVATE" /></el-select></el-form-item>
          </div>
          <el-button type="primary" class="wide-button" :loading="tagAdminLoading" @click="$emit('save-tag-item')">保存标签</el-button>
        </el-form>
      </section>

      <section class="soft-panel admin-tag-list-panel">
        <div class="section-title-row"><h2>标签知识库</h2><span>{{ flatTags.length }} 个标签</span></div>
        <div class="admin-tag-groups">
          <div v-for="category in adminTagTree" :key="category.id" class="admin-tag-group">
            <div class="admin-tag-group-head"><strong>{{ category.name }}</strong><span>{{ category.slug }}</span></div>
            <article v-for="tag in category.tags" :key="tag.id" class="admin-tag-row">
              <div class="admin-tag-copy">
                <strong><BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" /></strong>
                <span v-if="tag.descriptionZh">{{ tag.descriptionZh }}</span>
                <span>{{ tag.promptText }}</span>
                <div v-if="tag.previewImageUrl" class="mini-preview-chip">含预览图</div>
              </div>
              <div class="submission-actions">
                <el-button size="small" :icon="PictureFilled" @click="$emit('open-tag-gallery', tag)">图库</el-button>
                <el-button size="small" @click="$emit('edit-tag-item', { category, tag })">编辑</el-button>
                <el-button size="small" @click="$emit('disable-tag-item', tag)">停用</el-button>
              </div>
            </article>
            <div v-if="!category.tags.length" class="quiet-empty compact-empty">暂无标签</div>
          </div>
        </div>
      </section>
    </div>

    <div class="audit-layout">
      <section class="audit-column">
        <div class="section-title-row"><h2>待审核作品</h2></div>
        <div class="submission-list">
          <article v-for="audit in pendingAudits" :key="audit.id" class="submission-row reviewable">
            <img v-if="audit.artworkImageUrl" :src="audit.artworkImageUrl" :alt="audit.artworkTitle" />
            <div>
              <strong>{{ audit.artworkTitle || audit.contentType }}</strong>
              <span>{{ statusLabel(audit.status) }} · {{ formatDate(audit.createdAt) }}</span>
            </div>
            <div class="submission-actions">
              <el-button size="small" type="primary" @click="$emit('review-content-audit', { audit, status: 'APPROVED' })">通过</el-button>
              <el-button size="small" @click="$emit('review-content-audit', { audit, status: 'REJECTED' })">拒绝</el-button>
            </div>
          </article>
          <div v-if="!pendingAudits.length" class="quiet-empty">暂无待审核内容</div>
        </div>
      </section>

      <section class="audit-column">
        <div class="section-title-row"><h2>我的公开申请</h2></div>
        <div class="submission-list">
          <article v-for="audit in myAudits" :key="audit.id" class="submission-row">
            <img v-if="audit.artworkImageUrl" :src="audit.artworkImageUrl" :alt="audit.artworkTitle" />
            <div>
              <strong>{{ audit.artworkTitle || audit.contentType }}</strong>
              <span>{{ statusLabel(audit.status) }} · {{ formatDate(audit.createdAt) }}</span>
            </div>
          </article>
          <div v-if="!myAudits.length" class="quiet-empty">暂无公开申请</div>
        </div>
      </section>
    </div>

    <el-drawer
      :model-value="previewManagerVisible"
      size="min(920px, 96vw)"
      class="tag-gallery-admin-drawer"
      @close="closePreviewManager"
    >
      <template #header>
        <div class="tag-gallery-admin-head">
          <div>
            <p class="eyebrow">Tag Gallery</p>
            <h2>{{ activeTagDetail?.tag?.displayNameZh || activeTagDetail?.tag?.name || '标签图库' }}</h2>
          </div>
          <span>{{ activeTagDetail?.previews?.length || 0 }} 张图片</span>
        </div>
      </template>

      <div v-loading="tagPreviewLoading" class="tag-gallery-admin-body">
        <section class="soft-panel embedded-panel tag-preview-editor-panel">
          <div class="section-title-row">
            <h2>{{ previewForm.id ? '编辑 / 替换预览图' : '上传预览图' }}</h2>
            <el-button v-if="previewForm.id" size="small" @click="resetPreviewEditor">取消编辑</el-button>
          </div>
          <div class="tag-preview-editor-grid">
            <el-upload
              v-model:file-list="previewFileList"
              drag
              :auto-upload="false"
              :limit="1"
              accept="image/png,image/jpeg,image/webp,image/gif"
              :on-change="handlePreviewFileChange"
              :on-remove="clearPreviewFile"
              class="tag-preview-upload"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖入图片或点击选择</div>
              <template #tip><span>支持 PNG、JPG、WebP、GIF，编辑状态下选择文件即替换原图。</span></template>
            </el-upload>

            <el-form label-position="top" class="tag-preview-meta-form">
              <div class="tag-form-grid">
                <el-form-item label="预览类型">
                  <el-select v-model="previewForm.previewType">
                    <el-option label="封面" value="COVER" />
                    <el-option label="普通示例" value="EXAMPLE" />
                    <el-option label="对比示例" value="COMPARISON" />
                  </el-select>
                </el-form-item>
                <el-form-item label="场景类型">
                  <el-input v-model="previewForm.sceneKey" placeholder="CHARACTER / LANDSCAPE / GENERAL" />
                </el-form-item>
              </div>
              <el-form-item label="中文标题"><el-input v-model="previewForm.titleZh" placeholder="人物示例 / 风景对比" /></el-form-item>
              <el-form-item label="提示词快照"><el-input v-model="previewForm.promptSnapshot" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" /></el-form-item>
              <div class="tag-form-grid">
                <el-form-item label="排序"><el-input-number v-model="previewForm.sortOrder" :min="0" :step="10" controls-position="right" /></el-form-item>
                <el-form-item label="设为封面"><el-switch v-model="previewForm.cover" /></el-form-item>
              </div>
              <el-button type="primary" class="wide-button" :loading="tagPreviewLoading" @click="submitPreview">
                {{ previewForm.id ? '保存修改' : '上传图片' }}
              </el-button>
            </el-form>
          </div>
        </section>

        <section class="tag-preview-admin-grid">
          <article v-for="(preview, index) in activeTagDetail?.previews || []" :key="preview.id" class="tag-preview-admin-card" :class="{ cover: preview.cover }">
            <div class="tag-preview-admin-media">
              <img :src="preview.imageUrl" :alt="preview.titleZh || activeTagDetail?.tag?.name" loading="lazy" />
              <span v-if="preview.cover">当前封面</span>
            </div>
            <div class="tag-preview-admin-copy">
              <strong>{{ preview.titleZh || '未命名预览' }}</strong>
              <span>{{ preview.previewType }} · {{ preview.sceneKey }} · 排序 {{ preview.sortOrder }}</span>
            </div>
            <div class="tag-preview-admin-actions">
              <el-tooltip content="前移" placement="top"><el-button :icon="ArrowUp" circle size="small" :disabled="index === 0" @click="$emit('move-tag-preview', { preview, offset: -1 })" /></el-tooltip>
              <el-tooltip content="后移" placement="top"><el-button :icon="ArrowDown" circle size="small" :disabled="index === (activeTagDetail?.previews?.length || 0) - 1" @click="$emit('move-tag-preview', { preview, offset: 1 })" /></el-tooltip>
              <el-button size="small" @click="startPreviewEdit(preview)">编辑</el-button>
              <el-button v-if="!preview.cover" size="small" @click="$emit('set-tag-preview-cover', preview)">设为封面</el-button>
              <el-popconfirm title="确定删除这张预览图吗？" @confirm="$emit('delete-tag-preview', preview)">
                <template #reference><el-button size="small" type="danger" plain>删除</el-button></template>
              </el-popconfirm>
            </div>
          </article>
          <div v-if="!activeTagDetail?.previews?.length" class="quiet-empty">这个标签还没有预览图</div>
        </section>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowDown, ArrowUp, PictureFilled, Refresh, UploadFilled } from '@element-plus/icons-vue'
import BilingualTagLabel from './BilingualTagLabel.vue'

defineProps({
  auditLoading: { type: Boolean, default: false },
  tagAdminLoading: { type: Boolean, default: false },
  adminDashboardLoading: { type: Boolean, default: false },
  adminDashboard: { type: Object, default: null },
  tagAnalytics: { type: Object, default: () => ({ topTags: [], topCombinations: [], searchKeywords: [] }) },
  categoryForm: { type: Object, required: true },
  tagForm: { type: Object, required: true },
  adminTagTree: { type: Array, default: () => [] },
  flatTags: { type: Array, default: () => [] },
  previewManagerVisible: { type: Boolean, default: false },
  activeTagDetail: { type: Object, default: null },
  previewForm: { type: Object, required: true },
  tagPreviewLoading: { type: Boolean, default: false },
  pendingAudits: { type: Array, default: () => [] },
  myAudits: { type: Array, default: () => [] },
  formatMetricValue: { type: Function, required: true },
  statusCountText: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  pointReasonLabel: { type: Function, required: true },
  formatPoints: { type: Function, required: true },
  statusLabel: { type: Function, required: true }
})

const emit = defineEmits([
  'refresh',
  'save-tag-category',
  'reset-tag-form',
  'save-tag-item',
  'edit-tag-item',
  'disable-tag-item',
  'open-tag-gallery',
  'close-tag-gallery',
  'reset-preview-form',
  'edit-tag-preview',
  'save-tag-preview',
  'set-tag-preview-cover',
  'delete-tag-preview',
  'move-tag-preview',
  'review-content-audit'
])

const previewFile = ref(null)
const previewFileList = ref([])

function handlePreviewFileChange(file) {
  previewFile.value = file?.raw || null
}

function clearPreviewFile() {
  previewFile.value = null
  previewFileList.value = []
}

function resetPreviewEditor() {
  clearPreviewFile()
  emit('reset-preview-form')
}

function startPreviewEdit(preview) {
  clearPreviewFile()
  emit('edit-tag-preview', preview)
}

function submitPreview() {
  emit('save-tag-preview', previewFile.value)
  clearPreviewFile()
}

function closePreviewManager() {
  clearPreviewFile()
  emit('close-tag-gallery')
}

function formatDay(value) {
  if (!value) return '-'
  const parts = String(value).split('-')
  return parts.length === 3 ? `${parts[1]}/${parts[2]}` : String(value)
}
</script>
