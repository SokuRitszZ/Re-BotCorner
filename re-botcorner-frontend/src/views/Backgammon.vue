<template>
  <CardBody>
    <Row>
      <!--大版面-->
      <Col col="col-8">
        <div
          ref="parentRef"
          id="parent"
          style="width: 100%; height: 50vh; background-color: antiquewhite; text-align: center;"
        >
          <canvas ref="canvasRef" tabindex="0"></canvas>
        </div>
        <hr>
        <BackgammonRecordList
          ref="recordListRef"
          @play-record="playRecord"
        />
      </Col>
      <!--小版面-->
      <Col v-if="!hasLinkWebSocket" col="col-4">
        <button
          @click="initSocket"
          :disabled="hasClickedInitSocket"
          class="btn btn-primary w-100 rounded-0"
        >
          <span v-if="hasClickedInitSocket" class="spinner-grow spinner-grow-sm" role="status" aria-hidden="true"></span>
          连接Websocket
        </button>
      </Col>
      <Col v-else col="col-4">
        <MatchBoard
          ref="$matchBoard"
          :game-id="3"
          :botIds="botIds"
          :infos="infos"
        >
          <template v-slot:single-init>
            <input placeholder="白子BotID（非正数或者为空则为手动挡）" v-model="botIds[0]" type="number" class="form-control mb-2" />
            <input placeholder="红子BotID（非正数或者为空则为手动挡）" v-model="botIds[1]" type="number" class="form-control mb-2" />
          </template>
          <template v-slot:matched>
            <div class="d-flex flex-row justify-content-around">
              <div :class="{active: $matchBoard.checkMatchOk(index)}" class="w-50 m-1 p-1 text-center" style="transition: 0.5s" v-for="(info, index) in infos">
                <img :style="{ border: `10px solid ${index === 0 ? '#ccc' : '#900'}` }" :src="info.headIcon" class="w-100">
                <div :class="{'text-decoration-underline': info.id === USER().getUserID}">
                  {{info.username}}
                  <span style="color: gray"> #{{info.id}} </span>
                </div>
              </div>
            </div>
          </template>
          <template v-slot:pending>
            <div
              style="background-color: antiquewhite; width: 100%; height: 200px;"
            >
              <h2 class="text-center" v-if="$matchBoard.gameMode === 'multi'">
                你是
              </h2>
              <div v-if="$matchBoard.gameMode === 'multi' && $matchBoard.getMe() === 0" style="background-color: #ccc; border-radius: 50%; width: 50px; height: 50px; margin: auto" />
              <div v-if="$matchBoard.gameMode === 'multi' && $matchBoard.getMe() === 1" style="background-color: #900; border-radius: 50%; width: 50px; height: 50px; margin: auto" />
              <h3 style="text-align: center">当前回合是</h3>
              <div v-if="checker.curId === 0" style="background-color: #ccc; border-radius: 50%; width: 50px; height: 50px; margin: auto" />
              <div v-if="checker.curId === 1" style="background-color: #900; border-radius: 50%; width: 50px; height: 50px; margin: auto" />
            </div>
          </template>
          <template v-slot:game-over>
            <h2 class="text-center" style="clear: both; height: 100px; color: #ccc">
              <img v-if="$matchBoard.gameMode === 'multi'" class="float-start" width="100" :src="infos[0].headIcon" alt="">
              {{reasons[0]}}
            </h2>
            <hr>
            <h2 class="text-center" style="clear: both; height: 100px; color: #900">
              <img v-if="$matchBoard.gameMode === 'multi'" class="float-end" width="100" :src="infos[1].headIcon" alt="">
              {{reasons[1]}}
            </h2>
            <hr>
          </template>
        </MatchBoard>
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
          other-style="height: 500px; overflow: scroll"
        >
          <template v-slot:button>游戏说明</template>
          <template v-slot:content>
            <BackgammonInfo />
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
import randomId from "../script/randomid.js";
import timeFormat from "../script/timeFormat.js";
import BackgammonInfo from "./viewsChild/BackgammonInfo.vue";
import BackgammonRecordList from "./viewsChild/BackgammonRecordList.vue";
import MatchBoard from "../components/MatchBoard.vue";

