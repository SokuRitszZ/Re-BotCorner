import { defineStore } from 'pinia';
import API from './../script/api';

const LANG = defineStore('LANG', {
  state: () => {
    return {
      langs: {},
      list: []
    }
  },
  actions: {
    init() {
      API({
        url: `/lang/getAll`,
        type: 'get',
        success: resp => {
          for (let i = 0; i < resp.length; ++i) {
            const lang = resp[i];
            this.list.push(lang);
            this.langs[lang.id] = lang;
          }
        }
      });
    }
  }
});

export default LANG;