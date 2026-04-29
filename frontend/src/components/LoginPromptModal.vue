<template>
  <div
    v-if="modelValue"
    class="login-prompt-mask"
    role="dialog"
    aria-modal="true"
    aria-labelledby="login-prompt-title"
  >
    <div class="login-prompt-card">
      <p class="eyebrow">
        {{ copy.labels.protectedFeature }}
      </p>
      <h2 id="login-prompt-title">
        {{ copy.common.loginPromptTitle }}
      </h2>
      <p>{{ copy.common.loginPromptBody }}</p>
      <div class="actions">
        <BaseButton
          type="button"
          variant="secondary"
          @click="$emit('update:modelValue', false)"
        >
          稍后再说
        </BaseButton>
        <BaseButton
          type="button"
          @click="$emit('confirm')"
        >
          {{ copy.common.loginPromptAction }}
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import BaseButton from './BaseButton.vue'
import { copy } from '../constants/copy'

defineProps<{
  modelValue: boolean
}>()

defineEmits<{
  'update:modelValue': [boolean]
  confirm: []
}>()
</script>

<style scoped>
.login-prompt-mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: grid;
  place-items: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.48);
  backdrop-filter: blur(6px);
}

.login-prompt-card {
  width: min(440px, calc(100vw - 32px));
  display: grid;
  gap: 12px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.95);
  padding: 24px;
  box-shadow: 0 28px 60px rgba(15, 23, 42, 0.22);
}

.login-prompt-card h2,
.login-prompt-card p {
  margin: 0;
}

.login-prompt-card p:not(.eyebrow) {
  color: #64748b;
}

.actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 640px) {
  .actions {
    flex-direction: column-reverse;
  }
}
</style>
