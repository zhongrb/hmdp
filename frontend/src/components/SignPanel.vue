<template>
  <section class="surface-card sign-panel">
    <AppSectionHeader
      title="每日签到"
      subtitle="登录后可完成签到并累计活跃天数。"
      eyebrow="活跃任务"
    />

    <div class="sign-panel__summary">
      <p class="sign-panel__streak">
        {{ copy.status.signStreakPrefix }}
        <strong>{{ streak }}</strong>
        {{ copy.status.signStreakSuffix }}
      </p>
      <p class="body-muted">
        连续签到可帮助你持续跟进附近活动和优惠节奏。
      </p>
    </div>

    <StatusPanel
      v-if="message"
      :title="message"
      :tone="messageTone"
    />

    <div class="inline-actions">
      <BaseButton
        :disabled="signedToday || loading"
        :loading="submitting"
        @click="$emit('sign')"
      >
        {{ signedToday ? copy.actions.signedToday : copy.actions.signToday }}
      </BaseButton>
      <BaseButton
        variant="secondary"
        :loading="loading"
        @click="$emit('refresh')"
      >
        {{ copy.actions.refreshActivity }}
      </BaseButton>
    </div>
  </section>
</template>

<script setup lang="ts">
import AppSectionHeader from './AppSectionHeader.vue'
import BaseButton from './BaseButton.vue'
import StatusPanel from './StatusPanel.vue'
import { copy } from '../constants/copy'

withDefaults(
  defineProps<{
    streak: number
    signedToday: boolean
    loading?: boolean
    submitting?: boolean
    message?: string
    messageTone?: 'neutral' | 'error' | 'success'
  }>(),
  {
    loading: false,
    submitting: false,
    message: '',
    messageTone: 'neutral',
  },
)

defineEmits<{
  sign: []
  refresh: []
}>()
</script>

<style scoped>
.sign-panel {
  display: grid;
  gap: 18px;
}

.sign-panel__summary {
  display: grid;
  gap: 8px;
}

.sign-panel__streak {
  margin: 0;
  color: #0f172a;
  font-size: 1.05rem;
}

.sign-panel__streak strong {
  font-size: 1.8rem;
  color: #0369a1;
}
</style>
