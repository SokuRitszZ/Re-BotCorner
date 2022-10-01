<template>
  <CardBody>
    <Row>
      <Col col="col-8">
        <div
          ref="parentRef"
          id="parent"
          style="width: 100%; height: 50vh; background-color: antiquewhite; text-align: center;"
        >
          <canvas ref="canvasRef" tabindex="0"></canvas>
        </div>
        <hr>
        <Collapse
          collapse-id="record-list"
          button-style="width: 100%; border-radius: 0"
        >
          <template v-slot:button>
            最近比赛
          </template>
          <template v-slot:content>
            <h1>最近比赛</h1>
          </template>
        </Collapse>
      </Col>
      <Col v-if="!hasLinkWebSocket" col="col-4">
        <button
          @click="linkWebSocket"
          :disabled="hasClickedInitSocket"
          class="btn btn-primary"
          style="width: 100%; border-radius: 0;"
        >连接Websocket</button>
      </Col>
      <Col v-else col="col-4">
        <Collapse
          button-style="width: 100%; border-radius: 0"
          collapse-id="match-situation"
        >
          <template v-slot:button>
            匹配信息 / 局面信息
          </template>
          <template v-slot:content>
            <Row>
              <Col>
                <input type="radio" class="btn-check" autocomplete="off" :checked="gameMode === 'single'">
                <label @click="chooseGameMode('single')" style="width: 100%; border-radius: 0" class="btn btn-outline-secondary"
                       for="gameMode">
                  单人练习
                </label>
              </Col>
              <Col>
                <input type="radio" class="btn-check" autocomplete="off" :checked="gameMode === 'multi'">
                <label @click="chooseGameMode('multi')" style="width: 100%; border-radius: 0" class="btn btn-outline-secondary"
                       for="gameMode">
                  寻找对手
                </label>
              </Col>
            </Row>
            <hr>
<!--            单人模式 -->
            <template v-if="gameMode === 'single'">
              <input :disabled="isWaiting" v-model="singleBotId0" type="number" class="form-control mb-2" />
              <input :disabled="isWaiting" v-model="singleBotId1" type="number" class="form-control mb-2" />
              <button :disabled="isWaiting" @click="startSingleGaming" class="btn btn-success" style="width: 100%; border-radius: 0">开始游戏</button>
              <div v-if="isWaiting" style="width: 100%; text-align: center;">
                <span class="spinner-border spinner-border-sm text-warning" role="status" aria-hidden="true"></span>
                <span class="text-warning" style="font-size: small">等待Bot编译完成...</span>
              </div>
            </template>
<!--            多人模式 -->
            <template v-else-if="gameMode === 'multi'">
              <select :disabled="state !== 'toMatch'" class="form-control mb-3">
                <option :value="-1" selected>亲自出马</option>
              </select>
<!--              准备匹配-->
              <template v-if="state === 'toMatch'">
                <button @click="startMatching" class="btn btn-warning" style="border-radius: 0; width: 100%">开始匹配</button>
              </template>
<!--              正在匹配-->
              <template v-else-if="state === 'matching'">
                <button @click="cancelMatching" class="btn btn-danger" style="width: 100%; border-radius: 0">取消匹配</button>
                  <hr>
                  <div class="text-center">
                    <span class="spinner-border spinner-border-sm" role="status"></span> 寻找对手中...
                  </div>
              </template>
<!--              匹配成功-->
              <template v-else>
                <Row>
                  <Col col="col-4">
                    <img src="https://sdfsdf.dev/300x300.png" style="width: 100%">
                    <hr>
                    <div style="text-align: center">username</div>
                    <div style="text-align: center; color: gray">#userid</div>
                  </Col>
                  <Col col="col-8">
                    <button @click="matchOk" :disabled="allOk()" v-show="!iOk" class="btn btn-success mb-3" style="width: 100%; border-radius: 0;">确认</button>
                    <button @click="matchNot" :disabled="allOk()" v-show="iOk" class="btn btn-secondary mb-3" style="width: 100%; border-radius: 0;">取消</button>
                    <button :disabled="allOk()" class="btn btn-danger" style="width: 100%; border-radius: 0">退出</button>
                    <div v-show="uOk" style="color: green; text-align: center; font-size: 10px">对手已准备就绪</div>
                    <div v-show="allOk()" style="width: 100%; text-align: center;">
                      <span class="spinner-border spinner-border-sm text-warning" role="status" aria-hidden="true"></span>
                      <span class="text-warning" style="font-size: small">等待Bot编译完成...</span>
                    </div>
                  </Col>
                </Row>
              </template>
            </template>
          </template>
        </Collapse>
        <hr>
        <Window
          ref="chatroomWindowRef"
          @show="showChatRoomWindow"
          button-style="width: 100%; border-radius: 0%"
          title="聊天窗口"
        >
          <template v-slot:button>聊天窗口</template>
          <template v-slot:body>
            <ChatRoom
              ref="chatroomRef"
              :sendTalk="sendTalk"
              :isLeft="isLeft"
              :isRight="isRight"
            />
          </template>
        </Window>
        <hr>
        <Collapse
          collapse-id="game-info"
          button-style="width: 100%; border-radius: 0"
        >
          <template v-slot:button>游戏说明</template>
          <template v-slot:content>
            游戏说明
          </template>
        </Collapse>
      </Col>
    </Row>
  </CardBody>
