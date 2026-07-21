<template>
  <div class="auth-layout">
    <section class="auth-showcase soft-panel">
      <div class="auth-showcase-copy">
        <div class="brand-badge">
          <span class="brand-badge-mark"><MagicStick /></span>
          <span>ArtForge</span>
        </div>
        <h1>从提示词到可交付资源包，让 AI 图像创作成为可复用资产。</h1>
        <p>
          ArtForge 把 Forge 生图、作品沉淀、统一风格资源共创、任务协作和积分交易
          放进同一套工作流，让一次创作能够继续被整理、组合和使用。
        </p>
      </div>

      <div class="auth-pack-showcase">
        <div class="auth-pack-gallery" aria-label="翡翠童话森林资源包预览">
          <img
            class="auth-pack-cover"
            src="/images/style-packs/emerald-fable/cover.png"
            alt="翡翠童话森林风格资源包封面"
          />
          <img src="/images/style-packs/emerald-fable/vegetation-01.png" alt="翡翠童话森林植被资源" />
          <img src="/images/style-packs/emerald-fable/architecture-01.png" alt="翡翠童话森林建筑资源" />
          <img src="/images/style-packs/emerald-fable/characters-01.png" alt="翡翠童话森林角色资源" />
        </div>

        <div class="auth-pack-copy">
          <p class="auth-pack-label">平台示例资源包</p>
          <h2>翡翠童话森林</h2>
          <p>
            一套可以直接浏览、购买和持续协作维护的统一风格游戏美术资源，包含从环境搭建到界面表现所需的多类素材。
          </p>

          <div class="auth-pack-stats">
            <div>
              <strong>32</strong>
              <span>项资源</span>
            </div>
            <div>
              <strong>8</strong>
              <span>个类目</span>
            </div>
            <div>
              <strong>版本化</strong>
              <span>协作交付</span>
            </div>
          </div>

          <p class="auth-pack-categories">植被 · 建筑 · 角色 · 道具 · 生物 · 地形 · UI · 特效</p>
        </div>
      </div>

      <div class="auth-feature-list">
        <article v-for="item in highlights" :key="item.title" class="auth-feature-row">
          <div class="auth-feature-icon">
            <component :is="item.icon" />
          </div>
          <div class="auth-feature-text">
            <h2>{{ item.title }}</h2>
            <p>{{ item.description }}</p>
          </div>
        </article>
      </div>
    </section>

    <section class="auth-card soft-panel">
      <div class="auth-card-head">
        <div>
          <p class="eyebrow">Account</p>
          <h2>{{ authMode === 'login' ? '登录平台' : '创建账号' }}</h2>
          <p class="auth-card-subtitle">进入你的创作工作台。</p>
        </div>
        <el-segmented v-model="authMode" :options="authOptions" size="small" />
      </div>

      <el-form class="auth-form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="authForm.username" autocomplete="username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="authMode === 'register'" label="昵称">
          <el-input v-model="authForm.displayName" autocomplete="name" placeholder="平台内展示名称" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="authForm.password"
            type="password"
            show-password
            :autocomplete="authMode === 'login' ? 'current-password' : 'new-password'"
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-button type="primary" :loading="auth.loading.value" :icon="Key" class="wide-button" @click="submitAuth">
          {{ authMode === 'login' ? '登录' : '创建账号' }}
        </el-button>
      </el-form>

      <div class="auth-assist-row">
        <div class="auth-assist-copy">
          <strong>首次部署才需要管理员初始化</strong>
          <span>和普通登录入口分开，避免和日常登录混在一起。</span>
        </div>
        <el-button text @click="adminDialogVisible = true">管理员初始化</el-button>
      </div>
    </section>

    <el-dialog
      v-model="adminDialogVisible"
      width="min(540px, 92vw)"
      destroy-on-close
      align-center
      class="admin-bootstrap-dialog"
    >
      <template #header>
        <div class="admin-bootstrap-head">
          <p class="eyebrow">Admin Bootstrap</p>
          <h2>初始化管理员账号</h2>
          <p class="auth-card-subtitle">仅在首次部署、系统中尚未存在管理员账号时使用。</p>
        </div>
      </template>

      <el-form label-position="top" @submit.prevent>
        <el-form-item label="管理员用户名">
          <el-input v-model="adminBootstrapForm.username" placeholder="例如 admin" />
        </el-form-item>
        <el-form-item label="显示名称">
          <el-input v-model="adminBootstrapForm.displayName" placeholder="例如 平台管理员" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="adminBootstrapForm.password"
            type="password"
            show-password
            placeholder="请输入管理员密码"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer-row">
          <el-button @click="adminDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="adminBootstrapLoading" @click="submitBootstrapAdmin">
            创建管理员
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import '../auth-page.css'
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Key, MagicStick, Opportunity, SetUp } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const authMode = ref('login')
const adminDialogVisible = ref(false)
const authOptions = [
  { label: '登录', value: 'login' },
  { label: '注册', value: 'register' }
]

const authForm = reactive({ username: '', password: '', displayName: '' })
const adminBootstrapForm = reactive({ username: 'admin', password: '', displayName: '平台管理员' })
const adminBootstrapLoading = ref(false)

const redirectTarget = computed(() => {
  return typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/')
    ? route.query.redirect
    : '/app'
})

const highlights = [
  {
    title: '可控生成',
    description: '结构化提示词、模型与高级参数集中在同一个 Forge 工作台。',
    icon: MagicStick
  },
  {
    title: '资源包共创',
    description: '统一风格资源支持版本、协作者、授权、兑换与交付清单。',
    icon: SetUp
  },
  {
    title: '任务与社区',
    description: '从需求发布、投稿审核到作品展示、收藏订阅和奖励流转。',
    icon: Opportunity
  }
]

async function goToApp() {
  await auth.hydrateAuth()
  await router.push(redirectTarget.value)
  if (router.currentRoute.value.fullPath !== redirectTarget.value) {
    await router.replace('/app')
  }
}

async function submitAuth() {
  try {
    if (!authForm.username.trim() || !authForm.password.trim()) {
      ElMessage.warning('请先填写用户名和密码')
      return
    }
    if (authMode.value === 'register' && !authForm.displayName.trim()) {
      ElMessage.warning('注册时请填写昵称')
      return
    }

    if (authMode.value === 'login') {
      await auth.loginUser({
        ...authForm,
        username: authForm.username.trim(),
        password: authForm.password.trim()
      })
      ElMessage.success('已登录')
    } else {
      await auth.registerUser({
        ...authForm,
        username: authForm.username.trim(),
        password: authForm.password.trim(),
        displayName: authForm.displayName.trim()
      })
      ElMessage.success('账号已创建')
    }

    await goToApp()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function submitBootstrapAdmin() {
  if (!adminBootstrapForm.username.trim() || !adminBootstrapForm.password.trim() || !adminBootstrapForm.displayName.trim()) {
    ElMessage.warning('请填写完整的管理员信息')
    return
  }
  adminBootstrapLoading.value = true
  try {
    await auth.bootstrapAdminUser({
      username: adminBootstrapForm.username.trim(),
      password: adminBootstrapForm.password.trim(),
      displayName: adminBootstrapForm.displayName.trim()
    })
    ElMessage.success('管理员已创建')
    adminDialogVisible.value = false
    await router.replace('/app')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    adminBootstrapLoading.value = false
  }
}
</script>
