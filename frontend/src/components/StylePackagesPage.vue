<template>
  <section
    class="style-view content-hub-page market-page-shell style-package-page density-page"
    :class="`density-${density}`"
    v-loading="styleLoading"
  >
    <header class="page-hero hub-hero">
      <div class="hero-copy">
        <p class="eyebrow">Style Asset Marketplace</p>
        <h1>{{ isMarketView ? '风格资源市场' : '我的风格资源包' }}</h1>
        <p class="page-subtitle">{{ summaryText }}</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="$emit('go-related', isMarketView ? 'my-styles' : 'style-market')">
          {{ isMarketView ? '进入我的资源包' : '返回资源市场' }}
        </el-button>
        <el-button :icon="Refresh" :loading="styleLoading" @click="$emit('refresh', currentQuery())">刷新</el-button>
        <el-button v-if="!isMarketView" type="primary" @click="openCreateDialog">新建资源包</el-button>
      </div>
    </header>

    <section class="soft-panel market-toolbar-panel style-filter-panel">
      <div class="style-filter-grid">
        <el-input
          v-model="keyword"
          :prefix-icon="Search"
          placeholder="搜索资源包、资源类型、风格说明或标签"
          clearable
          class="market-search-input"
        />
        <el-select v-model="selectedTagId" clearable placeholder="风格标签" class="toolbar-select">
          <el-option v-for="tag in availableTags" :key="tag.id" :label="tagLabel(tag)" :value="tag.id" />
        </el-select>
        <el-select v-model="localStatus" clearable placeholder="状态" class="toolbar-select">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
        <el-select v-model="sortMode" placeholder="排序" class="toolbar-select">
          <el-option label="最近更新" value="latest" />
          <el-option label="资源数量最多" value="resources" />
          <el-option label="评分最高" value="rating" />
          <el-option label="协作者最多" value="collaborators" />
          <el-option label="价格最高" value="price" />
        </el-select>
      </div>

      <div class="hub-summary-row market-summary-row">
        <div>
          <h2>{{ isMarketView ? '发现可直接用于创作的统一风格资源' : '管理可持续迭代的风格资产库' }}</h2>
          <p>{{ isMarketView ? marketSummary : workspaceSummary }}</p>
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

    <section class="style-pack-grid">
      <article v-for="pack in visiblePacks" :key="pack.id" class="style-pack-card" @click="openPackDrawer(pack)">
        <div class="card-quick-actions style-card-actions">
          <button
            type="button"
            class="card-icon-action"
            :class="{ active: props.isFavoriteTarget?.('STYLE_PACKAGE', pack) }"
            @click.stop="$emit('toggle-favorite-target', { type: 'STYLE_PACKAGE', target: pack })"
          >
            <Star />
          </button>
          <button
            type="button"
            class="card-icon-action"
            :class="{ active: props.isSubscribedTarget?.('STYLE_PACKAGE', pack) }"
            @click.stop="$emit('toggle-subscription-target', { type: 'STYLE_PACKAGE', target: pack })"
          >
            <Bell />
          </button>
        </div>
        <div class="style-pack-preview" :class="`count-${Math.min(previewImages(pack).length, 5)}`">
          <div
            v-for="(preview, index) in previewImages(pack).slice(0, 5)"
            :key="preview.key"
            class="style-pack-preview-tile"
            :class="{ featured: index === 0 }"
          >
            <img :src="preview.url" :alt="preview.title || pack.name" />
            <span v-if="index === 0" class="status-badge floating" :class="statusBadgeClass(pack.status)">
              {{ statusText(pack.status) }}
            </span>
            <span v-if="index === 4 && previewImages(pack).length > 5" class="style-pack-more-count">
              +{{ previewImages(pack).length - 5 }} 张
            </span>
          </div>
          <div v-if="!previewImages(pack).length" class="style-pack-cover-fallback">
            <Collection />
          </div>
        </div>

        <div class="style-pack-preview-caption">
          <span>{{ resourceCount(pack) }} 项可用资源</span>
          <span>{{ categoryCount(pack) }} 个资源类目</span>
        </div>

        <div class="style-pack-card-body">
          <div class="style-pack-title-row">
            <div>
              <h3>{{ pack.name }}</h3>
              <p class="style-pack-owner">{{ ownerText(pack) }}</p>
            </div>
            <span class="style-price">{{ Number(pack.pricePoints || 0) }} pts</span>
          </div>

          <p class="style-pack-desc">{{ pack.styleStatement || pack.description || '暂未补充风格说明。' }}</p>

          <div v-if="pack.tags?.length" class="style-pack-tags">
            <span v-for="tag in pack.tags.slice(0, 5)" :key="tag.id" class="style-tag-chip">
              <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
            </span>
          </div>

          <div class="style-pack-metrics">
            <span v-for="item in styleStatsItems(pack)" :key="item.label">
              <strong>{{ item.value }}</strong>{{ item.label }}
            </span>
          </div>
        </div>
      </article>

      <div v-if="!visiblePacks.length" class="detail-empty-state hub-empty-state">
        <strong>{{ isMarketView ? '没有匹配的风格资源包' : '你还没有创建风格资源包' }}</strong>
        <span>调整搜索与标签筛选，或创建第一个可交付的统一风格资源集合。</span>
      </div>
    </section>

    <section class="soft-panel pager-panel">
      <div class="pager-copy">
        <strong>{{ isMarketView ? '资源市场分页' : '我的资源包分页' }}</strong>
        <span>共 {{ Number(queryState.total || 0) }} 个资源包，当前第 {{ currentPage }} 页。</span>
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
      v-model="packDrawerVisible"
      size="min(1040px, 96vw)"
      class="hub-drawer"
      :title="activePack ? activePack.name : '风格资源包详情'"
      destroy-on-close
    >
      <template v-if="activePack">
        <div class="hub-drawer-body style-drawer-body">
          <section class="soft-panel embedded-panel style-drawer-panel">
            <div class="drawer-topline">
              <div>
                <p class="eyebrow">Style Package</p>
                <h2>{{ activePack.name }}</h2>
              </div>
              <div class="drawer-top-badges">
                <span class="task-tier-chip">{{ activePack.commercialUse ? '支持商用' : '仅限非商用' }}</span>
                <span class="status-badge" :class="statusBadgeClass(activePack.status)">
                  {{ statusText(activePack.status) }}
                </span>
                <span class="task-tier-chip strong">{{ Number(activePack.pricePoints || 0) }} pts</span>
              </div>
            </div>

            <div class="style-detail-layout">
              <div class="style-detail-gallery">
                <div class="style-detail-main-preview">
                  <img
                    v-if="activePreviewUrl"
                    :src="activePreviewUrl"
                    :alt="activePack.name"
                    class="style-pack-cover-image"
                  />
                  <div v-else class="style-pack-cover-fallback">
                    <Collection />
                  </div>
                  <span class="style-detail-image-count">{{ previewImages(activePack).length }} 张预览</span>
                </div>
                <div v-if="previewImages(activePack).length" class="style-detail-thumbs">
                  <button
                    v-for="preview in previewImages(activePack)"
                    :key="preview.key"
                    type="button"
                    class="style-detail-thumb"
                    :class="{ active: activePreviewId === preview.key }"
                    @click="activePreviewId = preview.key"
                  >
                    <img :src="preview.url" :alt="preview.title || activePack.name" />
                  </button>
                </div>
              </div>

              <div class="style-detail-content">
                <p class="detail-lead">{{ activePack.description || activePack.styleStatement || '暂未补充说明。' }}</p>

                <div v-if="activePack.tags?.length" class="style-pack-tags">
                  <span v-for="tag in activePack.tags" :key="tag.id" class="style-tag-chip">
                    <BilingualTagLabel :name="tag.name" :display-name-zh="tag.displayNameZh" />
                  </span>
                </div>

                <div class="style-pack-metrics">
                  <span v-for="item in styleStatsItems(activePack)" :key="item.label">
                    <strong>{{ item.value }}</strong>{{ item.label }}
                  </span>
                </div>

                <section class="drawer-copy-block" v-if="activePack.styleStatement">
                  <h3>风格说明</h3>
                  <p>{{ activePack.styleStatement }}</p>
                </section>
                <section class="drawer-copy-block" v-if="activePack.promptGuide">
                  <h3>推荐提示词</h3>
                  <p>{{ activePack.promptGuide }}</p>
                </section>
                <section class="drawer-copy-block" v-if="activePack.negativePromptGuide">
                  <h3>反向提示词</h3>
                  <p>{{ activePack.negativePromptGuide }}</p>
                </section>

                <section class="drawer-copy-block license-summary-block">
                  <h3>资源授权</h3>
                  <p>{{ activePack.licenseSummary || '购买后可在项目中使用包内资源，不得单独转售或重新分发原始文件。' }}</p>
                </section>

                <section v-if="activePack.collaborators?.length" class="drawer-copy-block">
                  <h3>协作者</h3>
                  <div class="style-collaborator-list">
                    <div v-for="user in activePack.collaborators" :key="`${user.userId}-${user.role}`" class="style-collaborator-pill">
                      <span class="style-collaborator-avatar">{{ `${user.userId}`.slice(-2) }}</span>
                      <div>
                        <strong>#{{ user.userId }}</strong>
                        <span>{{ collaboratorRoleText(user.role) }}</span>
                      </div>
                    </div>
                  </div>
                </section>

                <div class="detail-actions">
                  <el-button @click="$emit('toggle-favorite-target', { type: 'STYLE_PACKAGE', target: activePack })">
                    {{ props.isFavoriteTarget?.('STYLE_PACKAGE', activePack) ? '取消收藏' : '收藏风格包' }}
                  </el-button>
                  <el-button @click="$emit('toggle-subscription-target', { type: 'STYLE_PACKAGE', target: activePack })">
                    {{ props.isSubscribedTarget?.('STYLE_PACKAGE', activePack) ? '取消订阅' : '订阅动态' }}
                  </el-button>
                  <el-button v-if="activePack.accessible" type="primary" @click="openManifest(activePack)">查看资源清单</el-button>
                  <el-button v-else-if="isMarketView" type="primary" @click="$emit('exchange-pack', activePack)">兑换资源包</el-button>
                  <el-button v-if="isMarketView" @click="openSubmissionDialog(activePack)">投稿效果作品</el-button>
                  <el-button v-if="isMarketView" @click="openReviewDialog(activePack)">评价资源包</el-button>

                  <template v-if="!isMarketView">
                    <el-button v-if="activePack.owner" @click="openEditDialog(activePack)">编辑资源包</el-button>
                    <el-button v-if="activePack.owner && activePack.status !== 'PUBLISHED'" type="primary" @click="$emit('publish-pack', activePack)">
                      发布
                    </el-button>
                    <el-button v-else-if="activePack.owner" @click="$emit('archive-pack', activePack)">归档</el-button>
                    <el-button v-if="activePack.owner" @click="openOwnerOps(activePack)">投稿审核</el-button>
                  </template>
                </div>
              </div>
            </div>
          </section>

          <section class="soft-panel embedded-panel resource-catalog-panel" v-loading="assetsLoading">
            <div class="section-title-row lower resource-section-heading">
              <div>
                <p class="eyebrow">Deliverable Assets</p>
                <h2>包内资源目录</h2>
                <span>{{ packAssets.length }} 项资源，按场景分类浏览。效果作品与可下载资源在这里严格分开。</span>
              </div>
              <div class="detail-actions">
                <el-button v-if="activePack.editable" type="primary" @click="openAssetEditor()">添加资源</el-button>
                <el-button @click="loadPackResources(activePack)">刷新目录</el-button>
              </div>
            </div>

            <div class="resource-category-tabs">
              <button
                type="button"
                :class="{ active: selectedAssetCategory === '' }"
                @click="selectedAssetCategory = ''"
              >
                全部 <span>{{ packAssets.length }}</span>
              </button>
              <button
                v-for="category in resourceCategories"
                :key="category.key"
                type="button"
                :class="{ active: selectedAssetCategory === category.key }"
                @click="selectedAssetCategory = category.key"
              >
                {{ resourceCategoryLabel(category.key) }} <span>{{ category.count }}</span>
              </button>
            </div>

            <div v-if="filteredPackAssets.length" class="resource-asset-grid">
              <article
                v-for="asset in filteredPackAssets"
                :key="asset.id"
                class="resource-asset-card"
                @click="openAssetDetail(asset)"
              >
                <div class="resource-asset-image">
                  <img :src="asset.thumbnailUrl || asset.previewImageUrl" :alt="asset.name" />
                  <span>{{ resourceCategoryLabel(asset.categoryKey) }}</span>
                </div>
                <div class="resource-asset-copy">
                  <div>
                    <strong>{{ asset.name }}</strong>
                    <span>修订 v{{ asset.revisionNumber }}</span>
                  </div>
                  <p>{{ asset.description || '统一风格资源，可在详情中查看尺寸、格式与生成信息。' }}</p>
                  <div class="resource-asset-meta">
                    <span>{{ asset.width && asset.height ? `${asset.width} × ${asset.height}` : '尺寸待补充' }}</span>
                    <span>{{ asset.fileFormat || 'PNG' }}</span>
                    <span>{{ asset.downloadable ? '已解锁' : '预览' }}</span>
                  </div>
                </div>
              </article>
            </div>
            <div v-else class="quiet-empty resource-empty-state">
              {{ selectedAssetCategory ? '这个分类还没有资源。' : '这个资源包尚未添加可交付资源。' }}
            </div>
          </section>

          <section class="soft-panel embedded-panel">
            <div class="section-title-row lower">
              <h2>版本记录</h2>
              <el-button size="small" @click="reloadPackDetail(activePack)">刷新</el-button>
            </div>
            <div class="submission-list compact-submission-list">
              <article v-for="version in stylePackageVersions" :key="version.id" class="submission-row">
                <div>
                  <strong>版本 v{{ version.versionNumber || version.id }}</strong>
                  <span>{{ formatDate(version.createdAt) }}</span>
                  <p class="submission-note-copy">
                    {{ version.styleStatement || version.description || '这个版本主要用于保留风格包快照。' }}
                  </p>
                  <div class="resource-version-stats">
                    <span>{{ version.resourceCount || 0 }} 项资源</span>
                    <span>{{ version.categoryCount || 0 }} 个类目</span>
                    <span>{{ version.changeNote || '版本快照' }}</span>
                  </div>
                </div>
              </article>
              <div v-if="!stylePackageVersions.length" class="quiet-empty compact-empty">还没有版本记录。</div>
            </div>
          </section>

          <section class="soft-panel embedded-panel">
            <div class="section-title-row lower">
              <h2>{{ isMarketView ? '公开评价' : '风格包评价' }}</h2>
              <el-button v-if="isMarketView" size="small" @click="openReviewDialog(activePack)">我要评价</el-button>
            </div>
            <div class="submission-list compact-submission-list">
              <article v-for="review in stylePackageReviews" :key="review.id" class="submission-row">
                <div>
                  <strong>#{{ review.userId }}</strong>
                  <span>{{ formatDate(review.createdAt) }}</span>
                  <el-rate :model-value="review.rating" disabled size="small" />
                  <p class="submission-note-copy">{{ review.comment || '这条评价没有留下更多说明。' }}</p>
                </div>
              </article>
              <div v-if="!stylePackageReviews.length" class="quiet-empty compact-empty">还没有评价记录。</div>
            </div>
          </section>

          <section class="soft-panel embedded-panel">
            <div class="section-title-row lower">
              <h2>效果展示与共创作品</h2>
            </div>
            <div v-if="activePack.artworks?.length" class="style-artwork-grid">
              <article v-for="artwork in activePack.artworks" :key="artwork.id" class="style-artwork-card">
                <img :src="artwork.imageUrl" :alt="artwork.title" />
                <strong>{{ artwork.title }}</strong>
              </article>
            </div>
            <div v-else class="quiet-empty compact-empty">这个资源包还没有效果展示作品。</div>
          </section>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="assetDetailVisible" width="min(920px, 96vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Resource Detail</p>
          <h2>{{ activeAsset?.name || '资源详情' }}</h2>
        </div>
      </template>
      <div v-if="activeAsset" class="resource-detail-layout">
        <div class="resource-detail-preview">
          <img :src="activeAsset.previewImageUrl" :alt="activeAsset.name" />
        </div>
        <div class="resource-detail-info">
          <div class="resource-detail-badges">
            <el-tag effect="plain">{{ resourceCategoryLabel(activeAsset.categoryKey) }}</el-tag>
            <el-tag type="info" effect="plain">v{{ activeAsset.revisionNumber }}</el-tag>
            <el-tag :type="activeAsset.downloadable ? 'success' : 'warning'" effect="plain">
              {{ activeAsset.downloadable ? '已解锁完整资源' : '仅公开预览' }}
            </el-tag>
          </div>
          <p>{{ activeAsset.description || '暂无资源说明。' }}</p>
          <dl class="resource-spec-list">
            <div><dt>尺寸</dt><dd>{{ activeAsset.width && activeAsset.height ? `${activeAsset.width} × ${activeAsset.height}` : '未记录' }}</dd></div>
            <div><dt>格式</dt><dd>{{ activeAsset.fileFormat || 'PNG' }}</dd></div>
            <div><dt>背景</dt><dd>{{ backgroundModeLabel(activeAsset.backgroundMode) }}</dd></div>
            <div><dt>贡献者</dt><dd>#{{ activeAsset.contributorId }}</dd></div>
          </dl>
          <section v-if="activeAsset.promptText" class="drawer-copy-block">
            <h3>生成提示词</h3>
            <p>{{ activeAsset.promptText }}</p>
          </section>
          <section v-if="activeAsset.negativePromptText" class="drawer-copy-block">
            <h3>反向提示词</h3>
            <p>{{ activeAsset.negativePromptText }}</p>
          </section>
          <div class="detail-actions">
            <el-button v-if="activeAsset.downloadable && activeAsset.fileUrl" type="primary" @click="downloadAssetFile(activeAsset)">下载原始资源</el-button>
            <el-button v-if="activePack?.editable" @click="openAssetEditor(activeAsset)">创建新修订</el-button>
            <el-button v-if="activePack?.editable" type="danger" plain @click="archiveAsset(activeAsset)">归档资源</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="assetEditorVisible" width="min(860px, 96vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Resource Editor</p>
          <h2>{{ assetForm.id ? '创建资源新修订' : '添加包内资源' }}</h2>
        </div>
      </template>
      <el-form label-position="top" class="resource-editor-form">
        <div class="resource-form-grid">
          <el-form-item label="资源名称">
            <el-input v-model="assetForm.name" placeholder="例如：萤光橡树 A" />
          </el-form-item>
          <el-form-item label="资源类目">
            <el-select v-model="assetForm.categoryKey" filterable allow-create default-first-option>
              <el-option v-for="item in resourceCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="预览图地址" class="resource-form-wide">
            <el-input v-model="assetForm.previewImageUrl" placeholder="/images/style-packs/example/tree.png" />
          </el-form-item>
          <el-form-item label="原始资源文件" class="resource-form-wide">
            <div class="resource-upload-row">
              <el-input v-model="assetForm.fileUrl" placeholder="上传文件，或填写已有资源地址" />
              <el-upload
                :show-file-list="false"
                :http-request="handleAssetFileUpload"
                :disabled="assetUploading"
                accept=".png,.jpg,.jpeg,.webp,.gif,.zip,.json,.txt"
              >
                <el-button :icon="Upload" :loading="assetUploading">上传文件</el-button>
              </el-upload>
            </div>
            <span class="resource-upload-hint">文件会进入私有存储，只有资源包拥有者、协作者和已兑换用户可以下载。</span>
          </el-form-item>
          <el-form-item label="宽度">
            <el-input-number v-model="assetForm.width" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="高度">
            <el-input-number v-model="assetForm.height" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="文件格式">
            <el-select v-model="assetForm.fileFormat">
              <el-option label="PNG" value="PNG" />
              <el-option label="WebP" value="WEBP" />
              <el-option label="JPG" value="JPG" />
              <el-option label="GIF" value="GIF" />
              <el-option label="ZIP" value="ZIP" />
              <el-option label="JSON" value="JSON" />
              <el-option label="TXT" value="TXT" />
            </el-select>
          </el-form-item>
          <el-form-item label="背景模式">
            <el-select v-model="assetForm.backgroundMode">
              <el-option label="透明背景" value="TRANSPARENT" />
              <el-option label="纯色背景" value="SOLID" />
              <el-option label="完整场景" value="SCENE" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="assetForm.sortOrder" :min="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="授权继承">
            <el-select v-model="assetForm.licenseScope">
              <el-option label="继承资源包授权" value="PACKAGE" />
              <el-option label="仅限个人项目" value="PERSONAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="资源说明" class="resource-form-wide">
            <el-input v-model="assetForm.description" type="textarea" :rows="3" resize="none" />
          </el-form-item>
          <el-form-item label="生成提示词" class="resource-form-wide">
            <el-input v-model="assetForm.promptText" type="textarea" :rows="3" resize="none" />
          </el-form-item>
          <el-form-item label="反向提示词" class="resource-form-wide">
            <el-input v-model="assetForm.negativePromptText" type="textarea" :rows="2" resize="none" />
          </el-form-item>
          <el-form-item label="生成参数 JSON" class="resource-form-wide">
            <el-input v-model="assetForm.generationParamsJson" type="textarea" :rows="3" resize="none" placeholder='{"model":"...","seed":1234}' />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="detail-actions">
          <el-button @click="assetEditorVisible = false">取消</el-button>
          <el-button type="primary" :loading="assetSaving" @click="saveAsset">保存资源</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="manifestVisible" width="min(900px, 96vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Version Manifest</p>
          <h2>{{ manifestData?.packageName || '资源清单' }}</h2>
        </div>
      </template>
      <div v-loading="manifestLoading" class="manifest-layout">
        <div class="manifest-summary">
          <div><strong>v{{ manifestData?.versionNumber || '-' }}</strong><span>当前交付版本</span></div>
          <div><strong>{{ manifestData?.resourceCount || 0 }}</strong><span>项资源</span></div>
          <div><strong>{{ manifestData?.categoryCount || 0 }}</strong><span>个类目</span></div>
          <div><strong>{{ manifestData?.commercialUse ? '允许' : '不允许' }}</strong><span>商业使用</span></div>
        </div>
        <p class="manifest-license">{{ manifestData?.licenseSummary || '请遵循资源包授权说明。' }}</p>
        <div class="manifest-resource-list">
          <button v-for="asset in manifestData?.assets || []" :key="asset.id" type="button" @click="openAssetDetail(asset)">
            <img :src="asset.thumbnailUrl || asset.previewImageUrl" :alt="asset.name" />
            <span><strong>{{ asset.name }}</strong><small>{{ resourceCategoryLabel(asset.categoryKey) }} · v{{ asset.revisionNumber }}</small></span>
          </button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="packEditorVisible" width="min(960px, 96vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Package Editor</p>
          <h2>{{ styleForm.id ? '编辑风格包' : '新建风格包' }}</h2>
        </div>
      </template>

      <div class="style-editor-form-grid">
        <section class="soft-panel embedded-panel style-featured-preview-panel">
          <div class="style-cover-preview">
            <strong>封面预览</strong>
            <img v-if="styleForm.coverImageUrl" :src="styleForm.coverImageUrl" alt="cover preview" />
            <div v-else class="style-pack-cover-fallback">暂无封面</div>
          </div>
          <div class="style-featured-preview">
            <strong>特色作品</strong>
            <img v-if="featuredArtwork?.imageUrl" :src="featuredArtwork.imageUrl" alt="featured artwork" />
            <div v-else class="style-pack-cover-fallback">未选择</div>
          </div>
        </section>

        <section class="soft-panel embedded-panel">
          <el-form label-position="top">
            <el-form-item label="风格包名称">
              <el-input :model-value="styleForm.name" @update:model-value="$emit('patch-style-form', { name: $event })" />
            </el-form-item>

            <el-form-item label="封面链接">
              <el-input
                :model-value="styleForm.coverImageUrl"
                placeholder="也可以先选特色作品，再一键同步成封面"
                @update:model-value="$emit('patch-style-form', { coverImageUrl: $event })"
              />
            </el-form-item>

            <el-form-item label="简介">
              <el-input
                :model-value="styleForm.description"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 4 }"
                resize="none"
                @update:model-value="$emit('patch-style-form', { description: $event })"
              />
            </el-form-item>

            <el-form-item label="风格说明">
              <el-input
                :model-value="styleForm.styleStatement"
                type="textarea"
                :autosize="{ minRows: 3, maxRows: 5 }"
                resize="none"
                @update:model-value="$emit('patch-style-form', { styleStatement: $event })"
              />
            </el-form-item>

            <el-form-item label="推荐提示词">
              <el-input
                :model-value="styleForm.promptGuide"
                type="textarea"
                :autosize="{ minRows: 3, maxRows: 6 }"
                resize="none"
                @update:model-value="$emit('patch-style-form', { promptGuide: $event })"
              />
            </el-form-item>

            <el-form-item label="反向提示词">
              <el-input
                :model-value="styleForm.negativePromptGuide"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 4 }"
                resize="none"
                @update:model-value="$emit('patch-style-form', { negativePromptGuide: $event })"
              />
            </el-form-item>

            <div class="detail-tab-grid">
              <el-form-item label="特色作品">
                <el-select
                  :model-value="styleForm.featuredArtworkId"
                  filterable
                  clearable
                  placeholder="从作品库中选择"
                  @update:model-value="$emit('patch-style-form', { featuredArtworkId: $event })"
                >
                  <el-option v-for="artwork in featuredArtworkOptions" :key="artwork.id" :label="featuredArtworkLabel(artwork)" :value="artwork.id" />
                </el-select>
              </el-form-item>

              <el-form-item label="价格积分">
                <el-input-number
                  :model-value="styleForm.pricePoints"
                  :min="0"
                  :step="5"
                  controls-position="right"
                  @update:model-value="$emit('patch-style-form', { pricePoints: $event })"
                />
              </el-form-item>
            </div>

            <div class="detail-actions">
              <el-button v-if="featuredArtwork?.imageUrl" @click="syncFeaturedToCover">同步特色作品信息</el-button>
            </div>

            <div class="detail-tab-grid">
              <el-form-item label="授权类型">
                <el-select
                  :model-value="styleForm.licenseType"
                  @update:model-value="$emit('patch-style-form', { licenseType: $event })"
                >
                  <el-option label="标准资源包授权" value="STANDARD" />
                  <el-option label="仅限个人使用" value="PERSONAL" />
                  <el-option label="CC0 公共领域" value="CC0" />
                </el-select>
              </el-form-item>
              <el-form-item label="允许商业使用">
                <el-switch
                  :model-value="styleForm.commercialUse"
                  @update:model-value="$emit('patch-style-form', { commercialUse: $event })"
                />
              </el-form-item>
            </div>

            <el-form-item label="授权摘要">
              <el-input
                :model-value="styleForm.licenseSummary"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 4 }"
                resize="none"
                @update:model-value="$emit('patch-style-form', { licenseSummary: $event })"
              />
            </el-form-item>

            <el-form-item label="风格标签">
              <el-select
                :model-value="styleForm.tagNames"
                multiple
                filterable
                allow-create
                default-first-option
                collapse-tags
                collapse-tags-tooltip
                clearable
                placeholder="选择已有标签，或直接输入新标签"
                @update:model-value="$emit('patch-style-form', { tagNames: $event || [] })"
              >
                <el-option v-for="tag in availableTags" :key="tag.id" :label="tagLabel(tag)" :value="tag.name" />
              </el-select>
            </el-form-item>

            <el-form-item label="协作者（用户ID#角色，逗号分隔）">
              <el-input
                :model-value="collaboratorInput"
                placeholder="例如 1024#CURATOR, 1025#CONTRIBUTOR"
                @update:model-value="updateCollaboratorInput"
              />
            </el-form-item>

            <section v-if="editorPackageArtworks.length" class="soft-panel embedded-panel">
              <div class="section-title-row lower">
                <h2>已收录作品</h2>
              </div>
              <div class="style-submission-grid compact">
                <article v-for="artwork in editorPackageArtworks" :key="artwork.id" class="style-submission-card compact">
                  <img v-if="artwork.imageUrl" :src="artwork.imageUrl" :alt="artwork.title" />
                  <div class="style-submission-meta">
                    <strong>{{ artwork.title }}</strong>
                    <div class="detail-actions">
                      <el-button size="small" @click="$emit('patch-style-form', { featuredArtworkId: artwork.id })">设为特色作品</el-button>
                      <el-button size="small" @click="$emit('patch-style-form', { coverImageUrl: artwork.imageUrl || '' })">设为封面</el-button>
                    </div>
                  </div>
                </article>
              </div>
            </section>
          </el-form>
        </section>
      </div>

      <template #footer>
        <div class="detail-actions">
          <el-button @click="packEditorVisible = false">取消</el-button>
          <el-button v-if="styleForm.id" @click="$emit('reset-form')">重置</el-button>
          <el-button type="primary" :loading="styleLoading" @click="submitPackEditor">保存风格包</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="packSubmissionVisible" width="min(960px, 96vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Package Submission</p>
          <h2>{{ activePack ? `向 ${activePack.name} 投稿作品` : '投稿作品' }}</h2>
        </div>
      </template>

      <div class="soft-panel embedded-panel submission-helper-panel">
        <p>选择你自己的作品投稿到这个风格包。待包主审核通过后，它才会被真正收录进风格包作品集。</p>
      </div>

      <div v-if="submissionEligibleArtworks.length" class="style-submission-grid">
        <article
          v-for="artwork in submissionEligibleArtworks"
          :key="artwork.id"
          class="style-submission-card"
          :class="{ selected: styleSubmissionForm.artworkId === artwork.id, disabled: isSubmissionArtworkLocked(artwork) }"
          @click="selectSubmissionArtwork(artwork)"
        >
          <img v-if="artwork.imageUrl" :src="artwork.imageUrl" :alt="artwork.title" />
          <div class="style-submission-meta">
            <strong>{{ artwork.title }}</strong>
            <div class="style-submission-badges">
              <el-tag size="small" effect="plain">{{ statusLabel(artwork.status) }}</el-tag>
              <el-tag v-if="submissionStatusByArtworkId.get(artwork.id)" size="small" type="warning">
                {{ submissionStateText(submissionStatusByArtworkId.get(artwork.id)) }}
              </el-tag>
            </div>
          </div>
        </article>
      </div>
      <div v-else class="quiet-empty compact-empty">你当前没有可投稿的作品。先去作品库生成或整理一些作品吧。</div>

      <el-form label-position="top" class="style-submission-note-form">
        <el-form-item label="投稿说明">
          <el-input
            :model-value="styleSubmissionForm.note"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 5 }"
            resize="none"
            placeholder="可以补充这组作品的风格描述、使用思路或版本说明"
            @update:model-value="$emit('patch-submission-form', { note: $event })"
          />
        </el-form-item>
      </el-form>

      <section v-if="currentPackSubmissionHistory.length" class="soft-panel embedded-panel">
        <div class="section-title-row lower">
          <h2>我在这个风格包里的投稿记录</h2>
        </div>
        <div class="submission-list compact-submission-list">
          <article v-for="submission in currentPackSubmissionHistory" :key="submission.id" class="submission-row">
            <img v-if="submission.artworkImageUrl" :src="submission.artworkImageUrl" :alt="submission.artworkTitle" />
            <div>
              <strong>{{ submission.artworkTitle }}</strong>
              <span>{{ submissionStateText(submission.status) }} · {{ formatDate(submission.createdAt) }}</span>
              <p v-if="submission.note" class="submission-note-copy">{{ submission.note }}</p>
              <p v-if="submission.reviewComment" class="submission-review-copy">审核备注：{{ submission.reviewComment }}</p>
            </div>
          </article>
        </div>
      </section>

      <template #footer>
        <div class="detail-actions">
          <el-button @click="packSubmissionVisible = false">关闭</el-button>
          <el-button type="primary" :loading="styleLoading" :disabled="!styleSubmissionForm.artworkId" @click="$emit('submit-submission')">
            提交投稿
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="packReviewVisible" width="min(640px, 94vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Package Review</p>
          <h2>{{ activePack ? `评价 ${activePack.name}` : '撰写评价' }}</h2>
        </div>
      </template>
      <el-form label-position="top">
        <el-form-item label="评分">
          <el-rate :model-value="styleReviewForm.rating" @update:model-value="$emit('patch-review-form', { rating: $event })" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            :model-value="styleReviewForm.comment"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 5 }"
            resize="none"
            @update:model-value="$emit('patch-review-form', { comment: $event })"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="detail-actions">
          <el-button @click="packReviewVisible = false">取消</el-button>
          <el-button type="primary" :loading="styleLoading" @click="submitReviewAndClose">提交评价</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="ownerOpsVisible" width="min(920px, 96vw)" destroy-on-close>
      <template #header>
        <div>
          <p class="eyebrow">Submission Review</p>
          <h2>{{ activePack ? `${activePack.name} 投稿审核` : '投稿审核' }}</h2>
        </div>
      </template>

      <div class="style-submission-grid compact">
        <article v-for="submission in styleSubmissions" :key="submission.id" class="style-submission-card compact">
          <img v-if="submission.artworkImageUrl" :src="submission.artworkImageUrl" :alt="submission.artworkTitle" />
          <div class="style-submission-meta">
            <strong>{{ submission.artworkTitle }}</strong>
            <div class="style-submission-badges">
              <el-tag size="small" effect="plain">{{ submissionStateText(submission.status) }}</el-tag>
              <el-tag size="small" type="info">投稿人 #{{ submission.submitterId }}</el-tag>
            </div>
            <p v-if="submission.note" class="submission-note-copy">{{ submission.note }}</p>
            <p v-if="submission.reviewComment" class="submission-review-copy">审核备注：{{ submission.reviewComment }}</p>
            <el-input
              v-if="submission.status === 'PENDING'"
              v-model="reviewDrafts[submission.id]"
              class="submission-review-input"
              type="textarea"
              :autosize="{ minRows: 2, maxRows: 4 }"
              resize="none"
              placeholder="给这次投稿留一点审核反馈"
            />
            <div v-if="submission.status === 'PENDING'" class="detail-actions">
              <el-button size="small" type="primary" @click="$emit('review-submission', { submission, status: 'APPROVED', comment: reviewDrafts[submission.id] || '' })">
                收录通过
              </el-button>
              <el-button size="small" @click="$emit('review-submission', { submission, status: 'REJECTED', comment: reviewDrafts[submission.id] || '' })">
                驳回
              </el-button>
            </div>
          </div>
        </article>
      </div>

      <div v-if="!styleSubmissions.length" class="quiet-empty compact-empty">这个风格包还没有收到投稿。</div>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { Bell, Collection, Refresh, Search, Star, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BilingualTagLabel from './BilingualTagLabel.vue'
import { useDisplayDensity } from '../composables/useDisplayDensity'
import {
  archiveStylePackageAsset,
  createStylePackageAsset,
  downloadStylePackageAsset,
  getStylePackageAssets,
  getStylePackageDetail,
  getStylePackageManifest,
  uploadStylePackageAssetFile,
  updateStylePackageAsset
} from '../api/stylePackages'

const props = defineProps({
  styleForm: { type: Object, required: true },
  styleLoading: { type: Boolean, default: false },
  allTags: { type: Array, default: () => [] },
  ownedArtworks: { type: Array, default: () => [] },
  queryState: { type: Object, required: true },
  marketStylePackages: { type: Array, default: () => [] },
  myStylePackages: { type: Array, default: () => [] },
  myStylePackageSubmissions: { type: Array, default: () => [] },
  stylePackageVersions: { type: Array, default: () => [] },
  stylePackageReviews: { type: Array, default: () => [] },
  styleSubmissions: { type: Array, default: () => [] },
  activeStylePackageName: { type: String, default: '' },
  styleSubmissionForm: { type: Object, required: true },
  styleReviewForm: { type: Object, required: true },
  styleStatsItems: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  isFavoriteTarget: { type: Function, default: null },
  isSubscribedTarget: { type: Function, default: null },
  focusTargetId: { type: Number, default: null },
  focusTargetStamp: { type: Number, default: 0 },
  initialView: { type: String, default: 'market' }
})

const emit = defineEmits([
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
  'patch-submission-form',
  'submit-submission',
  'prepare-review',
  'load-reviews',
  'patch-review-form',
  'submit-review',
  'review-submission',
  'go-related',
  'toggle-favorite-target',
  'toggle-subscription-target'
])

const currentView = ref(props.initialView)
const keyword = ref(props.queryState.keyword || '')
const selectedTagId = ref(props.queryState.tagId ?? null)
const localStatus = ref(props.queryState.status || '')
const sortMode = ref(props.queryState.sort || 'latest')
const currentPage = ref(props.queryState.page || 1)
const pageSize = ref(props.queryState.size || 12)
const { density, densityOptions } = useDisplayDensity()
const activePackId = ref(null)
const activePreviewId = ref('')
const packDrawerVisible = ref(false)
const packEditorVisible = ref(false)
const packSubmissionVisible = ref(false)
const packReviewVisible = ref(false)
const ownerOpsVisible = ref(false)
const activePackDetail = ref(null)
const packAssets = ref([])
const assetsLoading = ref(false)
const selectedAssetCategory = ref('')
const activeAsset = ref(null)
const assetDetailVisible = ref(false)
const assetEditorVisible = ref(false)
const assetUploading = ref(false)
const assetSaving = ref(false)
const manifestVisible = ref(false)
const manifestLoading = ref(false)
const manifestData = ref(null)
const collaboratorInput = ref('')
const reviewDrafts = reactive({})
const assetForm = reactive({
  id: null,
  logicalKey: '',
  name: '',
  categoryKey: 'VEGETATION',
  description: '',
  previewImageUrl: '',
  fileUrl: '',
  thumbnailUrl: '',
  promptText: '',
  negativePromptText: '',
  generationParamsJson: '',
  width: 1024,
  height: 1024,
  fileFormat: 'PNG',
  backgroundMode: 'TRANSPARENT',
  licenseScope: 'PACKAGE',
  sortOrder: 0
})
let queryTimer = null
let lastEmittedQueryKey = ''

const resourceCategoryOptions = [
  { value: 'VEGETATION', label: '植被' },
  { value: 'ARCHITECTURE', label: '建筑' },
  { value: 'PROPS', label: '道具' },
  { value: 'CHARACTERS', label: '角色' },
  { value: 'CREATURES', label: '生物' },
  { value: 'TERRAIN', label: '地形与材质' },
  { value: 'UI', label: '界面与图标' },
  { value: 'BACKGROUNDS', label: '背景与特效' }
]
const marketSummary = '像挑选游戏资源包一样浏览统一风格的植被、角色、建筑、道具与界面素材。'
const workspaceSummary = '维护资源目录、版本快照、授权范围和协作成员，形成真正可交付的风格资产。'

const isMarketView = computed(() => currentView.value === 'market')
const sourcePacks = computed(() => (isMarketView.value ? props.marketStylePackages : props.myStylePackages))
const visiblePacks = computed(() => sourcePacks.value || [])
const pagerSummary = computed(() => `${Number(props.queryState.total || 0)} 项 / 第 ${currentPage.value} 页`)

const availableTags = computed(() => {
  if (props.allTags?.length) return props.allTags
  const tagMap = new Map()
  sourcePacks.value.forEach((pack) => {
    ;(pack.tags || []).forEach((tag) => {
      if (!tagMap.has(tag.id)) tagMap.set(tag.id, tag)
    })
  })
  return [...tagMap.values()]
})

const featuredArtworkOptions = computed(() => props.ownedArtworks || [])
const featuredArtwork = computed(() => featuredArtworkOptions.value.find((item) => item.id === props.styleForm.featuredArtworkId) || null)
const editorPackageArtworks = computed(() => {
  if (!props.styleForm.id) return []
  return (props.myStylePackages.find((item) => item.id === props.styleForm.id)?.artworks || []).filter(Boolean)
})

const activePack = computed(() => {
  if (activePackDetail.value?.id === activePackId.value) return activePackDetail.value
  return visiblePacks.value.find((pack) => pack.id === activePackId.value)
    || sourcePacks.value.find((pack) => pack.id === activePackId.value)
    || null
})

const resourceCategories = computed(() => {
  const counts = new Map()
  packAssets.value.forEach((asset) => counts.set(asset.categoryKey, (counts.get(asset.categoryKey) || 0) + 1))
  return [...counts.entries()].map(([key, count]) => ({ key, count }))
})

const filteredPackAssets = computed(() => {
  if (!selectedAssetCategory.value) return packAssets.value
  return packAssets.value.filter((asset) => asset.categoryKey === selectedAssetCategory.value)
})

const activePreviewUrl = computed(() => {
  const previews = previewImages(activePack.value)
  return previews.find((preview) => preview.key === activePreviewId.value)?.url || previews[0]?.url || ''
})

const currentPackSubmissionHistory = computed(() => {
  return props.myStylePackageSubmissions.filter((item) => item.stylePackageId === activePackId.value)
})

const submissionStatusByArtworkId = computed(() => {
  const map = new Map()
  currentPackSubmissionHistory.value.forEach((item) => {
    map.set(item.artworkId, item.status)
  })
  return map
})

const submissionEligibleArtworks = computed(() => {
  return (props.ownedArtworks || []).filter((artwork) => ['ACTIVE', 'PENDING_AUDIT'].includes(artwork.status))
})

const summaryText = computed(() => {
  return isMarketView.value
    ? '每个资源包都包含一组可复用、可版本化、视觉一致的创作素材，不再只是提示词模板。'
    : '在这里维护封面、标签、授权、协作者和包内资源修订，并用作品展示最终使用效果。'
})

const currentQuery = () => ({
  keyword: keyword.value,
  tagId: selectedTagId.value,
  status: localStatus.value,
  sort: sortMode.value,
  page: currentPage.value,
  size: pageSize.value
})

const queryKey = (query) => JSON.stringify({
  keyword: query?.keyword || '',
  tagId: query?.tagId ?? null,
  status: query?.status || '',
  sort: query?.sort || 'latest',
  page: Number(query?.page) || 1,
  size: Number(query?.size) || 12
})

const emitRefresh = () => {
  const query = currentQuery()
  lastEmittedQueryKey = queryKey(query)
  emit('refresh', query)
}

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

const collaboratorRoleText = (role) => {
  const labels = {
    OWNER: '创建者',
    CURATOR: '策展协作',
    CONTRIBUTOR: '内容协作'
  }
  return labels[role] || role || '协作者'
}

const submissionStateText = (status) => {
  const labels = {
    PENDING: '已投稿待审',
    APPROVED: '已收录',
    REJECTED: '曾被驳回'
  }
  return labels[status] || status || '已投稿'
}

const statusBadgeClass = (status) => {
  if (status === 'PUBLISHED') return 'published'
  if (status === 'ARCHIVED') return 'archived'
  return (status || '').toLowerCase()
}

const previewImages = (pack) => {
  if (!pack) return []
  const previews = []
  const seen = new Set()
  const add = (url, key, title) => {
    if (!url || seen.has(url)) return
    seen.add(url)
    previews.push({ url, key, title })
  }
  add(pack.coverImageUrl, `cover-${pack.id}`, pack.name)
  for (const asset of pack.assetPreviews || []) {
    add(asset.thumbnailUrl || asset.previewImageUrl, `asset-${asset.id}`, asset.name)
  }
  for (const artwork of pack.artworks || []) {
    add(artwork.imageUrl, `artwork-${artwork.id}`, artwork.title)
  }
  return previews
}

const primaryImage = (pack) => previewImages(pack)[0]?.url || ''
const artworkCount = (pack) => Number(pack?.stats?.approvedArtworkCount || pack?.artworks?.length || 0)
const resourceCount = (pack) => Number(pack?.stats?.resourceCount || pack?.assetPreviews?.length || 0)
const categoryCount = (pack) => Number(pack?.stats?.categoryCount || new Set((pack?.assetPreviews || []).map((item) => item.categoryKey)).size || 0)
const ownerText = (pack) => {
  if (pack?.owner) return '由你维护'
  if (pack?.editable) return '你参与协作'
  const count = Number(pack?.stats?.collaboratorCount || 0)
  return count > 0 ? `${count} 位协作者参与` : '公开风格包'
}
const tagLabel = (tag) => (tag?.displayNameZh ? `${tag.displayNameZh} / ${tag.name}` : tag?.name || '')
const featuredArtworkLabel = (artwork) => `${artwork.title || '作品'} #${artwork.id}`
const resourceCategoryLabel = (key) => resourceCategoryOptions.find((item) => item.value === key)?.label || key || '未分类'
const backgroundModeLabel = (mode) => ({ TRANSPARENT: '透明背景', SOLID: '纯色背景', SCENE: '完整场景' }[mode] || mode || '未记录')

const reloadPackDetail = (pack) => {
  emit('load-versions', pack)
  emit('load-reviews', pack)
  emit('load-artworks', pack)
  loadPackResources(pack)
}

const loadPackResources = async (pack) => {
  if (!pack?.id) return
  const requestId = pack.id
  assetsLoading.value = true
  try {
    const [detail, assets] = await Promise.all([
      getStylePackageDetail(pack.id),
      getStylePackageAssets(pack.id)
    ])
    if (activePackId.value !== requestId) return
    activePackDetail.value = detail
    packAssets.value = assets || []
    if (selectedAssetCategory.value && !packAssets.value.some((asset) => asset.categoryKey === selectedAssetCategory.value)) {
      selectedAssetCategory.value = ''
    }
  } catch (error) {
    ElMessage.error(error?.message || '资源目录加载失败')
  } finally {
    if (activePackId.value === requestId) assetsLoading.value = false
  }
}

const openPackDrawer = (pack) => {
  activePackId.value = pack.id
  activePackDetail.value = null
  packAssets.value = []
  selectedAssetCategory.value = ''
  activePreviewId.value = previewImages(pack)[0]?.key || ''
  packDrawerVisible.value = true
  reloadPackDetail(pack)
  if (!isMarketView.value) emit('load-submissions', pack)
}

const resetAssetForm = () => {
  Object.assign(assetForm, {
    id: null,
    logicalKey: '',
    name: '',
    categoryKey: selectedAssetCategory.value || 'VEGETATION',
    description: '',
    previewImageUrl: '',
    fileUrl: '',
    thumbnailUrl: '',
    promptText: '',
    negativePromptText: '',
    generationParamsJson: '',
    width: 1024,
    height: 1024,
    fileFormat: 'PNG',
    backgroundMode: 'TRANSPARENT',
    licenseScope: 'PACKAGE',
    sortOrder: packAssets.value.length
  })
}

const openAssetDetail = (asset) => {
  activeAsset.value = asset
  assetDetailVisible.value = true
}

const openAssetEditor = (asset = null) => {
  resetAssetForm()
  if (asset) {
    Object.assign(assetForm, {
      id: asset.id,
      logicalKey: asset.logicalKey || '',
      name: asset.name || '',
      categoryKey: asset.categoryKey || 'PROPS',
      description: asset.description || '',
      previewImageUrl: asset.previewImageUrl || '',
      fileUrl: asset.fileUrl || '',
      thumbnailUrl: asset.thumbnailUrl || '',
      promptText: asset.promptText || '',
      negativePromptText: asset.negativePromptText || '',
      generationParamsJson: asset.generationParamsJson || '',
      width: asset.width || 1024,
      height: asset.height || 1024,
      fileFormat: asset.fileFormat || 'PNG',
      backgroundMode: asset.backgroundMode || 'TRANSPARENT',
      licenseScope: asset.licenseScope || 'PACKAGE',
      sortOrder: asset.sortOrder || 0
    })
  }
  assetDetailVisible.value = false
  assetEditorVisible.value = true
}

const saveAsset = async () => {
  if (!activePack.value?.id || !assetForm.name.trim() || !assetForm.previewImageUrl.trim()) {
    ElMessage.warning('请填写资源名称和预览图地址')
    return
  }
  assetSaving.value = true
  try {
    const payload = {
      logicalKey: assetForm.logicalKey || undefined,
      name: assetForm.name,
      categoryKey: assetForm.categoryKey,
      assetType: 'IMAGE',
      description: assetForm.description,
      previewImageUrl: assetForm.previewImageUrl,
      fileUrl: assetForm.fileUrl,
      thumbnailUrl: assetForm.thumbnailUrl || assetForm.previewImageUrl,
      promptText: assetForm.promptText,
      negativePromptText: assetForm.negativePromptText,
      generationParamsJson: assetForm.generationParamsJson || null,
      width: assetForm.width,
      height: assetForm.height,
      fileFormat: assetForm.fileFormat,
      backgroundMode: assetForm.backgroundMode,
      licenseScope: assetForm.licenseScope,
      sortOrder: assetForm.sortOrder
    }
    if (assetForm.id) await updateStylePackageAsset(activePack.value.id, assetForm.id, payload)
    else await createStylePackageAsset(activePack.value.id, payload)
    ElMessage.success(assetForm.id ? '资源新修订已创建' : '资源已加入资源包')
    assetEditorVisible.value = false
    await loadPackResources(activePack.value)
    emitRefresh()
  } catch (error) {
    ElMessage.error(error?.message || '资源保存失败')
  } finally {
    assetSaving.value = false
  }
}

const handleAssetFileUpload = async ({ file }) => {
  if (!activePack.value?.id) {
    ElMessage.warning('请先选择需要维护的资源包')
    return
  }
  assetUploading.value = true
  try {
    const uploaded = await uploadStylePackageAssetFile(activePack.value.id, file)
    assetForm.fileUrl = uploaded.fileUrl
    const extension = uploaded.originalFilename?.split('.').pop()?.toUpperCase()
    if (extension) assetForm.fileFormat = extension === 'JPEG' ? 'JPG' : extension
    ElMessage.success('原始资源已上传到私有存储')
  } catch (error) {
    ElMessage.error(error?.message || '资源文件上传失败')
  } finally {
    assetUploading.value = false
  }
}

const archiveAsset = async (asset) => {
  try {
    await ElMessageBox.confirm(`归档资源“${asset.name}”？历史版本仍会保留这个修订。`, '归档资源', { type: 'warning' })
    await archiveStylePackageAsset(activePack.value.id, asset.id)
    assetDetailVisible.value = false
    ElMessage.success('资源已归档')
    await loadPackResources(activePack.value)
    emitRefresh()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error?.message || '资源归档失败')
  }
}

