<template>
  <div :class="props.col || `col`">
    <slot></slot>
  </div>
</template>

<script setup>
const props = defineProps({
  col: {
    type: String,
    required: false
  }
});
</script>

<style></style>