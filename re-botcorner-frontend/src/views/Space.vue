<template>
  <CardBody>
    <Row>
      <Col col="col-4">
        <div class="card" style="width: 100%; border-radius: 0">
          <img style="border-bottom: 1px solid #ccc" :src="USER().getHeadIcon" class="card-img-top" alt="head_icon">
          <div class="username card-body">
            <h5 class="card-title" style="text-align: center; height: auto">{{ USER().getUsername }}<span style="color: gray; margin-left: 5px">#{{ userId }}</span></h5>
          </div>
          <Window
            title="更换头像"
            :opacity="1"
            button-style="border-radius: 0; width: 100%"
          >
            <template v-slot:button>更换头像</template>
            <template v-slot:body>
              <div
                class="frame"
                style="padding: 20px"
              >
                <Cropper ref="cropperRef" @cut="updateHeadIcon" />
                <div @click="cropperRef.cut()" class="mt-3 float-end btn btn-primary">裁剪</div>
              </div>
            </template>
          </Window>
        </div>
        <CardBody height="45vh">
          <CardBody height="100%">
          </CardBody>
        </CardBody>
      </Col>
      <Col col="col-8">
        <CardBody height="20vh"></CardBody>
        <template v-if="userId === USER().getUserID">
          <BotList :bots="bots">
          </BotList>
        </template>
      </Col>
    </Row>
  </CardBody>
</template>

<script setup>
import {nextTick, onMounted, ref} from 'vue';
import { useRoute } from 'vue-router';
import CardBody from '../components/CardBody.vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import USER from '../store/USER';
import BotList from './viewsChild/BotList.vue';
import {updateHeadIconApi} from '../script/api';
import Window from "../components/Window.vue";
import Cropper from "../components/Cropper.vue";
import LANG from "../store/LANG.js";
import MYBOTS from "../store/MYBOTS.js";

const route = useRoute();
const userId = ref(parseInt(route.params.userId));
const bots = ref([]);
const cropperRef = ref();

const updateHeadIcon = file => {
  const data = new FormData();
  data.append('file', file);
  updateHeadIconApi(data)
  .then(resp => {
    if (resp.result === 'ok') {
      (async () => {
        USER().setHeadIcon('');
        await nextTick();
        USER().setHeadIcon(resp.url);
      })();
    }
  });
};

onMounted(() => {
  Promise.all([MYBOTS().init(), LANG().init()])
    .then(() => {
      bots.value = MYBOTS().list
    });
});

</script>