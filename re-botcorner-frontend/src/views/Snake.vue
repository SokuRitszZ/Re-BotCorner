<template>
  <CardBody>
    <Row>
      <Col col="col-4">
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
          <template v-if="state === 'toMatch' || state === 'matching' || state === 'matched'">
            <Row>
              <Col><button @click="isSingleMode = true" style="width: 100%; border-radius: 0;"
                class="btn btn-outline-secondary">单人练习</button></Col>
              <Col><button @click="isSingleMode = false" style="width: 100%; border-radius: 0;"
                class="btn btn-outline-secondary">寻找对手</button></Col>
            </Row>
            <hr>
            <template v-if="isSingleMode">
              <!-- 选择单人模式 -->
              <button @click="startSingleGaming" style="width: 100%; border-radius: 0;"
                class="btn btn-success">游戏开始</button>
            </template>
            <template v-else>
              <!-- 选择多人模式 -->
              <template v-if="state === 'toMatch'">
                <!-- 未开始匹配 -->
                <div @click="startMatching" class="btn btn-warning">开始匹配</div>
              </template>
              <template v-else-if="state === 'matching'">
                <div class="btn btn-danger mb-3" @click="cancelMatching">取消</div>
                <h3 style="text-align: center;">
                  {{ matchingBoard }}
                </h3>
                <!-- 正在匹配 -->
              </template>
              <template v-else-if="state === 'matched'">
                <!-- 匹配成功 -->
                <Row>
                  <Col col="col-4">
                    <img style="width: 100%" :src="opponentHeadIcon" />
                    <hr>
                    <div style="text-align: center">
                      {{ opponentUsername }}
                    </div>
                    <div style="text-align: center; color: gray;">#{{ opponentUserId }}</div>
                  </Col>
                  <Col col="col-8">
                    <button v-if="!iAmOk()" @click="matchOk" class="btn btn-success mb-3" style="width: 100%">确认</button>
                    <button v-else @click="matchNotOk" class="btn btn-secondary mb-3" style="width: 100%">取消确认</button>
                    <button @click="exitMatching" class="btn btn-danger mb-1" style="width: 100%">取消匹配</button>
                    <div v-if="youROk()" style="color: green; text-align: center; font-size: 10px">对手已准备就绪</div>
                  </Col>
                </Row>
              </template>
            </template>
          </template>
          <template v-else>
            <Container>
              <Row>
                <template v-if="!ok0">
                  <!-- 0号输入口 -->
                  <template v-if="gameMode === 'single' || getMe() === 0">
                    <!-- 单人模式 || 属于自己操作 -->
                    <Col col="col-8">
                      <div class="btn-group" role="group" style="font-family: monospace">
                        <input type="radio" class="btn-check" v-model="choose0" value="0" id="choose00" autocomplete="off" checked>
                        <label class="btn btn-outline-primary" for="choose00">
                          ↑
                        </label>
                        <input type="radio" class="btn-check" v-model="choose0" value="1" id="choose01" autocomplete="off">
                        <label class="btn btn-outline-primary" for="choose01">
                          →
                        </label>
                        <input type="radio" class="btn-check" v-model="choose0" value="2" id="choose02" autocomplete="off">
                        <label class="btn btn-outline-primary" for="choose02">
                          ↓
                        </label>
                        <input type="radio" class="btn-check" v-model="choose0" value="3" id="choose03" autocomplete="off">
                        <label class="btn btn-outline-primary" for="choose03">
                        ←
                        </label>
                      </div>
                    </Col>
                    <Col col="col-4">
                      <button class="btn btn-success" @click="sendChoose0">
                        好
                      </button>
                    </Col>
                  </template>
                  <template v-else>
                    <!-- 多人模式 -->
                    <h4 style="text-align: center; color: blue">
                      {{ ok1 ? "已就绪" : "未就绪" }}
                    </h4>
                  </template>
                </template>
                <template v-else-if="state !== 'gameOver'" style="color: blue">
                  <!-- 输入就绪 -->
                  <h4 style="text-align: center; color: blue">已就绪</h4>
                </template>
                <template v-else>
                  <!-- 游戏结束 -->
                  <h4 style="text-align: center; color: blue">
                    {{ end0 }}
                  </h4>
                </template>
              </Row>
            </Container>
            <hr>
            <Container>
              <Row>
                <template v-if="!ok1">
                  <!-- 1号输入口 -->
                  <template v-if="gameMode === 'single' || getMe() === 1">
                    <!-- 单人模式 || 属于自己操作 -->
                    <Col col="col-8">
                      <div class="btn-group" role="group" style="font-family: monospace">
                        <input type="radio" class="btn-check" v-model="choose1" value="0" id="choose10" autocomplete="off" checked>
                        <label class="btn btn-outline-danger" for="choose10">
                          ↑
                        </label>
                        <input type="radio" class="btn-check" v-model="choose1" value="1" id="choose11" autocomplete="off">
                        <label class="btn btn-outline-danger" for="choose11">
                          →
                        </label>
                        <input type="radio" class="btn-check" v-model="choose1" value="2" id="choose12" autocomplete="off">
                        <label class="btn btn-outline-danger" for="choose12">
                          ↓
                        </label>
                        <input type="radio" class="btn-check" v-model="choose1" value="3" id="choose13" autocomplete="off">
                        <label class="btn btn-outline-danger" for="choose13">
                          ←
                        </label>
                      </div>
                    </Col>
                    <Col col="col-4">
                      <button class="btn btn-success" @click="sendChoose1">
                        好
                      </button>
                    </Col>
                  </template>
                  <template v-else>
                    <!-- 多人模式 -->
                    <h4 style="text-align: center; color: red">
                      {{ ok1 ? "已就绪" : "未就绪" }}
                    </h4>
                  </template>
                </template>
                <template v-else-if="state !== 'gameOver'">
                  <!-- 输入就绪 -->
                  <h4 style="text-align: center; color: red">
                    已就绪
                  </h4>
                </template>
                <template v-else>
                  <!-- 游戏结束 -->
                  <h4 style="text-align: center; color: red">
                    {{ end1 }}
                  </h4>
                </template>
              </Row>
            </Container>
          </template>
        </template>
      </Collapse>
      <hr>
      <Collapse button-style="width: 100%; border-radius: 0;" collapse-id="recent-match">
        <template v-slot:button>
          近期比赛
        </template>
        <template v-slot:content>
          <h1>近期比赛</h1>
        </template>
      </Collapse>
      </Col>
      <Col col="col-8">
      <div ref="parentRef" id="parent">
        <canvas ref="canvasRef" tabindex="0"></canvas>
      </div>
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

