import client from './client'

export const getPendingAudits = (params = {}) => client.get('/audits/pending', { params })
export const getMyAudits = (params = {}) => client.get('/audits/my', { params })
export const reviewAudit = (id, payload) => client.post(`/audits/${id}/review`, payload)
