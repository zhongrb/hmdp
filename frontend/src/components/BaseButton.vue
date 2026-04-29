<template>
  <component
    :is="tag"
    class="base-button"
    :class="[`base-button--${variant}`, { 'is-block': block, 'is-loading': loading }]"
    :disabled="disabled || loading"
    v-bind="forwardedAttrs"
  >
    <span
      v-if="loading"
      class="base-button__spinner"
      aria-hidden="true"
    />
    <span><slot /></span>
  </component>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'

const props = withDefaults(
  defineProps<{
    variant?: 'primary' | 'secondary' | 'ghost'
    loading?: boolean
    disabled?: boolean
    block?: boolean
    tag?: 'button' | 'a' | 'span'
  }>(),
  {
    variant: 'primary',
    loading: false,
    disabled: false,
    block: false,
    tag: 'button',
  },
)

const attrs = useAttrs()
const forwardedAttrs = computed(() => ({
  ...(props.tag === 'button' ? { type: 'button' } : {}),
  ...attrs,
}))
</script>

<style scoped>
.base-button {
  min-height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 18px;
  border: 1px solid transparent;
  border-radius: 16px;
  text-decoration: none;
  font-weight: 700;
  transition: transform 180ms ease, box-shadow 180ms ease, background-color 180ms ease, border-color 180ms ease, color 180ms ease;
}

.base-button:hover:not(:disabled) {
  transform: translateY(-1px);
}

.base-button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.base-button.is-block {
  width: 100%;
}

.base-button--primary {
  background: linear-gradient(135deg, #0369a1, #0284c7);
  color: #ffffff;
  box-shadow: 0 16px 28px rgba(3, 105, 161, 0.24);
}

.base-button--primary:hover:not(:disabled) {
  box-shadow: 0 20px 36px rgba(3, 105, 161, 0.3);
}

.base-button--secondary {
  background: rgba(255, 255, 255, 0.88);
  color: #0f172a;
  border-color: rgba(148, 163, 184, 0.35);
}

.base-button--secondary:hover:not(:disabled) {
  border-color: rgba(3, 105, 161, 0.28);
  background: #ffffff;
}

.base-button--ghost {
  background: transparent;
  color: #0369a1;
}

.base-button--ghost:hover:not(:disabled) {
  background: rgba(3, 105, 161, 0.08);
}

.base-button__spinner {
  width: 16px;
  height: 16px;
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.36);
  border-top-color: currentColor;
  animation: spin 0.9s linear infinite;
}

.base-button--secondary .base-button__spinner,
.base-button--ghost .base-button__spinner {
  border-color: rgba(15, 23, 42, 0.2);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
