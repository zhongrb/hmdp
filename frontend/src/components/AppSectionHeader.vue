<template>
  <header class="section-header">
    <div class="section-header__content">
      <p
        v-if="eyebrow"
        class="section-header__eyebrow"
      >
        {{ eyebrow }}
      </p>
      <h2 class="section-title">
        {{ title }}
      </h2>
      <p
        v-if="subtitle"
        class="section-subtitle"
      >
        {{ subtitle }}
      </p>
    </div>
    <div
      v-if="$slots.actions"
      class="section-header__actions"
    >
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  eyebrow?: string
}>()
</script>

<style scoped>
.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-header__content {
  display: grid;
  gap: 8px;
}

.section-header__eyebrow {
  margin: 0;
  color: #0369a1;
  font-size: 0.92rem;
  font-weight: 700;
}

.section-header__actions {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
  }
}
</style>
