<template>
  <div class="framework" style="overflow: auto">
    <transition>
      <div v-show="!isRoute('menu')" id="routes" style="position: absolute; color: yellow; height: 50px; z-index: 2; top: 20px; left: 270px">
        <transition-group>
          <div class="route-item" v-for="(route, index) in ROUTE().routes" 
          @click="ROUTE().backto(index)"
          :key="route"
          >
            <span v-if="route != ROUTE().current"> {{ route }} </span>
            <strong v-else> {{ route }} </strong>
          </div>
        </transition-group>
      </div>
    </transition>
    <MenuView v-if="isRoute('menu')" />
    <GameLobby v-if="isRoute('game')" />
    <Rating v-if="isRoute('rating')" />
    <Space v-if="isRoute('space')" />
    <Snake v-if="isRoute('snake')" />
    <Reversi v-if="isRoute('reversi')" />
  </div>
</template>

<script setup>
import USER from './store/USER.js';
import { onMounted } from 'vue';
import GAME from './store/GAME';
import LANG from './store/LANG';
import MenuView from './views/MenuView.vue';
import ROUTE from './store/ROUTE';
import GameLobby from './views/GameLobby.vue';
import Rating from './views/Rating.vue';
import Space from './views/Space.vue';
import Snake from './views/Snake.vue';
import Reversi from './views/Reversi.vue';
import API from './script/api';

const isRoute = route => {
  return ROUTE().current == route;
};

const loginByAcWingOS = () => {
  if (USER().AcWingOS == "AcWingOS") {
    USER().setToken('eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJmNmIyMzhjZGYxYTc0M2E5ODAxZjVmYThkMTBjMGNkNyIsInN1YiI6IjEiLCJpc3MiOiJzZyIsImlhdCI6MTY2NDE3NDg5NCwiZXhwIjoxNjY1Mzg0NDk0fQ.b2vPqG2HEDLPRGJml5sZ9RNpxRN-haG7emhpORJg3SM');
    USER().loginByToken();
  } else {
    API({
      url: "/user/account/acwing/acapp/apply_code/",
      type: 'get',
      async: false,
      success: resp => {
        if (resp.result === 'success') {
          USER().AcWingOS.api.oauth2.authorize(resp.appid, resp.redirect_uri, resp.scope, resp.state, resp => {
            if (resp.result === 'success') {
              const jwt_token = resp.jwt_token;
              USER().setToken(jwt_token);
              USER().loginByToken();
            } else {
              USER().AcWingOS.api.window.close();
            }
          });
        } else {
          USER().AcWingOS.api.window.close();
        }
      }
    });
  }
};

onMounted(() => {
  loginByAcWingOS();
  GAME().init();
  LANG().init();
});
</script>

<style scoped>
.framework {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: auto;
}

.route-item {
  display: inline-block;
  box-shadow: 0 0 10px black;
  margin-left: -10px;
  width: fit-content;
  background-color: yellow;
  color: blue;
  padding: 10px;
  border-radius: 2px;
  transition: 0.5s;
}

.v-enter-active {
  animation: fadeInRight 0.5s;
}

.v-leave-active {
  animation: fadeOutRight 0.5s;
}

.route-item:hover {
  cursor: pointer;
  scale: 1.2;
  z-index: 10;
  margin: 0 20px
}
</style>