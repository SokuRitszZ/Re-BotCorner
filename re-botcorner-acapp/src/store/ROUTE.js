import { defineStore } from "pinia";

const ROUTE = defineStore('ROUTE', {
  state: () => {
    return {
      index: 0,
      routeStack: ['menu']
    }
  },
  getters: {
    current() { return this.routeStack[this.index]; },
    routes() { return this.routeStack.slice(0, this.index + 1); }
  },
  actions: {
    goto(route) { this.routeStack[++this.index] = route; },
    backto(routeIndex) { this.index = routeIndex; }
  }
});

export default ROUTE;