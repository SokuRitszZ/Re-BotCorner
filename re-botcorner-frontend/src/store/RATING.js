import {defineStore} from "pinia";
import {getRatingApi} from "../script/api.js";

const RATING = defineStore('RATIONG', {
  state: () => {
    return {
      ratings: {},
      promiseOk: {}
    }
  },
  actions: {
    pullRating(game) {
      if (this.ratings[game] !== undefined) return Promise.resolve(this.ratings[game]);
      if (this.promiseOk[game] !== undefined) return this.promiseOk[game];
      return this.promiseOk[game] = getRatingApi(game).then(list => {
        list.sort((a, b) => {
          return b.rating - a.rating;
        });
        this.ratings[game] = list
        return list;
      });
    }
  }
});

export default RATING;