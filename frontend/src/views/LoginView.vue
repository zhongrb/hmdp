<template>
  <main>
    <h1>{{ copy.login.title }}</h1>
    <label>
      {{ copy.login.phoneLabel }}
      <input type="text" placeholder="请输入手机号" />
    </label>
    <label>
      {{ copy.login.codeLabel }}
      <input type="text" placeholder="请输入验证码" />
    </label>
    <button type="button">{{ copy.login.submit }}</button>
    <p>{{ copy.login.agreement }}</p>
  </main>
</template>

<script setup>
import { copy } from '../constants/copy'
</script>