const openManifest = async (pack) => {
  manifestVisible.value = true
  manifestLoading.value = true
  manifestData.value = null
  try {
    manifestData.value = await getStylePackageManifest(pack.id)
  } catch (error) {
    manifestVisible.value = false
    ElMessage.error(error?.message || '资源清单加载失败')
  } finally {
    manifestLoading.value = false
  }
}

const downloadAssetFile = async (asset) => {
  if (!activePack.value?.id || !asset?.id) return
  try {
    const blob = await downloadStylePackageAsset(activePack.value.id, asset.id)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    const extension = (asset.fileFormat || 'bin').toLowerCase()
    link.href = url
    link.download = `${asset.name || 'resource'}.${extension}`
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error(error?.message || '资源下载失败')
  }
}

const openCreateDialog = () => {
  activePackId.value = null
  collaboratorInput.value = ''
  emit('reset-form')
  packEditorVisible.value = true
}

const openEditDialog = (pack) => {
  emit('edit-pack', pack)
  collaboratorInput.value = (pack.collaborators || []).map((user) => `${user.userId}#${user.role || 'CONTRIBUTOR'}`).join(', ')
  packEditorVisible.value = true
}

const updateCollaboratorInput = (value) => {
  collaboratorInput.value = value
  const collaborators = value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => {
      const [userIdText, roleText] = item.split('#')
      const userId = userIdText?.trim()
      if (!/^\d+$/.test(userId || '')) return null
      return {
        userId,
        role: roleText?.trim() || 'CONTRIBUTOR'
      }
    })
    .filter(Boolean)
  emit('patch-style-form', { collaborators })
}

