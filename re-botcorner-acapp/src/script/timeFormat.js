const timeFormat = (time, fmt) => {
  if (fmt === undefined) fmt = "yyyy-MM-dd HH:mm:ss";
  let year = time.getFullYear();
  let month = time.getMonth() + 1;
  let day = time.getDate();
  let hour = time.getHours();
  let minute = time.getMinutes();
  let second = time.getSeconds();
  const zeros = zeta => zeta < 10 ? `0${zeta}` : zeta.toString();
  
  fmt = fmt.replace(`yyyy`, year).replace(`MM`, zeros(month)).replace(`dd`, zeros(day)).replace(`HH`, zeros(hour)).replace(`mm`, zeros(minute)).replace(`ss`, zeros(second));
  return fmt;
  // return `${year}-${zeros(month)}-${zeros(day)} ${zeros(hour)}:${zeros(minute)}:${zeros(second)}`;
};

export default timeFormat;