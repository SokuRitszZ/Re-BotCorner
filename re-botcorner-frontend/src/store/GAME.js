import { defineStore } from 'pinia';
import { getAllGameApi } from './../script/api';

const GAME = defineStore('GAME', {
  state: () => {
    return {
      games: {},
      list: []
    }
  },
  actions: {
    init() {
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