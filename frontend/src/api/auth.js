import client from './client'

export const register = (payload) => client.post('/auth/register', payload)
export const bootstrapAdmin = (payload) => client.post('/auth/bootstrap-admin', payload)
export const login = (payload) => client.post('/auth/login', payload)
export const me = () => client.get('/auth/me')
