<template>
  <CardBody>
    <Row>
      <Col col="col-8">
        <div ref="parentRef" id="parent">
          <canvas ref="canvasRef" tabindex="0"></canvas>
        </div>
        <hr>
        <SnakeRecordList
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
      <Col col="col-4" v-if="hasLinkWebSocket">
        <MatchBoard
          ref="$matchBoard"
          :game-id="1"
          :bot-ids="botIds"
          :infos="infos"
        >
          <template v-slot:single-init>
            <input placeholder="蓝蛇BotID（非正数或者为空则为手动挡）" v-model="botIds[0]" type="number" class="form-control mb-2" />
            <input placeholder="红蛇BotID（非正数或者为空则为手动挡）" v-model="botIds[1]" type="number" class="form-control mb-2" />
          </template>
          <template v-slot:matched>
            <div class="d-flex flex-row justify-content-around">
              <div :class="{active: $matchBoard.checkMatchOk(index)}" class="w-50 m-1 p-1 text-center" style="transition: 0.5s" v-for="(info, index) in infos">
                <img :style="{border: `10px solid ${index === 0 ? '#0D6EFD' : '#DC3545'}`}" :src="info.headIcon" class="w-100">
                <div :class="{'text-decoration-underline': info.id === USER().getUserID}">
                  {{info.username}}
                  <span style="color: gray"> #{{info.id}} </span>
                </div>
              </div>
            </div>
          </template>
          <template v-slot:pending>
            <div class="text-md-center" style="font-size: 2.5rem; height: 120px;">
              <span v-if="$matchBoard.getMe() === 0 && $matchBoard.gameMode === 'multi'">·</span>
              <span class="text-primary"> {{ inputOk[0] ? "已就绪" : "未就绪" }} </span>
              <DirectionKeyBoard
                :disabled="inputOk[0] || botIds[0] !== 0 || ($matchBoard.gameMode === 'multi' && $matchBoard.getMe() !== 0)"
                id="0"
                :class-name="{'btn-outline-primary': true}"
                @submit="setStep"
              />
            </div>
            <hr>
            <div class="text-md-center" style="font-size: 2.5rem; height: 120px;">
              <span v-if="$matchBoard.getMe() === 1 && gameMode === 'multi'">·</span> <span class="text-danger"> {{ inputOk[1] ? "已就绪" : "未就绪" }} </span>
              <DirectionKeyBoard
                :disabled="inputOk[1] || botIds[1] !== 0 || ($matchBoard.gameMode === 'multi' && $matchBoard.getMe() !== 1)"
                id="1"
                :class-name="{'btn-outline-danger': true}"
                @submit="setStep"
              />
            </div>
          </template>
          <template v-slot:game-over>
            <div class="w-100">
              <h4 class="text-center text-primary">
                {{reasons[0]}}
              </h4>
              <hr>
              <h4 class="text-center text-danger">
                {{reasons[1]}}
              </h4>
              <hr>
            </div>
          </template>
        </MatchBoard>
        <hr>
        <!-- 聊天窗口 -->
        <Window button-style="width: 100%; border-radius: 0" title="聊天窗口">
          <template v-slot:button>
            聊天窗口
          </template>
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
        <!-- 游戏介绍 -->
        <Collapse button-style="width: 100%; border-radius: 0" collapse-id="shoukai">
          <template v-slot:button>
            游戏介绍
          </template>
          <template v-slot:content>
            <SnakeInfo />
          </template>
        </Collapse>
      </Col>
    </Row>
  </CardBody>
</template>

<script setup>
import CardBody from '../components/CardBody.vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import Collapse from '../components/Collapse.vue';
import { onMounted, onUnmounted, ref } from 'vue';
import SOCKET from '../store/SOCKET';
import SnakeGame from '../script/games/snake/SnakeGame';
import Container from '../components/Container.vue';
import USER from '../store/USER';
import alert from '../script/alert';
import { getBotApi } from '../script/api';
import randomId from '../script/randomid';
import SnakeInfo from './viewsChild/SnakeInfo.vue';
import Window from '../components/Window.vue';
import ChatRoom from '../components/ChatRoom.vue';
import timeFormat from '../script/timeFormat';
import SnakeRecordList from "./viewsChild/SnakeRecordList.vue";
import DirectionKeyBoard from "../components/DirectionKeyBoard.vue";
import MatchBoard from "../components/MatchBoard.vue";

