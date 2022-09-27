<template>
  <CardBody>
    <Row>
      <!-- 大版面 -->
      <Col col="col-8">
        <div ref="parentRef" id="parent"
          style="width: 100%; height: 50vh; background-color: antiquewhite; text-align: center;"
        >
          <canvas ref="canvasRef" tabindex="0"></canvas>
        </div>
        <hr>
        <!-- 最近比赛 -->
        <Collapse
          collapse-id="record-list"
          button-style="width: 100%; border-radius: 0"
        >
          <template v-slot:button>
            最近比赛
          </template>
          <template v-slot:content>
            <div style="height: 300px; width: 100%; overflow: auto">
              <table class="table table-striped">
                <thead>
                  <tr>
                    <td>时间</td>
                    <td>黑子</td>
                    <td>白子</td>
                    <td>胜者</td>
                    <td>
                      <button :disabled="hasInitRecordList" @click="initRecordList" class="btn btn-secondary" style="padding: 0; width: 25px; line-height: 25px; border-radius: 5px;"><i class="bi bi-arrow-repeat"></i></button>
                    </td>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(record, index) in recordList">
                    <td>{{ record.createTime }}</td>
                    <td> <span><img :src="record.headIcon0" style="width: 45px; border-radius: 50%; padding: 1px; border: 1px solid black" alt=""></span><div style="display: inline-block; margin-left: 5px;">{{ record.username0 }}</div> </td>
                    <td> <span><img :src="record.headIcon1" style="width: 45px; border-radius: 50%; padding: 1px; border: 1px solid white" alt=""></span><div style="display: inline-block; margin-left: 5px;">{{ record.username1 }}</div></td>
                    <td :style="{ color: record.result == -1 ? 'black' : record.result == 0 ? 'black' : 'white', lineHeight: '45px' }">
                      <div v-if="record.result == -1 || record.result == 0" class="black-chess" style="box-shadow: 0 0 10px black; border-radius: 50%; display: inline-block; position: relative; width: 20px; height: 20px; margin: auto">
                        <div style="position: absolute; z-index: 1; border-radius: 50%; background-color: black; width: 20px; height: 20px" />
                        <div style="position: absolute; transform: translateY(1px); border-radius: 50%; background-color: white; width: 20px; height: 20px" />
                      </div>
                      <div v-if="record.result == -1 || record.result == 1" class="white-chess" style="box-shadow: 0 0 10px black; border-radius: 50%; display: inline-block; position: relative; width: 20px; height: 20px; margin: auto">
                        <div style="position: absolute; z-index: 1; border-radius: 50%; background-color: white; width: 20px; height: 20px" />
                        <div style="position: absolute; transform: translateY(1px); border-radius: 50%; background-color: black; width: 20px; height: 20px" />
                      </div>
                    </td>
                    <td>
                      <button @click="playRecord(index)" class="btn btn-primary" style="width: fit-content; padding: 0; width: 45px; height: 45px; border-radius: 0;">
                        <i class="bi bi-play-circle" style="font-size: 20px"></i>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <nav>
              <ul class="pagination justify-content-center">
                <li class="page-item">
                  <button @click="lastPage" class="page-link">
                    <span>&laquo;</span>
                  </button>
                </li>
                <li :class="`page-item ${pagePtr === idx ? 'active' : ''}`" v-for="(item, idx) in Math.ceil(allRecordList.length / 4)">
                  <button @click="turnRecordPage(idx)" class="page-link">{{ item }}</button>

                </li>
                <li class="page-item">
                  <button @click="nextPage" class="page-link">
                    <span>&raquo;</span>
                  </button>
                </li>
              </ul>
            </nav>         
          </template>
        </Collapse>
      </Col>
      <Col col="col-4" v-if="!hasLinkWebSocket">
        <button :disabled="hasClickedInitSocket" @click="initSocket" class="btn btn-primary" style="border-radius: 0; width: 100%">
          <span v-if="hasClickedInitSocket" class="spinner-grow spinner-grow-sm" role="status" aria-hidden="true"></span>
          连接WebSocket
        </button>
      </Col>
      <!-- 小版面 -->
      <Col col="col-4" v-if="hasLinkWebSocket">
      <!-- 匹配信息 / 局面信息 -->
        <Collapse button-style="width: 100%; border-radius: 0" collapse-id="match-situation">
          <template v-slot:button>
            匹配信息 / 局面信息
          </template>
          <template v-slot:content>
            <!-- 选择模式 -->
            <template v-if="state === 'toMatch' || state === 'matching' || state === 'matched'">
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
              <!-- 单人模式 -->
              <template v-if="gameMode === 'single'">
                <input :disabled="isWaiting" type="number" v-model="singleBotId0" class="form-control mb-2"
                  placeholder="黑子BotID（非正数或者为空则为亲自出马）">
                <input :disabled="isWaiting" type="number" v-model="singleBotId1" class="form-control mb-2"
                  placeholder="白子BotID（非正数或者为空则为亲自出马）">
                <button :disabled="isWaiting" class="btn btn-success" style="width: 100%; border-radius: 0"
                  @click="startSingleGaming">开始游戏</button>
                <div v-if="isWaiting" style="width: 100%; text-align: center;">
                  <span class="spinner-border spinner-border-sm text-warning" role="status" aria-hidden="true"></span>
                  <span class="text-warning" style="font-size: small">等待Bot编译完成...</span>
                </div>
              </template>
              <!-- 多人模式 -->
              <template v-if="gameMode === 'multi'">
                <select :disabled="state !== 'toMatch'" v-model="selectedBotId" class="form-control mb-3">
                  <option :value="-1">亲自出马</option>
                  <option v-for="bot in myBotList" :value="bot.id">{{ bot.title }}#{{ bot.id }}</option>
                </select>
                <!-- 还没开始匹配 -->
                <template v-if="state === 'toMatch'">
                  <button @click="startMatching" class="btn btn-warning" style="color: white; border-radius: 0">开始匹配</button>
                </template>
                <!-- 正在匹配 -->
                <template v-if="state === 'matching'">
                  <button @click="cancelMatching" class="btn btn-danger" style="width: 100%; border-radius: 0">取消匹配</button>
                  <hr>
                  <div class="text-center">
                    <span class="spinner-border spinner-border-sm" role="status">
                    </span>
                    寻找对手中...
                  </div>
                </template>
                <!-- 匹配成功 -->
                <template v-if="state === 'matched'">
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
                    <button :disabled="iuOk()" @click="switchMatchOk" v-if="!iOk" class="btn btn-success mb-3"
                      style="width: 100%; border-radius: 0;">确认</button>
                    <button :disabled="iuOk()" @click="switchMatchNot" v-if="iOk" class="btn btn-secondary mb-3"
                      style="width: 100%; border-radius: 0;">取消</button>
                    <button :disabled="iuOk()" @click="exitMatching" class="btn btn-danger"
                      style="width: 100%; border-radius: 0">退出</button>
                    <div v-if="uOk" style="color: green; text-align: center; font-size: 10px">对手已准备就绪</div>
                    <div v-if="iuOk()" style="width: 100%; text-align: center;">
                      <span class="spinner-border spinner-border-sm text-warning" role="status" aria-hidden="true"></span>
                      <span class="text-warning" style="font-size: small">等待Bot编译完成...</span>
                    </div>
                    </Col>
                  </Row>
                </template>
              </template>
            </template>
            <!-- 游戏进行时 -->
            <template v-if="state === 'gaming' || state === 'record'">
              <div
                style="background-color: gray; width: 100%; height: 300px; display: flex; flex-direction: column; justify-content: center;">
                <div v-if="gameMode === 'multi'" style="text-align: center; font-size: 60px">
                  <h1 v-if="myId() == 0" style="color: black">你是黑子</h1>
                  <h1 v-if="myId() == 1" style="color: white">你是白子</h1>
                </div>
                <hr>
                <div>
                  <div style="display: inline-block; width: 50%; text-align: center; color: black; font-size: 50px">{{
                    checker.cnt0 }}</div>
                  <div style="display: inline-block; width: 50% ;text-align: center; color: white; font-size: 50px">{{
                    checker.cnt1 }}</div>
                </div>
                <h3 style="text-align: center">当前回合为</h3>
                <div v-if="gameTurn === '黑子'" class="black-chess" style="width: 50px; height: 50px; margin: auto">
                  <div style="position: absolute; z-index: 1; border-radius: 50%; background-color: black; width: 50px; height: 50px" />
                  <div style="position: absolute; transform: translateY(2.5px); border-radius: 50%; background-color: white; width: 50px; height: 50px" />
                </div>
                <div v-if="gameTurn === '白子'" class="white-chess" style="width: 50px; height: 50px; margin: auto">
                  <div style="position: absolute; z-index: 1; border-radius: 50%; background-color: white; width: 50px; height: 50px" />
                  <div style="position: absolute; transform: translateY(2.5px); border-radius: 50%; background-color: black; width: 50px; height: 50px" />
                </div>
              </div>
            </template>
            <!-- 游戏结束 -->
            <template v-if="state === 'gameOver'">
              <div style="background-color: gray; height: 100px; font-size: 60px">
                <div style="display: inline-block; width: 50%; text-align: center; color: black">{{ checker.cnt0 }}</div>
                <div style="display: inline-block; width: 50% ;text-align: center; color: white">{{ checker.cnt1 }}</div>
              </div>
              <hr>
              <button v-if="!isPlayingRecord" class="btn btn-success mb-2" style="width: 100%; border-radius: 0"
                @click="saveRecord">
                保存录像
              </button>
              <button class="btn btn-secondary" style="width: 100%; border-radius: 0" @click="continueGaming">
                继续
              </button>
            </template>
          </template>
        </Collapse>
        <hr>
        <!-- 交流窗口 -->
        <Window ref="chatroomWindowRef" @show="showChatRoomWindow" button-style="width: 100%; border-radius: 0" title="聊天窗口">
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
  </CardBody>