</template>

<script setup>

import CardBody from "../components/CardBody.vue";
import Row from "../components/Row.vue";
import Col from "../components/Col.vue";
import Collapse from "../components/Collapse.vue";
import Window from "../components/Window.vue";
import BackgammonGame from "../script/games/backgammon/BackgammonGame.js";
import {onMounted, onUnmounted, ref} from "vue";
import alert from "../script/alert.js";
import SOCKET from "../store/SOCKET.js";
import ChatRoom from "../components/ChatRoom.vue";
import USER from "../store/USER.js";

/** 连接Websocket */

const hasLinkWebSocket = ref(false);
const hasClickedInitSocket = ref(false);

const initSocket = () => {
  SOCKET().connect({
    game: 'backgammon',
    onOpen() {
      hasLinkWebSocket.value = true;
      hasClickedInitSocket.value = false;
      console.log(`open websocket.`);
    },
    onClose() {
      hasLinkWebSocket.value = false;
      hasClickedInitSocket.value = false;
      console.log(`close websocket.`);
    },
    onMessage(message) {
      console.log(message);
    },
    onError(error) {
      hasLinkWebSocket.value = false;
      hasClickedInitSocket.value = false;
      console.log(error);
    }
  });
};

const linkWebSocket = () => {
  hasClickedInitSocket.value = true;
  initSocket();
};

/** 聊天室 */
const chatroomRef = ref(null);

const sendTalk = content => {
  SOCKET().sendMessage({
    action: 'sendTalk',
    content
  });
};

const isLeft = message => {
  return message.userId != USER().getUserID;
};

const isRight = message => {
  return message.userId == USER().getUserID;
};

/** 选择模式 */
const gameMode = ref('multi');
const state = ref('toMatch');

const chooseGameMode = mode => {
  gameMode.value = mode;
};

/** 单人模式 */

const singleBotId0 = ref(0);
const singleBotId1 = ref(0);
const isWaiting = ref(false);

const startSingleGaming = () => {
  SOCKET().sendMessage({
    action: 'startSingleGaming',
    botId0: singleBotId0.value,
    botId1: singleBotId1.value
  });
  // isWaiting.value = true;
  // console.log(singleBotId0.value, singleBotId1.value);
};

/** 多人模式 */

const iOk = ref(false);
const uOk = ref(false);

const startMatching = () => {
  state.value = 'matching';
  setTimeout(() => {
    if (state.value === 'matching') {
      successMatching();
    }
  }, 3000);
};

const cancelMatching = () => {
  state.value = 'toMatch';
};

const successMatching = () => {
  alert(`success`, `匹配成功`);
  state.value = 'matched';
};

const allOk = () => {
  return iOk.value && uOk.value;
};

const matchOk = () => {
  iOk.value = true;
  uOk.value = true;
};

const matchNot = () => {
  iOk.value = false;
};

const exitMatching = () => {
  state.value = 'toMatch';
};

/* 游戏 */

const parentRef = ref(null);
const canvasRef = ref(null);
const game = ref(null);
const checker = ref(null);

const initGame = ({ mode, stringifiedChess }) => {
  game.value = new BackgammonGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  game.value.start({
    mode: 'single',
    stringifiedChess,
    moveChessCallback: (indexFrom, indexTo) => {
      checker.value.moveChess(indexFrom, indexTo);
    }
  });
  checker.value = game.value.getChecker();
};

onMounted(() => {
  initSocket();
  ;;; initGame({
    mode: 'single',
    stringifiedChess: `0 5 2 0 2 0 2 0 2 0 1 5 2 0 2 0 1 3 2 0 2 0 2 0 0 5 1 5 2 0 2 0 2 0 0 3 2 0 2 0 0 5 2 0 2 0 2 0 2 0 1 2`
  });
});

onUnmounted(() => {
  SOCKET().disconnect();
});
</script>