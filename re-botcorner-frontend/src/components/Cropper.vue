<template>
  <div class="mb-3" style="width: 200px; height: 200px">
    <img ref="imgRef" :src="`https://sdfsdf.dev/500x500.png,${randomHex()},${randomHex()}`" style="width: 200px;">
  </div>
  <input style="display: inline-block; width: 100%" ref="inputRef" type="file" class="form-control" @change="changeImg">
  <div ref="insertRef" class="insert"></div>
</template>

<script setup>
import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.min.css';
import alert from '../script/alert.js';

import {nextTick, onMounted, ref} from "vue";

const imgRef = ref(null);
const inputRef = ref(null);
const insertRef = ref(null);

const cropper = ref(null);
const option = ref({});

const getCrop = () => {
  return cropper.value.getCroppedCanvas();
};

const canvas2base64 = canvas => {
  const url = canvas.toDataURL("image/png");
  return url;
};

const base642file = url => {
  let arr = url.split(','),
    mime = arr[0].match(/:(.*?);/)[1],
    bstr = atob(arr[1]),
    n = bstr.length,
    u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  const file = new File([u8arr], "filename", { type: mime });
  return file;
};

const getFile = () => {
  const canvas = getCrop();
  const base64 = canvas2base64(canvas);
  const file = base642file(base64);
  return file;
}

const cut = () => {
  const div = insertRef.value;
  div.innerHTML = '';
  const crop = getCrop();
  if (crop !== null) emits('cut', getFile());
  else alert("danger", )
};

const flushImg = () => {
  cropper.value.destroy();
  cropper.value = new Cropper(imgRef.value, option.value);
}

const changeImg = () => {
  const file = inputRef.value.files[0];
  if (typeof file === 'undefined' || file.type.split('/')[0] !== "image") {
    alert(`danger`, `文件类型不是图片`);
    return ;
  }
  const url = URL.createObjectURL(file);
  imgRef.value.setAttribute('src', url);
  flushImg();
};

const randomHex = () => {
  let res = "";
  for (let i = 0; i < 6; ++i) res += Math.floor(Math.random() * 16).toString(16);
  return res;
};

const initCropper = () => {
  option.value = {
    viewMode: 0,
    aspectRatio: 1 / 1
  }
  cropper.value = new Cropper(imgRef.value, option.value);
};

const emits = defineEmits(['cut']);

defineExpose({
  cut
});

onMounted(async () => {
  initCropper();
  await nextTick();
  flushImg();
});
</script>

<style scoped>
div {
  margin: auto;
}
</style>