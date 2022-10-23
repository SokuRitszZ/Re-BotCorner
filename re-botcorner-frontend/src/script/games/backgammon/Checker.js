import GameObject from "../../GameObject.js";
import G from "../../G.js";
import Chess from "./Chess.js";

function pos(x, y) {
  return { x, y }
}

export default class Checker extends GameObject {
  static d = [-1, 1, -3, 3];
  static poss = [
    [pos(0.5, 0.5)],
    [pos(0.25, 0.5), pos(0.75, 0.5)],
    [pos(0.25, 0.25), pos(0.5, 0.5), pos(0.75, 0.75)],
    [pos(0.33, 0.33), pos(0.33, 0.66), pos(0.66, 0.33), pos(0.66, 0.66)],
    [pos(0.25, 0.25), pos(0.25, 0.75), pos(0.5, 0.5), pos(0.75, 0.25), pos(0.75, 0.75)],
    [pos(0.25, 0.33), pos(0.5, 0.33), pos(0.75, 0.33), pos(0.25, 0.66), pos(0.5, 0.66), pos(0.75, 0.66)]
  ];

  constructor(parent) {
    super(parent);

    this.chess = parent.chess;
    this.inHome = parent.inHome;

    this.hasSelect = -1;
    this.toSelect = -1;
    this.position = null;
    this.selectedPosition = null;
    this.dice = [];
    this.curId = -1;
    this.step = 0;
    this.chessObject = [];
    this.logMove = [];
  }

  getPosition(x, y) {
    const L = this.parent.L;
    x = Math.floor(x / L);
    y = Math.floor(y / L);
    return {
      x, y
    }
  }

  setCurId(curId) {
    this.curId = curId;
  }

  getSelectedId(pos) {
    const { x, y } = pos;
    if (x < 5) {
      this.selectedPosition = {
        x: 0,
        y: y
      }
      if (y < 6) return 12 - y;
      else if (y > 6 && 13 > y) return 13 - y;
      else return 0;
    } else if (x > 5) {
      this.selectedPosition = {
        x: 6,
        y: y
      }
      if (y < 6) return 13 + y;
      else if (y > 6) return 12 + y;
      else return 25;
    }
    else return -1;
  }

  getId(pos) {
    const { x, y } = pos;
    if (x < 5) {
      this.position = {
        x: 0,
        y: y
      }
      if (y < 6) return 12 - y;
      else if (y > 6 && 13 > y) return 13 - y;
      else return 0;
    } else if (x > 5) {
      this.position = {
        x: 6,
        y: y
      }
      if (y < 6) return 13 + y;
      else if (y > 6) return 12 + y;
      else return 25;
    }
    else return -1;
  }

  setDice(dice) {
    this.dice = dice;
  }

  moveChess(from, to, isRev=false, chessObj=null) {
    if (!isRev) ++this.step;
    from = parseInt(from);
    to = parseInt(to);
    const id0 = this.chess[from][0];
    const id1 = this.chess[to][0];
    const end = id0 === 0 ? 25 : 0;
    const home = id1 === 0 ? 0 : 25;
    const chess = this.chess;
    if (from === -1 || from === 26) {
      chessObj.setInHome(false);
      if (from === -1) --this.inHome[0]
      if (from === 26) --this.inHome[1];
      chessObj.moveTo(to);
    } else {
      const chessObject = chess[from][1].pop();
      if (to !== end && id0 === (id1 ^ 1) && chess[to][1].length === 1) {
        const eatenChessObject = chess[to][1].pop();
        eatenChessObject.moveTo(home);
        chessObject.moveTo(to);
        chess[to][0] = id0;
        if (!isRev) {
          this.logMove.push({
            action: "move",
            step: this.step,
            from: to,
            to: home
          });
          this.logMove.push({
            action: "move",
            step: this.step,
            from, to
          });
        }
      } else if (to === end) {
        chessObject.setInHome(true);
        chessObject.moveTo(id0 === 0 ? -1 : 26);
        if (!isRev) this.logMove.push({
          action: "toEnd",
          step: this.step,
          chessObject,
          from
        });
      } else {
        chessObject.moveTo(to);
        chess[to][0] = id0;
        if (!isRev) this.logMove.push({
          action: "move",
          step: this.step,
          from,
          to
        });
      }
    }
  }

  rollback() {
    const curStep = this.step--;
    let step;
    while (this.logMove.length > 0 && (step = this.logMove[this.logMove.length - 1]).step === curStep) {
      this.logMove.pop();
      switch (step.action) {
        case "move": {
          const {from, to} = step;
          try {
            this.moveChess(to, from, true);
          } catch (e) {
            console.log(`---${to + " " + from}`);
            return ;
          }
        }
        break;
        case "toEnd": {
          const {chessObject, from} = step;
          this.moveChess(null, from, true, chessObject);
        }
        break;
      }
    }
  }

