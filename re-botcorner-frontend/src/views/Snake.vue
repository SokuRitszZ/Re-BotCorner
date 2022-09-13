<template>
  <CardBody>
    <Row>
      <Col col="col-8">
      <div ref="parentRef" id="parent">
        <canvas ref="canvasRef" tabindex="0"></canvas>
      </div>
      <!-- 最近比赛 -->
      <hr>
      <Collapse button-style="width: 100%; border-radius: 0;" collapse-id="recent-match" otherStyle="height: 30vh; overflow: auto">
        <template v-slot:button>
          最近比赛
        </template>
        <template v-slot:content>
          <div style="border: 1px solid #acacac; font-size: small; padding: 5px"
            class="mb-2" v-for="(record, index) in recordList">
            <Container>
              <Row>
                <Col col="col-2">
                  <div style="height: 100%; text-align: center; display: flex; flex-direction: column; justify-content: center">{{ record.createTime }}</div>
                </Col>
                <Col col="col-9">
                  <Container other-style="text-align: center">
                    <Row>
                      <Col>
                        <div :class="`btn ${ record.result == 0 ? 'btn-primary' : 'btn-outline-primary' }`" style="padding: 0;">
                          <Container>
                            <Row>
                              <Col col="col-4">
                                <img :src="record.headIcon0" style="width: 50px; margin: 5px;">
                              </Col>
                              <Col col="col-8">
                                <div style="font-size: 16px; display: flex; flex-direction: column; justify-content: center;">
                                  <div style="line-height: 200%; flex-direction: column; justify-content: center;">{{ record.username0 }}</div>
                                  <div style="color: gray;">#{{ record.userId0 }}</div>
                                </div>
                              </Col>
                            </Row>
                          </Container>
                        </div>
                      </Col>
                      <Col>
                        <div :class="`btn ${ record.result == 1 ? 'btn-danger' : 'btn-outline-danger' }`" style="padding: 0;">
                          <Container>
                            <Row>
                              <Col col="col-4">
                                <img :src="record.headIcon1" style="width: 50px; margin: 5px;">
                              </Col>
                              <Col col="col-8">
                                <div style="font-size: 16px; display: flex; flex-direction: column; justify-content: center;">
                                  <div style="line-height: 200%; flex-direction: column; justify-content: center;">{{ record.username1 }}</div>
                                  <div style="color: gray;">#{{ record.userId1 }}</div>
                                </div>
                              </Col>
                            </Row>
                          </Container>
                        </div>
                      </Col>
                    </Row>
                  </Container>
                </Col>
                <Col col="col-1">
                  <div style="height: 100%; display: flex; flex-direction: column; justify-content: center">
                    <button @click="playRecord(index)" class="btn btn-outline-primary" style="width: fit-content; border: none">
                      <i class="bi bi-play-circle" style="font-size: 20px"></i>
                    </button>
                  </div>
                </Col>
              </Row>
            </Container>
          </div>
        </template>
      </Collapse>
      </Col>
      <Col col="col-4">
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
              <button @click="startSingleGaming" style="width: 100%; border-radius: 0;"
                class="btn btn-success">游戏开始</button>
            </template>
            <template v-else>
              <select class="form-select mb-2" v-model="selectedBotId">
                <option :value="-1" selected>亲自出马</option>
                <option v-for="bot in myBotList" :value="bot.id">{{ bot.title }}#{{ bot.id }}</option>
              </select>
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
                <!-- 0号输入口 -->
                <template v-if="state !== 'gameOver' && !ok0">
                  <!-- 录像播放 -->
                  <template v-if="state !== 'gameOver' && gameMode === 'record'">
                    <h4 style="text-align: center; color: blue">
                      {{ ["↑", "→", " ↓", "←", "开始"][lastStep0] }}
                    </h4>
                  </template>
                  <!-- 单人模式 || 属于自己操作 -->
                  <div v-else-if="state !== 'gameOver' && (gameMode === 'single' || getMe() === 0) && selectedBotId === -1">
                    <div class="btn-group" role="group" style="font-family: monospace">
                      <Container>
                        <Row>
                          <Col> </Col>
                          <Col>
                          <input type="radio" class="btn-check" v-model="choose0" value="0" id="choose00"
                            autocomplete="off" checked>
                          <label class="btn btn-outline-primary" for="choose00">
                            <i class="bi bi-chevron-up"></i>
                          </label>
                          </Col>
                          <Col> </Col>
                        </Row>
                        <div class="mb-3"></div>
                        <Row>
                          <Col>
                          <input type="radio" class="btn-check" v-model="choose0" value="3" id="choose03"
                            autocomplete="off">
                          <label class="btn btn-outline-primary" for="choose03">
                            <i class="bi bi-chevron-left"></i>
                          </label>
                          </Col>
                          <Col>
                          <button class="btn btn-success" @click="sendChoose0">
                            <i class="bi bi-check-square"></i>
                          </button>
                          </Col>
                          <Col>
                          <input type="radio" class="btn-check" v-model="choose0" value="1" id="choose01"
                            autocomplete="off">
                          <label class="btn btn-outline-primary" for="choose01">
                            <i class="bi bi-chevron-right"></i>
                          </label>
                          </Col>
                        </Row>
                        <div class="mb-3"></div>
                        <Row>
                          <Col> </Col>
                          <Col>
                          <input type="radio" class="btn-check" v-model="choose0" value="2" id="choose02"
                            autocomplete="off">
                          <label class="btn btn-outline-primary" for="choose02">
                            <i class="bi bi-chevron-down"></i>
                          </label>
                          </Col>
                          <Col> </Col>
                        </Row>
                      </Container>
                    </div>
                  </div>
                  <template v-else>
                    <h4 style="text-align: center; color: blue">
                      {{ ok0 ? "已就绪" : "未就绪" }}
                    </h4>
                  </template>
                </template>
                <!-- 输入情况 -->
                <template v-else-if="state !== 'gameOver'">
                  <h4 style="text-align: center; color: blue">
                    {{ ok0 ? "已就绪" : "未就绪" }}
                  </h4>
                </template>
                <!-- 游戏结束 -->
                <template v-else>
                  <h4 style="text-align: center; color: blue">
                    {{ end0 }}
                  </h4>
                </template>
              </Row>
            </Container>
            <hr>
            <Container>
              <Row>
                <!-- 1号输入口 -->
                <template v-if="state !== 'gameOver' && !ok1">
                  <!-- 录像播放 -->
                  <template v-if="state !== 'gameOver' && gameMode === 'record'">
                    <h4 style="text-align: center; color: red">
                      {{ ["↑", "→", " ↓", "←", "开始"][lastStep1] }}
                    </h4>
                  </template>
                  <!-- 单人模式 || 属于自己操作 -->
                  <div v-else-if="state !== 'gameOver' && (gameMode === 'single' || getMe() === 1) && selectedBotId === -1">
                    <div class="btn-group" role="group" style="font-family: monospace">
                      <Container>
                        <Row>
                          <Col> </Col>
                          <Col>
                          <input type="radio" class="btn-check" v-model="choose1" value="0" id="choose10"
                            autocomplete="off" checked>
                          <label class="btn btn-outline-danger" for="choose10">
                            <i class="bi bi-chevron-up"></i>
                          </label>
                          </Col>
                          <Col> </Col>
                        </Row>
                        <div class="mb-3"></div>
                        <Row>
                          <Col>
                          <input type="radio" class="btn-check" v-model="choose1" value="3" id="choose13"
                            autocomplete="off">
                          <label class="btn btn-outline-danger" for="choose13">
                            <i class="bi bi-chevron-left"></i>
                          </label>
                          </Col>
                          <Col>
                          <button class="btn btn-success" @click="sendChoose1">
                            <i class="bi bi-check-square"></i>
                          </button>
                          </Col>
                          <Col>
                          <input type="radio" class="btn-check" v-model="choose1" value="1" id="choose11"
                            autocomplete="off">
                          <label class="btn btn-outline-danger" for="choose11">
                            <i class="bi bi-chevron-right"></i>
                          </label>
                          </Col>
                        </Row>
                        <div class="mb-3"></div>
                        <Row>
                          <Col> </Col>
                          <Col>
                          <input type="radio" class="btn-check" v-model="choose1" value="2" id="choose12"
                            autocomplete="off">
                          <label class="btn btn-outline-danger" for="choose12">
                            <i class="bi bi-chevron-down"></i>
                          </label>
                          </Col>
                          <Col> </Col>
                        </Row>
                      </Container>
                    </div>
                  </div>
                  <template v-else>
                    <h4 style="text-align: center; color: red">
                      {{ ok1 ? "已就绪" : "未就绪" }}
                    </h4>
                  </template>
                </template>
                <!-- 输入情况 -->
                <template v-else-if="state !== 'gameOver'">
                  <h4 style="text-align: center; color: red">
                    {{ ok1 ? "已就绪" : "未就绪" }}
                  </h4>
                </template>
                <!-- 游戏结束 -->
                <template v-else>
                  <h4 style="text-align: center; color: red">
                    {{ end1 }}
                  </h4>
                </template>
              </Row>
            </Container>
            <template v-if="state === 'gameOver'">
              <template v-if="gameMode !== 'record'">
                <hr>
                <button @click="saveRecord" class="btn btn-success">保存录像</button>
              </template>
              <hr>
              <button @click="remake" class="btn btn-secondary">继续</button>
            </template>
          </template>
        </template>
      </Collapse>
      <hr>
      <Collapse button-style="width: 100%; border-radius: 0" collapse-id="chatroom">
        <template v-slot:button>
          交流窗口
        </template>
        <template v-slot:content>
          <h1>
            交流窗口
          </h1>
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
import API from '../script/api';

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
const recordList = ref([]);
const lastStep0 = ref(4);
const lastStep1 = ref(4);
const myBotList = ref([]);
const selectedBotId = ref(-1);
const hasClickMatching = ref(false);

