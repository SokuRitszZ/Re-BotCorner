<template>
  <div :class="`container` + (otherClass || '')" :style="(otherStyle || '') + ' height: 100%; overflow: auto'">
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

<style scoped>
*::-webkit-scrollbar {
  display: none;
}
</style>