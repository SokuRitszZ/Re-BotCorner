<template>
  <div class="w-75 m-auto">
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
        <div class="row mt-3" style="height: 30px">
          <div class="col-9 d-flex flex-column justify-content-center">
            <Progress
              :animated="!isPausing && isPlayingRecord"
              :max="maxi"
              :value="playi"
              @change="rollTo"
              @mousedown="onRolling"
              @mouseup="rollOk"
            />
            <div style="font-size: 10px">
              {{playi}} / {{maxi}}
            </div>
          </div>
          <div class="col d-flex flex-column justify-content-center">
            <select v-model="speedPlayRecord" class="form-control h-100" style="padding: 0 0 0 7px">
              <option :value="0.5">x0.5</option>
              <option :value="1">x1</option>
              <option :value="2">x2</option>
              <option :value="4">x4</option>
            </select>
          </div>
          <div class="col">
            <div class="btn-group" role="group" aria-label="Basic example">
              <button @click="act(false, true)" :disabled="!isPausing" class="btn btn-primary p-0 w-100 h-100" style="font-size: 1rem">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="currentColor" class="bi bi-reply-fill" viewBox="0 0 16 16">
                  <path d="M5.921 11.9 1.353 8.62a.719.719 0 0 1 0-1.238L5.921 4.1A.716.716 0 0 1 7 4.719V6c1.5 0 6 0 7 8-2.5-4.5-7-4-7-4v1.281c0 .56-.606.898-1.079.62z"/>
                </svg>
              </button>
              <button @click="act(true)" v-if="isPausing" class="btn btn-primary p-0 w-100 h-100" style="font-size: 1rem">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="currentColor" class="bi bi-play-fill" viewBox="0 0 16 16">
                  <path d="m11.596 8.697-6.363 3.692c-.54.313-1.233-.066-1.233-.697V4.308c0-.63.692-1.01 1.233-.696l6.363 3.692a.802.802 0 0 1 0 1.393z"/>
                </svg>
              </button>
              <button @click="pause" v-else class="btn btn-primary p-0 w-100 h-100" style="font-size: 1rem">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="currentColor" class="bi bi-pause-fill" viewBox="0 0 16 16">
                  <path d="M5.5 3.5A1.5 1.5 0 0 1 7 5v6a1.5 1.5 0 0 1-3 0V5a1.5 1.5 0 0 1 1.5-1.5zm5 0A1.5 1.5 0 0 1 12 5v6a1.5 1.5 0 0 1-3 0V5a1.5 1.5 0 0 1 1.5-1.5z"/>
                </svg>
              </button>
              <button :disabled="!isPausing" @click="act(false)" class="btn btn-primary p-0 w-100 h-100" style="font-size: 1rem">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="currentColor" class="bi bi-forward-fill" viewBox="0 0 16 16">
                  <path d="m9.77 12.11 4.012-2.953a.647.647 0 0 0 0-1.114L9.771 5.09a.644.644 0 0 0-.971.557V6.65H2v3.9h6.8v1.003c0 .505.545.808.97.557z"/>
                </svg>
              </button>
            </div>
          </div>
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
  </div>
</template>

<script setup>

import CardBody from "../components/CardBody.vue";
import Row from "../components/Row.vue";
import Col from "../components/Col.vue";
import Collapse from "../components/Collapse.vue";
import Window from "../components/Window.vue";
import BackgammonGame from "../script/games/backgammon/BackgammonGame.js";
import {nextTick, onMounted, onUnmounted, ref} from "vue";
import alert, {removeAlert} from "../script/alert.js";
import SOCKET from "../store/SOCKET.js";
import ChatRoom from "../components/ChatRoom.vue";
import USER from "../store/USER.js";
import randomId from "../script/randomid.js";
import timeFormat from "../script/timeFormat.js";
import BackgammonInfo from "./viewsChild/BackgammonInfo.vue";
import BackgammonRecordList from "./viewsChild/BackgammonRecordList.vue";
import MatchBoard from "../components/MatchBoard.vue";
import Progress from "../components/Progress.vue";

const $matchBoard = ref();

/** 连接Websocket */

const hasLinkWebSocket = ref(false);
const hasClickedInitSocket = ref(false);
const routes = ref({});