const $matchBoard = ref();

/** 连接Websocket */

const hasLinkWebSocket = ref(false);
const hasClickedInitSocket = ref(false);
const routes = ref({});

const initSocket = () => {
  hasClickedInitSocket.value = true;
  SOCKET().connect({
    game: "test/backgammon",
    onOpen() {
      hasLinkWebSocket.value = true;
      hasClickedInitSocket.value = false;
      initRoutes();
      console.log(`open websocket.`);
    },
    onClose() {
      hasLinkWebSocket.value = false;
      hasClickedInitSocket.value = false;
      console.log(`close websocket.`);
    },
    onMessage(message) {
      message = JSON.parse(message.data);
      if (message.result === "success" && message.data) {
        let data = JSON.parse(message.data);
        route(data);
      } else if (message.result === "fail" && message.message) {
        alert("danger", message.message);
      }
    },
    onError(error) {
      hasLinkWebSocket.value = false;
      hasClickedInitSocket.value = false;
      console.log(error);
    }
  });
};

const initRoutes = () => {
  const wsRoutes = {
    startSingleGaming(json) {
      botIds.value = json.botIds;
      initGame("single", json.initData)
      $matchBoard.value.receivedStartSingleGaming();
    },
    setDice(json) {
      checker.value.setDice(json.dice);
      checker.value.setCurId(json.curId);
    },
    moveChess(json) {
      const {from, to} = json;
      checker.value.moveChess(from, to);
    },
    pass(json) {
      alert(`warning`, `${json.passed === 0 ? '白子' : '红子'}无棋可走，跳过`, 3000);
    },
    tellResult(json) {
      $matchBoard.value.receivedTellResult();
      const result = json.result;
      alert("success", `游戏结束！${result}`, 1000);
      const _reasons = json.reason.split("\n");
      reasons.value = _reasons;
    },
    startMatching(json) {
      $matchBoard.value.receivedStartMatching();
    },
    cancelMatching(json) {
      $matchBoard.value.receivedCancelMatching();
    },
    successMatching(json) {
      infos.value = json.infos;
      infos.value.forEach(info => {
        const message = {
          id: randomId(),
          username: info.username,
          userId: info.id,
          time: timeFormat(new Date(), "yyyy-MM-dd HH:mm")
        };
        chatroomRef.value.addTalk("enter", message);
      });
      $matchBoard.value.receivedSuccessMatching(json);
    },
    matchOk(json) {
      $matchBoard.value.receivedMatchOk(json.id);
    },
    matchNot(json) {
      $matchBoard.value.receivedMatchNot(json.id);
    },
    exitMatching(json) {
      $matchBoard.value.receivedExitMatching(json);
      // 聊天窗口中通知
      infos.value.forEach(info => {
        const message = {
          id: randomId(),
          username: info.username,
          userId: info.id,
          time: timeFormat(new Date(), "yyyy-MM-dd HH:mm")
        };
        chatroomRef.value.addTalk("exit", message);
      });
      infos.value = [];
    },
    startMultiGaming(json) {
      $matchBoard.value.receivedStartMultiGaming();
      botIds.value = json.botIds;
      initGame("multi", json.initData, (from, to) => {
        SOCKET().sendMessage({
          action: "setStep",
          step: {
            id: $matchBoard.value.getMe(),
            from, to
          }
        })
      });
    },
    sendTalk(json) {
      let message = {
        id: randomId(),
        userId: json.userId,
        username: json.username,
        content: json.content,
        time: json.time
      };
      chatroomRef.value.addTalk(`msg`, message);
      if (chatroomWindowRef.value.getState() == 'close') {
        chatroomWindowRef.value.getBtn().classList.add(`jumping-btn`);
        hasNewMessage.value = true;
      }
    }
  };
  routes.value = wsRoutes;
};

const route = json => {
  const action = json.action;
  if (action === "tellResult") console.log(json);
  routes.value[action](json);
};

const linkWebSocket = () => {
  hasClickedInitSocket.value = true;
  initSocket();
};

/** 匹配 */
const infos = ref([]);
const botIds = ref([null, null]);

