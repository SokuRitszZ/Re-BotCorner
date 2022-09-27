export default class Checker {
  constructor(parent) {
    this.parent = parent;

    this.snakes = parent.snakes;
  }
  
  moveSnake({ id, direction, status, isIncreasing }) {
    this.snakes[id].nextStep({ isIncreasing, d: direction, status });
  }

  setStatus(id, status) {
    this.snakes[id].setStatus(status);
  }
};