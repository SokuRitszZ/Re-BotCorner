const timeFormat = (time) => {
  let year = time.getFullYear();
  let month = time.getMonth() + 1;
  let day = time.getDate();
  let hour = time.getHours();
  let minute = time.getMinutes();
  let second = time.getSeconds();
  const zeros = zeta => zeta < 10 ? `0${zeta}` : zeta.toString();
  return `${year}-${zeros(month)}-${zeros(day)} ${zeros(hour)}:${zeros(minute)}:${zeros(second)}`;
};

export default timeFormat;