export default class GameObject {
  constructor(parent) {
    this.parent = parent;
    this.context = parent.context;
    
    this.isStart = false;
    this.timedelta = 0;
    
    parent.gameObjects.push(this);
  }

  start() {
    this.onStart();
  }

  update() {

  }

  destroy() {
    this.onDestroy();
    let gameObjects = this.parent.gameObjects;
    for (let i = 0; i < gameObjects.length; ++i) {
      if (gameObjects[i] === this) {
        gameObjects.splice(i, 1);
        break;
      }
    }
  }

  onStart() { }
  
  onDestroy() { }
};