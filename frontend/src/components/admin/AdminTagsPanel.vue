<template>
  <section class="admin-section-view" v-loading="tagAdminLoading">
    <div class="admin-section-heading">
      <div>
        <h1>标签图库</h1>
        <p>维护提示词知识库、中文解释与多场景预览图。</p>
      </div>
      <div class="admin-heading-actions">
        <el-button :icon="FolderAdd" @click="categoryDialogVisible = true">新建分类</el-button>
        <el-button type="primary" :icon="Plus" @click="openNewTag">新建标签</el-button>
      </div>
    </div>

    <div class="admin-tag-insights">
      <article>
        <div class="admin-panel-head"><div><h2>热门标签</h2><span>使用次数</span></div></div>
        <div class="admin-insight-tags">
          <span v-for="tag in tagAnalytics.topTags.slice(0, 8)" :key="tag.id">
            {{ tag.displayNameZh || tag.name }} <b>{{ tag.usageCount }}</b>
          </span>
          <div v-if="!tagAnalytics.topTags.length" class="quiet-empty compact-empty">暂无使用数据</div>
        </div>
      </article>
      <article>
        <div class="admin-panel-head"><div><h2>常用组合</h2><span>共同选择</span></div></div>
        <div class="admin-combination-list">
          <span v-for="item in tagAnalytics.topCombinations.slice(0, 5)" :key="`${item.tagAId}-${item.tagBId}`">
            <em>{{ item.tagADisplayNameZh || item.tagAName }} + {{ item.tagBDisplayNameZh || item.tagBName }}</em><b>{{ item.usageCount }}</b>
          </span>
          <div v-if="!tagAnalytics.topCombinations.length" class="quiet-empty compact-empty">暂无组合数据</div>
        </div>
      </article>
      <article>
        <div class="admin-panel-head"><div><h2>零结果搜索</h2><span>优先补充</span></div></div>
        <div class="admin-search-watch-list">
          <span v-for="item in zeroResultKeywords" :key="item.keyword"><em>{{ item.keyword }}</em><b>{{ item.zeroResultCount }}</b></span>
          <div v-if="!zeroResultKeywords.length" class="quiet-empty compact-empty">暂无零结果搜索</div>
        </div>
      </article>
    </div>

    <div class="admin-filter-bar admin-tag-filter-bar">
      <el-input v-model="keyword" clearable :prefix-icon="Search" placeholder="搜索中英文标签、说明或提示词" />
      <el-select v-model="categoryId" clearable placeholder="全部分类">
        <el-option v-for="category in adminTagTree" :key="category.id" :label="category.name" :value="category.id" />
      </el-select>
      <span class="admin-filter-count">{{ filteredRows.length }} / {{ flatTags.length }} 个标签</span>
    </div>

    <article class="admin-data-panel admin-tag-table-panel">
      <el-table :data="filteredRows" class="admin-table" empty-text="没有符合条件的标签">
        <el-table-column prop="categoryName" label="分类" width="130" />
        <el-table-column label="标签" min-width="210">
          <template #default="scope">
            <BilingualTagLabel :name="scope.row.tag.name" :display-name-zh="scope.row.tag.displayNameZh" />
          </template>
        </el-table-column>
        <el-table-column label="中文说明" min-width="240" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.tag.descriptionZh || '—' }}</template>
        </el-table-column>
        <el-table-column label="提示词" min-width="260" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.tag.promptText }}</template>
        </el-table-column>
        <el-table-column label="预览" width="90" align="center">
          <template #default="scope"><span class="admin-preview-state" :class="{ ready: scope.row.tag.previewImageUrl }">{{ scope.row.tag.previewImageUrl ? '已配置' : '待补充' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="176" fixed="right" align="right">
          <template #default="scope">
            <el-tooltip content="管理图库" placement="top"><el-button :icon="PictureFilled" circle size="small" @click="$emit('open-tag-gallery', scope.row.tag)" /></el-tooltip>
            <el-tooltip content="编辑标签" placement="top"><el-button :icon="Edit" circle size="small" @click="openTagEditor(scope.row)" /></el-tooltip>
            <el-popconfirm title="确定停用这个标签吗？" @confirm="$emit('disable-tag-item', scope.row.tag)">
              <template #reference><el-button :icon="Delete" circle size="small" type="danger" plain /></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </article>

    <el-dialog v-model="categoryDialogVisible" title="新建标签分类" width="min(460px, 92vw)" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="分类名称"><el-input v-model="categoryForm.name" placeholder="镜头 / 光线 / 材质" /></el-form-item>
        <el-form-item label="Slug"><el-input v-model="categoryForm.slug" placeholder="camera / light / material" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="categoryForm.sortOrder" :min="0" :step="10" controls-position="right" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tagAdminLoading" @click="saveCategory">创建分类</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="tagEditorVisible" size="min(560px, 94vw)" class="admin-tag-editor-drawer" destroy-on-close>
      <template #header><div class="admin-drawer-heading"><h2>{{ tagForm.id ? '编辑标签' : '新建标签' }}</h2><span>提示词知识库</span></div></template>
      <el-form label-position="top">
        <el-form-item label="所属分类">
          <el-select v-model="tagForm.categoryId" filterable placeholder="选择分类">
            <el-option v-for="category in adminTagTree" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <div class="tag-form-grid">
          <el-form-item label="英文标签名"><el-input v-model="tagForm.name" placeholder="cinematic" /></el-form-item>
          <el-form-item label="中文显示名"><el-input v-model="tagForm.displayNameZh" placeholder="电影感" /></el-form-item>
        </div>
        <el-form-item label="中文说明"><el-input v-model="tagForm.descriptionZh" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" /></el-form-item>
        <el-form-item label="正向提示词"><el-input v-model="tagForm.promptText" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" resize="none" /></el-form-item>
        <el-form-item label="反向提示词"><el-input v-model="tagForm.negativePromptText" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" /></el-form-item>
        <el-form-item label="预览图 URL"><el-input v-model="tagForm.previewImageUrl" placeholder="/images/tags/..." /></el-form-item>
        <div v-if="tagForm.previewImageUrl" class="tag-preview-box"><img :src="tagForm.previewImageUrl" alt="标签预览图" /></div>
        <div class="tag-form-grid">
          <el-form-item label="权重"><el-input-number v-model="tagForm.weight" :min="0.1" :max="3" :step="0.05" controls-position="right" /></el-form-item>
          <el-form-item label="可见性"><el-select v-model="tagForm.visibility"><el-option label="公开" value="PUBLIC" /><el-option label="私有" value="PRIVATE" /></el-select></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="admin-drawer-footer">
          <el-button @click="tagEditorVisible = false">取消</el-button>
          <el-button type="primary" :loading="tagAdminLoading" @click="saveTag">保存标签</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer :model-value="previewManagerVisible" size="min(920px, 96vw)" class="tag-gallery-admin-drawer" @close="closePreviewManager">
      <template #header>
        <div class="tag-gallery-admin-head">
          <div><p class="eyebrow">Tag Gallery</p><h2>{{ activeTagDetail?.tag?.displayNameZh || activeTagDetail?.tag?.name || '标签图库' }}</h2></div>
          <span>{{ activeTagDetail?.previews?.length || 0 }} 张图片</span>
        </div>
      </template>
      <div v-loading="tagPreviewLoading" class="tag-gallery-admin-body">
        <section class="soft-panel embedded-panel tag-preview-editor-panel">
          <div class="section-title-row"><h2>{{ previewForm.id ? '编辑 / 替换预览图' : '上传预览图' }}</h2><el-button v-if="previewForm.id" size="small" @click="resetPreviewEditor">取消编辑</el-button></div>
          <div class="tag-preview-editor-grid">
            <el-upload v-model:file-list="previewFileList" drag :auto-upload="false" :limit="1" accept="image/png,image/jpeg,image/webp,image/gif" :on-change="handlePreviewFileChange" :on-remove="clearPreviewFile" class="tag-preview-upload">
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖入图片或点击选择</div>
              <template #tip><span>支持 PNG、JPG、WebP、GIF，编辑状态下选择文件即可替换原图。</span></template>
            </el-upload>
            <el-form label-position="top" class="tag-preview-meta-form">
              <div class="tag-form-grid">
                <el-form-item label="预览类型"><el-select v-model="previewForm.previewType"><el-option label="封面" value="COVER" /><el-option label="普通示例" value="EXAMPLE" /><el-option label="对比示例" value="COMPARISON" /></el-select></el-form-item>
                <el-form-item label="场景类型"><el-input v-model="previewForm.sceneKey" placeholder="CHARACTER / LANDSCAPE / GENERAL" /></el-form-item>
              </div>
              <el-form-item label="中文标题"><el-input v-model="previewForm.titleZh" placeholder="人物示例 / 风景对比" /></el-form-item>
              <el-form-item label="提示词快照"><el-input v-model="previewForm.promptSnapshot" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" resize="none" /></el-form-item>
              <div class="tag-form-grid">
                <el-form-item label="排序"><el-input-number v-model="previewForm.sortOrder" :min="0" :step="10" controls-position="right" /></el-form-item>
                <el-form-item label="设为封面"><el-switch v-model="previewForm.cover" /></el-form-item>
              </div>
              <el-button type="primary" class="wide-button" :loading="tagPreviewLoading" @click="submitPreview">{{ previewForm.id ? '保存修改' : '上传图片' }}</el-button>
            </el-form>
          </div>
        </section>
        <section class="tag-preview-admin-grid">
          <article v-for="(preview, index) in activeTagDetail?.previews || []" :key="preview.id" class="tag-preview-admin-card" :class="{ cover: preview.cover }">
            <div class="tag-preview-admin-media"><img :src="preview.imageUrl" :alt="preview.titleZh || activeTagDetail?.tag?.name" loading="lazy" /><span v-if="preview.cover">当前封面</span></div>
            <div class="tag-preview-admin-copy"><strong>{{ preview.titleZh || '未命名预览' }}</strong><span>{{ preview.previewType }} · {{ preview.sceneKey }} · 排序 {{ preview.sortOrder }}</span></div>
            <div class="tag-preview-admin-actions">
              <el-tooltip content="前移" placement="top"><el-button :icon="ArrowUp" circle size="small" :disabled="index === 0" @click="$emit('move-tag-preview', { preview, offset: -1 })" /></el-tooltip>
              <el-tooltip content="后移" placement="top"><el-button :icon="ArrowDown" circle size="small" :disabled="index === (activeTagDetail?.previews?.length || 0) - 1" @click="$emit('move-tag-preview', { preview, offset: 1 })" /></el-tooltip>
              <el-button size="small" @click="startPreviewEdit(preview)">编辑</el-button>
              <el-button v-if="!preview.cover" size="small" @click="$emit('set-tag-preview-cover', preview)">设为封面</el-button>
              <el-popconfirm title="确定删除这张预览图吗？" @confirm="$emit('delete-tag-preview', preview)"><template #reference><el-button size="small" type="danger" plain>删除</el-button></template></el-popconfirm>
            </div>
          </article>
          <div v-if="!activeTagDetail?.previews?.length" class="quiet-empty">这个标签还没有预览图</div>
        </section>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ArrowDown, ArrowUp, Delete, Edit, FolderAdd, PictureFilled, Plus, Search, UploadFilled } from '@element-plus/icons-vue'
