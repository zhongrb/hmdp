<template>
  <main>
    <h1>{{ copy.vouchers.title }}</h1>
    <p>{{ copy.vouchers.empty }}</p>
  </main>
</template>

<script setup>
import { copy } from '../constants/copy'
</script>