const initSocket = () => {
  hasClickedInitSocket.value = true;
  SOCKET().connect({
    game: "backgammon",
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
      if (json.errorMessage) {
        $matchBoard.value.initState();
        alert("danger", json.errorMessage, 2000);
        return ;
      }
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

const clearRecord = () => {
  // 清除正在播放的录像
  clearTimeout(playingRecordId.value);
  playi.value = 1;
  maxi.value = 1;
  isPlayingRecord.value = false;
};

const initGame = (mode, initData, moveChessCallback) => {
  if (mode !== "record") clearRecord();

  parentRef.value.innerHTML = "";
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
  alert("info", `游戏开始！初始点数如下，${initData.initStart === 0 ? "白" : "红" }先手。五秒后开始`, 5000);
};

/** 录像 */

const maxi = ref(1);
const playi = ref(1);
const backPlayi = ref(1);
const playingRecordId = ref(0);
const speedPlayRecord = ref(1);
const isPausing = ref(false);
const steps = ref("");
const isPlayingRecord = ref(false);
const recordPlaying = ref(null);
const indexToRoll = ref(0);

const pause = () => {
  if (!isPlayingRecord.value) return ;
  isPausing.value = true;
  clearTimeout(playingRecordId.value);
};

const playRecord = record => {
  recordPlaying.value = record;
  isPlayingRecord.value = true;
  isPausing.value = false;
  playi.value = 0;
  clearTimeout(playingRecordId.value);
  const json = JSON.parse(record.json);
  const initData = json.initData;
  const jsonSteps = json.steps.trim().split("\n");
  const n = jsonSteps.length;
  steps.value = json.steps.trim().split("\n");
  initGame("record", initData, () => {});
  const startStep = steps.value[0];
  const newSteps = [];
  for (let i = 1; i < n; i += 2) newSteps.push([jsonSteps[i], jsonSteps[i + 1]]);
  steps.value = newSteps;
  maxi.value = newSteps.length;
  new Promise(resolve => {
    playingRecordId.value = setTimeout(() => {
      // 告知开局的时候要等个5秒
      const curId = parseInt(startStep[2]);
      checker.value.setCurId(curId);
      const dice = startStep.slice(3).split("").map(x => parseInt(x));
      checker.value.setDice(dice);
      resolve();
    }, 5000);
  }).then(() => {
    return new Promise(resolve => {
      setTimeout(() => {
        resolve();
      }, 1000);
    })
  }).then(() => {
    act(true);
  });
};

const act = (isContinue, isRev=false) => {
  if (!isPlayingRecord.value) return ;
  isPausing.value = !isContinue;
  if (isRev) playi.value -= 1;
  let step = steps.value[playi.value];
  let stepDc = step[1];
  step = step[0];
  const action = step.slice(0, 2);
  switch (action) {
    case "mv":
      const [from, to] = [
        parseInt(step[2], 36),
        parseInt(step[3], 36)
      ];
      if (!isRev) checker.value.moveChess(from, to);
      else checker.value.rollback();
      break;
    case "ps":
      const ps = parseInt(step[2]);
      alert("warning", `${ps === 1 ? "白" : "红"}无棋可走，跳过`);
      if (!isRev) checker.value.setCurId(ps);
      else checker.value.setCurId(ps ^ 1);
      break;
    case "tn":
      const tn = parseInt(step[2]);
      if (!isRev) checker.value.setCurId(tn);
      else checker.value.setCurId(tn ^ 1);
      break;
  }
  if (isRev) stepDc = steps.value[playi.value - 1][1];
  const dice = stepDc.slice(3).split("").map(die => parseInt(die));
  const curId = parseInt(stepDc[2]);
  checker.value.setCurId(curId);
  checker.value.setDice(dice);
  if (!isRev) ++playi.value;
  if (playi.value < maxi.value) {
    if (isContinue)
      playingRecordId.value = setTimeout(() => {
        act(true);
      }, 750 / speedPlayRecord.value);
  } else {
    isPausing.value = true;
    isPlayingRecord.value = false;
  }
};

const onRolling = () => {
  backPlayi.value = playi.value;
  pause();
};

const rollOk = () => {
  if (!recordPlaying.value) return ;
  clearTimeout(playingRecordId.value);
  const backupSpeed = speedPlayRecord.value;
  speedPlayRecord.value = 10000;
  const playiFrom = backPlayi.value;
  const n = playi.value;
  playi.value = backPlayi.value;
  if (playiFrom < n)
    for (let i = playiFrom; i < n; ++i)
      act(false);
  else for (let i = playiFrom; i > n; --i)
    act(false, true);
  removeAlert();
  speedPlayRecord.value = backupSpeed;
  playingRecordId.value = setTimeout(() => {
    act(true);
  }, 1000);
};

const rollTo = index => {
  indexToRoll.value = index;
  nextTick(() => {
    playi.value = index
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
