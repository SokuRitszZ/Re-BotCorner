import G from "../../G";
import Checker from "./Checker";
import GameMap from "./GameMap";

export default class ReversiGame {
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
  }
  
  start({ mode, initData, putChessCallback }) {
    this.mode = mode;
    this.chess = initData.initChess;
    this.rows = this.chess.length;
    this.cols = this.chess[0].length;
    this.gameMap = new GameMap(this, { rows: this.rows, cols: this.cols });
    this.checker = new Checker(this);
    const context = this.context;
    context.canvas.addEventListener('click', e => {
      if (this.mode !== 'record') {
        const pos = this.checker.getPosition(e.clientX, e.clientY);
        const rc = this.checker.getRowCol(pos.x, pos.y);
        putChessCallback(rc.r, rc.c);
      }
    });

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