<template>
  <div id="menu">
    <div class="title" style="width: 100%; height: 30%; text-align: center; text-shadow: 5px 5px 10px black;">
      <h1 style="color: yellow; text-align: center; font-size: 5rem;">
        <strong>
          BotCorner-简单博弈小站
        </strong>
      </h1>
    </div>
    <div class="menu-list" style="height: 70%">
      <div class="menu-item" @click="ROUTE().goto('game')"> 游戏大厅 </div>
      <div class="menu-item" @click="ROUTE().goto('rating')"> 排行榜 </div>
      <div class="menu-item" @click="ROUTE().goto('space')"> 个人空间 </div>
    </div>
  </div>
</template>

<script setup>
import ROUTE from '../store/ROUTE';
</script>

<style scoped>
#menu {
  width: 100%;
  height: 50%;
  user-select: none;
}

.menu-item {
  width: fit-content;
  height: fit-content;
  color: yellow;
  text-align: center;
  font-size: 3rem;
  line-height: 10vh;
  margin: auto;
  transition: 0.2s;
  padding: 0 20px;
  border-radius: 10px;
}

.menu-item:hover {
  background-color: rgba(0, 0, 0, 0.4);
  scale: 1.3;
  cursor: pointer;
}

h1 {
  transition: 0.2s;
}

h1:hover {
  text-shadow: 0px 0px 20px black;
}
</style>