const syncFeaturedToCover = () => {
  if (!featuredArtwork.value?.imageUrl) return
  const artworkTagNames = (featuredArtwork.value.tags || []).map((tag) => tag.name).filter(Boolean)
  emit('patch-style-form', {
    coverImageUrl: featuredArtwork.value.imageUrl,
    tagNames: [...new Set([...(props.styleForm.tagNames || []), ...artworkTagNames])],
    promptGuide: props.styleForm.promptGuide || featuredArtwork.value.promptText || ''
  })
}

const isSubmissionArtworkLocked = (artwork) => {
  const status = submissionStatusByArtworkId.value.get(artwork.id)
  return status === 'PENDING' || status === 'APPROVED'
}

const selectSubmissionArtwork = (artwork) => {
  if (isSubmissionArtworkLocked(artwork)) return
  emit('patch-submission-form', { artworkId: artwork.id })
}

const submitPackEditor = () => {
  emit('save')
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

const submitReviewAndClose = () => {
  emit('submit-review')
  packReviewVisible.value = false
}

const openOwnerOps = (pack) => {
  activePackId.value = pack.id
  emit('load-submissions', pack)
  ownerOpsVisible.value = true
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
    selectedTagId.value = value?.tagId ?? null
    localStatus.value = value?.status || ''
    sortMode.value = value?.sort || 'latest'
    currentPage.value = value?.page || 1
    pageSize.value = value?.size || 12
    lastEmittedQueryKey = queryKey(value || {})
  },
  { deep: true }
)

