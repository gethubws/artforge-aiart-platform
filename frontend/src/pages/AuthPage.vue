<template>
  <div class="auth-layout">
    <section class="auth-showcase soft-panel">
      <div class="auth-showcase-copy">
        <div class="brand-badge">
          <span class="brand-badge-mark"><MagicStick /></span>
          <span>ArtForge</span>
        </div>
        <h1>把生成、整理、协作和运营收进同一个创作后台。</h1>
        <p>
          登录后会直接进入平台主界面，在同一套工作流里管理 Forge 生图、作品沉淀、风格包分发和任务协作。
        </p>
      </div>

      <div class="auth-metric-row">
        <div class="auth-metric-card">
          <strong>Forge</strong>
          <span>模型、LoRA、VAE 和高级参数统一接入</span>
        </div>
        <div class="auth-metric-card">
          <strong>Style Pack</strong>
          <span>发布、版本、评价、兑换与投稿闭环</span>
        </div>
        <div class="auth-metric-card">
          <strong>Task Flow</strong>
          <span>任务发布、投稿审核、奖励发放一体化</span>
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
          <el-input v-model="authForm.password" type="password" show-password autocomplete="current-password" placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" :loading="auth.loading.value" :icon="Key" class="wide-button" @click="submitAuth">
          {{ authMode === 'login' ? '登录' : '创建账号' }}
        </el-button>
      </el-form>

      <div class="auth-secondary">
        <button class="auth-secondary-toggle" type="button" @click="adminPanelOpen = !adminPanelOpen">
          <span>管理员初始化</span>
          <span>{{ adminPanelOpen ? '收起' : '展开' }}</span>
        </button>

        <div v-if="adminPanelOpen" class="admin-bootstrap-box">
          <p class="admin-bootstrap-hint">仅在首次部署、尚未存在管理员账号时使用。</p>
          <el-form label-position="top" @submit.prevent>
            <el-form-item label="管理员用户名">
              <el-input v-model="adminBootstrapForm.username" placeholder="例如 admin" />
            </el-form-item>
            <el-form-item label="显示名称">
              <el-input v-model="adminBootstrapForm.displayName" placeholder="例如 平台管理员" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="adminBootstrapForm.password" type="password" show-password placeholder="请输入管理员密码" />
            </el-form-item>
            <el-button class="wide-button" :loading="adminBootstrapLoading" @click="submitBootstrapAdmin">创建管理员</el-button>
          </el-form>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import '../auth-page.css'
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Key, MagicStick, Opportunity, PictureRounded, SetUp } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const authMode = ref('login')
const adminPanelOpen = ref(false)
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
    title: 'Forge 深度控制',
    description: '模型、LoRA、VAE、高清修复和高级参数都集中在同一个工作台里。',
    icon: MagicStick
  },
  {
    title: '风格包与资源运营',
    description: '支持风格包发布、版本记录、评价、兑换和作品收录，形成持续供给。',
    icon: SetUp
  },
  {
    title: '企业任务协作',
    description: '从需求发布、投稿审核到奖励发放，任务市场已经接入主流程。',
    icon: Opportunity
  },
  {
    title: '作品资产沉淀',
    description: '图库、广场、模型资源页与收藏偏好会持续积累，让平台越用越完整。',
    icon: PictureRounded
  }
]

const goToApp = async () => {
  await auth.hydrateAuth()
  await router.push(redirectTarget.value)
  if (router.currentRoute.value.fullPath !== redirectTarget.value) {
    await router.replace('/app')
  }
}

const submitAuth = async () => {
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

const submitBootstrapAdmin = async () => {
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
    await router.replace('/app')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    adminBootstrapLoading.value = false
  }
}
</script>
