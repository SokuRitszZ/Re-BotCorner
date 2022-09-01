import { defineStore } from 'pinia';
import API from './../script/api';

const GAME = defineStore('GAME', {
  state: () => {
    return {
      games: {},
      list: []
    }
  },
  actions: {
    init() {
      API({
        url: '/game/getAll',
        type: 'get',
        needJWT: false,
        success: resp => {
          for (let i = 0; i < resp.length; ++i) {
            const game = resp[i];
            this.list.push(game);
            this.games[game.id] = game;
          }
        },
      });
    }
  }
});

export default GAME;