<template>
  <main>
    <h1>{{ copy.blogs.feedTitle }}</h1>
    <p>{{ copy.blogs.empty }}</p>
  </main>
</template>

<script setup>
import { copy } from '../constants/copy'
</script>
