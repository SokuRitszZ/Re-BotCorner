<template>
  <button class="btn btn-primary mb-2" :style="buttonStyle" type="button" data-bs-toggle="collapse" :data-bs-target="`#${collapseId}`"
    aria-expanded="false" :aria-controls="collapseId">
    <slot name="button"></slot>
  </button>
  <div class="collapse mb-2" :id="collapseId">
    <div class="card card-body" :style="otherStyle || ''">
      <slot name="content"></slot>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  buttonStyle: {
    type: String,
  },
  collapseId: {
    type: String,
    required: true
  },
  otherStyle: {
    type: String,
  }
});

</script>

<style scoped>
.card {
  border-radius: 0;
}

.card-body::-webkit-scrollbar {
  display: none;
}
</style>