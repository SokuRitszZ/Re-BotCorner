<template>
  <div ref="$father" class="progress m-0" @mousedown="handleMousedown" @mouseup="handleMouseup" @mouseleave="handleMouseleave">
    <div class="progress-bar m-0" :class="{'progress-bar-animated': animated, 'progress-bar-striped': animated}" role="progressbar" aria-label="Basic example" :style="{width: progressValue}"></div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from "vue";

const props = defineProps({
  max: Number,
  value: Number || String,
  animated: {
    type: Boolean,
    default: false
  },
});

const emit = defineEmits([
  'change'
])

const currentValue = computed(() => {
  if (typeof props.value !== "number") return 0;
  return props.value;
})

const progressValue = computed(() => {
  const result = `${Math.min(1, currentValue.value / props.max) * 100}%`
  return result;
});

const isMoving = ref(false);

function debounce(fn, timeout) {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, timeout);
  };
};

const handleMousedown = e => {
  isMoving.value = true;
  handleMousemove(e);
};

const handleMouseup = () => {
  isMoving.value = false;
};

const $father = ref();

const handleMousemove = e => {
  if (!isMoving.value) return ;
  const vle = e.offsetX;
  const max = $father.value.clientWidth;
  emit("change", Math.floor(vle / max * props.max));
};

const handleMouseleave = () => {
  isMoving.value = false;
};

onMounted(() => {
  // $father.value.addEventListener("mousemove", debounce(handleMousemove, 10));
  $father.value.addEventListener("mousemove", handleMousemove);
})

</script>

<style scoped>
.progress {
  cursor: ew-resize
}
</style>