</template>

<script setup>
import CardBody from '../components/CardBody.vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import Collapse from '../components/Collapse.vue';
import { onMounted, onUnmounted, ref } from 'vue';
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

const gameMode = ref('single');

const parentRef = ref(null);
const canvasRef = ref(null);

// ---交流窗口
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

// ---

// ---游戏模式
const game = ref(null); 
const checker = ref(null);
const state = ref("toMatch");
const myBotList = ref([]);
const hasStarted = ref(false);

const chooseGameMode = _gameMode => { gameMode.value = _gameMode; };

const continueGaming = () => {
  initState();
};

// ---

// ---游戏进行时
const gameTurn = ref('黑子');
// ---

// ---录像
const allRecordList = ref([]);
const recordList = ref([]);
const pagePtr = ref(0);
const isPlayingRecord = ref(false);

const playRecord = index => {
  const record = recordList.value[index];
  SOCKET().sendMessage({
    action: 'playRecord',
    id: parseInt(record.id)
  });
};

const lastPage = () => {
  turnRecordPage(Math.max(0, pagePtr.value - 1));
};

const nextPage = () => {
  turnRecordPage(Math.min(Math.ceil(allRecordList.value.length / 4) - 1, pagePtr.value + 1));
};

const turnRecordPage = page => {
  recordList.value = [];
  for (let i = 0; i < 4; ++i) {
    if (page * 4 + i >= allRecordList.value.length) break;
    recordList.value.push(allRecordList.value[page * 4 + i]);
  }
  pagePtr.value = page;
};
// ---