watch([keyword, selectedTagId, localStatus, sortMode], () => {
  window.clearTimeout(queryTimer)
  queryTimer = window.setTimeout(() => {
    currentPage.value = 1
    const query = currentQuery()
    if (queryKey(query) === lastEmittedQueryKey) return
    emitRefresh()
  }, 260)
})

watch(
  () => [props.focusTargetId, props.focusTargetStamp, sourcePacks.value.length],
  () => {
    if (!props.focusTargetId) return
    const matched = sourcePacks.value.find((pack) => pack.id === props.focusTargetId)
    if (!matched) return
    openPackDrawer(matched)
  },
  { immediate: true }
)
</script>

<style scoped>
.style-pack-card {
  position: relative;
}

.style-card-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 3;
}

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
  background: rgba(255, 255, 255, 0.82);
  color: #64748b;
  cursor: pointer;
  transition: color 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.card-icon-action.active {
  color: #0f766e;
  border-color: rgba(16, 185, 129, 0.26);
  background: rgba(236, 253, 245, 0.95);
}

.license-summary-block {
  border-left: 3px solid #10b981;
  padding-left: 14px;
}

.resource-section-heading {
  align-items: flex-end;
  gap: 20px;
}

.resource-section-heading > div:first-child {
  display: grid;
  gap: 5px;
}