const initGameState = () => {
  game.value = null; 
  checker.value = null;
  isSingleMode.value = true;
  state.value = 'toMatch';
  gameMode.value = '';
  choose0.value = 0;
  choose1.value = 0;
  ok0.value = false;
  ok1.value = false;
  end0.value = '';
  end1.value = '';
  userId0.value = 0;
  userId1.value = 0;
  matchOk0.value = false;
  matchOk1.value = false;
  matchingBoard.value = '';
  opponentHeadIcon.value = '';
  opponentUsername.value = '';
  opponentUserId.value = '';
  lastStep0.value = 4;
  lastStep1.value = 4;
};

const dbg = () => {
  setInterval(() => {
    console.log(state.value, gameMode.value);
  }, 100);
};

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

const chooseSingleMode = () => {
  if (state.value === "matching") {
    cancelMatching();
  } else if (state.value === "matched") {
    exitMatching();
  }
  isSingleMode.value = true;
};

const chooseMultiMode = () => {
  isSingleMode.value = false;
};

const startSingleGaming = () => {
  hasClickMatching.value = true;
  SOCKET().sendMessage({
    action: 'startSingleGaming'
  });
};

const startMatching = () => {
  if (hasClickMatching.value) return ;
  hasClickMatching.value = true;
  SOCKET().sendMessage({
    action: 'startMatching',
    useBotId: selectedBotId.value
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

const playRecord = index => {
  initGameState();
  const record = recordList.value[index];
  SOCKET().sendMessage({
    action: 'playRecord',
    id: parseInt(record.id)
  });
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

const saveRecord = () => {
  SOCKET().sendMessage({
    action: 'saveRecord'
  });
};

const remake = () => {
  initGameState();
};

const receivedStartSingleGaming = json => {
  const userId = USER().getUserID;
  game.value = new SnakeGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
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
  if (gameMode.value === "record") {
    lastStep0.value = direction0;
    lastStep1.value = direction1;
  }
  ok0.value = false;
  ok1.value = false;
};

const receivedStartMatching = json => {
  hasClickMatching.value = false;
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
  game.value = new SnakeGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  game.value.start({
    map: json.map,
    userId0: json.userId0,
    userId1: json.userId1
  });
  userId0.value = json.userId0;
  userId1.value = json.userId1;
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

const receivedPlayRecord = json => {
  const userId = USER().getUserID;
  game.value = new SnakeGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  game.value.start({
    map: json.map,
    userId0: userId,
    userId1: userId
  });
  userId0.value = userId;
  userId1.value = userId;
  state.value = "playingRecord";
  gameMode.value = "record";
  checker.value = game.value.getChecker();
};

const receivedSaveRecord = json => {
  if (json.hasSaved) {
    alert(`warning`, `已保存该录像`);
  } else {
    alert(`success`, `保存录像成功`);
  }
};

const receivedTellResult = json => {
  end0.value = json.result === 0 ? "WIN" : json.result === 1 ? "LOSE" : "DRAW";
  end1.value = json.result === 1 ? "WIN" : json.result === 0 ? "LOSE" : "DRAW";
  state.value = "gameOver";
  if (gameMode.value === 'multi') {
    switch (json.result) {
      case getMe():
        alert(`success`, `胜利！Rating +${json.score}`, 5000);
        break;
      case 1 - getMe():
        alert(`danger`, `战败... Rating -${json.score}`, 5000);
        break;
      case -1:
        alert(`warning`, `平局`, 5000);
        break;
    }
  } else {
    switch (json.result) {
      case 0:
        alert(`primary`, `蓝方获胜！`, 5000);
        break;
      case 1:
        alert(`danger`, `红方获胜！`, 5000);
        break;
      case -1:
        alert(`warning`, `平局`, 5000);
    }
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
      } else if (json.action === "playRecord") {
        receivedPlayRecord(json);
      } else if (json.action === "saveRecord") {
        receivedSaveRecord(json);
      } else if (json.action === "tellResult") {
        receivedTellResult(json);
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
  API({
    url: '/record/getListByGameId',
    type: 'get',
    data: {
      gameId: 1
    },
    needJWT: true,
    success: resp => {
      recordList.value = resp.reverse();
    }
  });
  API({
    url: '/bot/getByGame',
    type: 'get',
    data: {
      gameId: 1
    },
    needJWT: true,
    success: resp => {
      myBotList.value = resp;
      myBotList.value.map(bot => {
        bot.createTime = new Date(bot.createTime);
        bot.modifyTime = new Date(bot.modifyTime);
      });
    }
  })
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