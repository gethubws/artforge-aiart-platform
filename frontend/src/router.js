import { createRouter, createWebHistory } from 'vue-router'
import AuthPage from './pages/AuthPage.vue'
import PlatformPage from './pages/PlatformPage.vue'
import { useAuthStore } from './stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/app'
  },
  {
    path: '/auth',
    name: 'auth',
    component: AuthPage,
    meta: { guestOnly: true }
  },
  {
    path: '/app',
    name: 'app',
    component: PlatformPage,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  await auth.hydrateAuth()

  if (to.meta.requiresAuth && !auth.isAuthenticated.value) {
    return { name: 'auth', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && auth.isAuthenticated.value) {
    return to.query.redirect && typeof to.query.redirect === 'string' ? to.query.redirect : { name: 'app' }
  }

  return true
})

export default router