import BilingualTagLabel from '../BilingualTagLabel.vue'

const props = defineProps({
  tagAdminLoading: { type: Boolean, default: false },
  tagAnalytics: { type: Object, default: () => ({ topTags: [], topCombinations: [], searchKeywords: [] }) },
  categoryForm: { type: Object, required: true },
  tagForm: { type: Object, required: true },
  adminTagTree: { type: Array, default: () => [] },
  flatTags: { type: Array, default: () => [] },
  previewManagerVisible: { type: Boolean, default: false },
  activeTagDetail: { type: Object, default: null },
  previewForm: { type: Object, required: true },
  tagPreviewLoading: { type: Boolean, default: false }
})

const emit = defineEmits([
  'save-tag-category', 'reset-tag-form', 'save-tag-item', 'edit-tag-item', 'disable-tag-item',
  'open-tag-gallery', 'close-tag-gallery', 'reset-preview-form', 'edit-tag-preview', 'save-tag-preview',
  'set-tag-preview-cover', 'delete-tag-preview', 'move-tag-preview'
])

const keyword = ref('')
const categoryId = ref(null)
const categoryDialogVisible = ref(false)
const tagEditorVisible = ref(false)
const previewFile = ref(null)
const previewFileList = ref([])

const rows = computed(() => props.adminTagTree.flatMap((category) => (category.tags || []).map((tag) => ({
  category,
  categoryName: category.name,
  tag
}))))

