import client from './client'

export const createTask = (payload) => client.post('/tasks', payload)
export const updateTask = (id, payload) => client.put(`/tasks/${id}`, payload)
export const publishTask = (id) => client.post(`/tasks/${id}/publish`)
export const closeTask = (id) => client.post(`/tasks/${id}/close`)
export const getTaskDetail = (id) => client.get(`/tasks/${id}`)
export const getTaskMarket = (params = {}) => client.get('/tasks/market', { params })
export const getMyTasks = (params = {}) => client.get('/tasks/my', { params })
export const submitTaskArtwork = (id, payload) => client.post(`/tasks/${id}/submissions`, payload)
export const getTaskSubmissions = (id, params = {}) => client.get(`/tasks/${id}/submissions`, { params })
export const getMyTaskSubmissions = (params = {}) => client.get('/tasks/submissions/my', { params })
export const reviewTaskSubmission = (id, payload) => client.post(`/tasks/submissions/${id}/review`, payload)
