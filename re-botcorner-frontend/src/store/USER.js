import { defineStore } from 'pinia';
import alert from './../script/alert';
import API from './../script/api';
import router from './../routes/index';

const USER = defineStore(`USER`, {
  state: () => {
    return {
      isPulling: false,
      isLogined: false,
      token: null,
      userID: null,
      username: null,
      headIcon: null
    }
  },
  getters: {
    checkIsPulling() { return this.isPulling; },

    checkIsLogined() { return this.isLogined; },

    getToken() { return this.token; },

    getUserID() { return this.userID; },

    getUsername() { return this.username; },

    getHeadIcon() { return this.headIcon; }
  },
  actions: {
    changeIsPulling(value) { this.isPulling = value; },

    changeIsLogined(value) { this.isLogined = value; },

    setToken(token) { this.token = token; },
    
    setUserID(userID) { this.userID = userID; },

    setUsername(username) { this.username = username; },

    setHeadIcon(headIcon) { this.headIcon = headIcon; },

    loginByToken() {
      if (localStorage.getItem(`token`) == null) return ;
      this.setToken(localStorage.getItem('token'));
      this.changeIsPulling(true);
      API({
        url: `/account/getInfo`,
        type: `get`,
        needJWT: true,
        success: resp => {
          this.setUserID(resp.id);
          this.setUsername(resp.username);
          this.setHeadIcon(resp.headIcon);
          this.changeIsPulling(false);
          this.changeIsLogined(true);
        },
        error: resp => {
          this.changeIsPulling(false);
          console.log(resp);
        }
      });
    },
    
    loginByUP(username, password) {
      this.changeIsPulling(true);
      API({
        url: '/account/token/',
        type: 'post',
        data: {
          username,
          password,
        },
        success: resp => {
          if (resp.result === "success") {
            localStorage.setItem(`token`, resp.token);
            this.changeIsPulling(false);
            this.loginByToken();
            alert(`success`, `登陆成功`);
          } else {
            alert(`danger`, `登录失败：${resp.result}`);
            this.changeIsPulling(false);
          }
        },
        error: resp => {
          console.log(resp);
          this.changeIsPulling(false);
          alert(`danger`, `登录失败：用户名或密码错误`);
        }
      })
    },

    logout() {
      this.changeIsLogined(false);
      this.setToken(null);
      localStorage.setItem(`token`, null);
      alert(`warning`, `注销成功`);
      router.push({ name: `home` });
    }
  }
});

export default USER;