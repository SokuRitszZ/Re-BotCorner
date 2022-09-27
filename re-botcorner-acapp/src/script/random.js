import { LoremIpsum } from "lorem-ipsum";

const app = new LoremIpsum();

const randomInteger = (l, r) => {
  const len = r - l;
  return l + Math.floor(Math.random() * len);
}

const randomWord = () => {
  return app.generateWords(1);
}

const randomSentence = () => {
  return app.generateSentences(1);
}

const randomParagraph = () => {
  return app.generateParagraphs(1);
} 

const randomBots = (num) => {
  let res = [];
  for (let i = 0; i < num; ++i) {
    res.push({
      id: randomInteger(1, num),
      title: randomWord(),
      userId: 5,
      description: randomParagraph(),
      code: randomParagraph() + '\n' + randomParagraph(),
      rating: randomInteger(1000, 2000),
      gameId: randomInteger(1, 3),
      langId: randomInteger(1, 3),
      createTime: new Date(),
      modifyTime: new Date(),
      isShow: false
    });
  }
  return res;
};

export {
  randomInteger,
  randomWord,
  randomSentence,
  randomParagraph,
  randomBots
};