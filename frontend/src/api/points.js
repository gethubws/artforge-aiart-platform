import client from './client'

export const getPointAccount = (params = {}) => client.get('/points/account', { params })
export const claimDailyPoints = () => client.post('/points/daily-claim')
