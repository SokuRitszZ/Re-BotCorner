import G from "../../G";
import GameObject from "../../GameObject";

export default class GameMap extends GameObject {
  constructor(parent, { map }) {
    super(parent);

    this.map = map;

    this.rows = map.length;
    this.cols = map[0].length;
    
    this.L = 0;
  }

  onStart() {
  }

  update() {
    const updateSize = () => {
      this.L = Math.floor(Math.min(this.parent.parent.clientWidth / this.cols, this.parent.parent.clientHeight / this.rows));
      this.parent.L = this.L;
      this.context.canvas.width = this.L * this.cols;
      this.context.canvas.height = this.L * this.rows;
    }

    updateSize();

    this.render();
  }

  render() {
    const renderMap = () => {
      G.rectangle({
        x: 0,
        y: 0,
        lx: this.context.canvas.height,
        ly: this.context.canvas.width,
        color: '#fafafa'
      });
    };

    const renderGrid = () => {
      const oddColor = '#dddddd', evenColor = '#efefef';
      for (let i = 0; i < this.rows; ++i) {
        for (let j = 0; j < this.cols; ++j) {
          G.rectangle({
            x: i * this.L,
            y: j * this.L,
            lx: this.L,
            ly: this.L,
            color: (i + j) % 2 === 1 ? oddColor : evenColor
          });
        }
      }
    };
    
    const renderWalls = () => {
      for (let i = 0; i < this.rows; ++i) {
        for (let j = 0; j < this.cols; ++j) {
          if (this.map[i][j] === 1) {
            G.rectangle({
              x: i * this.L,
              y: j * this.L,
              lx: this.L,
              ly: this.L,
              color: '#896c50'
            });
          } 
        }
      }
    };
    
    renderMap();
    renderGrid();
    renderWalls();
  }

  onDestroy() {

  }
}