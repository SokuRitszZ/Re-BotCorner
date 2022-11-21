import { defineStore } from 'pinia';
import { getAllLangApi } from './../script/api';

const LANG = defineStore('LANG', {
  state: () => {
    return {
      langs: {},
      list: [],
      promiseOk: null
    }
  },
  actions: {
    init() {
      if (this.promiseOk !== null) return this.promiseOk;
      return this.promiseOk = getAllLangApi().then(langList => {
        this.list = langList;
        this.list.map(lang => {
          this.langs[lang.id] = lang;
        });
      });
    }
  }
});

export default LANG;