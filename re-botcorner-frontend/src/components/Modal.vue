<template>
<!-- Button trigger modal -->
<button type="button" :class="btnClass" :style="toggleButtonStyle || ''" data-bs-toggle="modal" :data-bs-target="`#${modalID}`">
  <slot name="button"></slot>
</button>

<!-- Modal -->
<div class="modal fade" :id="modalID" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" :id="modalID">
          {{ title }}
        </h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <slot name="body"></slot>
      </div>
      <div class="modal-footer">
        <button @click="closeAction" type="button" class="btn btn-secondary" data-bs-dismiss="modal">{{ closeTitle }}</button>
        <button @click="submitAction" type="button" class="btn btn-primary">{{ submitTitle }}</button>
      </div>
    </div>
  </div>
</div>
</template>

<script setup>
import { Modal } from 'bootstrap/dist/js/bootstrap';
import { onMounted, ref } from 'vue';

const modalRef = ref(null);

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  modalID: {
    type: String,
    required: true
  },
  btnClass: {
    type: String,
    required: true
  },
  closeTitle: {
    type: String,
    required: true
  },
  submitTitle: {
    type: String,
    required: true
  },
  submitAction: {
    type: Function,
    required: false,
    default: function(){}
  },
  closeAction: {
    type: Function,
    required: false,
    default: function(){}
  },
  toggleButtonStyle: {
    type: String,
    required: false,
  },
});

const hide = () => {
  modalRef.value.hide();
}

defineExpose({
  hide
});

onMounted(() => {
  modalRef.value = new Modal(document.getElementById(props.modalID), { keyboard: false });
});

</script>