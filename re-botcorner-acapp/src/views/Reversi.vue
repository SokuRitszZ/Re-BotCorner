<template>
  <div class="w-75 m-auto">
    <Row>
      <!-- 大版面 -->
      <Col col="col-8">
        <div ref="parentRef" id="parent"
             style="width: 100%; height: 50vh; background-color: antiquewhite; text-align: center;"
        >
          <canvas ref="canvasRef" tabindex="0"></canvas>
        </div>
        <Progress
          :max="maxi"
          :value="playi"
          animated
          class="mt-3"
        />
        <hr>
        <!-- 最近比赛 -->
        <ReversiRecordList
          ref="recordListRef"
          @play-record="playRecord"
        />
      </Col>
      <Col col="col-4" v-if="!hasLinkWebSocket">
        <button
          :disabled="hasClickedInitSocket"
          @click="initSocket"
          class="btn btn-primary w-100 rounded-0"
        >
          <span v-if="hasClickedInitSocket" class="spinner-grow spinner-grow-sm" role="status" aria-hidden="true"></span>
          连接WebSocket
        </button>
      </Col>
      <!-- 小版面 -->
      <Col col="col-4" v-if="hasLinkWebSocket">
        <MatchBoard
          ref="$matchBoard"
          :game-id="2"
          :bot-ids="botIds"
          :infos="infos"
        >
          <template v-slot:single-init>
            <input placeholder="白子BotID（非正数或者为空则为手动挡）" v-model="botIds[0]" type="number" class="form-control mb-2" />
            <input placeholder="黑子BotID（非正数或者为空则为手动挡）" v-model="botIds[1]" type="number" class="form-control mb-2" />
          </template>
          <template v-slot:matched>
            <div class="d-flex flex-row justify-content-around">
              <div :class="{active: $matchBoard.checkMatchOk(index)}" class="w-50 m-1 p-1 text-center" style="transition: 0.5s" v-for="(info, index) in infos">
                <img :style="{ border: `10px solid ${index === 0 ? '#222' : '#ccc'}` }" :src="info.headIcon" class="w-100">
                <div :class="{'text-decoration-underline': info.id === USER().getUserID}">
                  {{info.username}}
                  <span style="color: gray"> #{{info.id}} </span>
                </div>
              </div>
            </div>
          </template>
          <template v-slot:pending>
            <div
              style="background-color: gray; width: 100%; height: 300px; display: flex; flex-direction: column; justify-content: center;">
              <div class="text-center" v-if="$matchBoard.gameMode === 'multi'" style="font-size: 60px">
                <h1 v-if="$matchBoard.getMe() == 0" class="text-black">你是黑子</h1>
                <h1 v-if="$matchBoard.getMe() == 1" class="text-white">你是白子</h1>
                <hr>
              </div>
              <div>
                <div class="text-center w-50 text-black" style="display: inline-block; font-size: 50px">
                  {{ checker.cnt(0) }}
                </div>
                <div class="text-center w-50 text-white" style="display: inline-block; font-size: 50px">
                  {{ checker.cnt(1) }}
                </div>
              </div>
              <h3 style="text-align: center">当前回合为</h3>
              <div v-if="checker.curId === 0" class="black-chess" style="width: 50px; height: 50px; margin: auto">
                <div style="position: absolute; z-index: 1; border-radius: 50%; background-color: black; width: 50px; height: 50px" />
                <div style="position: absolute; transform: translateY(2.5px); border-radius: 50%; background-color: white; width: 50px; height: 50px" />
              </div>
              <div v-if="checker.curId === 1" class="white-chess" style="width: 50px; height: 50px; margin: auto">
                <div style="position: absolute; z-index: 1; border-radius: 50%; background-color: white; width: 50px; height: 50px" />
                <div style="position: absolute; transform: translateY(2.5px); border-radius: 50%; background-color: black; width: 50px; height: 50px" />
              </div>
            </div>
          </template>
          <template v-slot:game-over>
            <h2 class="text-center overflow-auto" style="clear: both; height: 100px; color: #222">
              <img v-if="$matchBoard.gameMode === 'multi'" class="float-start" width="100" :src="infos[0].headIcon" alt="">
              {{reasons[0]}}
            </h2>
            <hr>
            <h2 class="text-center overflow-auto" style="clear: both; height: 100px; color: #ccc">
              <img v-if="$matchBoard.gameMode === 'multi'" class="float-end" width="100" :src="infos[1].headIcon" alt="">
              {{reasons[1]}}
            </h2>
            <hr>
          </template>
        </MatchBoard>
        <hr>
        <!-- 交流窗口 -->
        <Window ref="chatroomWindowRef" @show="showChatRoomWindow" button-style="width: 100%; border-radius: 0" title="聊天窗口">
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
        <!-- 游戏说明 -->
        <Collapse button-style="width: 100%; border-radius: 0" collapse-id="game-info">
          <template v-slot:button>
            游戏说明
          </template>
          <template v-slot:content>
            <ReversiInfo />
          </template>
        </Collapse>
      </Col>
    </Row>
  </div>
</template>

