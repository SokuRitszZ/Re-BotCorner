<template>
  <div class="framework position-absolute w-100 d-flex flex-column justify-content-center" style="height: 93.5%">
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
    <Backgammon v-if="isRoute('backgammon')" />
  </div>
</template>

<script setup>
import 'bootstrap/dist/js/bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';

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
import Backgammon from "./views/Backgammon.vue";
import api from "./script/api";

const isRoute = route => {
  return ROUTE().current == route;
};

const loginByAcWingOS = () => {
  if (USER().AcWingOS == "AcWingOS") {
    USER().setToken("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyOTM2ZDFhZDZlZjM0YmNlOTBjODA5NzZhZjIxMDYzZiIsInN1YiI6IjMiLCJpc3MiOiJzZyIsImlhdCI6MTY2NzA5NzQ5NSwiZXhwIjoxNjY4MzA3MDk1fQ.Ka8RzFtOw2t1n6YKFQZDJnJVyFyw-7WVWurHRhhTPYw");
    USER().loginByToken();
  } else {
    api({
      url: "/user/account/acwing/acapp/apply_code/",
      method: "GET"
    })
      .then(resp => {
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
  background-color: rgba(0, 0, 255, 0.8);
}

.route-item {
  display: inline-block;
  margin-left: 10px;
  width: fit-content;
  background-color: yellow;
  color: blue;
  padding: 10px;
  transition: 0.5s;
}

.v-enter-active {
  animation: fadeInRight 0.5s;
}

.v-leave-active {
  animation: fadeInRight 0.5s linear reverse;
}

.route-item:hover {
  cursor: pointer;
  transform: translateY(-20%);
  z-index: 10;
}
</style>