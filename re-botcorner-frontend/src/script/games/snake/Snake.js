import C from "../../C";
import G from "../../G";
import GameObject from "../../GameObject";
import Cell from './Cell';

export default class Snake extends GameObject {
  static {
    /** 0: U, 1: R, 2: D, 3: L */
    Snake.prototype.dx = [-1, 0, 1, 0];
    Snake.prototype.dy = [0, 1, 0, -1];
    Snake.prototype.edx = [
      [-1, -1],
      [-1, 1],
      [1, 1],
      [1, -1]
    ];
    Snake.prototype.edy = [
      [-1, 1],
      [1, 1],
      [1, -1],
      [-1, -1]
    ];
  }

  constructor(parent, { id, userId }) {
    super(parent);
    
    this.id = id;
    this.userId = userId;

    this.cells = [new Cell(
      id === 0 ? this.parent.rows - 2 : 1,
      id === 0 ? 1 : this.parent.cols - 2
    )];
    this.color = id === 0 ? 'blue' : 'red';
    this.eyeDirection = id === 0 ? 0 : 2;
    this.speed = 5;
    this.status = 'idle';
    this.nextCell = null;
    this.isIncreasing = false;
  }

  nextStep({ isIncreasing, d, status }) {
    if (this.status === 'die') return ;
    this.nextCell = new Cell(this.cells[0].r + this.dx[d], this.cells[0].c + this.dy[d]);
    this.eyeDirection = d;
    this.status = status;
    this.isIncreasing = isIncreasing;
    
    const n = this.cells.length;
    for (let i = n; i; --i) {
      this.cells[i] = new Cell(this.cells[i - 1].r, this.cells[i - 1].c);
    }
  }

  setStatus(status) {
    this.status = status;
  }

  onStart() { } 

  update() { 
    const updateMove = () => {
      if (this.nextCell === null) return ;
      const dx = this.nextCell.x - this.cells[0].x;
      const dy = this.nextCell.y - this.cells[0].y;
      const distance = C.distance(this.nextCell, this.cells[0]);
      if (distance < C.EPS()) {
        this.cells[0] = this.nextCell;
        this.nextCell = null;
        if (this.status !== 'die') this.status = 'idle';
        if (!this.isIncreasing) {
          this.cells.pop();
        }
      } else {
        const moveDistance = this.speed * this.timedelta / 1000;
        this.cells[0].x += moveDistance * dx / distance;
        this.cells[0].y += moveDistance * dy / distance;
        if (!this.isIncreasing) {
          const n = this.cells.length;
          const tail = this.cells[n - 1];
          const tailTarget = this.cells[n - 2];
          const dx = tailTarget.x - tail.x;
          const dy = tailTarget.y - tail.y;
          tail.x += moveDistance * dx / distance;
          tail.y += moveDistance * dy / distance;
        }
      }
    };

    updateMove();
    this.render();
  }

  render() { 
    const renderCells = () => {
      if (this.status === 'die') this.color = '#ffffff';
      const L = this.parent.L;
      for (const cell of this.cells) {
        G.circle({
          x: cell.x * L,
          y: cell.y * L,
          radius: L / 2 * 0.8,
          color: this.color
        });
      }
    };

    const renderBody = () => {
      const L  = this.parent.L;
      const n = this.cells.length;
      const EPS = C.EPS();
      for (let i = 1; i < n; ++i) {
        const a = this.cells[i - 1], b = this.cells[i];
        let dx = C.dx(a, b);
        let dy = C.dy(a, b);
        if (dx < EPS && dy < EPS) continue;
        if (dx < EPS) {
          G.rectangle({
            x: (a.x - 0.4) * L,
            y: Math.min(a.y, b.y) * L,
            lx: L * 0.8,
            ly: dy * L,
            color: this.color
          });
        } else {
          G.rectangle({
            x: Math.min(a.x, b.x) * L,
            y: (a.y - 0.4) * L,
            lx: dx * L,
            ly: L * 0.8,
            color: this.color
          });
        }
      }
    };

    const renderEye = () => {
      const d = this.eyeDirection;
      const L = this.parent.L;
      const x0 = this.cells[0].x + 0.1 * this.edx[d][0], y0 = this.cells[0].y + 0.1 * this.edy[d][0];
      const x1 = this.cells[0].x + 0.1 * this.edx[d][1], y1 = this.cells[0].y + 0.1 * this.edy[d][1];
      G.circle({
        x: x0 * L,
        y: y0 * L,
        radius: L * 0.05,
        color: this.status !== 'die' ? '#ffffff' : '#000000'
      });
      G.circle({
        x: x1 * L,
        y: y1 * L,
        radius: L * 0.05,
        color: this.status !== 'die' ? '#ffffff' : '#000000'
      });
    };
    
    renderCells();
    renderBody();
    renderEye();
  }

  onDestroy() { }

};