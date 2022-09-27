<template> 
  <div class="game-card-container" style="margin: auto">
    <div class="card game-card" v-for="game in GAME().list" @click="ROUTE().goto(game.title)">
      <div class="card-body game-card-body">
        <h3 style="font-weight: 800;">
          {{ game.name }}
        </h3>
        <hr>
        <div style="font-weight: 500;">{{ game.description }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import GAME from '../store/GAME';
import ROUTE from '../store/ROUTE';

</script>

<style scoped>
.game-card-container {
  display: flex;
  overflow: visible;
  justify-content: baseline;
}

.game-card {
  width: 200px;
  height: 300px;
  border-radius: 0;
  box-shadow: -1px 0 5px #111;
  cursor: pointer;
  transition: 0.5s;
  margin-left: -40px;
  background-color: yellow;
  color: rgba(0, 0, 255, 0.8)
}

.game-card:hover {
  transform: scale(1.05);
  box-shadow: 0 0 30px gray;
  z-index: 99;
  margin: 0 60px;
}
</style>