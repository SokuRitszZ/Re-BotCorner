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

export const registerApi = (username, password, confirmedPassword) => {
  return new Promise((resolve, reject) => {
    API({
      url: '/account/register/',
      type: 'post',
      data: {
        username,
        password,
        confirmedPassword
      },
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const loginApi = (username, password) => {
  return new Promise((resolve, reject) => {
    API({
      url: '/account/token/',
      type: 'post',
      data: {
        username,
        password,
      },
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const getInfoApi = () => {
  return new Promise((resolve, reject) => {
    API({
      url: `/account/getInfo`,
      type: `get`,
      needJWT: true,
      async: false,
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const getAllGameApi = () => {
  return new Promise((resolve, reject) => {
    API({
      url: '/game/getAll',
      type: 'get',
      needJWT: false,
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const getAllLangApi = () => {
  return new Promise((resolve, reject) => {
    API({
      url: '/lang/getAll',
      type: 'get',
      async: false,
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const getRecordListApi = gameId => {
  return new Promise((resolve, reject) => {
    API({
      url: '/record/getListByGameId',
      type: 'get',
      data: {
        gameId
      },
      needJWT: true,
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const getBotApi = gameId => {
  return new Promise((resolve, reject) => {
    API({
      url: '/bot/getByGame',
      type: 'get',
      data: {
        gameId
      },
      needJWT: true,
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const getAllBotApi = () => {
  return new Promise((resolve, reject) => {
    API({
      url: `/bot/getAll`,
      type: 'get',
      needJWT: true,
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const addBotApi = (title, langId, gameId, description, code) => {
  return new Promise((resolve, reject) => {
    API({
      url: '/bot/add',
      type: 'post',
      needJWT: true,
      data: {
        title,
        langId,
        gameId,
        description,
        code
      },
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const deleteBotApi = id => {
  return new Promise((resolve, reject) => {
    API({
      url: '/bot/delete',
      type: 'delete',
      needJWT: true,
      data: { id },
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const updateBotApi = (id, info) => {
  return new Promise((resolve, reject) => {
    API({
      url: '/bot/update',
      type: 'post',
      needJWT: true,
      data: { id, ...info },
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};

export const getRatingApi = game => {
  return new Promise((resolve, reject) => {
    API({
      url: `/getrating/${game}`,
      type: 'get',
      async: false,
      success: resp => resolve(resp),
      error: err => reject(err)
    });
  });
};