<template>
  <section class="empty-card">
    <p
      v-if="eyebrow"
      class="eyebrow"
    >
      {{ eyebrow }}
    </p>
    <h2 class="section-title">
      {{ title }}
    </h2>
    <p class="section-subtitle">
      {{ description }}
    </p>
    <div
      v-if="$slots.actions"
      class="empty-state__actions"
    >
      <slot name="actions" />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description: string
  eyebrow?: string
}>()
</script>

<style scoped>
.empty-card {
  display: grid;
  gap: 14px;
  justify-items: start;
}

.empty-state__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
