import { defineStore } from 'pinia';
import { getAllGameApi } from './../script/api';
import USER from "./USER.js";

const GAME = defineStore('GAME', {
  state: () => {
    return {
      games: {},
      list: [],
      promiseOk: null
    }
  },
  actions: {
    init() {
      if (this.promiseOk !== null) return this.promiseOk;
      const promiseLogin = USER().loginByToken();
      return this.promiseOk = promiseLogin.then(() => {
        return getAllGameApi()
      }).then(gameList => {
        this.list = gameList;
        gameList.map(game => {
          this.games[game.id] = game;
        });
      }).catch(() => {
        this.promiseOk = null;
      });

      return new Promise((resolve) => {
        getAllGameApi().then(gameList => {
          this.list = gameList;
          gameList.map(game => {
            this.games[game.id] = game;
          });
          resolve();
        });
      });
    }
  }
});

export default GAME;