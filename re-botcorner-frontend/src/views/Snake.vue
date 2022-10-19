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
        <button :disabled="hasClickedInitSocket" @click="initSocket" class="btn btn-primary" style="border-radius: 0; width: 100%;">
          <span v-if="hasClickedInitSocket" class="spinner-grow spinner-grow-sm" role="status" aria-hidden="true"></span>
          连接WebSocket
        </button>
      </Col>
      <Col col="col-4" v-if="hasLinkWebSocket">
        <!-- 匹配信息 / 局面信息 -->
        <Collapse button-style="width: 100%; border-radius: 0" collapse-id="info">
          <template v-slot:button>
            <template v-if="state === 'toMatch' || state === 'matching' || state === 'matched'">
              匹配信息
            </template>
            <template v-else>
              局面信息
            </template>
          </template>
          <template v-slot:content>
            <!-- 游戏尚未开始 -->
            <template v-if="state === 'toMatch' || state === 'matching' || state === 'matched'">
              <Container>
                <Row>
                  <Col>
                    <input type="radio" class="btn-check" v-model="isSingleMode" :value="true" id="singleMode"
                           autocomplete="off" checked>
                    <label @click="chooseSingleMode" style="width: 100%" class="btn btn-outline-secondary" for="singleMode">
                      单人练习
                    </label>
                  </Col>
                  <Col>
                    <input type="radio" class="btn-check" v-model="isSingleMode" :value=false id="multiMode"
                           autocomplete="off">
                    <label @click="chooseMultiMode" style="width: 100%" class="btn btn-outline-secondary" for="multiMode">
                      寻找对手
                    </label>
                  </Col>
                </Row>
              </Container>
              <hr>
              <template v-if="isSingleMode">
                <!-- 选择单人模式 -->
                <transition name="all-ok">
                  <div v-if="gameState === 'init'" style="width: 100%; text-align: center;">
                    <input v-model="botIds[0]" type="number" class="form-control mb-2" placeholder="选择蓝方的BotID（不填则为亲自出马）" :disabled="hasStartSingleGaming">
                    <input v-model="botIds[1]" type="number" class="form-control mb-2" placeholder="选择红方的BotID（不填则为亲自出马）" :disabled="hasStartSingleGaming">
                    <button @click="startSingleGaming" style="width: 100%; border-radius: 0;"
                            class="btn btn-success" :disabled="gameState === 'starting'">游戏开始</button>
                  </div>
                  <div v-else class="text-center">
                    <div class="m-auto spinner-border" style="width: 3rem; height: 3rem;" role="status"></div>
                    <div>等待Bot编译完成...</div>
                  </div>
                </transition>
              </template>
              <template v-else>
                <select class="form-select mb-2" v-model="myBotId" :disabled="state !== 'toMatch'">
                  <option :value="0" selected>亲自出马</option>
                  <option v-for="bot in myBotList" :value="bot.id">{{ bot.title }}#{{ bot.id }}</option>
                </select>
                <!-- 选择多人模式 -->
                <template v-if="state === 'toMatch'">
                  <!-- 未开始匹配 -->
                  <button :disabled="hasClickMatching"  @click="startMatching" class="btn btn-warning">开始匹配</button>
                </template>
                <!-- 正在匹配 -->
                <template v-else-if="state === 'matching'">
                  <div class="btn btn-danger" @click="cancelMatching">取消匹配</div>
                  <div>
                    <hr>
                    <div class="text-center">
                      <span class="spinner-border spinner-border-sm" role="status"></span> 寻找对手中...
                    </div>
                  </div>
                </template>
                <template v-else-if="state === 'matched'">
                  <!-- 匹配成功 -->
                  <transition name="all-ok">
                    <div v-if="!allOk()" class="d-flex flex-row justify-content-around">
                      <div :class="{active: matchIsOk[index]}" class="w-50 m-1 p-1 text-center" style="transition: 0.5s" v-for="(info, index) in infos">
                        <img :style="{
                          border: `10px solid ${index === 0 ? 'blue' : 'red'}`
                        }" :src="info.headIcon" class="w-100">
                        <div :class="{'text-decoration-underline': info.id === USER().getUserID}">
                          {{info.username}}
                          <span style="color: gray"> #{{info.id}} </span>
                        </div>
                      </div>
                    </div>
                    <div v-else class="text-center">
                      <div class="m-auto spinner-border" style="width: 3rem; height: 3rem;" role="status"></div>
                      <div>等待Bot编译完成...</div>
                    </div>
                  </transition>
                  <hr>
                  <button
                    @click="toggleMatch"
                    :disabled="allOk()"
                    :class="{'btn-outline-success': !matchIsOk[getMe()], 'btn-success': matchIsOk[getMe()]}" class="btn w-100 rounded-0"
                  >准备</button>
                  <button
                    @click="exitMatching"
                    :disabled="allOk()"
                    class="btn btn-danger w-100 rounded-0 mt-3"
                  >退出</button>
                </template>
              </template>
            </template>
            <template v-else>
              <Container>
                <Row>
                  <!-- 0号输入口 -->
                  <!-- 游戏还没结束并且还没准备好输出 -->
                  <template v-if="state !== 'gameOver'">
                    <!-- 播放录像的时候 显示决策 -->
                    <template v-if="gameMode === 'record'">
                      <h4 style="text-align: center; color: blue">
                        {{ ["↑", "→", " ↓", "←", "开始"][lastStep0] }}
                      </h4>
                    </template>
                    <!-- 不选择机器人 且 单人模式/多人模式下是自己的时候 显示键盘 -->
                    <div v-else class="text-md-center" style="font-size: 2.5rem">
                      <span v-if="getMe() === 0 && gameMode === 'multi'">·</span> <span class="text-primary"> {{ inputOk[0] ? "已就绪" : "未就绪" }} </span>
                      <DirectionKeyBoard
                        :disabled="inputOk[0] || botIds[0] !== 0 || (gameMode === 'multi' && getMe() !== 0)"
                        id="0"
                        :class-name="{'btn-outline-primary': true}"
                        @submit="setStep"
                      />
                    </div>
                    <!-- 选择了机器人之后 || 不是自己的时候 显示是否已经就绪 -->
                  </template>
                  <!-- 游戏还没结束 但输出已经准备好了 -->
                  <template v-else-if="state !== 'gameOver'">
                    <h4 style="text-align: center; color: blue">
                      <span v-if="getMe() === 0 && gameMode === 'multi'">·</span> <span>{{ inputOk[0] ? "已就绪" : "未就绪" }}</span>
                    </h4>
                  </template>
                  <!-- 游戏结束 -->
                  <template v-else>
                    <h4 class="text-center" style="color: blue">
                      {{reasons[0]}}
                    </h4>
                  </template>
                </Row>
              </Container>
              <hr>
              <Container>
                <Row>
                  <!-- 1号输入口 -->
                  <!-- 游戏还没结束并且还没准备好输出 -->
                  <template v-if="state !== 'gameOver'">
                    <!-- 播放录像的时候 显示决策 -->
                    <template v-if="gameMode === 'record'">
                      <h4 style="text-align: center; color: red">
                        {{ ["↑", "→", " ↓", "←", "开始"][lastStep1] }}
                      </h4>
                    </template>
                    <!-- 不选择机器人 且 单人模式/多人模式下是自己的时候 显示键盘 -->
                    <div v-else class="text-md-center" style="font-size: 2.5rem">
                      <span v-if="getMe() === 1 && gameMode === 'multi'">·</span> <span class="text-danger"> {{ inputOk[1] ? "已就绪" : "未就绪" }} </span>
                      <DirectionKeyBoard
                        :disabled="inputOk[1] || botIds[1] !== 0 || (gameMode === 'multi' && getMe() !== 1)"
                        id="1"
                        :class-name="{'btn-outline-danger': true}"
                        @submit="setStep"
                      />
                    </div>
                  </template>
                  <!-- 游戏还没结束 但输出已经准备好了 -->
                  <template v-else-if="state !== 'gameOver'">
                    <h4 style="text-align: center; color: red">
                      <span v-if="getMe() === 1 && gameMode === 'multi'">·</span> <span>{{ inputOk[1] ? "已就绪" : "未就绪" }}</span>
                    </h4>
                  </template>
                  <!-- 游戏结束 -->
                  <template v-else>
                    <h4 class="text-center" style="color: red">
                      {{reasons[1]}}
                    </h4>
                  </template>
                </Row>
              </Container>
              <template v-if="state === 'gameOver'">
                <hr>
                <button @click="remake" class="btn btn-secondary w-100">继续</button>
              </template>
            </template>
          </template>
        </Collapse>
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

