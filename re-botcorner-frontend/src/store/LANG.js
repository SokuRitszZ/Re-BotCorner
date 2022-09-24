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
      getAllLangApi().then(langList => {
        this.list = langList;
        langList.map(lang => {
          this.langs[lang.id] = lang;
        });
      });
    }
  }
});

export default LANG;