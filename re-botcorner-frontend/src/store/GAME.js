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
      getAllGameApi().then(gameList => {
        this.list = gameList;
        gameList.map(game => {
          this.games[game.id] = game;
        });
      });
    }
  }
});

export default GAME;