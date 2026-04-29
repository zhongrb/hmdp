import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

const TOKEN_KEY = 'hmdp-token'
const USER_KEY = 'hmdp-user'

export interface AuthUser {
  id: number
  nickName: string
  icon?: string | null
}

function readToken() {
  return window.localStorage.getItem(TOKEN_KEY)
}

function readUser(): AuthUser | null {
  const raw = window.localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(readToken())
  const user = ref<AuthUser | null>(readUser())
  const redirectPath = ref<string | null>(null)

  const isAuthenticated = computed(() => Boolean(token.value))

  function setSession(nextToken: string, nextUser: AuthUser) {
    token.value = nextToken
    user.value = nextUser
    window.localStorage.setItem(TOKEN_KEY, nextToken)
    window.localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
  }

  function clearSession() {
    token.value = null
    user.value = null
    window.localStorage.removeItem(TOKEN_KEY)
    window.localStorage.removeItem(USER_KEY)
  }

  function setRedirectPath(path: string | null) {
    redirectPath.value = path
  }

  return {
    token,
    user,
    redirectPath,
    isAuthenticated,
    setSession,
    clearSession,
    setRedirectPath,
  }
})
