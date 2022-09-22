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
};