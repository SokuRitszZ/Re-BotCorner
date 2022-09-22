import G from "../../G";
import GameObject from "../../GameObject";

export default class Checker extends GameObject {
  static {
    Checker.prototype.dx = [ -1, -1, 0, 1, 1, 1, 0, -1 ];
    Checker.prototype.dy = [ 0, 1, 1, 1, 0, -1, -1, -1 ];
  }
  
  constructor(parent, { putChessCallback }) {
    super(parent);
    
    this.chess = parent.chess;
    this.putChessCallback = putChessCallback;

    this.cnt0 = 0;
    this.cnt1 = 0;
  }

  isIn(r, c) {
    return r >= 0 && c >= 0 && r < this.parent.rows && c < this.parent.cols;
  }

  isValidDir(r, c, i, id) {
    const dx = Checker.prototype.dx;
    const dy = Checker.prototype.dy;
    let nr = r + dx[i];
    let nc = c + dy[i];
    let cnt = 0;
    while (this.isIn(nr, nc) && this.chess[nr][nc] === (id ^ 1)) {
      nr += dx[i];
      nc += dy[i];
      cnt = 1;
    }
    return cnt === 1 && this.isIn(nr, nc) && this.chess[nr][nc] === id;
  }

  putChess(r, c, id) {
    this.chess[r][c] = id;
    const dx = Checker.prototype.dx;
    const dy = Checker.prototype.dy;
    for (let i = 0; i < 8; ++i) {
      if (this.isValidDir(r, c, i, id)) {
        let nr = r + dx[i];
        let nc = c + dy[i];
        while (this.isIn(nr, nc) && this.chess[nr][nc] === (id ^ 1)) {
          this.chess[nr][nc] = id;
          nr += dx[i];
          nc += dy[i];
        }
      }
    }
  }

  getPosition(ex, ey) {
    const context = this.parent.context;
    const canvas = context.canvas;
    const rect = canvas.getBoundingClientRect();
    return { x: ey - rect.top, y: ex - rect.left }
  }

  getRowCol(x, y) {
    const L = this.parent.L;
    return {
      r: Math.floor(x / L),
      c: Math.floor(y / L)
    };
  }

  get0() {
    let result = 0;
    for (let i = 0; i < this.parent.rows; ++i) {
      for (let j = 0; j < this.parent.cols; ++j) {
        result += this.parent.chess[i][j] == 0 ? 1 : 0;
      }
    }
    this.cnt0 = result;
  }
  get1() {
    let result = 0;
    for (let i = 0; i < this.parent.rows; ++i) {
      for (let j = 0; j < this.parent.cols; ++j) {
        result += this.parent.chess[i][j] == 1 ? 1 : 0;
      }
    }
    this.cnt1 = result;
  }
  onStart() { 

  }

  update() {
    const updateCount = () => {
      this.get0();
      this.get1();
    };

    updateCount();

    this.render();
  }

  render() {
    const L = this.parent.L;
    const parent = this.parent;
    const chess = parent.chess;

    const renderChess = () => {
      for (let i = 0; i < parent.rows; ++i) {
        for (let j = 0; j < parent.cols; ++j) {
          if (chess[i][j] !== 2) {
            G.circle({
              x: (i + 0.53) * L,
              y: (j + 0.5) * L,
              radius: L * 0.4,
              color: chess[i][j] === 0 ? `#ffffff` : `#000000`
            });
            G.circle({
              x: (i + 0.5) * L,
              y: (j + 0.5) * L,
              radius: L * 0.4,
              color: chess[i][j] === 0 ? `#000000` : `#ffffff`
            });
          }
        }
      }
    }

    renderChess();
  }

  onDestroy() {
    const onDestroyRemoveListener = () => {
      // const context: CanvasRenderingContext2D = this.parent.context;
      const context = this.parent.context;
      const canvas = context.canvas;
      canvas.removeEventListener('click', this.handleClick, false);
    }

    onDestroyRemoveListener();
  }
}