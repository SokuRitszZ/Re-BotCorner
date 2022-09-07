<template>
  <div :class="`container` + (otherClass || '')" :style="otherStyle || ''">
    <slot></slot>
  </div>
</template>

<script setup>
const props = defineProps({
  otherClass: {
    type: String
  },
  otherStyle: {
    type: String
  }
});
</script>

<style></style>