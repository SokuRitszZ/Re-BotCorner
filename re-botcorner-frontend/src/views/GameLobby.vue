<template> <Container>
    <Row>
      <Col col="col-9">
      <CardBody>
        <div class="game-card-container">
          <div class="card game-card" v-for="game in GAME().list" @click="router.push({ name: game.title })">
            <div class="card-body game-card-body">
              <h3>{{ game.name }}</h3>
              <hr>
              <div>{{ game.description }}</div>
            </div>
          </div>
        </div>
      </CardBody>
      </Col>
      <Col col="col-3">
      <CardBody>
        <h1>Demo</h1>
      </CardBody>
      </Col>
    </Row>
  </Container>
</template>

<script setup>
import Container from '../components/Container.vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import CardBody from '../components/CardBody.vue';
import GAME from '../store/GAME';
import router from '../routes';
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