// ---单人模式
const singleBotId0 = ref(0);
const singleBotId1 = ref(0);
const isWaiting = ref(false);

const startSingleGaming = () => {
  isWaiting.value = true;
  let botId0 = singleBotId0.value == null || singleBotId0.value <= 0 ? 0 : singleBotId0.value;
  let botId1 = singleBotId1.value == null || singleBotId1.value <= 0 ? 0 : singleBotId1.value;
  singleBotId0.value = botId0;
  singleBotId1.value = botId1;
  SOCKET().sendMessage({
    action: `startSingleGaming`,
    botId0,
    botId1
  });
};

// ---

// ---多人模式
const opponentId = ref(-1);
const opponentUserId = ref(0);
const opponentUsername = ref('');
const opponentHeadIcon = ref('');
const iOk = ref(false);
const uOk = ref(false);
const selectedBotId = ref(-1);
const chatroomRef = ref(null);

const myId = () => {
  return 1 - opponentId.value;
};

const urId = () => {
  return opponentId.value;
};

const iuOk = () => {
  return iOk.value && uOk.value;
};

const isLeft = message => {
  return message.userId != USER().getUserID;
};

const isRight = message => {
  return message.userId == USER().getUserID;
};

const startMatching = () => {
  SOCKET().sendMessage({
    action: `startMatching`,
    botId: selectedBotId.value
  });
};

