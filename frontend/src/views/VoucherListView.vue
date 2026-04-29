<template>
  <main class="page-stack">
    <section class="hero-card two-column-grid">
      <div class="section-grid">
        <div>
          <p class="eyebrow">
            {{ copy.vouchers.title }}
          </p>
          <h1 class="page-title">
            {{ copy.vouchers.heroTitle }}
          </h1>
          <p class="page-lead">
            {{ copy.vouchers.heroBody }}
          </p>
        </div>
        <div class="metric-row">
          <span class="metric-pill">公开可见</span>
          <span class="metric-pill">登录后抢券</span>
          <span class="metric-pill">签到联动</span>
        </div>
      </div>

      <SignPanel
        :streak="activityStore.streak"
        :signed-today="activityStore.signedToday"
        :loading="activityStore.loading"
        :submitting="activityStore.submitting"
        :message="signMessage"
        :message-tone="signMessageTone"
        @sign="handleSign"
        @refresh="loadSignStreak"
      />
    </section>

    <section class="surface-card section-grid">
      <AppSectionHeader
        :title="copy.vouchers.title"
        :subtitle="copy.vouchers.subtitle"
        eyebrow="活动列表"
      >
        <template #actions>
          <BaseButton
            variant="secondary"
            :loading="loading"
            @click="loadVouchers"
          >
            {{ copy.actions.refreshActivity }}
          </BaseButton>
        </template>
      </AppSectionHeader>
      <div class="metric-row">
        <span class="metric-pill">{{ copy.vouchers.browseHint }}</span>
      </div>
    </section>

    <StatusPanel
      v-if="loading"
      :title="copy.common.loading"
      :description="copy.status.activityLoading"
    />

    <StatusPanel
      v-else-if="errorMessage"
      :title="errorMessage"
      tone="error"
    >
      <template #actions>
        <BaseButton
          variant="secondary"
          @click="loadVouchers"
        >
          {{ copy.common.retry }}
        </BaseButton>
      </template>
    </StatusPanel>

    <EmptyState
      v-else-if="vouchers.length === 0"
      eyebrow="活动待补充"
      :title="copy.vouchers.title"
      :description="copy.vouchers.empty"
    >
      <template #actions>
        <BaseButton
          variant="secondary"
          @click="loadVouchers"
        >
          {{ copy.common.retry }}
        </BaseButton>
      </template>
    </EmptyState>

    <section
      v-else
      class="card-grid voucher-grid"
    >
      <VoucherCard
        v-for="voucher in vouchers"
        :key="voucher.id"
        :voucher="voucher"
        :claiming="claimingVoucherId === voucher.id"
        :disabled="!canClaim(voucher)"
        @claim="handleClaim(voucher.id)"
      />
    </section>

    <LoginPromptModal
      v-model="showLoginPrompt"
      @confirm="goToLogin"
    />
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { claimSeckillVoucher, fetchSeckillVouchers, type Voucher } from '../api/voucher'
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import LoginPromptModal from '../components/LoginPromptModal.vue'
import SignPanel from '../components/SignPanel.vue'
import StatusPanel from '../components/StatusPanel.vue'
import VoucherCard from '../components/VoucherCard.vue'
import { copy } from '../constants/copy'
import { useAuthStore } from '../stores/auth'
import { useActivityStore } from '../stores/activity'

const router = useRouter()
const authStore = useAuthStore()
const activityStore = useActivityStore()
const loading = ref(false)
const errorMessage = ref('')
const signMessage = ref('')
const signMessageTone = ref<'neutral' | 'error' | 'success'>('neutral')
const showLoginPrompt = ref(false)
const claimingVoucherId = ref<number | null>(null)
const vouchers = ref<Voucher[]>([])

async function loadVouchers() {
  loading.value = true
  errorMessage.value = ''
  try {
    vouchers.value = await fetchSeckillVouchers()
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message ?? copy.errors.activityLoad
  } finally {
    loading.value = false
  }
}

async function loadSignStreak() {
  signMessage.value = ''
  if (!authStore.isAuthenticated) {
    activityStore.reset()
    return
  }
  try {
    await activityStore.refreshStreak()
  } catch (error: any) {
    signMessageTone.value = 'error'
    signMessage.value = error.response?.data?.message ?? copy.errors.signLoad
  }
}

function canClaim(voucher: Voucher) {
  return voucher.status === 1
}

async function handleClaim(voucherId: number) {
  if (!authStore.isAuthenticated) {
    showLoginPrompt.value = true
    return
  }
  claimingVoucherId.value = voucherId
  signMessage.value = ''
  try {
    await claimSeckillVoucher(voucherId)
    signMessageTone.value = 'success'
    signMessage.value = copy.vouchers.claimSuccess
    await loadVouchers()
  } catch (error: any) {
    signMessageTone.value = 'error'
    signMessage.value = error.response?.data?.message ?? copy.errors.network
  } finally {
    claimingVoucherId.value = null
  }
}

async function handleSign() {
  if (!authStore.isAuthenticated) {
    showLoginPrompt.value = true
    return
  }
  try {
    await activityStore.submitSign()
    signMessageTone.value = 'success'
    signMessage.value = `${copy.status.signStreakPrefix} ${activityStore.streak} ${copy.status.signStreakSuffix}`
  } catch (error: any) {
    const nextMessage = error.response?.data?.message ?? copy.errors.network
    if (nextMessage === '今天已经签到过了') {
      activityStore.markSignedToday()
    }
    signMessageTone.value = 'error'
    signMessage.value = nextMessage
  }
}

async function goToLogin() {
  showLoginPrompt.value = false
  await router.push({ name: 'login', query: { redirect: '/vouchers' } })
}

onMounted(async () => {
  await loadVouchers()
  await loadSignStreak()
})
</script>

<style scoped>
.voucher-grid {
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
}

@media (min-width: 1200px) {
  .voucher-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
