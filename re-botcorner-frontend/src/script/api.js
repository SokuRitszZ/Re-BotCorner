import USER from './../store/USER';
import { mode, api_url } from '../config.json';
import axios from "axios";

const api = axios.create({
  baseURL: `${api_url[mode]}/api`
});

export default api;

const headers = () => {
  return {
    Authorization: `Bearer ${USER().getToken}`
  }
};

const callback = resp => new Promise(resolve => resolve(resp.data));

export const registerApi = (username, password, confirmedPassword) => {
  return api.post("/account/register/", {
    username,
    password,
    confirmedPassword
  }).then(callback);
};

export const loginApi = (username, password) => {
  return api.post("/account/token/", {
    username, password
  }).then(callback);
};

export const getInfoApi = () => {
  return api.get("/account/getInfo", {
    headers: headers()
  }).then(callback);
};

export const getAllGameApi = () => {
  return api.get("/game/getAll").then(callback);
};

export const getAllLangApi = () => {
  return api.get("/lang/getAll").then(callback);
};

export const getRecordListApi = gameId => {
  return api({
    url: "/record/getListByGameId",
    type: "GET",
    params: {
      gameId
    },
    headers: headers()
  }).then(callback);
};

export const getBotApi = gameId => {
  return api({
    url: "/bot/getByGame",
    type: "GET",
    params: {
      gameId
    },
    headers: headers()
  }).then(callback);
};

export const getAllBotApi = () => {
  return api({
    url: "/bot/getAll",
    type: "GET",
    headers: headers()
  }).then(callback);
};

export const addBotApi = (title, langId, gameId, description, code) => {
  return api.post("/bot/add", {
    title, langId, gameId, description, code
  }, {
    headers: headers()
  }).then(callback);
};

export const deleteBotApi = id => {
  return api({
    url: "/bot/delete",
    method: "DELETE",
    headers: headers(),
    params: { id }
  }).then(callback);
};

export const updateBotApi = (id, info) => {
  return api.post("/bot/update", {
    id, ...info
  }, {
    headers: headers()
  }).then(callback);
};

export const getRatingApi = game => {
  return api({
    url: `/getrating/${game}`,
    method: "GET"
  }).then(callback);
};

export const updateHeadIconApi = data => {
  return api.post("/updateHeadIcon", data, {
    "Content-type": false,
    "Process-data": false,
    headers: headers()
  }).then(callback);
};