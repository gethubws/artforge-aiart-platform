import { computed, ref } from 'vue'
import { bootstrapAdmin, login, me, register } from '../api/auth'

const tokenKey = 'aiart_token'
const currentUser = ref(null)
const hydrated = ref(false)
const loading = ref(false)

const isAuthenticated = computed(() => Boolean(currentUser.value))
const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

const setToken = (token) => {
  if (token) {
    localStorage.setItem(tokenKey, token)
  } else {
    localStorage.removeItem(tokenKey)
  }
}

const clearAuth = () => {
  setToken('')
  currentUser.value = null
}

const hydrateAuth = async () => {
  if (hydrated.value) return currentUser.value
  hydrated.value = true
  if (!localStorage.getItem(tokenKey)) return null
  try {
    currentUser.value = await me()
    return currentUser.value
  } catch {
    clearAuth()
    return null
  }
}

const loginUser = async (payload) => {
  loading.value = true
  try {
    const response = await login(payload)
    setToken(response.token)
    currentUser.value = response.user
    return response.user
  } finally {
    loading.value = false
  }
}

const registerUser = async (payload) => {
  loading.value = true
  try {
    const response = await register(payload)
    setToken(response.token)
    currentUser.value = response.user
    return response.user
  } finally {
    loading.value = false
  }
}

const bootstrapAdminUser = async (payload) => {
  loading.value = true
  try {
    const response = await bootstrapAdmin(payload)
    setToken(response.token)
    currentUser.value = response.user
    return response.user
  } finally {
    loading.value = false
  }
}

const logoutUser = () => {
  clearAuth()
}

export const useAuthStore = () => ({
  currentUser,
  hydrated,
  loading,
  isAuthenticated,
  isAdmin,
  hydrateAuth,
  loginUser,
  registerUser,
  bootstrapAdminUser,
  logoutUser,
  clearAuth
})
