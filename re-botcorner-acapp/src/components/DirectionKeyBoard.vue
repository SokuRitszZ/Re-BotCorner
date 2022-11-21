<template>
  <div id="grid" class="btn-group float-end" role="group" style="font-family: monospace">
    <input :disabled="disabled" type="radio" class="btn-check" v-model="choose" :value="0" :id="`choose0-${id}`"
           autocomplete="off" checked>
    <label style="grid-area: up" :class="className" class="btn text-md-center" :for="`choose0-${id}`">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-chevron-up" viewBox="0 0 16 16">
        <path fill-rule="evenodd" d="M7.646 4.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1-.708.708L8 5.707l-5.646 5.647a.5.5 0 0 1-.708-.708l6-6z"/>
      </svg>
    </label>
    <input :disabled="disabled" type="radio" class="btn-check" v-model="choose" :value="3" :id="`choose3-${id}`"
           autocomplete="off">
    <label style="grid-area: left" :class="className" class="btn text-md-center" :for="`choose3-${id}`">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-chevron-left" viewBox="0 0 16 16">
        <path fill-rule="evenodd" d="M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"/>
      </svg>
    </label>
    <input :disabled="disabled" type="radio" class="btn-check" v-model="choose" :value="1" :id="`choose1-${id}`"
           autocomplete="off">
    <label style="grid-area: right" :class="className" class="btn text-md-center" :for="`choose1-${id}`">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-chevron-right" viewBox="0 0 16 16">
        <path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/>
      </svg>
    </label>
    <input :disabled="disabled" type="radio" class="btn-check" v-model="choose" :value="2" :id="`choose2-${id}`"
           autocomplete="off">
    <label style="grid-area: down" :class="className" class="btn text-md-center" :for="`choose2-${id}`">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-chevron-down" viewBox="0 0 16 16">
        <path fill-rule="evenodd" d="M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708z"/>
      </svg>
    </label>
    <button :disabled="disabled" style="grid-area: submit" class="btn btn-success" @click="submit">
      <i class="bi bi-check-square"></i>
    </button>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";

const choose = ref(0);

const props = defineProps({
  color: {
    type: String
  },
  className: {},
  id: {
    type: String,
    required: true
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

const submit = () => {
  emit('submit', props.id, choose.value);
};

const emit = defineEmits(['submit']);

onMounted(() => {
});

</script>

<style scoped>
#grid {
  --button-wid: 2.5rem;
  display: grid;
  grid: repeat(3, var(--button-wid)) / repeat(3, var(--button-wid));
  grid-template-areas: ". up ."
  "left submit right"
  ". down .";
}

.btn {
  border-radius: 0px;
}

</style>