const parentRef = ref(null);
const canvasRef = ref(null);
const chatroomRef = ref(null);
const $matchBoard = ref();

const botIds = ref([null, null]);

const game = ref(null);
const checker = ref(null);
const state = ref("toMatch");
const gameMode = ref("");

const choose = ref([0, 0]);
const inputOk = ref([false, false]);
const reasons = ref([]);

/**
 * 初始化游戏（正在创建游戏）
 *
 * @param mode
 * @param initData
 */
const initGame = (mode, initData) => {
  canvasRef.value.remove();
  canvasRef.value = document.createElement("canvas");
  parentRef.value.append(canvasRef.value);
  game.value = new SnakeGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  game.value.start({ initData });
  state.value = "waitingInput";
  gameMode.value = mode;
  checker.value = game.value.getChecker();
};

const setStep = (id, direction) => {
  SOCKET().sendMessage({
    action: "setStep",
    step: {
      id: parseInt(id),
      direction
    }
  });
};

const playingRecordId = ref(0);

const playRecord = record => {
  clearInterval(playingRecordId.value);
  const recordJson = JSON.parse(record.json);
  game.value = new SnakeGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  const initData = recordJson.initData;
  game.value.start({initData});
  checker.value = game.value.getChecker();
  const steps = recordJson.steps;
  const n = steps.length;
  let i = 0;
  const act = () => {
    checker.value.moveSnake({
      id: 0,
      direction: parseInt(steps[i++])
    });
    checker.value.moveSnake({
      id: 1,
      direction: parseInt(steps[i++])
    });
    if (i >= n) {
      const result = record.result;
      alert(`success`, `游戏结束`);
      if (result === "平局" || result === "红蛇胜利") game.value.snakes[0].setStatus("die");
      if (result === "平局" || result === "蓝蛇胜利") game.value.snakes[1].setStatus("die");
      return ;
    }
    playingRecordId.value = setTimeout(() => {
      act()
    }, 250);
  };
  act();
};

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

const hasClickedInitSocket = ref(false);
const hasLinkWebSocket = ref(false);

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
    }
  };
  const onError = error => {
    console.log(error);
    alert(`danger`, `无法连接到Websocket`);
    hasClickedInitSocket.value = false;
  };
  SOCKET().connect({
    game: `test/snake`,
    onOpen,
    onClose,
    onMessage,
    onError
  });
};

const recordListRef = ref();

const infos = ref([]);

const websocketRoute = json => {
  const wsRoutes = {
    startSingleGaming(json) {
      botIds.value = json.botIds;
      initGame("single", json.initData);
      $matchBoard.value.receivedStartSingleGaming();
    },
    setStep(json) {
      const step = json.step;
      const id = step.id;
      inputOk.value[id] = true;
    },
    moveSnake(json) {
      const directions = json.directions;
      for (let i = 0; i < 2; ++i) {
        checker.value.moveSnake({
          id: i,
          direction: directions[i]
        });
      }
      inputOk.value[0] = false;
      inputOk.value[1] = false;
    },
    startMatching(json) {
      $matchBoard.value.receivedStartMatching();
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
    cancelMatching(json) {
      $matchBoard.value.receivedCancelMatching();
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
      initGame("multi", json.initData);
    },
    tellResult(json) {
      $matchBoard.value.receivedTellResult();
      const result = json.result;
      alert("success", `游戏结束！${result}`, 1000);
      const _reasons = json.reason.split("\n");
      reasons.value = _reasons;
      if (reasons.value[0] !== "获胜") game.value.snakes[0].setStatus("die");
      if (reasons.value[1] !== "获胜") game.value.snakes[1].setStatus("die");
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
    },
  };
  wsRoutes[json.action](json);
};

onMounted(() => {
  initSocket();
  game.value = new SnakeGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
});

onUnmounted(() => {
  SOCKET().disconnect();
});

</script>

<style scoped>
.btn {
  border-radius: 0;
}

#parent {
  border: 1px solid #dedede;
  width: 100%;
  height: 50vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

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

div::-webkit-scrollbar {
  display: none;
}
</style>