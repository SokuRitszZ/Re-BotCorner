import { defineStore } from 'pinia';
import alert from './../script/alert';
import API, { getInfoApi } from './../script/api';

const USER = defineStore(`USER`, {
  state: () => {
    return {
      isPulling: false,
      isLogined: false,
      token: null,
      userID: null,
      username: null,
      headIcon: null,
      AcWingOS: "AcWingOS" 
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
      this.changeIsPulling(true);
      API({
        url: '/account/getInfo',
        type: 'get',
        needJWT: true,
        async: false,
        success: info => {
          this.setUserID(info.id);
          this.setUsername(info.username);
          this.setHeadIcon(info.headIcon);
          this.changeIsPulling(false);
          this.changeIsLogined(true);
        },
        error: err => {
          this.changeIsPulling(false);
        }
      });
    },

    logout() {
      this.changeIsLogined(false);
      this.setToken(null);
      alert(`warning`, `注销成功`);
    }
  }
});

export default USER;