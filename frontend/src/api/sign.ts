import { http } from './http'

export async function signToday() {
  const { data } = await http.post('/sign')
  return data.data
}

export async function fetchSignStreak() {
  const { data } = await http.get('/sign/streak')
  return data.data as number
}
