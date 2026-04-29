import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchSignStreak, signToday } from '../api/sign'
import { useAuthStore } from './auth'

function currentDateKey() {
  const now = new Date()
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  const day = `${now.getDate()}`.padStart(2, '0')
  return `${now.getFullYear()}${month}${day}`
}

function signMarkerKey(userId: number) {
  return `hmdp-sign-${userId}-${currentDateKey()}`
}

export const useActivityStore = defineStore('activity', () => {
  const streak = ref(0)
  const signedToday = ref(false)
  const loading = ref(false)
  const submitting = ref(false)

  const streakText = computed(() => `${streak.value}`)

  function readSignMarker() {
    const authStore = useAuthStore()
    const userId = authStore.user?.id
    if (!userId) {
      return false
    }
    return window.localStorage.getItem(signMarkerKey(userId)) === '1'
  }

  function writeSignMarker() {
    const authStore = useAuthStore()
    const userId = authStore.user?.id
    if (!userId) {
      return
    }
    window.localStorage.setItem(signMarkerKey(userId), '1')
  }

  async function refreshStreak() {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      reset()
      return
    }
    loading.value = true
    try {
      const nextStreak = await fetchSignStreak()
      streak.value = nextStreak
      signedToday.value = readSignMarker()
    } finally {
      loading.value = false
    }
  }

  async function submitSign() {
    submitting.value = true
    try {
      await signToday()
      const nextStreak = await fetchSignStreak()
      streak.value = nextStreak
      signedToday.value = true
      writeSignMarker()
      return nextStreak
    } finally {
      submitting.value = false
    }
  }

  function markSignedToday() {
    signedToday.value = true
    writeSignMarker()
  }

  function reset() {
    streak.value = 0
    signedToday.value = false
    loading.value = false
    submitting.value = false
  }

  return {
    streak,
    signedToday,
    loading,
    submitting,
    streakText,
    refreshStreak,
    submitSign,
    markSignedToday,
    reset,
  }
})
