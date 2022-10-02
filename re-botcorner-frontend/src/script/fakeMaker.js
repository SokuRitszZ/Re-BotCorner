import {LoremIpsum} from "lorem-ipsum";
import timeFormat from "./timeFormat.js";

const randomHex = () => {
  let result = '';
  for (let i = 0; i < 6; ++i) { result += Math.floor(Math.random() * 16).toString(16); }
  return result;
};

const fakeMaker = (obj, count) => {
  const list = [];
  function Fake(obj) {
    const result = {};
    for (let key in obj) {
      const value = obj[key];
      switch (value.type) {
        case 'number': result[key] = Math.floor(Math.random() * value.max); break;
        case 'word': result[key] = new LoremIpsum().generateWords(1); break;
        case 'paragraph': result[key] = new LoremIpsum().generateParagraphs(1); break;
        case 'time': result[key] = timeFormat(new Date(Math.floor(2e12 * Math.random()))); break;
        case 'custom': result[key] = value.values[Math.floor(value.values.length * Math.random())]; break;
        case 'image': result[key] = `https://sdfsdf.dev/500x500.png,${randomHex()},${randomHex()}`; break;
      }
    }
    return result;
  };
  for (let i = 0; i < count; ++i) { list.push(Fake(obj)); }
  return list;
};

export default fakeMaker;