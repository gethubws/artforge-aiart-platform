import client from './client'

export const getMyArtworks = (params = {}) => client.get('/artworks/my', { params })
export const getPublicArtworks = (params = {}) => client.get('/artworks/public', { params })
export const requestArtworkPublish = (id) => client.post(`/artworks/${id}/request-publish`)
export const updateArtwork = (id, payload) => client.put(`/artworks/${id}`, payload)
export const archiveArtwork = (id) => client.post(`/artworks/${id}/archive`)
export const bulkRequestArtworkPublish = (artworkIds) => client.post('/artworks/bulk/request-publish', { artworkIds })
export const bulkUpdateArtworkVisibility = (artworkIds, visibility) => client.post('/artworks/bulk/visibility', { artworkIds, visibility })
export const bulkArchiveArtworks = (artworkIds) => client.post('/artworks/bulk/archive', { artworkIds })
export const getArtworkDetail = (id) => client.get(`/artworks/${id}`)
