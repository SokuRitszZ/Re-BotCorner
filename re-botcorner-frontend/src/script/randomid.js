const randomId = () => {
  return "xxxxxxx-xxxxxxxxxxxxx-xxx-xxxxxxxxxxxxxxxxx".replace(/[x]/g, c => {
    return Math.floor(Math.random() * 16).toString(16);
  });
};

export default randomId;