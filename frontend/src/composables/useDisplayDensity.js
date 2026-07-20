import { ref, watch } from 'vue'

const storageKey = 'aiart_content_density'
const allowedDensities = new Set(['large', 'standard', 'compact'])

function initialDensity() {
  if (typeof window === 'undefined') return 'standard'
  const saved = window.localStorage.getItem(storageKey)
  return allowedDensities.has(saved) ? saved : 'standard'
}

const density = ref(initialDensity())

watch(density, (value) => {
  if (typeof window !== 'undefined' && allowedDensities.has(value)) {
    window.localStorage.setItem(storageKey, value)
  }
})

export const densityOptions = [
  { label: '大图', value: 'large' },
  { label: '标准', value: 'standard' },
  { label: '紧凑', value: 'compact' }
]

export function useDisplayDensity() {
  return { density, densityOptions }
}