.resource-section-heading h2,
.resource-section-heading p {
  margin: 0;
}

.resource-section-heading span {
  color: #64748b;
  font-size: 13px;
}

.resource-category-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 2px 0 14px;
}

.resource-category-tabs button {
  min-height: 34px;
  padding: 0 12px;
  border: 1px solid #dbe5df;
  border-radius: 6px;
  background: #fff;
  color: #475569;
  white-space: nowrap;
  cursor: pointer;
}

.resource-category-tabs button span {
  margin-left: 5px;
  color: #94a3b8;
  font-size: 12px;
}

.resource-category-tabs button.active {
  border-color: #059669;
  background: #ecfdf5;
  color: #047857;
}

.resource-asset-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
}

.resource-asset-card {
  min-width: 0;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.16s ease, box-shadow 0.16s ease;
}

.resource-asset-card:hover {
  transform: translateY(-2px);
  border-color: #86c9ae;
  box-shadow: 0 12px 24px rgba(15, 118, 110, 0.09);
}

.resource-asset-image {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #edf4f0;
}

.resource-asset-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.resource-asset-image span {
  position: absolute;
  left: 9px;
  bottom: 9px;
  padding: 4px 7px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.92);
  color: #166534;
  font-size: 12px;
  font-weight: 700;
}

