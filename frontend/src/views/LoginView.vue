<template>
  <main class="page-stack">
    <section class="hero-card two-column-grid">
      <div class="section-grid">
        <div>
          <p class="eyebrow">
            {{ copy.login.title }}
          </p>
          <h1 class="page-title">
            欢迎继续你的本地生活浏览
          </h1>
          <p class="page-lead">
            {{ copy.login.tips }}
          </p>
        </div>
        <div class="metric-row">
          <span class="metric-pill">抢券</span>
          <span class="metric-pill">签到</span>
          <span class="metric-pill">发布互动</span>
        </div>
      </div>

      <section class="surface-card">
        <AppSectionHeader
          :title="copy.login.title"
          :subtitle="copy.status.loginSuccessHint"
          eyebrow="短信验证"
        />

        <form
          class="form-grid"
          @submit.prevent="submitLogin"
        >
          <label
            class="form-label"
            for="login-phone"
          >
            {{ copy.login.phoneLabel }}
            <input
              id="login-phone"
              v-model.trim="form.phone"
              type="text"
              inputmode="tel"
              :placeholder="copy.login.phonePlaceholder"
            >
          </label>

          <label
            class="form-label"
            for="login-code"
          >
            {{ copy.login.codeLabel }}
            <input
              id="login-code"
              v-model.trim="form.code"
              type="text"
              inputmode="numeric"
              :placeholder="copy.login.codePlaceholder"
            >
          </label>

          <div class="inline-actions">
            <BaseButton
              :loading="sending"
              variant="secondary"
              @click="requestCode"
            >
              {{ sending ? copy.login.sending : copy.login.sendCode }}
            </BaseButton>
            <BaseButton
              type="submit"
              :loading="submitting"
            >
              {{ submitting ? copy.login.submitting : copy.login.submit }}
            </BaseButton>
          </div>

          <p
            v-if="message"
            class="message-text"
            :class="{ 'is-error': messageTone === 'error', 'is-success': messageTone === 'success' }"
            :role="messageTone === 'error' ? 'alert' : undefined"
            aria-live="polite"
          >
            {{ message }}
          </p>

          <p class="body-muted">
            {{ copy.login.agreement }}
          </p>
        </form>
      </section>
    </section>

    <EmptyState
      :eyebrow="copy.marketing.trustTitle"
      title="未登录也可以先完成主要浏览"
      :description="copy.marketing.trustBody"
    >
      <template #actions>
        <BaseButton
          tag="a"
          href="/shops"
          variant="secondary"
        >
          {{ copy.actions.goShops }}
        </BaseButton>
        <BaseButton
          tag="a"
          href="/blogs"
        >
          {{ copy.actions.exploreBlogs }}
        </BaseButton>
      </template>
    </EmptyState>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login, sendCode } from '../api/auth'
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import { copy } from '../constants/copy'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const sending = ref(false)
const submitting = ref(false)
const message = ref('')
const messageTone = ref<'success' | 'error'>('success')
const form = reactive({
  phone: '13800138000',
  code: '',
})

async function requestCode() {
  sending.value = true
  message.value = ''
  try {
    await sendCode(form.phone)
    messageTone.value = 'success'
    message.value = copy.status.codeSent
  } catch (error: any) {
    messageTone.value = 'error'
    message.value = error.response?.data?.message ?? copy.errors.network
  } finally {
    sending.value = false
  }
}

async function submitLogin() {
  submitting.value = true
  message.value = ''
  try {
    const result = await login(form)
    authStore.setSession(result.token, result.user)
    messageTone.value = 'success'
    message.value = copy.login.success
    const redirect = String(route.query.redirect || authStore.redirectPath || '/shops')
    authStore.setRedirectPath(null)
    await router.replace(redirect)
  } catch (error: any) {
    messageTone.value = 'error'
    message.value = error.response?.data?.message ?? copy.errors.network
  } finally {
    submitting.value = false
  }
}
</script>
