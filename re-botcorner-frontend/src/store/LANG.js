import { defineStore } from 'pinia';
import { getAllLangApi } from './../script/api';

const LANG = defineStore('LANG', {
  state: () => {
    return {
      langs: {},
      list: []
    }
  },
  actions: {
    init() {
      return new Promise((resolve) => {
        getAllLangApi().then(langList => {
          this.list = langList;
          this.list.map(lang => {
            this.langs[lang.id] = lang;
          });
        });
        resolve();
      });
    }
  }
});

export default LANG;