.resource-asset-copy {
  display: grid;
  gap: 8px;
  padding: 12px;
}

.resource-asset-copy > div:first-child {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.resource-asset-copy strong {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #16302a;
}

.resource-asset-copy > div:first-child span {
  flex: none;
  color: #94a3b8;
  font-size: 12px;
}

.resource-asset-copy p {
  min-height: 38px;
  margin: 0;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: #64748b;
  font-size: 13px;
  line-height: 1.45;
}

.resource-asset-meta,
.resource-version-stats,
.resource-detail-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.resource-asset-meta span,
.resource-version-stats span {
  color: #64748b;
  font-size: 12px;
}

.resource-empty-state {
  min-height: 150px;
  display: grid;
  place-items: center;
}

.resource-detail-layout {
  display: grid;
  grid-template-columns: minmax(280px, 0.9fr) minmax(320px, 1.1fr);
  gap: 24px;
}

.resource-detail-preview {
  aspect-ratio: 1;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #edf4f0;
}

.resource-detail-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.resource-detail-info {
  display: grid;
  align-content: start;
  gap: 16px;
}

.resource-detail-info > p {
  margin: 0;
  color: #475569;
  line-height: 1.7;
}

.resource-spec-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 0;
}

.resource-spec-list div {
  padding: 10px 12px;
  border: 1px solid #e5ebe8;
  border-radius: 6px;
  background: #f8fbf9;
}

