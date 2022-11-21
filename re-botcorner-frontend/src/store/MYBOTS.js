import {defineStore} from "pinia";
import USER from "./USER.js";
import {getAllBotApi} from "../script/api.js";

const MYBOTS = defineStore('MYBOTS', {
  state: () => {
    return {
      list: [],
      promiseOk: null
    }
  },
  getters: {

  },
  actions: {
    init() {
      if (this.promiseOk !== null) return this.promiseOk;
      const promiseLogin = USER().loginByToken();
      return this.promiseOk = promiseLogin.then(id => {
        return getAllBotApi();
      }).then(botList => {
        this.list = botList;
        this.list.map(bot => {
          bot.createTime = new Date(bot.createTime);
          bot.modifyTime = new Date(bot.modifyTime);
        });
        return Promise.resolve();
      });
    }
  }
});

export default MYBOTS;