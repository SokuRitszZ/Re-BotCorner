<template>
  <div :class="props.col || `col`" :style="`${otherStyle || ''}`">
    <slot></slot>
  </div>
</template>

<script setup>
const props = defineProps({
  col: {
    type: String,
    required: false
  },
  otherStyle: {
    type: String,
    default: ''
  }
});
</script>

<style></style>