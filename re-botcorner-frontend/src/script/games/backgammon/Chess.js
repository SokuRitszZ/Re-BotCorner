import GameObject from "../../GameObject.js";
import C from "../../C.js";
import G from "../../G.js";

export default class Chess extends GameObject {
  static logMove = [];
  static step = 0;

  constructor(parent, {id, x, y}) {
    super(parent);

    this.id = id;
    this.x = x;
    this.y = y;

    this.tx = -1;
    this.ty = -1;
    this.a = 0;
    this.isInHome = false;
  }

  setInHome(isInHome) {
    this.isInHome = isInHome;
  }

  moveTo(to) {
    this.destroy();
    const checker = this.parent.checker;
    const count = 0 <= to && to < 26 ? checker.chess[to][1].length + 1 : to === -1 ? checker.inHome[0] : checker.inHome[1];
    const pos = checker.getChessPosition(to, count - 1);
    const L = this.parent.L;
    this.tx = pos.x;
    this.ty = pos.y;
    const distance = C.distance({x: this.x, y: this.y}, {x: this.tx, y: this.ty});
    const t = 0.5;
    // s = at^2 / 2
    // a = 2s / t^2
    // v^2 = 2as
    this.a = 2 * distance / (t * t);
    this.goInto();
  }

  onStart() {
  }

  update() {
    const updateMove = () => {
      const a = this.a;
      const dt = this.timedelta / 1000;
      if (a !== 0) {
        const curPos = {x: this.x, y: this.y};
        const dest = {x: this.tx, y: this.ty};
        const distance = C.distance({x: this.x, y: this.y}, {x: this.tx, y: this.ty});
        const v = Math.sqrt(2 * a * distance);
        const mov = Math.min(distance, v * dt);
        if (mov < C.EPS()) {
          this.x = this.tx;
          this.y = this.ty;
          this.a = 0;
          return ;
        }
        const ndx = C.ndx(curPos, dest);
        const ndy = C.ndy(curPos, dest);
        const theta = Math.atan2(ndx, ndy);
        const movx = mov * Math.sin(theta);
        const movy = mov * Math.cos(theta);
        this.x += movx;
        this.y += movy;
      }
    };

    updateMove();

    this.render();
  }

  render() {
    const L = this.parent.L;
    const renderChess = () => {
      if (!this.isInHome) {
        G.circle({
          x: this.x * L,
          y: this.y * L,
          radius: L / 2,
          color: `#222`
        });
        G.circle({
          x: this.x * L,
          y: this.y * L,
          radius: L / 2 * 0.95,
          color: this.id === 0 ? `#ccc` : `#900`
        });
      } else {
        G.rectangle({
          x: this.x * L,
          y: this.y * L,
          lx: 0.33 * L,
          ly: L,
          color: this.id === 0 ? `#ccc` : `#900`
        });
        G.rectangleSide({
          x: this.x * L,
          y: this.y * L,
          lx: 0.33 * L,
          ly: L,
          color: `#000`,
          width: L / 50
        });
      }
    };

    renderChess();
  }

  onDestroy() {
  }
};