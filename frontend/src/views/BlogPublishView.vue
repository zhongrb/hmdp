<template>
  <main>
    <h1>{{ copy.blogs.publishTitle }}</h1>
    <p>{{ copy.login.required }}</p>
  </main>
</template>

<script setup>
import { copy } from '../constants/copy'
</script>
