import { createRouter, createWebHistory } from "vue-router";

import USER from './../store/USER';
import alert from './../script/alert';

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import("../views/Home.vue")
  },
  {
    path: '/space/:userId',
    name: 'space',
    component: () => import("../views/Space.vue"),
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/game',
    name: 'game',
    component: () => import("../views/GameLobby.vue"),
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/rating',
    name: 'rating',
    component: () => import("../views/Rating.vue"),
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/group',
    name: 'group',
    component: () => import("../views/Group.vue"),
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/group/:id',
    name: 'groupRoom',
    component: () => import("../views/GroupRoom.vue")
  },
  {
    path: '/notfound',
    name: 'notfound',
    component: () => import("../views/NotFound.vue"),
  },
  {
    path: '/snake',
    name: 'snake',
    component: () => import("../views/Snake.vue"),
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/reversi',
    name: 'reversi',
    component: () => import("../views/Reversi.vue"),
    meta: {
      requireAuth: true
    }
  },
  {
    path: '/backgammon',
    name: 'backgammon',
    component: () => import("../views/Backgammon.vue"),
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

router.beforeEach( async (to, from, next) => {
  if (to.meta.requireAuth && !USER().checkIsLogined) {
    await USER().loginByToken(true).then(() => {
      next();
    }).catch(() => {
      alert("danger", "未登录");
      next({ name: "home" });
    });
  } else {
    next();
  }
});

export default router;