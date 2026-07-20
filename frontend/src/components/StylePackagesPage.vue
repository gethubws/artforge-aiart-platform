<template>
  <section
    class="style-view content-hub-page market-page-shell style-package-page density-page"
    :class="`density-${density}`"
    v-loading="styleLoading"
  >
    <header class="page-hero hub-hero">
      <div class="hero-copy">
        <p class="eyebrow">Style Collection</p>
        <h1>{{ isMarketView ? '风格市场' : '我的风格包' }}</h1>
        <p class="page-subtitle">{{ summaryText }}</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="$emit('go-related', isMarketView ? 'my-styles' : 'style-market')">
          {{ isMarketView ? '进入我的风格包' : '返回风格市场' }}
        </el-button>
        <el-button :icon="Refresh" :loading="styleLoading" @click="$emit('refresh', currentQuery())">刷新</el-button>
        <el-button v-if="!isMarketView" type="primary" @click="openCreateDialog">新建风格包</el-button>
      </div>
    </header>

    <section class="soft-panel market-toolbar-panel style-filter-panel">
      <div class="style-filter-grid">
        <el-input
          v-model="keyword"
          :prefix-icon="Search"
          placeholder="搜索名称、简介、风格说明或标签"
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
          <el-option label="收录作品最多" value="artworks" />
          <el-option label="评分最高" value="rating" />
          <el-option label="协作者最多" value="collaborators" />
          <el-option label="价格最高" value="price" />
        </el-select>
      </div>

      <div class="hub-summary-row market-summary-row">
        <div>
          <h2>{{ isMarketView ? '浏览风格成果集合' : '管理你的风格资产' }}</h2>
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
          <span>{{ artworkCount(pack) }} 张已收录作品</span>
          <span v-if="previewImages(pack).length > 1">共 {{ previewImages(pack).length }} 张预览</span>
          <span v-else>等待更多共创作品</span>
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
        <strong>{{ isMarketView ? '没有匹配的风格包' : '你还没有创建风格包' }}</strong>
        <span>试试调整搜索与标签筛选，后续继续扩展分类与市场规则也会更自然。</span>
      </div>
    </section>

    <section class="soft-panel pager-panel">
      <div class="pager-copy">
        <strong>{{ isMarketView ? '风格市场分页' : '我的风格包分页' }}</strong>
        <span>共 {{ Number(queryState.total || 0) }} 个风格包，当前第 {{ currentPage }} 页。</span>
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
      :title="activePack ? activePack.name : '风格包详情'"
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
                  <el-button v-if="isMarketView" type="primary" @click="openSubmissionDialog(activePack)">投稿作品</el-button>
                  <el-button v-if="isMarketView" @click="openReviewDialog(activePack)">我要评价</el-button>
                  <el-button v-if="isMarketView" @click="$emit('exchange-pack', activePack)">兑换风格包</el-button>

                  <template v-if="!isMarketView">
                    <el-button @click="openEditDialog(activePack)">编辑风格包</el-button>
                    <el-button v-if="activePack.status !== 'PUBLISHED'" type="primary" @click="$emit('publish-pack', activePack)">
                      发布
                    </el-button>
                    <el-button v-else @click="$emit('archive-pack', activePack)">归档</el-button>
                    <el-button @click="openOwnerOps(activePack)">投稿审核</el-button>
                  </template>
                </div>
              </div>
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
                  <strong>{{ version.versionName || `版本 #${version.id}` }}</strong>
                  <span>{{ formatDate(version.createdAt) }}</span>
                  <p class="submission-note-copy">
                    {{ version.styleStatement || version.description || '这个版本主要用于保留风格包快照。' }}
                  </p>
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
              <h2>收录作品</h2>
            </div>
            <div v-if="activePack.artworks?.length" class="style-artwork-grid">
              <article v-for="artwork in activePack.artworks" :key="artwork.id" class="style-artwork-card">
                <img :src="artwork.imageUrl" :alt="artwork.title" />
                <strong>{{ artwork.title }}</strong>
              </article>
            </div>
            <div v-else class="quiet-empty compact-empty">这个风格包还没有收录作品。</div>
          </section>
        </div>
      </template>
    </el-drawer>

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
import { Bell, Collection, Refresh, Search, Star } from '@element-plus/icons-vue'
import BilingualTagLabel from './BilingualTagLabel.vue'
import { useDisplayDensity } from '../composables/useDisplayDensity'

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
const collaboratorInput = ref('')
const reviewDrafts = reactive({})
let queryTimer = null
let lastEmittedQueryKey = ''

const marketSummary = '先看内容预览，再点开详情、投稿、评价或兑换。'
const workspaceSummary = '主页面聚焦预览与筛选，编辑、发布、归档和投稿审核都收进弹层里处理。'

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
  return visiblePacks.value.find((pack) => pack.id === activePackId.value)
    || sourcePacks.value.find((pack) => pack.id === activePackId.value)
    || null
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
    ? '把风格包当成可浏览、可协作、可交易的风格作品集合，而不是单纯的提示词模板。'
    : '在这里维护你的风格资产：封面、标签、风格说明、协作者，以及被收录的作品成果。'
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
  for (const artwork of pack.artworks || []) {
    add(artwork.imageUrl, `artwork-${artwork.id}`, artwork.title)
  }
  return previews
}

const primaryImage = (pack) => previewImages(pack)[0]?.url || ''
const artworkCount = (pack) => Number(pack?.stats?.approvedArtworkCount || pack?.artworks?.length || 0)
const ownerText = (pack) => {
  if (pack?.owner) return '由你维护'
  const count = Number(pack?.stats?.collaboratorCount || 0)
  return count > 0 ? `${count} 位协作者参与` : '公开风格包'
}
const tagLabel = (tag) => (tag?.displayNameZh ? `${tag.displayNameZh} / ${tag.name}` : tag?.name || '')
const featuredArtworkLabel = (artwork) => `${artwork.title || '作品'} #${artwork.id}`

const reloadPackDetail = (pack) => {
  emit('load-versions', pack)
  emit('load-reviews', pack)
  emit('load-artworks', pack)
}

const openPackDrawer = (pack) => {
  activePackId.value = pack.id
  activePreviewId.value = previewImages(pack)[0]?.key || ''
  packDrawerVisible.value = true
  reloadPackDetail(pack)
  if (!isMarketView.value) emit('load-submissions', pack)
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
</style>