<script setup>
import CardBody from '../components/CardBody.vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import Collapse from '../components/Collapse.vue';
import {nextTick, onMounted, onUnmounted, ref} from 'vue';
import SOCKET from '../store/SOCKET';
import ReversiGame from '../script/games/reversi/ReversiGame'
import alert from '../script/alert';
import { getBotApi, getRecordListApi } from '../script/api';
import timeFormat from '../script/timeFormat';
import ReversiInfo from './viewsChild/ReversiInfo.vue';
import Window from '../components/Window.vue';
import ChatRoom from '../components/ChatRoom.vue';
import USER from '../store/USER';
import randomId from '../script/randomid';
import ReversiRecordList from "./viewsChild/ReversiRecordList.vue";
import MatchBoard from "../components/MatchBoard.vue";
import Progress from "../components/Progress.vue";

const parentRef = ref(null);
const canvasRef = ref(null);

/** 匹配 */
const $matchBoard = ref();
const botIds = ref([null, null]);
const infos = ref([]);

/** 聊天窗口 */
const chatroomRef = ref(null);
const chatroomWindowRef = ref(null);
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

/** 游戏模式 */
const game = ref(null);
const checker = ref(null);
const reasons = ref([]);

/** 录像 */
const playingRecordId = ref(0);
const playi = ref(1);
const maxi = ref(1);

const playRecord = record => {
  playi.value = 0;
  clearTimeout(playingRecordId.value);
  const json = JSON.parse(record.json);
  const initData = json.initData;
  const steps = json.steps.trim();
  initGame("record", initData, () => {});
  const n = steps.length;
  maxi.value = n;
  const act = () => {
    const step = steps.slice(playi.value, playi.value + 2);
    playi.value += 2;
    if (step === "ps") {
      alert("warning", `${checker.value.curId === 0 ? "黑子" : "白子"}跳过`);
      checker.value.pass();
    } else {
      let rc = step.split("").map(c => parseInt(c));
      checker.value.putChess(...rc, checker.value.curId);
    }
    if (playi.value >= n) return ;
    playingRecordId.value = setTimeout(act, 750)
  }
  playingRecordId.value = setTimeout(act, 750);
};

const websocketRoute = json => {
  console.log(json.action);
  const wsRoutes = {
    startSingleGaming(json) {
      if (json.errorMessage) {
        $matchBoard.value.initState();
        alert("danger", json.errorMessage, 2000);
        return ;
      }
      botIds.value = json.botIds;
      const putChessCallback = (r, c) => {
        SOCKET().sendMessage({
          action: "setStep",
          step: {
            id: checker.value.curId,
            r, c
          }
        });
      };
      initGame("single", json.initData, putChessCallback);
      $matchBoard.value.receivedStartSingleGaming();
    },
    putChess(json) {
      checker.value.putChess(json.r, json.c, json.id);
    },
    pass(json) {
      alert(`warning`, `${json.passTo == 1 ? '黑子' : '白子'}跳过`);
      checker.value.pass();
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
      const putChessCallback = (r, c) => {
        SOCKET().sendMessage({
          action: "setStep",
          step: {
            id: $matchBoard.value.getMe(),
            r, c
          }
        });
      };
      initGame("multi", json.initData, putChessCallback);
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
  wsRoutes[json.action](json);
};

const initGame = (mode, initData, putChessCallback) => {
  parentRef.value.innerHTML = "";
  canvasRef.value = document.createElement("canvas");
  parentRef.value.append(canvasRef.value);
  game.value = new ReversiGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  game.value.start({
    initData,
    putChessCallback: putChessCallback
  });
  checker.value = game.value.getChecker();
};

const hasLinkWebSocket = ref(false);
const hasClickedInitSocket = ref(false);

const initSocket = () => {
  hasClickedInitSocket.value = true;
  const onOpen = () => {
    console.log(`open websocket.`);
    hasLinkWebSocket.value = true;
    hasClickedInitSocket.value = false;
  };

  const onClose = () => {
    console.log(`close websocket.`);
    hasLinkWebSocket.value = false;
  };

  const onMessage = message => {
    message = JSON.parse(message.data);
    if (message.result === "success" && message.data) {
      let data = JSON.parse(message.data);
      websocketRoute(data);
    } else if (message.result === "fail" && message.message) {
      alert("danger", message.message);
    }
  };

  const onError = error => {
    console.log(error);
    alert(`danger`, `无法连接到Websocket`);
    hasClickedInitSocket.value = false;
  };

  SOCKET().connect({
    game: "reversi",
    onOpen,
    onClose,
    onMessage,
    onError
  });
};

onMounted(() => {

  initSocket();

  game.value = new ReversiGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
});

onUnmounted(() => {
  SOCKET().disconnect();
});
</script>

<style scoped>
div::-webkit-scrollbar {
  display: none;
}

.all-ok-enter-active {
  animation: waitFadeIn 2s;
}

.all-ok-leave-active {
  animation: waitFadeOut 0.5s;
}

.active {
  color: white;
  background-color: green;
}

@keyframes waitFadeOut {
  from {
    overflow: hidden;
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
    overflow: hidden;
    display: none;
    opacity: 0;
    max-height: 0;
  }
  25% {
    display: none;
    opacity: 0;
    max-height: 0;
  }
  100% {
    display: block;
    opacity: 100%;
    max-height: 1000px;
  }
}
</style>