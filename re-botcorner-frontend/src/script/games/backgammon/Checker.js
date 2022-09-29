import GameObject from "../../GameObject.js";
import G from "../../G.js";

export default class Checker extends GameObject {
  constructor(parent) {
    super(parent);

    this.chess = parent.chess;

    this.cnt0 = 0;
    this.cnt1 = 0;
    this.hasSelect = -1;
  }

  getPosition(x, y) {
    const L = this.parent.L;
    x = Math.floor(x / L);
    y = Math.floor(y / L);
    if (0 <= x && x < 5) {
      return 12 - y;
    } else if (11 > x && x > 5) {
      return 13 + y;
    } else {
      return -1;
    }
  }

  moveChess(indexFrom, indexTo) {
    this.chess[indexTo][0] = this.chess[indexFrom][0];
    this.chess[indexFrom][1]--;
    this.chess[indexTo][1]++;
  }

  onStart() {

  }

  update() {
    this.render();
  }

  render() {
    const L = this.parent.L;

    const renderChess = () => {
      for (let i = 0; i < 13; ++i) {
        const len = this.chess[i][1];
        const y = 13 - i - 0.5;
        if (this.chess[i][0] === 2) continue;
        for (let j = 0, x = 0.5, k = 0; j < len; ++j) {
          if (j === 0 || j === 5 || j === 9 || j === 12 || j === 14 || j === 15) {
            x = ++k * 0.5;
          }
          G.circle({
            x: x * L,
            y: y * L,
            radius: L / 2,
            color: '#222'
          });
          G.circle({
            x: x * L,
            y: y * L,
            radius: L / 2 * 0.99,
            color: this.chess[i][0] === 0 ? '#ccc' : '#900'
          });
          x += 1;
        }
      }
      for (let i = 13; i < 26; ++i) {
        const len = this.chess[i][1];
        const y = i - 13 + 0.5;
        for (let j = 0, x = 0.5, k = 0; j < len; ++j) {
          if (this.chess[i][0] === 2) continue;
          if (j === 0 || j === 5 || j === 9 || j === 12 || j === 14 || j === 15) {
            x = ++k * 0.5;
          }
          G.circle({
            x: (11 - x) * L,
            y: y * L,
            radius: L / 2,
            color: '#222'
          });
          G.circle({
            x: (11 - x) * L,
            y: y * L,
            radius: L / 2 * 0.99,
            color: this.chess[i][0] === 0 ? '#ccc' : '#900'
          });
          x += 1;
        }
      }
    };

    const renderSelect = () => {
      const L = this.parent.L;
      if (this.hasSelect !== -1) {
        const s = this.hasSelect;
        if (s < 13) {
          G.rectangleSide({
            x: 0,
            y: (12 - s) * L,
            lx: 5 * L,
            ly: L,
            width: L / 50,
            color: '#222'
          });
        } else {
          G.rectangleSide({
            x: 6 * L,
            y: (s - 13) * L,
            lx: 5 * L,
            ly: L,
            width: L / 50,
            color: '#222'
          });
        }
      }
    };

    renderChess();
    renderSelect();
  }
}