const parentRef = ref(null);
const canvasRef = ref(null);
const chatroomRef = ref(null);

const botIds = ref([ null, null ]);
const hasStartSingleGaming = ref(false);

const game = ref(null);
const checker = ref(null);
const isSingleMode = ref(true);
const state = ref("toMatch");
const gameMode = ref("");

const myBotList = ref([]);
const myBotId = ref(0);
const hasClickMatching = ref(false);

/**
 * 初始化游戏状态
 */
const initGameState = () => {
  game.value = null;
  checker.value = null;
  isSingleMode.value = true;
  state.value = 'toMatch';
  gameState.value = "init";
  gameMode.value = '';

  myBotId.value = 0;
  infos.value = [];
  botIds.value = [0, 0];

  inputOk.value = [false, false];
  matchIsOk.value = [false, false];
};

/**
 * 选择单人模式
 */
const chooseSingleMode = () => {
  if (state.value === "matching")  cancelMatching();
  else if (state.value === "matched") exitMatching();
  isSingleMode.value = true;
};

/**
 * 选择多人模式
 */
const chooseMultiMode = () => {
  isSingleMode.value = false;
};

const choose = ref([0, 0]);
const inputOk = ref([false, false]);
const reasons = ref([]);
/**
 * 游戏状态
 * init 还没开始
 * starting 正在创建
 * pending 正在进行
 * end 结束
 *
 * @type {Ref<UnwrapRef<string>>}
 */
