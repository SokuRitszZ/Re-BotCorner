<template>
  <Container>
    <div class="w-100 h-100 d-flex flex-column justify-content-center">
      <div class="game-card-container">
        <div :key="game.id" class="card game-card" v-for="game in GAME().list" @click="ROUTE().goto(game.title)">
          <div class="card-body game-card-body">
            <h3>{{ game.name }}</h3>
            <hr>
            <div>{{ game.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </Container>
</template>

<script setup>
import Container from '../components/Container.vue';
import ROUTE from "../store/ROUTE";
import GAME from '../store/GAME';
</script>

<style scoped>
.game-card-container {
  display: flex;
  flex-direction: row;
}

.game-card {
  color: #0000ff;
  width: 200px;
  height: 300px;
  border-radius: 0;
  box-shadow: 0 0 5px #ffff00;
  background-color: #ffff00;
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
  box-shadow: 0 0 30px #ffff00;
  margin-right: 60px;
}
</style>