<template>
  <CardBody>
    <Row>
      <Col col="col-4">
        <div class="card" style="width: 100%; border-radius: 0">
          <img style="border-bottom: 1px solid #ccc" :src="USER().getHeadIcon" class="card-img-top" alt="head_icon">
          <div class="username card-body">
            <h5 class="card-title" style="text-align: center; height: auto">{{ USER().getUsername }}<span style="color: gray; margin-left: 5px">#{{ userId }}</span></h5>
          </div>
          <button @click="updateInfo" v-if="userId === USER().getUserID" class="btn btn-dark" style="border-radius: 0; color: #eee; height: auto">
            修改信息
          </button>
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
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import CardBody from '../components/CardBody.vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import USER from '../store/USER';
import BotList from './viewsChild/BotList.vue';
import API from '../script/api';
import alert from '../script/alert';

const route = useRoute();
const userId = ref(route.params.userId);
const bots = ref([]);

const updateInfo = () => {
  alert(`warning`, `敬请期待`);
};

onMounted(() => {
  API({
    url: '/bot/getAll',
    type: 'get',
    needJWT: true,
    success: resp => {
      bots.value = resp;
      bots.value.map(bot => {
        bot.createTime = new Date(bot.createTime);
        bot.modifyTime = new Date(bot.modifyTime);
      });
    }
  })
});

</script>

<style>
</style>