const gameState = ref("init");

/**
 * 初始化游戏（正在创建游戏）
 *
 * @param mode
 * @param initData
 */
const initGame = (mode, initData) => {
  game.value = new SnakeGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  game.value.start({ initData });
  state.value = "waitingInput";
  gameMode.value = mode;
  checker.value = game.value.getChecker();
};

const startSingleGaming = () => {
  gameState.value = "starting";
  myBotId.value = 0;
  let botIds0 = 0;
  let botIds1 = 0;
  if (botIds.value[0] !== undefined || botIds.value[0] !== null)
    botIds0 = Math.max(botIds.value[0], 0);
  if (botIds.value[1] !== undefined || botIds.value[1] !== null)
    botIds1 = Math.max(botIds.value[1], 0);
  botIds.value = [ botIds0, botIds1 ];
  SOCKET().sendMessage({
    action: 'startSingleGaming',
    botIds: [ botIds0, botIds1 ]
  });
};

const startMatching = () => {
  if (hasClickMatching.value) return ;
  hasClickMatching.value = true;
  SOCKET().sendMessage({
    action: 'startMatching',
    botId: myBotId.value
  }).then(() => {}, error => {
    hasClickMatching.value = false;
  });
};

const cancelMatching = () => {
  SOCKET().sendMessage({
    action: 'cancelMatching'
  });
  hasClickMatching.value = false;
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
  initGameState();
  const recordJson = JSON.parse(record.json);
  game.value = new SnakeGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  const initData = recordJson.initData;
  game.value.start({initData});
  checker.value = game.value.getChecker();
  const steps = recordJson.steps;
  let ptr = 0;
  playingRecordId.value = setInterval(() => {
    if (ptr >= steps.length) {
      alert(`success`, `游戏结束`);
      clearInterval(playingRecordId.value);
      return ;
    }
    checker.value.moveSnake({
      id: 0,
      direction: parseInt(steps[ptr++])
    });
    checker.value.moveSnake({
      id: 1,
      direction: parseInt(steps[ptr++])
    });
  }, 250);
};

