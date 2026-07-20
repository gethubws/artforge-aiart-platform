import client from './client'

export const toggleFavoriteTarget = (payload) => client.post('/engagement/favorites/toggle', payload)
export const toggleSubscriptionTarget = (payload) => client.post('/engagement/subscriptions/toggle', payload)
export const getFavoriteTargets = (params = {}) => client.get('/engagement/favorites', { params })
export const getSubscriptionTargets = (params = {}) => client.get('/engagement/subscriptions', { params })
export const getNotifications = (params = {}) => client.get('/engagement/notifications', { params })
export const markNotificationRead = (id) => client.post(`/engagement/notifications/${id}/read`)
export const markAllNotificationsRead = () => client.post('/engagement/notifications/read-all')
