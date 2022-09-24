import USER from './../store/USER';
import $ from 'jquery';
import { mode, api_url } from '../config.json';

const API = ({ url, type, data, async, success, error, needJWT }) => {
  let headers = {};
  if (needJWT) {
    headers = {
      Authorization: `Bearer ${USER().getToken}`
    }
  };
  if (async === undefined) async = true;
  $.ajax({
    url: `${api_url[mode]}/api${url}`,
    type,
    headers,
    data,
    async,
    success,
    error: error || (resp => console.log(resp)) 
  });
};

export default API;