const toggleMatch = () => {
  console.log(getMe());
  if (matchIsOk.value[getMe()]) matchNot();
  else matchOk();
};

const matchOk = () => {
  SOCKET().sendMessage({
    action: 'matchOk'
  });
};

const allOk = () => {
  for (let ok of matchIsOk.value) if (!ok) return false;
  return true;
};

const matchNot = () => {
  if (allOk()) return ;
  SOCKET().sendMessage({
    action: 'matchNot'
  });
};

const exitMatching = () => {
  SOCKET().sendMessage({
    action: 'exitMatching'
  });
  hasClickMatching.value = false;
};

const remake = () => {
  initGameState();
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
  initGameState();
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

const getMe = () => {
  for (let index in infos.value) {
    const info = infos.value[index];
    if (info.id === USER().getUserID) return parseInt(index);
  }
  return -1;
};

const matchIsOk = ref([]);

const websocketRoute = json => {
  const wsRoutes = {
    startSingleGaming(json) {
      hasStartSingleGaming.value = false;
      gameState.value = "pending";
      botIds.value = json.botIds;
      gameMode.value = "single";
      initGame("single", json.initData);
    },
    setStep(json) {
      const step = json.step;
      const id = step.id;
      inputOk.value[id] = true;
    },
    moveSnake(json) {
      const state = json.state;
      const directions = json.directions;
      const isIncreasing = json.isIncreasing;
      for (let i = 0; i < 2; ++i) {
        checker.value.moveSnake({
          id: i,
          direction: directions[i]
        });
      }
      inputOk.value[0] = false;
      inputOk.value[1] = false;
      if (gameMode.value === "record") {
        lastStep0.value = direction0;
        lastStep1.value = direction1;
      }
    },
    startMatching(json) {
      hasClickMatching.value = false;
      state.value = 'matching';
    },
    successMatching(json) {
      infos.value = json.infos;
      state.value = 'matched';
      alert(`success`, `成功匹配!`);
      infos.value.forEach(info => {
        const message = {
          id: randomId(),
          username: info.username,
          userId: info.id,
          time: timeFormat(new Date(), "yyyy-MM-dd HH:mm")
        };
        chatroomRef.value.addTalk("enter", message);
      });
      matchIsOk.value = new Array(infos.value.length);
      matchIsOk.value.fill(false, 0);
    },
    cancelMatching(json) {
      state.value = 'toMatch';
    },
    matchOk(json) {
      matchIsOk.value[json.id] = true;
    },
    matchNot(json) {
      matchIsOk.value[json.id] = false;
    },
    exitMatching(json) {
      const myId = getMe();
      const id = json.id;
      // 如果是自己退出的那就是回到toMatch状态
      if (myId === id) state.value = "toMatch";
      // 否则回到matching状态
      else state.value = "matching";
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
      // 清除信息
      infos.value = [];
      // 通知
      alert("info", "有人退出了匹配");
    },
    startMultiGaming(json) {
      state.value = "waitingInput";
      botIds.value = json.botIds;
      gameMode.value = "multi";
      gameState.value = "pending";
      initGame("multi", json.initData);
    },
    tellResult(json) {
      state.value = "gameOver";
      const result = json.result;
      alert("success", `游戏结束！${result}`, 1000);
      const _reasons = json.reason.split("\n");
      reasons.value = _reasons;
      gameState.value = "gameOver";
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

const initBotList = () => {
  getBotApi(1).then(list => {
    myBotList.value = list;
    myBotList.value.map(bot => {
      bot.createTime = new Date(bot.createTime);
      bot.modifyTime = new Date(bot.modifyTime);
    });
  });
};

onMounted(() => {
  initSocket();
  initBotList();
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
  animation: waitFadeIn 2s;
}

.all-ok-leave-active {
  animation: waitFadeOut 0.5s;
}

@keyframes waitFadeOut {
  from {
    overflow: hidden;
    opacity: 1;
    max-height: 250px;
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
    max-height: 250px;
  }
}

div::-webkit-scrollbar {
  display: none;
}
</style>