const filteredRows = computed(() => {
  const needle = keyword.value.trim().toLowerCase()
  return rows.value.filter((row) => {
    if (categoryId.value && row.category.id !== categoryId.value) return false
    if (!needle) return true
    return [row.tag.name, row.tag.displayNameZh, row.tag.descriptionZh, row.tag.promptText]
      .filter(Boolean).some((value) => String(value).toLowerCase().includes(needle))
  })
})

const zeroResultKeywords = computed(() => (props.tagAnalytics.searchKeywords || []).filter((item) => Number(item.zeroResultCount || 0) > 0).slice(0, 6))

function openNewTag() {
  emit('reset-tag-form')
  tagEditorVisible.value = true
}

function openTagEditor(row) {
  emit('edit-tag-item', { category: row.category, tag: row.tag })
  tagEditorVisible.value = true
}

function saveCategory() {
  emit('save-tag-category')
  categoryDialogVisible.value = false
}

function saveTag() {
  emit('save-tag-item')
  tagEditorVisible.value = false
}

function handlePreviewFileChange(file) { previewFile.value = file?.raw || null }
function clearPreviewFile() { previewFile.value = null; previewFileList.value = [] }
function resetPreviewEditor() { clearPreviewFile(); emit('reset-preview-form') }
function startPreviewEdit(preview) { clearPreviewFile(); emit('edit-tag-preview', preview) }
function submitPreview() { emit('save-tag-preview', previewFile.value); clearPreviewFile() }
function closePreviewManager() { clearPreviewFile(); emit('close-tag-gallery') }
</script>
