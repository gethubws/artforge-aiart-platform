import client from './client'

export const getTagTree = () => client.get('/tags/tree')
export const getAdminTagTree = () => client.get('/tags/admin/tree')
export const getTagCategories = () => client.get('/tags/categories')
export const getTagOptions = () => client.get('/tags/options')
export const getTags = (params) => client.get('/tags', { params })
export const getTagDetail = (id) => client.get(`/tags/${id}`)
export const buildPrompt = (payload) => client.post('/tags/build-prompt', payload)
export const createTagCategory = (payload) => client.post('/tags/categories', payload)
export const createTag = (payload) => client.post('/tags', payload)
export const updateTag = (id, payload) => client.put(`/tags/${id}`, payload)
export const deactivateTag = (id) => client.delete(`/tags/${id}`)
