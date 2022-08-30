import USER from './../store/USER';
import $ from 'jquery';

const API = ({ url, type, data, async, success, error, needJWT }) => {
  let headers = {};
  if (needJWT) {
    headers = {
      Authorization: `Bearer ${USER().getToken}`
    }
  };
  if (async === undefined) async = false;
  $.ajax({
    url: `http://localhost:8080${url}`,
    type,
    headers,
    data,
    async,
    success,
    error: error || (resp => console.log(resp)) 
  });
};

export default API;