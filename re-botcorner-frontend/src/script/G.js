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
};