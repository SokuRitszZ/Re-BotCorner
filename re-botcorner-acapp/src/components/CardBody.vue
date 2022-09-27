<template>
  <div class="card">
    <div class="card-body" :style="`max-height: 50vh; height: ${height || `90%`}; ${otherStyle || ''}; ${width ? `width: ${width}` : ''}`">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  height: {
    type: String,
    required: false
  },
  otherStyle: {
    type: String,
    required: false
  },
  width: {
    type: String,
    required: false
  }
});
</script>

<style scoped>

.card {
  margin-top: 20px;
  border-radius: 0;
}

.card-body {
  background-color: #fcfcfc;
  border-radius: 0;
  height: 70vh;
  box-shadow: 0 0 5px #eeeeee;
  transition: 0.2s;
  overflow: auto;
}

.card-body:hover {
  box-shadow: 0 0 7.5px #eeeeee;
}
</style>