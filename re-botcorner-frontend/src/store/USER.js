import { defineStore } from 'pinia';
import alert from './../script/alert';
import { getInfoApi, loginApi } from './../script/api';
import router from './../routes/index';
import MYBOTS from "./MYBOTS.js";

const USER = defineStore(`USER`, {
  state: () => {
    return {
      isPulling: false,
      isLogined: false,
      token: null,
      userID: null,
      username: null,
      headIcon: null,
      promiseLogin: null
    }
  },
  getters: {
    checkIsPulling() { return this.isPulling; },

    checkIsLogined() { return this.isLogined; },

    getToken() { return this.token; },

    getUserID() { return this.userID; },

    getUsername() { return this.username; },

    getHeadIcon() { return this.headIcon; },

    getPromiseLogin() { return this.promiseLogin; }
  },
  actions: {
    changeIsPulling(value) { this.isPulling = value; },

    changeIsLogined(value) { this.isLogined = value; },

    setToken(token) { this.token = token; },
    
    setUserID(userID) { this.userID = userID; },

    setUsername(username) { this.username = username; },

    setHeadIcon(headIcon) { this.headIcon = headIcon; },

    loginByToken() {
      if (this.promiseLogin !== null) return this.promiseLogin;
      const token = localStorage.getItem("token");
      if (token === null || token.length === 0) return Promise.reject();
      this.setToken(token);
      this.changeIsPulling(true);
      return this.promiseLogin = getInfoApi()
        .then(info => {
          this.setUserID(parseInt(info.id));
          this.setUsername(info.username);
          this.setHeadIcon(info.headIcon);
          this.changeIsPulling(false);
          this.changeIsLogined(true);
          return Promise.resolve(this.userID);
        })
        .catch(err => {
          this.changeIsPulling(false);
          return Promise.reject();
        });
    },

    loginByUP(username, password) {
      this.changeIsPulling(true);
      this.promiseLogin = null;
      loginApi(username, password)
        .then(resp => {
          if (resp.result === "success") {
            localStorage.setItem("token", resp.token);
            this.loginByToken();
            alert("success", `登录成功`);
          } else {
            alert("danger", `登录失败：${resp.result}`);
          }
          this.changeIsPulling(false);
        })
        .catch(err => {
          this.changeIsPulling(false);
          alert("danger", `登录失败：用户名或密码错误`);
        });
    },

    logout() {
      this.changeIsLogined(false);
      this.setToken("");

      this.promiseLogin = null;
      MYBOTS().promiseOk = null;

      localStorage.setItem(`token`, "");
      alert(`warning`, `注销成功`);
      router.push({ name: `home` });
    }
  }
});

export default USER;