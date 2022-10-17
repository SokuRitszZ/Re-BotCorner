<template>
  <div class="box" style="position: relative; width: 100%; height: 100px; background-color: #0a58ca; padding: 20px;">
    <div ref="chessRef" class="chess" />
    <div ref="inner-box" class="inner-box" style="height: 30px; width: 30px; left: 80px; z-index: 0; position: absolute; background-color: #a52834"/>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";

const chessRef = ref(null);

const move = (div, x, y, t) => {
};

onMounted(() => {
  move(chessRef.value, 0, 0, 1);
});
</script>

<style scoped>
.chess {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #0dcaf0;
  cursor: pointer;
  transition: 0.5s;
  position: absolute;
  z-index: 1;
}
</style>