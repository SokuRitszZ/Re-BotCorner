<template>
  <CardBody>
    <Row>
      <!--      大版面-->
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
            <div style="height: 300px; width: 100%; overflow: auto">
              <table class="table table-striped">
                <thead>
                <tr>
                  <td>时间</td>
                  <td>白子</td>
                  <td>红子</td>
                  <td>胜者</td>
                  <td>
                    <button @click="initRecordList" class="btn btn-secondary" style="padding: 0; width: 25px; line-height: 25px; border-radius: 5px;">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-arrow-repeat" viewBox="0 0 16 16">
                        <path d="M11.534 7h3.932a.25.25 0 0 1 .192.41l-1.966 2.36a.25.25 0 0 1-.384 0l-1.966-2.36a.25.25 0 0 1 .192-.41zm-11 2h3.932a.25.25 0 0 0 .192-.41L2.692 6.23a.25.25 0 0 0-.384 0L.342 8.59A.25.25 0 0 0 .534 9z"/>
                        <path fill-rule="evenodd" d="M8 3c-1.552 0-2.94.707-3.857 1.818a.5.5 0 1 1-.771-.636A6.002 6.002 0 0 1 13.917 7H12.9A5.002 5.002 0 0 0 8 3zM3.1 9a5.002 5.002 0 0 0 8.757 2.182.5.5 0 1 1 .771.636A6.002 6.002 0 0 1 2.083 9H3.1z"/>
                      </svg>
                    </button>
                  </td>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(record, index) in recordList">
                  <td>{{record.createTime}}</td>
                  <td> <span><img :src="record.headIcon0" style="width: 40px; border-radius: 50%; padding: 1px; border: 1px solid #ccc" alt=""></span><div style="display: inline-block; margin-left: 5px;">{{ record.username0 }}</div></td>
                  <td> <span><img :src="record.headIcon1" style="width: 40px; border-radius: 50%; padding: 1px; border: 1px solid #900" alt=""></span><div style="display: inline-block; margin-left: 5px;">{{ record.username1 }}</div></td>
                  <td style="line-height: 40px"> {{record.result === 0 ? '白子' : '红子'}} </td>
                  <td>
                    <button @click="playRecord(index)" class="btn btn-primary" style="padding: 0; width: 40px; height: 40px; border-radius: 0">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-play-circle-fill" viewBox="0 0 16 16">
                        <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM6.79 5.093A.5.5 0 0 0 6 5.5v5a.5.5 0 0 0 .79.407l3.5-2.5a.5.5 0 0 0 0-.814l-3.5-2.5z"/>
                      </svg>
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
      <!--      小版面-->
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
              <!--            单人模式 -->
              <template v-if="gameMode === 'single'">
                <input placeholder="白子BotID（非正数或者为空则为手动挡）" :disabled="isWaiting" v-model="singleBotId0" type="number" class="form-control mb-2" />
                <input placeholder="红子BotID（非正数或者为空则为手动挡）" :disabled="isWaiting" v-model="singleBotId1" type="number" class="form-control mb-2" />
                <button :disabled="isWaiting" @click="startSingleGaming" class="btn btn-success" style="width: 100%; border-radius: 0">开始游戏</button>
                <div v-if="isWaiting" style="width: 100%; text-align: center;">
                  <span class="spinner-border spinner-border-sm text-warning" role="status" aria-hidden="true"></span>
                  <span class="text-warning" style="font-size: small">等待Bot编译完成...</span>
                </div>
              </template>
              <!--            多人模式 -->
              <template v-else-if="gameMode === 'multi'">
                <select v-model="selectedBotId" :disabled="state !== 'toMatch'" class="form-control mb-3">
                  <option :value="0" selected>亲自出马</option>
                  <option v-for="bot in myBotList" :value="bot.id">{{bot.title}}#{{bot.id}}</option>
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
                      <img :src="opponent.headIcon" style="width: 100%">
                      <hr>
                      <div style="text-align: center">{{opponent.username}}</div>
                      <div style="text-align: center; color: gray">#{{opponent.id}}</div>
                    </Col>
                    <Col col="col-8">
                      <button @click="matchOk" :disabled="allOk()" v-show="!iOk" class="btn btn-success mb-3" style="width: 100%; border-radius: 0;">确认</button>
                      <button @click="matchNot" :disabled="allOk()" v-show="iOk" class="btn btn-secondary mb-3" style="width: 100%; border-radius: 0;">取消</button>
                      <button @click="exitMatching" :disabled="allOk()" class="btn btn-danger" style="width: 100%; border-radius: 0">退出</button>
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
            <!--            游戏中 || 录像播放中-->
            <template v-else-if="state === 'gaming' || state === 'record'">
              <div
                style="background-color: antiquewhite; width: 100%; height: 200px;"
              >
                <h2 v-if="gameMode === 'multi'" style="text-align: center;">
                  你是
                </h2>
                <div v-if="gameMode === 'multi' && playId === 0" style="background-color: #ccc; border-radius: 50%; width: 50px; height: 50px; margin: auto" />
                <div v-if="gameMode === 'multi' && playId === 1" style="background-color: #900; border-radius: 50%; width: 50px; height: 50px; margin: auto" />
                <h3 style="text-align: center">当前回合是</h3>
                <div v-if="checker.curId === 0" style="background-color: #ccc; border-radius: 50%; width: 50px; height: 50px; margin: auto" />
                <div v-if="checker.curId === 1" style="background-color: #900; border-radius: 50%; width: 50px; height: 50px; margin: auto" />
              </div>
            </template>
            <!--            游戏结束-->
            <template v-else-if="state === 'gameOver'">
              <h2 style="text-align: center">游戏结束，结果为</h2>
              <h3 style="text-align: center">{{resultText}}</h3>
              <button @click="initState" class="btn btn-secondary mb-3" style="width: 100%; border-radius: 0">继续</button>
              <button @click="saveRecord" class="btn btn-success" style="width: 100%; border-radius: 0">保存录像</button>
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
import {nextTick, onMounted, onUnmounted, ref} from "vue";
import alert from "../script/alert.js";
import SOCKET from "../store/SOCKET.js";
import ChatRoom from "../components/ChatRoom.vue";
import USER from "../store/USER.js";
import {getRecordListApi} from "../script/api.js";
import randomId from "../script/randomid.js";
import timeFormat from "../script/timeFormat.js";
import BackgammonInfo from "./viewsChild/BackgammonInfo.vue";