const cancelMatching = () => {
  SOCKET().sendMessage({
    action: `cancelMatching`
  });
};

const switchMatchOk = () => {
  SOCKET().sendMessage({
    action: `switchMatchOk`,
    id: myId()
  });
};

const switchMatchNot = () => {
  SOCKET().sendMessage({
    action: `switchMatchNot`,
    id: myId()
  });
};

const exitMatching = () => {
  SOCKET().sendMessage({
    action: `exitMatching`,
    id: myId()
  });
};

const initState = () => {
  iOk.value = false;
  uOk.value = false;
  state.value = 'toMatch';
  opponentId.value = -1;
  opponentUserId.value = 0;
  opponentUsername.value = '';
  opponentHeadIcon.value = '';
  selectedBotId.value = -1;
  isWaiting.value = false;
  hasStarted.value = false;
  isPlayingRecord.value = false;
};
// ---


const saveRecord = () => {
  SOCKET().sendMessage({
    action: `saveRecord`
  });
};

const putChessCallback = (r, c) => {
  SOCKET().sendMessage({
    action: `putChess`,
    id: myId(), r, c
  });
};

const websocketRoute = json => {
  console.log(json.action);
  const wsRoutes = {
    startSingleGaming(json) {
      if (json.result != 'ok') {
        alert(`danger`, json.result);
      } else {
        initGame('single', json.rows, json.cols, json.stringifiedChess);
      }
      isWaiting.value = false;
    },
    putChess(json) {
      if (json.result === 'ok') {
        checker.value.putChess(json.r, json.c, json.id);
        gameTurn.value = json.id == 1 ? '黑子' : '白子';
      } else {
        alert(`danger`, `非法操作`);
      }
    },
    pass(json) {
      alert(`warning`, `${json.passTo == 1 ? '黑子' : '白子'}跳过`);
      gameTurn.value = json.passTo == 0 ? '黑子' : '白子';
    },
    gameOver(json) {
      alert(
        json.result == 0 ? `dark` : `light`,
        (json.result == 0 ? `黑子获胜` : `白子获胜`) + `\n战败原因: ${json.reason}`,
        3000
      );
      state.value = 'gameOver';
    },
    saveRecord(json) {
      if (json.saveResult === 'ok') {
        alert(`success`, `保存录像成功`);
        allRecordList.value.unshift({
          id: json.id,
          createTime: json.createTime,
          userId0: json.userId0,
          userId1: json.userId1,
          username0: json.username0,
          username1: json.username1,
          headIcon0: json.headIcon0,
          headIcon1: json.headIcon1,
          result: json.result
        });
        turnRecordPage(pagePtr.value);
      } else {
        alert(`warning`, `已保存该录像`);
      }
    },
    playRecord(json) {
      initGame('record', json.rows, json.cols, json.stringifiedChess);
    },
    startMatching(json) {
      state.value = "matching";
    },
    cancelMatching(json) {
      state.value = 'toMatch';
    },
    successMatching(json) {
      state.value = "matched";
      opponentId.value = json.id;
      opponentUserId.value = json.userId;
      opponentUsername.value = json.username;
      opponentHeadIcon.value = json.headIcon;
      alert(`success`, `匹配成功`, 1000);
      const message = {
        id: randomId(),
        username: json.username,
        userId: json.userId,
        time: timeFormat(new Date(), `yyyy-MM-dd HH:mm`)
      };
      chatroomRef.value.addTalk(`enter`, message);
    },
    switchMatchOk(json) {
      const id = json.id;
      if (id === myId()) iOk.value = true;
      else uOk.value = true;
    },
    switchMatchNot(json) {
      const id = json.id;
      if (id === myId()) iOk.value = false;
      else uOk.value = false;
    },
    exitMatching(json) {
      const id = json.id;
      state.value = 'toMatch';
      if (id != myId()) {
        alert(`warning`, `对方退出了房间`);
        const message = {
          id: randomId(),
          username: opponentUsername.value,
          userId: opponentUserId.value,
          time: timeFormat(new Date(), `yyyy-MM-dd HH:mm`)
        };
        chatroomRef.value.addTalk(`exit`, message);
      }
      opponentId.value = -1;
      opponentUserId.value = 0;
      opponentUsername.value = '';
      opponentHeadIcon.value = '';
    },
    startMultiGaming(json) {
      initGame('multi', json.rows, json.cols, json.stringifiedChess);
    },
    sendTalk(json) {
      if (json.result === "ok") {
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
      } else {
        alert('danger', json.result);
      }
    }
  };
  wsRoutes[json.action](json);
};

