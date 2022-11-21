<template>
  <CardBody>
    <Row>
      <Col col="col-9">
        <div class="game-card-container mt-4">
          <div class="card game-card" v-for="game in GAME().list" @click="router.push({ name: game.title })">
            <div class="card-body game-card-body">
              <h3>{{ game.name }}</h3>
              <hr>
              <div>{{ game.description }}</div>
            </div>
          </div>
        </div>
      </Col>
      <Col col="col-3">
        <h1>待开发区域</h1>
        <hr>
      </Col>
    </Row>
  </CardBody>
</template>

<script setup>
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import CardBody from '../components/CardBody.vue';
import GAME from '../store/GAME';
import router from '../routes';
import {onMounted} from "vue";

onMounted(() => {
  GAME().init();
});
</script>

<style scoped>
.game-card-container {
  display: flex;
  flex-direction: row;
}

.game-card {
  width: 200px;
  height: 300px;
  border-radius: 0;
  box-shadow: 0 0 5px #989898;
  cursor: pointer;
  margin-left: -40px;
  transition: 0.5s;
  overflow: auto;
  padding: 10px;
}

.game-card::-webkit-scrollbar {
  display: none;
}

.game-card:first-child {
  margin-left: 20px;
}

.game-card:hover {
  transform: scale(1.05);
  box-shadow: 0 0 30px #989898;
  margin-right: 60px;
}
</style>