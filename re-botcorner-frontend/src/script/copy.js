const copy = (object) => {
  return JSON.parse(JSON.stringify(object));
};

export default copy;