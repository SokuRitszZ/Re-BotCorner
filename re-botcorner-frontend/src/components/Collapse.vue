<template>
  <button class="btn btn-primary mb-2" :style="buttonStyle" type="button" data-bs-toggle="collapse" :data-bs-target="`#${collapseId}`"
    aria-expanded="false" :aria-controls="collapseId">
    <slot name="button"></slot>
  </button>
  <div class="collapse mb-2" :id="collapseId">
    <div class="card card-body">
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
  }
});

const state = ref("makingGame");

</script>

<style scoped>
.card {
  border-radius: 0;
}
</style>