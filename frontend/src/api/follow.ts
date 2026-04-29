import { http } from './http'
import type { AuthUser } from '../stores/auth'

export async function followUser(targetUserId: number) {
  const { data } = await http.post(`/follows/${targetUserId}`)
  return data.data
}

export async function unfollowUser(targetUserId: number) {
  const { data } = await http.delete(`/follows/${targetUserId}`)
  return data.data
}

export async function fetchCommonFollows(targetUserId: number) {
  const { data } = await http.get(`/follows/common/${targetUserId}`)
  return data.data as AuthUser[]
}
