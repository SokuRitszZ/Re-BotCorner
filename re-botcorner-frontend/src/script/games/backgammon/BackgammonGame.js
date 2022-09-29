import G from "../../G.js";
import GameMap from "./GameMap.js";
import Checker from "./Checker.js";

export default class BackgammonGame {
  constructor({ parent, context }) {
    this.parent = parent;
    this.context = context;
    G.setContext(context);

    this.gameObjects = [];
    this.chess = [];
    this.lastTimestep = null;
    this.gameMap = null;
    this.checker = null;

    this.L = 0;
    this.hasAddListener = false;
  }

  start({ mode, stringifiedChess, moveChessCallback }) {
    this.mode = mode;
    for (let i = 0; i < 26; ++i) {
      this.chess[i] = [2, 0];
    }
    stringifiedChess.split(' ').map((item, index) => {
      this.chess[Math.floor(index / 2)][index % 2] = parseInt(item);
    });

    this.gameMap = new GameMap(this);
    this.checker = new Checker(this);

    if (!this.hasAddListener) {
      this.hasAddListener = true;
      const context = this.context;
      context.canvas.oncontextmenu = () => false;
      context.canvas.addEventListener('mousedown', e => {
        if (e.button === 0) {
          const pos = this.checker.getPosition(e.offsetY, e.offsetX);
          if (pos === this.checker.hasSelect) {
            this.checker.hasSelect = -1;
          } else if (pos !== -1) {
            this.checker.hasSelect = pos;
          }
        } else if (e.button === 2) {
          if (this.checker.hasSelect !== -1) {
            const pos = this.checker.getPosition(e.offsetY, e.offsetX);
            moveChessCallback(this.checker.hasSelect, pos);
            this.checker.hasSelect = -1;
          }
        }
      });
      context.canvas.addEventListener('contextmenu', e => {

      });
      context.canvas.addEventListener('mousemove', e => {
        const pos = this.checker.getPosition(e.offsetY, e.offsetX);
      });
    }

    this.startEngine();
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
