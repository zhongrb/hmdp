<template>
  <div
    v-if="modelValue"
    class="action-result-mask"
    role="dialog"
    aria-modal="true"
    aria-labelledby="action-result-title"
  >
    <div class="action-result-card">
      <p
        class="eyebrow"
        :class="`is-${tone}`"
      >
        {{ eyebrowText }}
      </p>
      <h2 id="action-result-title">
        {{ title }}
      </h2>
      <p>{{ message }}</p>
      <div class="actions">
        <BaseButton
          type="button"
          @click="$emit('update:modelValue', false)"
        >
          我知道了
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseButton from './BaseButton.vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    message: string
    tone?: 'success' | 'error'
  }>(),
  {
    tone: 'success',
  },
)

defineEmits<{
  'update:modelValue': [boolean]
}>()

const eyebrowText = computed(() => (props.tone === 'success' ? '领取成功' : '领取失败'))
</script>

<style scoped>
.action-result-mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: grid;
  place-items: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.48);
  backdrop-filter: blur(6px);
}

.action-result-card {
  width: min(440px, calc(100vw - 32px));
  display: grid;
  gap: 12px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.97);
  padding: 24px;
  box-shadow: 0 28px 60px rgba(15, 23, 42, 0.22);
}

.action-result-card h2,
.action-result-card p {
  margin: 0;
}

.action-result-card p:not(.eyebrow) {
  color: #475569;
  line-height: 1.6;
}

.eyebrow.is-success {
  color: #0f766e;
}

.eyebrow.is-error {
  color: #b91c1c;
}

.actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
