import client from './client'

export const createStylePackage = (payload) => client.post('/style-packages', payload)
export const updateStylePackage = (id, payload) => client.put(`/style-packages/${id}`, payload)
export const publishStylePackage = (id) => client.post(`/style-packages/${id}/publish`)
export const archiveStylePackage = (id) => client.post(`/style-packages/${id}/archive`)
export const exchangeStylePackage = (id) => client.post(`/style-packages/${id}/exchange`)
export const submitStylePackageArtwork = (id, payload) => client.post(`/style-packages/${id}/submissions`, payload)
export const getStylePackageSubmissions = (id, params = {}) => client.get(`/style-packages/${id}/submissions`, { params })
export const getStylePackageArtworks = (id, params = {}) => client.get(`/style-packages/${id}/artworks`, { params })
export const getStylePackageVersions = (id, params = {}) => client.get(`/style-packages/${id}/versions`, { params })
export const getStylePackageReviews = (id, params = {}) => client.get(`/style-packages/${id}/reviews`, { params })
export const saveStylePackageReview = (id, payload) => client.post(`/style-packages/${id}/reviews`, payload)
export const getMyStylePackageSubmissions = (params = {}) => client.get('/style-packages/submissions/my', { params })
export const reviewStylePackageSubmission = (id, payload) => client.post(`/style-packages/submissions/${id}/review`, payload)
export const getMyStylePackages = (params = {}) => client.get('/style-packages/my', { params })
export const getMarketStylePackages = (params = {}) => client.get('/style-packages/market', { params })
export const getStylePackageDetail = (id) => client.get(`/style-packages/${id}`)
export const getStylePackageAssets = (id, params = {}) => client.get(`/style-packages/${id}/assets`, { params })
export const createStylePackageAsset = (id, payload) => client.post(`/style-packages/${id}/assets`, payload)
export const uploadStylePackageAssetFile = (id, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return client.post(`/style-packages/${id}/assets/upload`, formData)
}
export const updateStylePackageAsset = (id, assetId, payload) => client.put(`/style-packages/${id}/assets/${assetId}`, payload)
export const archiveStylePackageAsset = (id, assetId) => client.post(`/style-packages/${id}/assets/${assetId}/archive`)
export const downloadStylePackageAsset = (id, assetId) => client.get(`/style-packages/${id}/assets/${assetId}/download`, {
  responseType: 'blob'
})
export const getStylePackageManifest = (id) => client.get(`/style-packages/${id}/manifest`)
