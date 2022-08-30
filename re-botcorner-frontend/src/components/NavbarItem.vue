<template>
  <li ref="itemRef" class="nav-link">
    <slot></slot>
  </li>
</template>

<script setup>
import { onMounted, ref } from 'vue';

const props = defineProps({
  classes: {
    required: false
  }
});

const itemRef = ref(null);

onMounted(() => {
  if (props.classes) {
    for (let classObj of props.classes) {
      itemRef.value.classList.add(classObj);
    }
  }
})
</script>