/** 游戏 */
const parentRef = ref(null);
const canvasRef = ref(null);
const game = ref(null);
const checker = ref(null);
const reasons = ref([]);

const initGame = (mode, initData, moveChessCallback) => {
  canvasRef.value.remove();
  canvasRef.value = document.createElement("canvas");
  parentRef.value.append(canvasRef.value);
  game.value = new BackgammonGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  const defaultMoveChessCallback = (from, to) => {
    SOCKET().sendMessage({
      action: "setStep",
      step: {
        id: `${checker.value.chess[from][0]}`,
        from, to
      }
    });
  }
  game.value.start({
    initData,
    moveChessCallback: moveChessCallback || defaultMoveChessCallback
  });
  checker.value = game.value.getChecker();
  const initDice = initData.initDice;
  checker.value.setDice(initDice);
  alert("info", `游戏开始！初始点数如下，${initData.initStart === 0 ? "白" : "红" }先手。\n五秒后开始`, 5000);
};

/** 录像 */

const playingRecordId = ref(0);

const playRecord = record => {
  clearTimeout(playingRecordId.value);
  const json = JSON.parse(record.json);
  const initData = json.initData;
  const steps = json.steps.trim().split("\n");
  initGame("record", initData, () => {});
  new Promise(resolve => {
    playingRecordId.value = setTimeout(() => {
      // 告知开局的时候要等个5秒
      const startStep = steps[0];
      const curId = parseInt(startStep[2]);
      checker.value.setCurId(curId);
      const dice = startStep.slice(3).split("").map(x => parseInt(x));
      checker.value.setDice(dice);
      resolve();
    }, 5000);
  }).then(() => {
    const n = steps.length;
    let i = 1;
    const act = () =>{
      const step = steps[i++];
      const action = step.slice(0, 2);
      switch (action) {
        case "mv":
          const [from, to] = [
            parseInt(step[2], 36),
            parseInt(step[3], 36)
          ];
          checker.value.moveChess(from, to);
          break;
        case "ps":
          alert("warning", `${checker.value.curId === 0 ? "白" : "红"}无棋可走，跳过`);
          const ps = parseInt(step[2]);
          checker.value.setCurId(ps);
          break;
        case "tn":
          const tn = parseInt(step[2]);
          checker.value.setCurId(tn);
          break;
      }
      const curId = parseInt(steps[i][2]);
      const dice = steps[i++].slice(3).split("").map(die => parseInt(die));
      checker.value.setCurId(curId);
      checker.value.setDice(dice);
      if (i < n) {
        playingRecordId.value = setTimeout(() => {
          act();
        }, 750);
      }
    };
    act();
  });
};

/** 聊天室 */
const chatroomWindowRef = ref(null);
const chatroomRef = ref(null);
const hasNewMessage = ref(false);

const sendTalk = content => {
  SOCKET().sendMessage({
    action: 'sendTalk',
    content
  });
};

const showChatRoomWindow = () => {
  hasNewMessage.value = false;
  chatroomWindowRef.value.getBtn().classList.remove('jumping-btn');
};

const isLeft = message => {
  return message.userId != USER().getUserID;
};

const isRight = message => {
  return message.userId == USER().getUserID;
};

/** 选择模式 */

onMounted(() => {
  game.value = new BackgammonGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  initSocket();
});

onUnmounted(() => {
  SOCKET().disconnect();
});
</script>

<style scoped>
.active {
  color: white;
  background-color: green;
}

.all-ok-enter-active {
  animation: waitFadeIn 3s;
}

.all-ok-leave-active {
  animation: waitFadeOut 0.5s;
}

@keyframes waitFadeOut {
  from {
    opacity: 1;
    max-height: 1000px;
  }
  to {
    opacity: 0;
    max-height: 0;
  }
}

@keyframes waitFadeIn {
  0% {
    display: none;
    opacity: 0;
    max-height: 0;
  }
  16.7% {
    display: none;
    opacity: 0;
    max-height: 0;
  }
  30% {
    opacity: 1;
  }
  100% {
    display: block;
    max-height: 1000px;
  }
}
</style>
