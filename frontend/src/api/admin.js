import client from './client'

export const getAdminDashboard = () => client.get('/admin/dashboard')
export const getAdminUsers = (params) => client.get('/admin/users', { params })
export const updateAdminUser = (userId, payload) => client.put(`/admin/users/${userId}`, payload)
