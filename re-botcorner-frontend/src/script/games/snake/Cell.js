export default class Cell {
  constructor(r, c) {
    this.r = r;
    this.c = c;
    
    this.x = this.r + 0.5;
    this.y = this.c + 0.5;
  }
};