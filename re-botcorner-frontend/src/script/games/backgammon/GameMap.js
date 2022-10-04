import G from "../../G.js";
import GameObject from "../../GameObject.js";

export default class GameMap extends GameObject {
  constructor(parent) {
    super(parent);

    this.L = 0;
  }

  onStart() {
  }

  update() {
    const updateSize = () => {
      const parentRect = this.parent.parent;
      this.L = Math.floor(Math.min(parentRect.clientHeight / 11, parentRect.clientWidth / 14));
      this.parent.L = this.L;
      this.context.canvas.width = this.L * 14;
      this.context.canvas.height = this.L * 11;
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
        color: '#e3ac72'
      });
    };

    const renderGrid = () => {
      const L = this.L;
      for (let i = 0; i < 6; i += 2) {
        G.triangle({
          x: 0,
          y: (i + 0.5) * L,
          l: L,
          h: 5 * L,
          color: '#ddd',
          angle: 0
        });
        G.triangle({
          x: 0,
          y: (i + 1.5) * L,
          l: L,
          h: 5 * L,
          color: '#800',
          angle: 0
        });
        G.triangle({
          x: 0,
          y: (i + 0.5 + 7) * L,
          l: L,
          h: 5 * L,
          color: '#ddd',
          angle: 0
        });
        G.triangle({
          x: 0,
          y: (i + 1.5 + 7) * L,
          l: L,
          h: 5 * L,
          color: '#800',
          angle: 0
        });
      }
      for (let i = 0; i < 6; i += 2) {
        G.triangle({
          x: 11 * L,
          y: (i + 0.5) * L,
          l: L,
          h: 5 * L,
          color: '#800',
          angle: Math.PI
        });
        G.triangle({
          x: 11 * L,
          y: (i + 1.5) * L,
          l: L,
          h: 5 * L,
          color: '#ddd',
          angle: Math.PI
        });
        G.triangle({
          x: 11 * L,
          y: (i + 0.5 + 7) * L,
          l: L,
          h: 5 * L,
          color: '#800',
          angle: Math.PI
        });
        G.triangle({
          x: 11 * L,
          y: (i + 1.5 + 7) * L,
          l: L,
          h: 5 * L,
          color: '#ddd',
          angle: Math.PI
        });
      }

      G.rectangleSide({
        x: 0,
        y: 13 * L,
        lx: 5 * L,
        ly: L,
        color: `#900`,
        width: L / 20
      });
      G.rectangleSide({
        x: 6 * L,
        y: 13 * L,
        lx: 5 * L,
        ly: L,
        color: `#ccc`,
        width: L / 20
      });
    };

    renderBackground();
    renderGrid();
  }

  onDestroy() {
  }
}