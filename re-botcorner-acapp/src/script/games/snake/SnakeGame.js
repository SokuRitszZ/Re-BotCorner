import G from "../../G";
import Checker from "./Checker";
import GameMap from "./GameMap";
import Snake from "./Snake";

export default class SnakeGame {
  constructor({ parent, context }) {
    this.parent = parent;
    this.context = context;
    G.setContext(context);

    this.gameObjects = [];
    this.snakes = [];
    this.walls = [];
    this.lastTimestamp = null;
    this.gameMap = null;
    this.checker = null;

    this.L = 0;
  }

  start({ initData }) {
    this.rows = initData.rows;
    this.cols = initData.cols;
    this.gameMap = new GameMap(this, {
      map: initData.initG
    });
    this.snakes.push(
      new Snake(this, {
        id: 0,
      })
    );
    this.snakes.push(
      new Snake(this, {
        id: 1,
      })
    );
    this.checker = new Checker(this);
    this.startEngine();
  }

  startEngine() {
    const animateEngine = timestamp => {
      const gameObjects = this.gameObjects;
      for (const gameObject of gameObjects) {
        if (!gameObject.isStarted) {
          gameObject.isStarted = true;
          gameObject.start();
        } else {
          gameObject.timedelta = timestamp - this.lastTimestamp;
          gameObject.update();
        }
      }
      this.lastTimestamp = timestamp;
      requestAnimationFrame(animateEngine);
    };
    requestAnimationFrame(animateEngine);
  }

  endGaming() {
    for (const gameObject of this.gameObjects) {
      gameObject.destroy();
    }
  }
  
  getChecker() { return this.checker; }
}