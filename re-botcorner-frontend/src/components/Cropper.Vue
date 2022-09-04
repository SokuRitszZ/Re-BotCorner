<template>
  <div>
    <img ref="imgRef" src="http://localhost:8080/static/Celeste.png" alt="" width="350">
    <div class="preview-cropper"></div>
  </div>
  <hr>
  <button @click="downloadCanvas(getCrop())" class="btn btn-primary mb-3">裁剪</button>
  <!-- <button @click="download" class="btn btn-primary"></button> -->
</template>

<script setup>
import Cropper from 'cropperjs';
import { onMounted, ref } from 'vue';

import 'cropperjs/dist/cropper.css';

const imgRef = ref(null);
const cropper = ref(null);

const getCrop = () => {
  return cropper.value.getCroppedCanvas();
};

const downloadCanvas = (canvas) => {
  let contentType = `image/png`;
  let imgURL = canvas.toDataURL(contentType);
  
  let downloadLink = document.createElement(`a`);
  downloadLink.download = "download";
  downloadLink.href = imgURL;
  downloadLink.dataset.downloadurl = [contentType, downloadLink.download, downloadLink.href].join(':');

  document.body.appendChild(downloadLink);
  downloadLink.click();
  document.body.removeChild(downloadLink);
}

onMounted(() => {
  const option = {
    viewMode: 0,
    aspectRatio: 1 / 1,
    preview: '.preview-cropper' 
  };
  cropper.value = new Cropper(imgRef.value, option);
});

</script>

<style scoped>
.preview-cropper {
  width: 100px;
  height: 100px;
  overflow: hidden;
} 
</style>