const parentRef = ref(null);
const canvasRef = ref(null);

const game = ref(null);
const checker = ref(null);
const isSingleMode = ref(true);
const state = ref("toMatch");
const gameMode = ref('');
const choose0 = ref(0);
const choose1 = ref(0);
const ok0 = ref(false);
const ok1 = ref(false);
const end0 = ref('');
const end1 = ref('');
const userId0 = ref(0);
const userId1 = ref(0);
const matchOk0 = ref(false);
const matchOk1 = ref(false);
const matchingBoard = ref('');
const opponentHeadIcon = ref('');
const opponentUsername = ref('');
const opponentUserId = ref(0);

const getMe = () => {
  if (userId0.value == USER().getUserID) return 0;
  else if (userId1.value == USER().getUserID) return 1;
  else return -1;
};

const iAmOk = () => {
  return userId0.value === USER().getUserID && matchOk0.value || userId1.value === USER().getUserID && matchOk1.value;
};

const youROk = () => {
  return userId0.value === USER().getUserID && matchOk1.value || userId1.value === USER().getUserID && matchOk0.value;
};

const startSingleGaming = () => {
  SOCKET().sendMessage({
    action: 'startSingleGaming'
  });
};

const startMatching = () => {
  SOCKET().sendMessage({
    action: 'startMatching'
  });
};

const cancelMatching = () => {
  SOCKET().sendMessage({
    action: 'cancelMatching'
  });
};

const sendChoose0 = () => {
  SOCKET().sendMessage({
    action: 'moveSnake',
    id: 0,
    direction: choose0.value
  });
  choose0.value = 0;
  ok0.value = true;
};

const sendChoose1 = () => {
  SOCKET().sendMessage({
    action: 'moveSnake',
    id: 1,
    direction: choose1.value
  });
  choose1.value = 0;
  ok1.value = true
};

const matchOk = () => {
  SOCKET().sendMessage({
    action: 'matchOk'
  });
};

const matchNotOk = () => {
  SOCKET().sendMessage({
    action: 'matchNotOk'
  });
};

const exitMatching = () => {
  SOCKET().sendMessage({
    action: 'exitMatching'
  });
};

