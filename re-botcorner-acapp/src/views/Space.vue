<template>
  <Container>
    <Row>
      <Col col="col-3">
        <div class="card" style="width: 100%; border-radius: 0; opacity: 0.99;">
          <img style="border-bottom: 1px solid #ccc" :src="USER().getHeadIcon" class="card-img-top" alt="head_icon">
          <div class="username card-body">
            <h5 class="card-title" style="text-align: center; height: auto">{{ USER().getUsername }}<span style="color: gray; margin-left: 5px">#{{ userId }}</span></h5>
          </div>
          <button @click="updateInfo" v-if="userId === USER().getUserID" class="btn btn-dark" style="border-radius: 0; color: #eee; height: auto">
            修改信息
          </button>
        </div>
      </Col>
      <Col col="col-9">
        <template v-if="userId === USER().getUserID">
          <BotList :bots="bots">
          </BotList>
        </template>
      </Col>
    </Row>
  </Container>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import USER from '../store/USER';
import BotList from './viewsChild/BotList.vue';
import { getAllBotApi } from '../script/api';
import alert from '../script/alert';
import Container from '../components/Container.vue';

const userId = ref(USER().getUserID);
const bots = ref([]);

const updateInfo = () => {
  alert(`warning`, `敬请期待`);
};

const getBots = () => {
  getAllBotApi()
  .then(botList => {
    bots.value = botList;
    bots.value.map(bot => {
      bot.createTime = new Date(bot.createTime);
      bot.modifyTime = new Date(bot.modifyTime);
    });
  });
};

onMounted(() => {
  getBots();
});

</script>

<style>
</style>