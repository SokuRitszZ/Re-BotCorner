export default class Checker {
  constructor(parent) {
    this.parent = parent;

    this.snakes = parent.snakes;
  }
  
  moveSnake({ id, direction }) {
    this.snakes[id].nextStep({ d: direction });
  }

  setStatus(id, status) {
    this.snakes[id].setStatus(status);
  }
};