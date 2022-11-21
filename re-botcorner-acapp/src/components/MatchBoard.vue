<template>
  <Collapse
    button-style="width: 100%; border-radius: 0"
    other-style="overflow: hidden"
    collapse-id="match-situation"
  >
    <template v-slot:button>
      匹配信息 / 局面信息
    </template>
    <template v-slot:content>
      <transition name="all-ok">
        <!--1、创建对局（又分单人、双人模式），2、等待编译，4、等待匹配，5、局面（单人、多人、录像），6、结束-->
        <div v-if="gameState === 'init'">
          <Row>
            <Col>
              <input type="radio" class="btn-check" autocomplete="off" :checked="gameMode === 'single'">
              <label @click="setGameMode('single')" class="btn btn-outline-secondary w-100 rounded-0"
                     for="gameMode">
                单人练习
              </label>
            </Col>
            <Col>
              <input type="radio" class="btn-check" autocomplete="off" :checked="gameMode === 'multi'">
              <label @click="setGameMode('multi')" class="btn btn-outline-secondary w-100 rounded-0"
                     for="gameMode">
                寻找对手
              </label>
            </Col>
          </Row>
          <hr>
          <div v-if="gameMode === 'single'">
            <div class="w-100 text-center" v-if="gameState === 'init'">
              <slot name="single-init"></slot>
              <button @click="startSingleGaming" class="btn btn-success w-100 rounded-0">开始游戏</button>
            </div>
          </div>
          <div v-else-if="gameMode === 'multi'">
            <select v-model="myBotId" :disabled="matchState !== 'toMatch'" class="form-control mb-3">
              <option :value="0" selected>亲自出马</option>
              <option v-for="bot in myBotList" :value="bot.id">{{bot.title}}#{{bot.id}}</option>
            </select>
            <button
              @click="startMatching"
              :disabled="gameState !== 'init'"
              class="btn btn-warning w-100 rounded-0"
              style="border-radius: 0; width: 100%"
            >
              开始匹配
            </button>
          </div>
        </div>
        <div v-else-if="gameState === 'starting'">
          <transition name="all-ok">
            <div v-if="gameMode === 'single'" class="text-center">
              <div class="m-auto spinner-border" style="width: 3rem; height: 3rem;" role="status"></div>
              <div>等待Bot编译完成...</div>
            </div>
            <div v-else-if="gameMode === 'multi'">
              <!--匹配中，匹配成功，准备开始-->
              <div v-if="matchState === 'matching'">
                <div class="text-center">
                  <span class="spinner-border spinner-border-sm" role="status"></span>
                  寻找对手中...
                </div>
                <hr>
                <button @click="cancelMatching" class="btn btn-danger w-100 rounded-0">取消匹配</button>
              </div>
              <div v-else-if="matchState === 'matched'">
                <div v-if="!allOk()">
                  <slot name="matched"></slot>
                  <hr>
                  <button
                    @click="toggleMatch"
                    :disabled="allOk()"
                    :class="{'btn-outline-success': !matchIsOk[getMe()], 'btn-success': matchIsOk[getMe()]}" class="btn w-100 rounded-0"
                  >
                    准备
                  </button>
                  <button
                    @click="exitMatching"
                    :disabled="allOk()"
                    class="btn btn-danger w-100 rounded-0 mt-3"
                  >
                    退出
                  </button>
                </div>
                <div v-else class="text-center">
                  <div class="m-auto spinner-border" style="width: 3rem; height: 3rem;" role="status"></div>
                  <div>等待Bot编译完成...</div>
                </div>
              </div>
            </div>
          </transition>
        </div>
        <div v-else-if="gameState === 'pending'">
          <!--自定义区域-->
          <slot name="pending"></slot>
        </div>
        <div v-else-if="gameState === 'gameOver'">
          <slot name="game-over"></slot>
          <button @click="initState" class="btn btn-secondary mb-3" style="width: 100%; border-radius: 0">继续</button>
        </div>
      </transition>
    </template>
  </Collapse>
