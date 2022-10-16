<template>
  <div :class="(props.col || `col`) + ' h-100'" style="overflow: scroll">
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

<style scoped>
*::-webkit-scrollbar {
  display: none;
}
</style>