.resource-spec-list dt {
  color: #94a3b8;
  font-size: 12px;
}

.resource-spec-list dd {
  margin: 3px 0 0;
  color: #16302a;
  font-weight: 700;
}

.resource-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.resource-form-wide {
  grid-column: 1 / -1;
}

.resource-upload-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  width: 100%;
}

.resource-upload-hint {
  display: block;
  margin-top: 6px;
  color: #7b8b84;
  font-size: 12px;
  line-height: 1.55;
}

.manifest-layout {
  min-height: 240px;
}

.manifest-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border: 1px solid #dfe8e3;
  border-radius: 8px;
  overflow: hidden;
}

.manifest-summary div {
  display: grid;
  gap: 3px;
  padding: 14px;
  border-right: 1px solid #dfe8e3;
}

.manifest-summary div:last-child {
  border-right: 0;
}

.manifest-summary strong {
  color: #065f46;
  font-size: 20px;
}

.manifest-summary span,
.manifest-license {
  color: #64748b;
  font-size: 13px;
}

.manifest-license {
  margin: 14px 0;
  padding: 10px 12px;
  border-left: 3px solid #10b981;
  background: #f0fdf4;
}

.manifest-resource-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.manifest-resource-list button {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  padding: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.manifest-resource-list img {
  width: 64px;
  height: 52px;
  border-radius: 4px;
  object-fit: cover;
  background: #edf4f0;
}

.manifest-resource-list span {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.manifest-resource-list strong,
.manifest-resource-list small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.manifest-resource-list small {
  color: #64748b;
}

@media (max-width: 760px) {
  .resource-detail-layout,
  .resource-form-grid {
    grid-template-columns: 1fr;
  }

  .resource-form-wide {
    grid-column: auto;
  }

  .resource-upload-row {
    grid-template-columns: 1fr;
  }

  .resource-upload-row .el-upload,
  .resource-upload-row .el-button {
    width: 100%;
  }

  .manifest-summary,
  .manifest-resource-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .manifest-summary div:nth-child(2) {
    border-right: 0;
  }

  .manifest-summary div:nth-child(-n + 2) {
    border-bottom: 1px solid #dfe8e3;
  }
}
</style>
