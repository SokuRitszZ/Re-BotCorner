import G from "../../G";
import GameObject from "../../GameObject";

export default class GameMap extends GameObject {
  constructor(parent, { rows, cols }) {
    super(parent);

    this.rows = rows;
    this.cols = cols;

    this.L = 0;
  }

  onStart() {
    
  }

  update() { 
    const updateSize = () => {
      const parentRect = this.parent.parent;
      this.L = Math.floor(Math.min(parentRect.clientHeight / this.rows, parentRect.clientWidth / this.cols));
      this.parent.L = this.L;
      this.context.canvas.width = this.L * this.cols;
      this.context.canvas.height = this.L * this.rows;
    };

    updateSize();
    
    this.render();
  }
  
  render() {
    const renderBackground = () => {
      G.rectangle({
        x: 0,
        y: 0,
        lx: this.context.canvas.height,
        ly: this.context.canvas.width,
        color: '#008800'
      });
    };

    const renderGrid = () => {
      const L = this.L;
      for (let i = 0; i < this.cols; ++i) {
        G.segment({
          x0: 0,
          y0: i * L,
          x1: this.rows * L,
          y1: i * L,
          width: L / 100,
          color: "#000000",
        });
      }
      for (let i = 0; i < this.rows; ++i) {
        G.segment({
          x0: i * L,
          y0: 0,
          x1: i * L,
          y1: this.cols * L,
          width: L / 100,
          color: "#000000",
        });
      }
    };

    renderBackground();
    renderGrid();
  }

  onDestroy() {
    super.onDestroy();
  }
}