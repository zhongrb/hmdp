import type { Router } from 'vue-router'
import { useAuthStore } from '../stores/auth'

export function installRouterGuards(router: Router) {
  router.beforeEach((to) => {
    const authStore = useAuthStore()
    if (!to.meta.requiresAuth) {
      return true
    }
    if (authStore.isAuthenticated) {
      return true
    }
    authStore.setRedirectPath(to.fullPath)
    return {
      name: 'login',
      query: {
        redirect: to.fullPath,
      },
    }
  })
}
