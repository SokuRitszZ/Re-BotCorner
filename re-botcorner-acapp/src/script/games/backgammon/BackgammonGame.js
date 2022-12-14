import G from "../../G.js";
import GameMap from "./GameMap.js";
import Checker from "./Checker.js";
import Chess from "./Chess.js";

export default class BackgammonGame {
  constructor({ parent, context }) {
    this.parent = parent;
    this.context = context;
    G.setContext(context);

    this.gameObjects = [];
    this.chess = [];
    this.inHome = [0, 0];
    this.lastTimestep = null;
    this.gameMap = null;
    this.checker = null;
    this.curPos = {};

    this.L = 0;
    this.hasAddListener = false;
    this.moveChessCallback = () => {};
  }


  start({ initData, moveChessCallback }) {
    this.startEngine();
    this.startGame({initData});
    this.moveChessCallback = moveChessCallback;
    if (!this.hasAddListener) this.startAddListener();
  }

  startGame({initData}) {
    this.gameObjects = [];
    this.chess = [];
    this.inHome = [0, 0];
    this.lastTimestep = null;

    this.gameMap = new GameMap(this);
    this.checker = new Checker(this);
    this.checker.setCurId(initData.initStart);

    for (let i = 0; i < 26; ++i) {
      this.chess[i] = [2, []];
    }
    this.gameMap.update();
    const L = this.L;

    initData.initChess.forEach((chess, i) => {
      i = parseInt(i);
      this.chess[i][0] = chess.type;
      const n = chess.count;
      for (let j = 0; j < n; ++j) {
        const pos = this.checker.getChessPosition(i, j + 1);
        this.chess[i][1].push(new Chess(this, {
          id: this.chess[i][0],
          x: pos.x,
          y: pos.y
        }));
      }
    });
  }

  startAddListener() {
    this.hasAddListener = true;
    const context = this.context;
    context.canvas.oncontextmenu = () => false;
    context.canvas.focus();
    window.addEventListener('keyup', e => {
      if (e.key === 'd') {
        const pos = this.curPos;
        const id = this.checker.getSelectedId(pos);
        if (id === this.checker.hasSelect) {
          this.checker.hasSelect = -1;
        } else if (id !== -1) {
          this.checker.hasSelect = id;
        }
      } else if (e.key === 'f') {
        if (this.checker.hasSelect !== -1) {
          const pos = this.curPos;
          const id = this.checker.getSelectedId(pos);
          this.moveChessCallback(this.checker.hasSelect, id);
          this.checker.hasSelect = -1;
        }
      }
    });
    context.canvas.addEventListener('mousedown', e => {
      if (e.button === 0) {
        const pos = this.checker.getPosition(e.offsetY, e.offsetX);
        const id = this.checker.getSelectedId(pos);
        if (id === this.checker.hasSelect) {
          this.checker.hasSelect = -1;
        } else if (id !== -1) {
          this.checker.hasSelect = id;
        }
      } else if (e.button === 2) {
        if (this.checker.hasSelect !== -1) {
          const pos = this.checker.getPosition(e.offsetY, e.offsetX);
          const id = this.checker.getSelectedId(pos);
          this.moveChessCallback(this.checker.hasSelect, id);
          this.checker.hasSelect = -1;
        }
      }
    });
    context.canvas.addEventListener('mousemove', e => {
      const pos = this.checker.getPosition(e.offsetY, e.offsetX);
      this.curPos = pos;
      const id = this.checker.getId(pos);
      if (id !== -1) this.checker.toSelect = id;
      else this.checker.toSelect = -1;
    });
  }

  startEngine() {
    const animateEngine = timestep => {
      this.gameObjects.map(gameObject => {
        if (!gameObject.isStarted) {
          gameObject.isStarted = true;
          gameObject.start();
        } else {
          gameObject.timedelta = timestep - this.lastTimestep;
          gameObject.update();
        }
      });
      this.lastTimestep = timestep;
      requestAnimationFrame(animateEngine);
    };
    requestAnimationFrame(animateEngine);
  }

  endGaming() {
    this.gameObjects.map(gameObject => {
      gameObject.destroy();
    });
  }

  getChecker() {
    return this.checker;
  }
}
