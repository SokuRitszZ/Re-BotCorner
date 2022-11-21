import USER from './../store/USER';
import { mode, api_url } from '../config.json';
import axios from "axios";

const api = axios.create({
  baseURL: `${api_url[mode]}/api`
});

const headers = () => {
  const token = USER().getToken;
  if (token === null || token.length === 0) return {};
  return {
    Authorization: `Bearer ${USER().getToken}`
  }
};

api.interceptors.request.use(config => {
  const newHeaders = { ...config.headers, ...headers() };
  config.headers = newHeaders;
  return config;
}, error => {
  return Promise.reject(error);
});

api.interceptors.response.use(response => response.data, error => Promise.reject(error));

export default api;

export const registerApi = (username, password, confirmedPassword) => {
  return api.post("/account/register/", {
    username,
    password,
    confirmedPassword
  });
};

export const loginApi = (username, password) => {
  return api.post("/account/token/", {
    username, password
  });
};

export const getInfoApi = () => {
  return api.get("/account/getInfo", {
    headers: headers()
  }).catch(err => {});
};

export const getAllGameApi = () => {
  return api.get("/game/getAll");
};

export const getAllLangApi = () => {
  return api.get("/lang/getAll");
};

export const getRecordListApi = gameId => {
  return api({
    url: "/record/getListByGameId",
    type: "GET",
    params: {
      gameId
    },
    headers: headers()
  });
};

export const getBotApi = gameId => {
  return api({
    url: "/bot/getByGame",
    type: "GET",
    params: {
      gameId
    },
    headers: headers()
  });
};

export const getAllBotApi = () => {
  return api({
    url: "/bot/getAll",
    type: "GET",
    headers: headers()
  });
};

export const addBotApi = (title, langId, gameId, description, code) => {
  return api.post("/bot/add", {
    title, langId, gameId, description, code
  }, {
    headers: headers()
  });
};

export const deleteBotApi = id => {
  return api({
    url: "/bot/delete",
    method: "DELETE",
    headers: headers(),
    params: { id }
  });
};

export const updateBotApi = (id, info) => {
  return api.post("/bot/update", {
    id, ...info
  }, {
    headers: headers()
  });
};

export const getRatingApi = game => {
  return api({
    url: `/getrating/${game}`,
    method: "GET"
  });
};

export const updateHeadIconApi = data => {
  return api.post("/updateHeadIcon", data);
};

export const phoneAuthApi = phone => {
  return api.post("/account/phoneauth/", {phone});
};

export const phoneLoginApi = (phone, authCode) => {
  return api.post("/account/phonelogin/", { phone, authCode });
};

export const getGroupListApi = () => {
  return api({
    url: "/group/list",
    method: "GET"
  });
};

export const createGroupApi = data => {
  return api.post("/group/create/", data);
};

export const getGroupByIdApi = id => {
  return api({
    url: "/group/getById",
    method: "GET",
    params: { id }
  });
};

export const deleteGroupApi = id => {
  return api.post("/group/delete/", {id});
};

export const applyGroupApi = (groupId, application) => {
  return api.post("/group/apply/", {groupId, application});
};

export const getApplicationApi = () => {
  return api({
    url: "/group/application",
    method: "get"
  });
};


export const handleApplicationApi = (groupId, applicantId, state) => {
  return api.post("/group/handleApp/", {groupId, applicantId, state});
};

export const getMembers = groupId => {
  return api({
    url: "/group/members",
    method: "GET",
    params: { groupId }
  });
};

export const resignFromGroupApi = groupId => {
  return api.post("/group/resign/", {groupId});
};

export const createContestApi = ({title, groupId, gameId, rule, time}) => {
  return api.post("/contest/create/",{title, groupId, gameId, rule, time});
};

export const getContestsApi = groupId => {
  return api({
    url: "/contest/getAll",
    method: "GET",
    params: {groupId}
  });
};

export const removeContestApi = contestId => {
  return api.post("/contest/remove/", {contestId});
}