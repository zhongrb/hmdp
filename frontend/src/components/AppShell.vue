<template>
  <div class="app-shell">
    <AppHeader />
    <main class="app-shell__main">
      <div class="page-shell">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue'
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.app-shell__main {
  padding-top: 12px;
}
</style>
