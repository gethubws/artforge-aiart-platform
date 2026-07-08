import client from './client'

export const getTagTree = () => client.get('/tags/tree')
export const getAdminTagTree = () => client.get('/tags/admin/tree')
export const buildPrompt = (payload) => client.post('/tags/build-prompt', payload)
export const createTagCategory = (payload) => client.post('/tags/categories', payload)
export const createTag = (payload) => client.post('/tags', payload)
export const updateTag = (id, payload) => client.put(`/tags/${id}`, payload)
export const deactivateTag = (id) => client.delete(`/tags/${id}`)