const receivedStartSingleGaming = json => {
  const userId = USER().getUserID;
  game.value.start({
    map: json.map,
    userId0: userId,
    userId1: userId
  });
  userId0.value = userId;
  userId1.value = userId;
  state.value = "waitingInput";
  gameMode.value = "single";
  checker.value = game.value.getChecker();
};

const receivedMoveSnake = json => {
  const direction0 = json.direction0;
  const direction1 = json.direction1;
  const isIncreasing = json.isIncreasing;
  const status0 = json.status0;
  const status1 = json.status1;
  checker.value.moveSnake({
    id: 0,
    direction: direction0,
    status: status0,
    isIncreasing
  });
  checker.value.moveSnake({
    id: 1,
    direction: direction1,
    status: status1,
    isIncreasing
  });
  if (status0 === "die" || status1 === "die") {
    state.value = "gameOver";
    end0.value = status0 === "die" ? "LOSE" : "WIN";
    end1.value = status1 === "die" ? "LOSE" : "WIN";
    return ;
  }
  ok0.value = false;
  ok1.value = false;
};

const receivedStartMatching = json => {
  state.value = 'matching';
};

const receivedSuccessMatching = json => {
  opponentHeadIcon.value = json.opponentHeadIcon;
  opponentUserId.value = json.opponentUserId;
  opponentUsername.value = json.opponentUsername;
  const you = json.opponentId;
  const me = 1 - you;
  userId0.value = me === 0 ? USER().getUserID : opponentUserId.value;
  userId1.value = me === 1 ? USER().getUserID : opponentUserId.value;
  state.value = 'matched';
  alert(`success`, `成功匹配!`);
};

const receivedCancelMatching = json => {
  state.value = 'toMatch';
}

const receivedMatchOk = json => {
  if (json.id === 0) {
    matchOk0.value = true;
  } else {
    matchOk1.value = true;
  }
};

const receivedMatchNotOk = json => {
  if (json.id === 0) {
    matchOk0.value = false;
  } else {
    matchOk1.value = false;
  }
};

const receivedExitMatching = json => {
  if (userId0.value === USER().getUserID && json.id === 0 || userId1.value === USER().getUserID && json.id === 1) {
    state.value = 'toMatch';
  } else {
    state.value = 'matching';
    alert(`warning`, `对方退出了房间`);
  }
  userId0.value = userId1.value = 0;
  opponentHeadIcon.value = opponentUsername.value = '';
  opponentUserId.value = 0;
  matchOk0.value = matchOk1.value = false;
};

const receivedStartMultiGaming = json => {
  console.log(json);
  game.value.start({
    map: json.map,
    userId0: json.userId0,
    userId1: json.userId1
  });
  userId0.value = json.userId0;
  userId1.value = json.userId1;
  console.log(userId0.value, userId1.value);
  state.value = "waitingInput";
  gameMode.value = "multi";
  checker.value = game.value.getChecker();
};

const receivedSetDirection = json => {
  if (json.id == 0) {
    ok0.value = true;
  } else {
    ok1.value = true;
  }
};

onMounted(() => {
  SOCKET().connect({
    game: 'snake',
    onOpen() {
      console.log('open websocekt.');
    },
    onClose() {
      console.log('close websocket.');
    },
    onMessage(message) {
      let json = JSON.parse(message.data);
      console.log(json.action);
      if (json.action === "startSingleGaming") {
        receivedStartSingleGaming(json);
      } else if (json.action === "moveSnake") {
        receivedMoveSnake(json);
      } else if (json.action === "startMatching") {
        receivedStartMatching(json);
      } else if (json.action === "successMatching") {
        receivedSuccessMatching(json);
      } else if (json.action === "cancelMatching") {
        receivedCancelMatching(json);
      } else if (json.action === "matchOk") {
        receivedMatchOk(json);
      } else if (json.action === "matchNotOk") {
        receivedMatchNotOk(json);
      } else if (json.action === "exitMatching") {
        receivedExitMatching(json);
      } else if (json.action === "startMultiGaming") {
        receivedStartMultiGaming(json);
      } else if (json.action === "setDirection") {
        receivedSetDirection(json);
      }
    },
    onError(error) {
      console.log(error);
    }
  });
  game.value = new SnakeGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });

  let matchingBoardList = ["匹配中·", "匹配中··", "匹配中···"];
  let i = 0; 
  setInterval(() => {
    i = (i + 1) % 3;
    matchingBoard.value = matchingBoardList[i];
  }, 1000);
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
</style>