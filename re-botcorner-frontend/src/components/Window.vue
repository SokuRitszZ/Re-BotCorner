<template>
  <button ref="btnRef" @click="show" class="btn btn-primary" :style="buttonStyle">
    <slot name="button"></slot>
  </button>
  <div class="window-board" ref="boardRef" style="display: none; position: fixed; left: 50vw; top: 50vh; width: 500px; height: 500px; background-color: #f4f4f4; border-radius: 10px; border: 5px solid #ccc; overflow: hidden; box-shadow: 0 0 10px #aaa; z-index: 100; overflow: hidden">
    <div class="window-head no-select" ref="headRef" style="width: 100%; height: 40px; background-color: #ccc; padding: 5px">
      <div style="position: absolute; left: 10px; font-size: 20px">{{ title }}</div>
      <div @click.stop="close" class="btn btn-danger" style="position: absolute; padding: 0; right: 0; top: 7px; margin-right: 5px; height: 20px; width: 20px; border-radius: 50%;">
      </div>
    </div>
    <div class="window-body" style="width: 100%; height: calc(100%-50px);">
      <slot name="body"></slot>
    </div>
    <div class="window-foot no-select" style="position: absolute; bottom: 0; width: 100%; height: 20px; background-color: aliceblue;">
      <div class="window-right-foot" ref="rightFootRef" style="position: absolute; right: 0; width: 20px; height: 20px; background-color: none; border-radius: 1px; font-size: 5px; padding: 5px; color: #888"><i class="bi bi-arrows-fullscreen"></i></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';

const boardRef = ref(null);
const headRef = ref(null);
const rightFootRef = ref(null);
const btnRef = ref(null);

const state = ref('close');

const show = () => {
  if (state.value === 'open') {
    boardRef.value.classList.add(`animate__animated`);
    boardRef.value.classList.add(`animate__headShake`);
    setTimeout(() => {
      boardRef.value.classList.remove(`animate__animated`);
      boardRef.value.classList.remove(`animate__headShake`);
    }, 1000);
  } else {
    emit('show');
    boardRef.value.style.display = "block";
    boardRef.value.classList.add(`opening-board`);
    setTimeout(() => {
      boardRef.value.classList.remove(`opening-board`);
      state.value = 'open';
    }, 500);
  }
};

const close = () => {
  if (state.value === 'close') return ;
  boardRef.value.classList.add(`closing-board`);
  emit('close');
  setTimeout(() => {
    boardRef.value.classList.remove(`closing-board`);
    boardRef.value.style.display = "none";
    state.value = 'close';
  }, 500);
};

const getState = () => {
  return state.value;
}

const initBoard = () => {
  const board = boardRef.value;
  const head = headRef.value;
  const rightFoot = rightFootRef.value;

  board.style.opacity = props.opacity;
  
  head.isMovingBoard = { value: false };
  rightFoot.isZooming = { value: false };

  head.addEventListener('mousedown', function (e) {
    switch (e.button) {
      case 0:
        this.isMovingBoard.value = true;
        this.offsetX = e.offsetX;
        this.offsetY = e.offsetY;
        break;
    }
  });
  head.addEventListener('mouseup', function (e) {
    switch (e.button) {
      case 0:
        this.isMovingBoard.value = false; break;
    }
  });
  rightFoot.addEventListener('mousedown', function (e) {
    switch (e.button) {
      case 0:
        const rec = this.getBoundingClientRect();
        this.isZooming.value = true; 
        this.offsetX = rec.right - e.clientX;
        this.offsetY = rec.bottom - e.clientY;
        break;
    }
  });
  rightFoot.addEventListener('mouseup', function (e) {
    switch (e.button) {
      case 0:
        this.isZooming.value = false; break;
    }
  });
  window.addEventListener('mouseup', function (e) {
    switch (e.button) {
      case 0:
        head.isMovingBoard.value = false;
        rightFoot.isZooming.value = false;
        break;
    }
  });
  window.addEventListener('mousemove', function (e) {
    if (head.isMovingBoard.value) {
      board.style.left = e.clientX - head.offsetX + 'px';
      board.style.top = e.clientY - head.offsetY + 'px';
    }
    if (rightFoot.isZooming.value) {
      const boardRec = board.getBoundingClientRect();
      const right = e.clientX + rightFoot.offsetX;
      const bottom = e.clientY + rightFoot.offsetY;
      const width = Math.max(100, right - boardRec.left);
      const height = Math.max(100, bottom - boardRec.top);
      board.style.width = `${width}px`;
      board.style.height = `${height}px`;
    }
  });
};

const getBtn = () => {
  return btnRef.value;
};

const props = defineProps({
  title: {
    type: String,
    default: 'window'
  },
  opacity: {
    type: Number,
    default: 1
  },
  buttonStyle: {
    type: String,
    default: ''
  }
});

defineExpose({
  show,
  close,
  getBtn,
  getState
});

const emit = defineEmits(['show', 'close']);

onMounted(() => { 
  initBoard();
});
</script>

<style scoped>
.opening-board {
  animation-name: openingBoard;
  animation-duration: 0.5s;
}

.closing-board {
  animation-name: closingBoard;
  animation-duration: 0.5s;
}

@keyframes openingBoard {
  from {
    width: 0;
    height: 0;
    opacity: 0;
  }

  to {
    display: block;
  }
}

.no-select {
  user-select: none;
}

@keyframes closingBoard {
  from {
    display: block;
  }

  to {
    width: 0;
    height: 0;
    opacity: 0;
  }
}

</style>
