import C from "./C.js";

export default class G {
  static setContext(context) {
    G.context = context;
  }

  static getContext() {
    return G.context;
  }

  static rectangle({ x, y, lx, ly, color }) {
    const context = G.getContext();
    context.fillStyle = color;
    context.fillRect(y, x, ly, lx);
  }

  static circle({ x, y, radius, color }) {
    const context = G.getContext();
    context.fillStyle = color;
    context.beginPath();
    context.arc(y, x, radius, 0, Math.PI * 2);
    context.fill();
  }

  static segment({ x0, y0, x1, y1, width, color }) {
    const context = G.getContext();
    // const context = document.createElement('canvas').getContext('2d');
    context.lineWidth = width;
    context.strokeStyle = color;
    context.beginPath();
    context.moveTo(y0, x0);
    context.lineTo(y1, x1);
    context.closePath();
    context.stroke();
  }

  static triangle({ x, y, l, h, color, angle }) {
    const context = G.getContext();
    [x, y] = [y, x];
    const [x0, y0] = [0, -l / 2];
    const [x1, y1] = [0, l / 2];
    const [x2, y2] = [h, 0];
    const p0 = C.rotate(x0, y0, angle);
    const p1 = C.rotate(x1, y1, angle);
    const p2 = C.rotate(x2, y2, angle);
    context.beginPath();
    context.moveTo(x + p0.y, y + p0.x);
    context.lineTo(x + p1.y, y + p1.x);
    context.lineTo(x + p2.y, y + p2.x);
    context.closePath();
    context.fillStyle = color;
    context.fill();
  }

  static rectangleSide({ x, y, lx, ly, color, width }) {
    const context = G.getContext();
    let [nx, ny] = [x + lx, y + ly];
    [x, y] = [y, x];
    [nx, ny] = [ny, nx];
    context.beginPath();
    context.moveTo(x, y);
    context.lineTo(x, ny);
    context.lineTo(nx, ny);
    context.lineTo(nx, y);
    context.closePath();
    context.strokeStyle = color;
    context.lineWidth = width;
    context.stroke();
  }
};