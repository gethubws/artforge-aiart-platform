import client from './client'

export const getTagTree = () => client.get('/tags/tree')
export const getAdminTagTree = () => client.get('/tags/admin/tree')
export const getAdminTagDetail = (id) => client.get(`/tags/admin/${id}`)
export const getTagAnalytics = () => client.get('/tags/admin/analytics')
export const getTagCategories = () => client.get('/tags/categories')
export const getTagOptions = () => client.get('/tags/options')
export const getTags = (params) => client.get('/tags', { params })
export const getTagDetail = (id) => client.get(`/tags/${id}`)
export const buildPrompt = (payload) => client.post('/tags/build-prompt', payload)
export const createTagCategory = (payload) => client.post('/tags/categories', payload)
export const createTag = (payload) => client.post('/tags', payload)
export const updateTag = (id, payload) => client.put(`/tags/${id}`, payload)
export const deactivateTag = (id) => client.delete(`/tags/${id}`)

export const addTagPreview = (tagId, file, payload = {}) => {
  const form = previewFormData(file, payload)
  return client.post(`/tags/${tagId}/previews`, form)
}

export const updateTagPreview = (tagId, previewId, payload) => client.put(`/tags/${tagId}/previews/${previewId}`, payload)

export const replaceTagPreviewImage = (tagId, previewId, file) => {
  const form = new FormData()
  form.append('file', file)
  return client.post(`/tags/${tagId}/previews/${previewId}/image`, form)
}

export const deleteTagPreview = (tagId, previewId) => client.delete(`/tags/${tagId}/previews/${previewId}`)
export const reorderTagPreviews = (tagId, previewIds) => client.put(`/tags/${tagId}/previews/order`, { previewIds })

function previewFormData(file, payload) {
  const form = new FormData()
  form.append('file', file)
  Object.entries(payload).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') form.append(key, String(value))
  })
  return form
}
