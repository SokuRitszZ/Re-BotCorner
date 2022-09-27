const alert = (type, content, remain) => {
  const removeAlert = (div) => {
    div.classList.add(`alert-out`);
    setTimeout(() => {
      div.remove();
    }, 450);
  };
  let div;
  if (div = document.querySelector(`.alert`)) {
    removeAlert(div);
  }
  div = document.createElement('div');
  div.classList.add('alert');
  div.classList.add(`alert-${type}`);
  div.style.position = `fixed`;
  div.innerText = content;
  document.querySelector(`body`).insertBefore(
    div, 
    document.querySelectorAll('body>*')[0]
  );
  div.addEventListener('click', () => {
    removeAlert(div);
  });
  setTimeout(() => {
    if (div) {
      removeAlert(div);
    }
  }, (remain || 1000) + 500);
};

export default alert;