/** 连接Websocket */

const hasLinkWebSocket = ref(false);
const hasClickedInitSocket = ref(false);
const routes = ref({});
const resultText = ref('');

const initSocket = () => {
  initState();
  hasClickedInitSocket.value = true;
  SOCKET().connect({
    game: 'backgammon',
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
      const json = JSON.parse(message.data);
      route(json);
    },
    onError(error) {
      hasLinkWebSocket.value = false;
      hasClickedInitSocket.value = false;
      console.log(error);
    }
  });
};

const saveRecord = () => {
  SOCKET().sendMessage({
    action: `saveRecord`
  });
};

const opponent = ref({});
const playId = ref(0);

const initRoutes = () => {
  const theRoutes = {
    startSingleGaming(json) {
      if (json.result != 'ok') alert(`danger`, json.result);
      else {
        initGame({
          mode: 'single',
          stringifiedChess: json.stringifiedData
        });
        state.value = 'gaming';
      }
      isWaiting.value = false;
    },
    setDice(json) {
      checker.value.setDice(json.dice);
      checker.value.setCurId(json.curId);
      if (json.start !== undefined)
        alert(`success`, `游戏开始，先手为${json.start === 0 ? '白子' : '红子'}`, 3000);
    },
    moveChess(json) {
      if (json.result !== 'success') alert(`danger`, `非法操作`, 3000);
      else checker.value.moveChess(json.from, json.to);
    },
    pass(json) {
      alert(`warning`, `${json.passed === 0 ? '白子' : '红子'}无棋可走，跳过`, 3000);
    },
    tellResult(json) {
      let winner = json.winner === 0 ? "白子" : "红子";
      const result = json.result;
      alert(`success`, `游戏结束！\n胜者为${winner} ${result}`, 3000);
      state.value = `gameOver`;
      resultText.value = `胜者${winner} ${result}`;
    },
    saveRecord(json) {
      if (json.message === '录像成功保存') {
        recordList.value.unshift(json);
        alert(`success`, json.message, 1000);
      }
      else if (json.message === '录像已经保存') alert(`warning`, json.message, 1000);
      else alert(`danger`, json.message, 1000);
    },
    startMatching(json) {
      state.value = "matching";
    },
    cancelMatching(json) {
      state.value = 'toMatch';
    },
    successMatching(json) {
      const {id, username, headIcon} = {...json};
      opponent.value = {id, username, headIcon};
      playId.value = json.playId;
      state.value = 'matched';
      alert(`success`, `匹配成功`, 3000);
      const message = {
        id: randomId(),
        username,
        userId: id,
        time: timeFormat(new Date(), `yyyy-MM-dd HH:mm`)
      };
      chatroomRef.value.addTalk(`enter`, message);
    },
    matchOk(json) {
      if (playId.value === json.playId) iOk.value = true;
      else uOk.value = true;
    },
    matchNot(json) {
      if (playId.value === json.playId) iOk.value = false;
      else uOk.value = false;
    },
    exitMatching(json) {
      iOk.value = uOk.value = false;
      if (playId.value === json.playId) {
        state.value = "toMatch";
        const message = {
          id: randomId(),
          username: USER().username,
          userId: USER().userID,
          time: timeFormat(new Date(), `yyyy-MM-dd HH:mm`)
        };
        chatroomRef.value.addTalk(`exit`, message);
      } else {
        state.value = "matching";
        alert(`warning`, `对方退出了房间`, 3000);
        const message = {
          id: randomId(),
          username: opponent.value.username,
          userId: opponent.value.id,
          time: timeFormat(new Date(), `yyyy-MM-dd HH:mm`)
        };
        chatroomRef.value.addTalk(`exit`, message);
      }

    },
    startMultiGaming(json) {
      isWaiting.value = false;
      initGame({
        mode: 'multi',
        stringifiedChess: json.stringifiedData,
        moveChessCallback: (from, to) => {
          SOCKET().sendMessage({
            action: 'moveChess',
            id: playId.value,
            from, to
          });
        }
      });
      iOk.value = uOk.value = false;
      state.value = 'gaming';
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
  routes.value = theRoutes;
};

const route = json => {
  const action = json.action;
  console.log(action);
  routes.value[action](json);
};

const linkWebSocket = () => {
  hasClickedInitSocket.value = true;
  initSocket();
};

/** bot */
const myBotList = ref([]);

/** 游戏 */

const parentRef = ref(null);
const canvasRef = ref(null);
const game = ref(null);
const checker = ref(null);

const initGame = ({ mode, stringifiedChess, moveChessCallback }) => {
  if (game.value !== null) game.value.endGaming();
  game.value = new BackgammonGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  game.value.start({
    mode: 'single',
    stringifiedChess,
    moveChessCallback: moveChessCallback || ((from, to) => {
      SOCKET().sendMessage({
        action: 'moveChess',
        id: `${checker.value.chess[from][0]}`,
        from,
        to
      });
    })
  });
  checker.value = game.value.getChecker();
};

/** 录像 */

const allRecordList = ref([]);
const recordList = ref([]);
const pagePtr = ref(0);
const playingRecord = ref(0);

const initRecordList = () => {
  getRecordListApi(3)
  .then((list) => {
    allRecordList.value = list.reverse();
    turnRecordPage(0);
  });
};

const playRecord = index => {
  clearTimeout(playingRecord.value);
  const json = JSON.parse(recordList.value[index].json);
  const steps = json.steps;
  const n = steps.length;
  let i = 0;
  const nextAction = () => {
    if (i < n - 1) return steps[i].split(' ')[0];
    return null;
  };
  const act = () =>{
    if (i >= n) return ;
    const step = steps[i++].split(' ');
    const action = step[0];
    let hasNext = false;
    switch (action) {
      case "start":
        initGame({
          mode: 'record',
          stringifiedChess: step.slice(2).join(' '),
          moveChessCallback: () => {},
        });
        checker.value.setCurId(parseInt(step[1]));
        break;
      case "move":
        let [id, from, to] = step.slice(1, 4);
        checker.value.moveChess(from, to);
        act();
        hasNext = true;
        break;
      case "dice":
        const dice = step.slice(2);
        dice.map((d, i) => {
          dice[i] = parseInt(d);
        });
        const curId = parseInt(step[1]);
        checker.value.setCurId(curId);
        checker.value.setDice(dice);
        if (nextAction() === 'turn' || nextAction() === 'pass') act(), hasNext = true;
        break;
      case "pass":
        alert(`warning`, `${checker.value.curId === 0 ? '白子' : '红子'}无子可动，跳过`, 3000);
        checker.value.setCurId(1 - checker.value.curId);
        break;
      case "turn":
        checker.value.setCurId(1 - checker.value.curId);
        break;
      case "result":
        if (step[2] === '中退') {
          alert(`success`, `结束，${step[1] == 0 ? '红子' : '白子'} ${step[2]}`, 3000);
          resultText.value = `${step[1] == 0 ? '红子' : '白子'} ${step[2]}`;
        } else {
          alert(`success`, `结束，${step[1] == 0 ? '白子' : '红子'} ${step[2]}`, 3000);
          resultText.value = `${step[1] == 0 ? '白子' : '红子'} ${step[2]}`;
        }
        state.value = "gameOver";
        break;
    }
    if (!hasNext) {
      playingRecord.value = setTimeout(() => {
        act();
      }, 750);
    }
  };
  act();
  nextTick(() => {
    setTimeout(() => {
      state.value = "record";
    }, 1000);
  })
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

const gameMode = ref('single');
const state = ref('toMatch');

const chooseGameMode = mode => {
  gameMode.value = mode;
};

/** 单人模式 */

const singleBotId0 = ref(null);
const singleBotId1 = ref(null);
const isWaiting = ref(false);

const startSingleGaming = () => {
  if (singleBotId0.value <= 0 || singleBotId0.value === null) singleBotId0.value = 0;
  if (singleBotId1.value <= 0 || singleBotId1.value === null) singleBotId1.value = 0;
  SOCKET().sendMessage({
    action: 'startSingleGaming',
    botId0: singleBotId0.value,
    botId1: singleBotId1.value
  });
  isWaiting.value = true;
};

/** 多人模式 */

const iOk = ref(false);
const uOk = ref(false);
const selectedBotId = ref(0);

const startMatching = () => {
  SOCKET().sendMessage({
    action: 'startMatching',
    botId: selectedBotId.value
  });
};

const cancelMatching = () => {
  SOCKET().sendMessage({
    action: 'cancelMatching'
  });
};

const allOk = () => {
  return iOk.value && uOk.value;
};

const matchOk = () => {
  SOCKET().sendMessage({
    action: 'matchOk'
  });
};

const matchNot = () => {
  SOCKET().sendMessage({
    action: 'matchNot'
  });
};

const exitMatching = () => {
  SOCKET().sendMessage({
    action: 'exitMatching'
  });
};

const initState = () => {
  resultText.value = '';
  gameMode.value = 'single';
  state.value = 'toMatch';
  iOk.value = false;
  uOk.value = false;
  isWaiting.value = false;
};

onMounted(() => {
  game.value = new BackgammonGame({
    parent: parentRef.value,
    context: canvasRef.value.getContext('2d')
  });
  initSocket();
  initRecordList();
});

onUnmounted(() => {
  SOCKET().disconnect();
});
</script>