</template>

<script setup>
import {onMounted, ref} from "vue";
import USER from "../store/USER.js";
import Row from "./Row.vue";
import Col from "./Col.vue";
import Collapse from "./Collapse.vue";
import SOCKET from "../store/SOCKET.js";
import {getBotApi} from "../script/api.js";
import alert from "../script/alert.js";

const props = defineProps({
  gameId: {
    type: Number
  },
  botIds: {
    type: Array
  },
  infos: {
    type: Array
  }
});

const gameState = ref("init");
const gameMode = ref("single");

const matchState = ref("toMatch");
const matchIsOk = ref([false, false]);

const myBotId = ref(0);
const myBotList = ref([]);

const setGameMode = _gameMode => {
  gameMode.value = _gameMode;
};

const setGameState = _gameState => {
  gameState.value = _gameState;
};

const setMatchState = _matchState => {
  matchState.value = _matchState;
};

const checkMatchOk = index => {
  return matchIsOk.value[index];
};

const sendMessage = message => {
  SOCKET().sendMessage(message);
};

const startSingleGaming = () => {
  setGameState("starting");
  for (let i = 0; i < 2; ++i)
    if (props.botIds[i] === undefined || props.botIds[i] === null)
      props.botIds[i] = 0;
  sendMessage({
    action: "startSingleGaming",
    botIds: props.botIds
  });
};

const receivedStartSingleGaming = () => {
  setGameState("pending");
  setGameMode("single");
  sendMessage({
    action: "startGame"
  });
};

const startMatching = () => {
  setGameState("starting");
  sendMessage({
    action: "startMatching",
    botId: myBotId.value
  });
};

const receivedStartMatching = () => {
  setGameState("starting");
  setMatchState("matching");
};

const cancelMatching = () => {
  sendMessage({
    action: "cancelMatching"
  });
};

const receivedCancelMatching = () => {
  setMatchState("toMatch");
  setGameState("init");
};

const receivedSuccessMatching = json => {
  setMatchState("matched");
  matchIsOk.value = new Array(json.infos.length);
  matchIsOk.value.fill(false, 0);
  alert(`success`, `成功匹配!`);
};

const exitMatching = () => {
  sendMessage({
    action: "exitMatching"
  });
};

const receivedExitMatching = json => {
  const me = getMe();
  const id = json.id;
  if (me === id) {
    setMatchState("toMatch");
    setGameState("init");
  } else {
    setMatchState("matching");
    alert("info", "有人退出了匹配");
  }
};

const allOk = () => matchIsOk.value.filter(ok => !ok).length === 0;

const receivedStartMultiGaming = () => {
  setGameState("pending");
  setMatchState("toMatch");
  sendMessage({
    action: "startGame"
  });
};

const receivedTellResult = () => {
  setGameState("gameOver");
};

const getMe = () => {
  for (let index in props.infos) {
    const info = props.infos[index];
    if (info.id === USER().getUserID)
      return parseInt(index);
  }
  return -1;
};

const toggleMatch = () => {
  if (matchIsOk.value[getMe()]) {
    sendMessage({
      action: "matchNot"
    });
  } else {
    sendMessage({
      action: "matchOk"
    });
  }
};

const receivedMatchOk = id => {
  matchIsOk.value[id] = true;
};

const receivedMatchNot = id => {
  matchIsOk.value[id] = false;
};

const initState = () => {
  matchIsOk.value = [false, false];
  setGameMode("single");
  setGameState("init");
  setMatchState("toMatch");
};

defineExpose({
  checkMatchOk,
  getMe,
  gameMode,
  initState,
  receivedStartMatching,
  receivedCancelMatching,
  receivedSuccessMatching,
  receivedStartSingleGaming,
  receivedMatchOk,
  receivedMatchNot,
  receivedExitMatching,
  receivedStartMultiGaming,
  receivedTellResult
});

onMounted(() => {
  getBotApi(props.gameId).then(list => myBotList.value = list);
});

</script>

<style scoped>
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