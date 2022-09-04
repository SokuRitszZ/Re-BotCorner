import { defineStore } from "pinia";
import USER from "./USER";

const SOCKET = defineStore('SOCKET', {
  state: () => {
    return {
      socket: null
    }
  },
  actions: {
    connect({ game, onOpen, onClose, onMessage, onError }) {
      const URL = `ws://localhost:8080/websocket/${game}`;
      const token = USER().getToken;
      this.socket = new WebSocket(`${URL}/${token}`);
      this.socket.onopen = onOpen;
      this.socket.onclose = onClose;
      this.socket.onmessage = onMessage;
      this.socket.onerror = onError;
    },
    disconnect() {
      this.socket.close();
    },
    sendMessage(messageObject) {
      this.socket.send(JSON.stringify(messageObject));
    }
  }
});

export default SOCKET;