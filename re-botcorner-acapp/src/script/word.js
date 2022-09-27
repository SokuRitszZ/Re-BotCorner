const word = (theWord) => {
  let result = '';
  result += theWord[0].toUpperCase();
  result += theWord.slice(1);
  return theWord;
};

export default word;