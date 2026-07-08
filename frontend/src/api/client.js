import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  timeout: 360000
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('aiart_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use((response) => {
  const body = response.data
  if (body && typeof body.code === 'number' && body.code !== 0) {
    const error = new Error(body.message || 'Request failed')
    error.code = body.code
    error.responseBody = body
    throw error
  }
  return body?.data ?? body
})

export default client