const initGame = (mode, rows, cols, stringifiedChess) => {
  if (mode !== 'record') isPlayingRecord.value = false;
  else isPlayingRecord.value = true;
  game.value.endGaming();
  game.value = new ReversiGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  game.value.start({
    mode,
    rows,
    cols,
    stringifiedChess,
    userId0: 0,
    userId1: 1,
    putChessCallback: putChessCallback
  });
  gameTurn.value = '黑子';
  checker.value = game.value.getChecker();
  state.value = "gaming";
  hasStarted.value = false;
};

const hasLinkWebSocket = ref(false);
const hasClickedInitSocket = ref(false);

const initSocket = () => {
  initState();
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
    websocketRoute(JSON.parse(message.data));
  };

  const onError = error => {
    console.log(error);
    alert(`danger`, `无法连接到Websocket`);
    hasClickedInitSocket.value = false;
  };
  
  SOCKET().connect({
    game: `reversi`,
    onOpen,
    onClose,
    onMessage,
    onError
  });
};

const hasInitRecordList = ref(false);
const initRecordList = () => {
  hasInitRecordList.value = true;
  getRecordListApi(2)
  .then(list => {
    allRecordList.value = list.reverse();
    turnRecordPage(0);
    hasInitRecordList.value = false;
  })
  .catch(err => {
    alert(`danger`, `获取录像失败`, 1000);
    hasInitRecordList.value = false;
  });
};

const initBotList = () => {
  getBotApi(2)
  .then(list => {
    myBotList.value = list;
    myBotList.value.map(bot => {
      bot.createTime = new Date(bot.createTime);
      bot.modifyTime = new Date(bot.modifyTime);
    });
  });
};

onMounted(() => {

  initSocket();
  initRecordList();
  initBotList();
  
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
</style>