  getChessPosition(i, count) {
    let result = {};
    if (1 <= i && i <= 12) {
      const y = 13.5 - i - (i > 6 ? 1 : 0);
      let x = 0.5;
      for (let j = 0, k = 0; j < count; ++j) {
        if (j === 0 || j === 5 || j === 9 || j === 12 || j === 14)
          x = ++k * 0.5;
        x += 1;
      }
      x -= 1;
      result = {x, y};
    } else if (13 <= i && i <= 24) {
      const y = i - 12.5 + (i > 18 ? 1 : 0);
      let x = 0.5;
      for (let j = 0, k = 0; j < count; ++j) {
        if (j === 0 || j === 5 || j === 9 || j === 12 || j === 14 || j === 15)
          x = ++k * 0.5;
        x += 1;
      }
      x -= 1;
      x = 11 - x
      result = {x, y};
    } else if (i === 0) {
      const y = 6.5;
      let x = 0.5;
      for (let j = 0, k = 0; j < count; ++j) {
        if (j === 0 || j === 5 || j === 9 || j === 12 || j === 14 || j === 15)
          x = ++k * 0.5;
        x += 1;
      }
      x -= 1;
      result = {x, y};
    } else if (i === 25) {
      const y = 6.5;
      let x = 0.5;
      for (let j = 0, k = 0; j < count; ++j) {
        if (j === 0 || j === 5 || j === 9 || j === 12 || j === 14 || j === 15)
          x = ++k * 0.5;
        x += 1;
      }
      x -= 1;
      x = (11 - x);
      result = {x, y};
    } else if (i === -1) {
      let x = 11 - 0.33;
      const y = 13;
      for (let i = 0; i < count; ++i) x -= 0.33;
      result = {x, y};
    } else if (i === 26) {
      let x = 0;
      const y = 13;
      for (let i = 0; i < count; ++i) x += 0.33;
      result = {x, y};
    }

    return result;
  }

  onStart() {
    const L = this.parent.L;
    const onStartAddChess = () => {
      for (let i = 1; i <= 12; ++i) {
        const [type, count] = this.chess[i];
        const y = 13.5 - i - (i > 6 ? 1 : 0);
        if (type === 2) continue;
        for (let j = 0, x = 0.5, k = 0; j < count; ++j) {
          if (j === 0 || j === 5 || j === 9 || j === 12 || j === 14 || j === 15)
            x = ++k * 0.5;
          this.chessObject.push(new Chess(this.parent, {
            id: type,
            x: x,
            y: y
          }));
        }
      }
    };

    onStartAddChess();
  }

  update() {
    this.render();
  }

  render() {
    const L = this.parent.L;

    const renderSelect = () => {
      const L = this.parent.L;
      if (this.hasSelect !== -1) {
        const s = this.hasSelect;
        if (s < 13) {
          G.rectangleSide({
            x: this.selectedPosition.x * L,
            y: this.selectedPosition.y * L,
            lx: 5 * L,
            ly: L,
            width: L / 50,
            color: '#222'
          });
        } else {
          G.rectangleSide({
            x: this.selectedPosition.x * L,
            y: this.selectedPosition.y * L,
            lx: 5 * L,
            ly: L,
            width: L / 50,
            color: '#222'
          });
        }
      }
    };

    const renderToSelect = () => {
      const L = this.parent.L;
      if (this.toSelect !== -1) {
        const s = this.toSelect;
        if (s < 13) {
          G.rectangleSide({
            x: this.position.x * L,
            y: this.position.y * L,
            lx: 5 * L,
            ly: L,
            width: L / 50,
            color: '#008'
          });
        } else {
          G.rectangleSide({
            x: this.position.x * L,
            y: this.position.y * L,
            lx: 5 * L,
            ly: L,
            width: L / 50,
            color: '#008'
          });
        }
      }
    };

    const renderDice = () => {
      const n = this.dice.length;
      const x = 5;
      for (let i = 0; i < n; ++i) {
        const y = 6 + Checker.d[i];
        const num = this.dice[i] - 1;
        const poss = Checker.poss;
        const L = this.parent.L;
        G.rectangle({
          x: (x + 0.1) * L,
          y: (y + 0.1) * L,
          lx: 0.8 * L,
          ly: 0.8 * L,
          color: this.curId === 0 ? `#ccc` : `#900`
        });
        for (let j = 0; j < poss[num].length; ++j) {
          G.circle({
            x: (x + poss[num][j].x) * L,
            y: (y + poss[num][j].y) * L,
            radius: L / 15,
            color: this.curId === 0 ? `#900` : `#ccc`
          });
        }
      }
    };

    renderSelect();
    renderToSelect();
    renderDice();
  }
}