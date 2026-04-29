<template>
  <section
    class="status-card"
    :class="[`status-card--${tone}`]"
    :role="tone === 'error' ? 'alert' : undefined"
    aria-live="polite"
  >
    <p class="status-title">
      {{ title }}
    </p>
    <p
      v-if="description"
      class="status-description"
    >
      {{ description }}
    </p>
    <div
      v-if="$slots.actions"
      class="status-actions"
    >
      <slot name="actions" />
    </div>
  </section>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    description?: string
    tone?: 'neutral' | 'error' | 'success'
  }>(),
  {
    description: '',
    tone: 'neutral',
  },
)
</script>

<style scoped>
.status-card {
  display: grid;
  gap: 10px;
}

.status-card--error {
  border-color: rgba(220, 38, 38, 0.18);
  background: rgba(254, 242, 242, 0.92);
}

.status-card--success {
  border-color: rgba(22, 163, 74, 0.18);
  background: rgba(240, 253, 244, 0.92);
}

.status-title {
  margin: 0;
  color: #0f172a;
  font-weight: 700;
}

.status-description {
  margin: 0;
  color: #64748b;
}

.status-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
