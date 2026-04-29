import { http } from './http'

export interface LoginPayload {
  phone: string
  code: string
}

export interface AuthUser {
  id: number
  nickName: string
  icon?: string | null
}

export interface LoginResponse {
  token: string
  user: AuthUser
}

export async function sendCode(phone: string) {
  const { data } = await http.post('/auth/code', { phone })
  return data
}

export async function login(payload: LoginPayload) {
  const { data } = await http.post('/auth/login', payload)
  return data.data as LoginResponse
}
