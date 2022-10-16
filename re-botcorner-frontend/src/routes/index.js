import { createRouter, createWebHashHistory, createWebHistory } from "vue-router";

import Home from '../views/Home.vue';
import Space from '../views/Space.vue';
import GameLobby from '../views/GameLobby.vue';
import NotFound from '../views/NotFound.vue';
import Snake from '../views/Snake.vue';
import Reversi from '../views/Reversi.vue';
import Rating from '../views/Rating.vue';
import Group from '../views/Group.vue';
import GroupDetail from '../views/GroupRoom.vue';
import ContestDetail from '../views/ContestDetail.vue';
import Backgammon from '../views/Backgammon.vue';

import USER from './../store/USER';
import alert from './../script/alert';

const routes = [
  {
    path: '/',
    name: 'home',
    component: Home
  },
  {
    path: '/space/:userId',
    name: 'space',
    component: Space,
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/game',
    name: 'game',
    component: GameLobby,
  },
  {
    path: '/rating',
    name: 'rating',
    component: Rating
  },
  {
    path: '/group',
    name: 'group',
    component: Group
  },
  {
    path: '/group/:id',
    name: 'groupRoom',
    component: GroupDetail
  },
  {
    path: '/contest/:id',
    name: 'contestDetail',
    component: ContestDetail
  },
  {
    path: '/notfound',
    name: 'notfound',
    component: NotFound,
  },
  {
    path: '/snake',
    name: 'snake',
    component: Snake,
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/reversi',
    name: 'reversi',
    component: Reversi,
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/backgammon',
    name: 'backgammon',
    component: Backgammon,
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/hex',
    name: 'hex',
    redierct: '/notfound'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to, from, next) => {
  if (to.meta.requireAuth && !USER().checkIsLogined) {
    await USER().loginByToken(true);
    if (USER().checkIsLogined) {
      next();
    } else {
      alert(`danger`, `未登录`);
      next({ name: `home` });
    }
  } else {
    next();
  }
});

export default router;