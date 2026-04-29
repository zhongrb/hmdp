<template>
  <BaseButton
    variant="ghost"
    :loading="loading"
    @click="$emit('toggle')"
  >
    {{ liked ? copy.blogs.liked : copy.blogs.like }}
  </BaseButton>
</template>

<script setup lang="ts">
import BaseButton from './BaseButton.vue'
import { copy } from '../constants/copy'

withDefaults(
  defineProps<{
    liked: boolean
    loading?: boolean
  }>(),
  {
    loading: false,
  },
)

defineEmits<{
  toggle: []
}>()
</script>
