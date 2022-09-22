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
    this.hasAddListener = false;
  }
  
  start({ mode, rows, cols, stringifiedChess, userId0, userId1, putChessCallback }) {
    this.mode = mode;
    this.rows = rows;
    this.cols = cols;
    for (let i = 0, k = 0; i < rows; ++i) {
      this.chess[i] = [];
      for (let j = 0; j < cols; ++j) {
        this.chess[i][j] = parseInt(stringifiedChess[k++]);
      }
    }
    this.gameMap = new GameMap(this, { map: this.chess, rows, cols });
    this.checker = new Checker(this, { putChessCallback });
    
    if (!this.hasAddListener) {
      this.hasAddListener = true;
      const context = this.context;
      // const context = document.createElement('canvas').getContext('2d');
      context.canvas.addEventListener('click', e => {
        if (this.mode !== 'record') {
          const pos = this.checker.getPosition(e.clientX, e.clientY);
          const rc = this.checker.getRowCol(pos.x, pos.y);
          this.checker.putChessCallback(rc.r, rc.c);
        }
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