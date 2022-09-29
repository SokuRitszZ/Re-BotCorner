export default class C {
  static {
    C.prototype.EPS = () => 1e-2;
  }

  static EPS() {
    return 1e-2;
  }

  static distance(object1, object2) {
    const dx = object1.x - object2.x;
    const dy = object1.y - object2.y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  static dx(object1, object2) {
    return Math.abs(object1.x - object2.x);
  }

  static dy(object1, object2) {
    return Math.abs(object1.y - object2.y);
  }

  static rotate(x, y, angle) {
    const l = Math.sqrt(x * x + y * y);
    const alpha = Math.asin(y / l);
    return {
      x: l * Math.cos(alpha + angle),
      y: l * Math.sin(alpha + angle)
    };
  }
};