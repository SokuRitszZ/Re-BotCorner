<template>
  <CardBody>
    <Row>
      <Col col="col-8">
      <div ref="parentRef" id="parent">
        <canvas ref="canvasRef" tabindex="0"></canvas>
      </div>
      <hr>
      <!-- 最近比赛 -->
      <Collapse button-style="width: 100%; border-radius: 0;" collapse-id="recent-match" otherStyle="height: 30vh; overflow: auto">
        <template v-slot:button>
          最近比赛
        </template>
        <template v-slot:content>
          <div style="height: 600px; width: 100%; overflow: hidden">
            <table class="table table-striped">
              <thead>
                <tr>
                  <td>时间</td>
                  <td>蓝方</td>
                  <td>红方</td>
                  <td>胜者</td>
                  <td>回放</td>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(record, index) in recordList">
                  <td>{{ record.createTime }}</td>
                  <td> <span><img :src="record.headIcon0" style="width: 45px; border-radius: 50%; padding: 1px; border: 1px solid blue" alt=""></span><div style="display: inline-block; margin-left: 5px;">{{ record.username0 }}</div> </td>
                  <td> <span><img :src="record.headIcon1" style="width: 45px; border-radius: 50%; padding: 1px; border: 1px solid red" alt=""></span><div style="display: inline-block; margin-left: 5px;">{{ record.username1 }}</div></td>
                  <td :style="{ color: record.result == -1 ? 'black' : record.result == 0 ? 'blue' : 'red', lineHeight: '45px' }">{{ record.result == -1 ? "平局" : record.result == 0 ? "蓝方" : "红方" }}</td>
                  <td>
                    <button @click="playRecord(index)" class="btn btn-primary" style="width: fit-content;">
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
              <input v-model="singleBotId[0]" type="number" class="form-control mb-2" placeholder="选择蓝方的BotID（不填则为亲自出马）" :disabled="hasStartSingleGaming">
              <input v-model="singleBotId[1]" type="number" class="form-control mb-2" placeholder="选择红方的BotID（不填则为亲自出马）" :disabled="hasStartSingleGaming">
              <button @click="startSingleGaming" style="width: 100%; border-radius: 0;"
                class="btn btn-success" :disabled="hasStartSingleGaming">游戏开始</button>
              <div v-if="hasStartSingleGaming" style="width: 100%; text-align: center;">
                <span class="spinner-border spinner-border-sm text-warning" role="status" aria-hidden="true"></span>
                <span class="text-warning" style="font-size: small">等待Bot编译完成...</span>
              </div>
            </template>
            <template v-else>
              <select class="form-select mb-2" v-model="selectedBotId" :disabled="state !== 'toMatch'">
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
                    <button v-else @click="matchNotOk" class="btn btn-secondary mb-3" style="width: 100%" :disabled="allOk()">取消确认</button>
                    <button @click="exitMatching" class="btn btn-danger mb-1" style="width: 100%" :disabled="allOk()">取消匹配</button>
                    <div v-if="youROk()" style="color: green; text-align: center; font-size: 10px">对手已准备就绪</div>
                    <div v-if="allOk()" style="width: 100%; text-align: center;">
                      <span class="spinner-border spinner-border-sm text-warning" role="status" aria-hidden="true"></span>
                      <span class="text-warning" style="font-size: small">等待Bot编译完成...</span>
                    </div>
                  </Col>
                </Row>
              </template>
            </template>
          </template>
          <template v-else>
            <Container>
              <Row>
                <!-- 0号输入口 -->
                <!-- 游戏还没结束并且还没准备好输出 -->
                <template v-if="state !== 'gameOver' && !ok0">
                  <!-- 播放录像的时候 显示决策 -->
                  <template v-if="gameMode === 'record'">
                    <h4 style="text-align: center; color: blue">
                      {{ ["↑", "→", " ↓", "←", "开始"][lastStep0] }}
                    </h4>
                  </template>
                  <!-- 不选择机器人 且 单人模式/多人模式下是自己的时候 显示键盘 -->
                  <div v-else-if="gameMode === 'single' && singleBotId[0] === 0 || gameMode === 'multi' && getMe() === 0 && selectedBotId === -1">
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
                  <!-- 选择了机器人之后 || 不是自己的时候 显示是否已经就绪 -->
                  <template v-else>
                    <h4 style="text-align: center; color: blue">
                      <!-- 是自己的时候标记自己是哪一方的 -->
                      <span v-if="getMe() === 0 && gameMode === 'multi'">·</span> <span> {{ ok0 ? "已就绪" : "未就绪" }} </span>
                    </h4>
                  </template>
                </template>
                <!-- 游戏还没结束 但输出已经准备好了 -->
                <template v-else-if="state !== 'gameOver'">
                  <h4 style="text-align: center; color: blue">
                    <span v-if="getMe() === 0 && gameMode === 'multi'">·</span> <span>{{ ok0 ? "已就绪" : "未就绪" }}</span>
                  </h4>
                </template>
                <!-- 游戏结束 -->
                <template v-else>
                  <h4 style="text-align: center; color: blue">
                    <span v-if="getMe() === 0 && gameMode === 'multi'">·</span> <span> {{ end0 }} </span>
                  </h4>
                </template>
              </Row>
            </Container>
            <hr>
            <Container>
              <Row>
                <!-- 1号输入口 -->
                <!-- 游戏还没结束并且还没准备好输出 -->
                <template v-if="state !== 'gameOver' && !ok1">
                  <!-- 播放录像的时候 显示决策 -->
                  <template v-if="gameMode === 'record'">
                    <h4 style="text-align: center; color: red">
                      {{ ["↑", "→", " ↓", "←", "开始"][lastStep1] }}
                    </h4>
                  </template>
                  <!-- 不选择机器人 且 单人模式/多人模式下是自己的时候 显示键盘 -->
                  <div v-else-if="gameMode === 'single' && singleBotId[1] === 0 || gameMode === 'multi' && getMe() === 1 && selectedBotId === -1">
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
                  <!-- 选择了机器人 || 不是自己的时候 显示是否已经就绪 -->
                  <template v-else>
                    <h4 style="text-align: center; color: red">
                      <!-- 是自己的时候标记自己是哪一方的 -->
                      <span v-if="getMe() === 1 && gameMode === 'multi'">·</span> <span> {{ ok1 ? "已就绪" : "未就绪" }} </span>
                    </h4>
                  </template>
                </template>
                <!-- 游戏还没结束 但输出已经准备好了 -->
                <template v-else-if="state !== 'gameOver'">
                  <h4 style="text-align: center; color: red">
                    <span v-if="getMe() === 1 && gameMode === 'multi'">·</span> <span>{{ ok1 ? "已就绪" : "未就绪" }}</span>
                  </h4>
                </template>
                <!-- 游戏结束 -->
                <template v-else>
                  <h4 style="text-align: center; color: red">
                    <span v-if="getMe() === 1 && gameMode === 'multi'">·</span> <span>{{ end1 }}</span>
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
      <!-- 聊天窗口 -->
      <Collapse button-style="width: 100%; border-radius: 0" collapse-id="chatroom">
        <template v-slot:button>
          交流窗口
        </template>
        <template v-slot:content>
          <div ref="chatroomRef" style="border: 1px solid #ccc; width: 100%; height: 600px; padding: 10px; box-sizing: border-box; overflow: auto">
            <div :id="`msg-${message.id}`" v-for="(message, index) in messages" class="mt-2">
              <div v-if="isShownTime(index)" style="margin: auto; background-color: rgba(3, 3, 3, 0.2); text-align: center">
                {{ message.time }}
              </div>
              <div>{{ message.username }}
                <span style="color: #aaa">
                  #{{ message.userId }}
                </span>
              </div>
              <div
                style="
                  border: 1px solid #ccc;
                  padding: 10px;
                  border-radius: 0 10px 10px 10px;"
                class="message-content"
              >{{ message.content }}</div>
            </div>
          </div>
          <div id="chatroom-input" class="mt-2" style="width: 100%; height: 50px;">
            <div style="display: inline-block; height: 100%; width: 80%;">
              <input placeholder="单条消息不超过64个字" @keyup.enter="sendTalk" v-model="toSendTalk" type="text" class="form-control" style="width: 95%; height: 95%; border-radius: 0;">
            </div>
            <div style="display: inline-block; height: 100%; width: 20%;">
              <button @click="sendTalk" class="btn btn-primary" style="height: 100%; width: 100%">发送</button>
            </div>
          </div>
        </template>
      </Collapse>
      <hr>
      <!-- 游戏介绍 -->
      <Collapse button-style="width: 100%; border-radius: 0" collapse-id="shoukai">
        <template v-slot:button>
          游戏介绍
        </template>
        <template v-slot:content>
          <h1>盘蛇</h1>
          <hr>
          <p>玩家可以操控蛇向上、向右、向下、向左移动。双方都做好决策之后才能移动。</p>
          <p>如果撞到蛇身或者墙壁，蛇将死亡，则战败。如果同时死亡，则判为平局。</p>
          <p>蛇在10步之前每走一步增长一次、第11步开始每走三步增长一次。</p>
          <Modal 
            ref="botTemplateModalRef"
            title="Bot模板"
            modalID="bot-template"
            btnClass="btn btn-primary"
            closeTitle="关闭"
            submitTitle="好了"
            :submitAction="botTemplateModalHide"
            toggle-button-style="border-radius: 0"
          >
            <template v-slot:button>
              Bot代码模板
            </template>
            <template v-slot:body>
              <select @change="changeTemplateLang" class="form-control" v-model="selectedTemplateLang">
                <option value="-1" selected>选择语言</option>
                <option v-for="lang in LANG().list" :value="lang.id">
                  {{ lang.id }}.{{ lang.lang }}
                </option>
              </select>
              <hr>
              <h2>参数说明</h2>
              <code>方向：0为上，1为右，2为下，3为左</code>
              <br>
              <code>id: 你当前是什么颜色的蛇：0为蓝色，1为红色</code>
              <br>
              <code>rows: 地图的高度</code>
              <br>
              <code>cols: 地图的宽度</code>
              <br>
              <code>step: 当前是第几步（从0开始算起）</code>
              <br>
              <code>g: 地图描述，为1时表示被占领的地方</code>
              <br>
              <code>len: 两条蛇的长度</code>
              <br>
              <code>snake: 蛇身每节所在的位置</code>
              <br>
              <hr>
              <MonacoEditor 
                ref="botTemplateEditorRef"
                height="500px"
                editor-id="bot-template"
              />
            </template>
          </Modal>
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
import { nextTick, onMounted, onUnmounted, ref } from 'vue';
import SOCKET from '../store/SOCKET';
import SnakeGame from '../script/games/snake/SnakeGame';
import Container from '../components/Container.vue';
import USER from '../store/USER';
import alert from '../script/alert';
import API from '../script/api';
import randomId from '../script/randomid';
import Modal from '../components/Modal.vue';
import MonacoEditor from '../components/MonacoEditor.vue';
import LANG from '../store/LANG';
import snakeDemo from '../templateBotCode/snakeDemo';

const parentRef = ref(null);
const canvasRef = ref(null);
const chatroomRef = ref(null);
const botTemplateModalRef = ref(null);
const botTemplateEditorRef = ref(null);

const singleBotId = ref([ null, null ]);
const hasStartSingleGaming = ref(false);

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
const opponentUserId = ref(0);
const opponentHeadIcon = ref('');
const opponentUsername = ref('');

const pagePtr = ref(1);
const allRecordList = ref([]);
const recordList = ref([]);

const lastStep0 = ref(4);
const lastStep1 = ref(4);
const myBotList = ref([]);
const selectedBotId = ref(-1);
const hasClickMatching = ref(false);
const toSendTalk = ref('');

const messages = ref([]);

const selectedTemplateLang = ref(-1);

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

const botTemplateModalHide = () => {
  botTemplateModalRef.value.hide();
};

const changeTemplateLang = () => {
  const langId = selectedTemplateLang.value;
  const lang = LANG().langs[langId].lang;
  botTemplateEditorRef.value.setLang(lang);
  botTemplateEditorRef.value.setContent(snakeDemo(langId));
};

const dbg = () => {
  setInterval(() => {
    console.log(state.value, gameMode.value);
  }, 100);
};

const isShownTime = index => {
  const msgs = messages.value;
  if (index === 0 || msgs[index].time !== msgs[index - 1].time) return true; 
  return false;
};

const turnRecordPage = idx => {
  let lst = recordList.value = [];
  const n = allRecordList.value.length;
  for (let i = idx * 4; i < Math.min((idx + 1) * 4, n); ++i) {
    lst.push(allRecordList.value[i]);
  }
  pagePtr.value = idx;
};

const lastPage = () => {
  turnRecordPage(Math.max(0, pagePtr.value - 1));
};

const nextPage = () => {
  turnRecordPage(Math.min(Math.ceil(allRecordList.value.length / 4) - 1, pagePtr.value + 1));
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

const allOk = () => {
  return iAmOk() && youROk();
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
  hasStartSingleGaming.value = true;
  hasClickMatching.value = true;
  selectedBotId.value = -1;
  let singleBotId0 = 0;
  let singleBotId1 = 0;
  if (singleBotId.value[0] !== undefined || singleBotId.value[0] !== null) {
    singleBotId0 = Math.max(singleBotId.value[0], 0);
  }
  if (singleBotId.value[1] !== undefined || singleBotId.value[1] !== null) {
    singleBotId1 = Math.max(singleBotId.value[1], 0);
  }
  singleBotId.value = [ singleBotId0, singleBotId1 ];
  SOCKET().sendMessage({
    action: 'startSingleGaming', 
    singleBotId0,
    singleBotId1 
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
  if (allOk()) return ;
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

const sendTalk = () => {
  if (toSendTalk.value.length === 0) return ;
  SOCKET().sendMessage({
    action: 'sendTalk',
    content: toSendTalk.value
  });
  toSendTalk.value = '';
};

const receivedStartSingleGaming = json => {
  hasStartSingleGaming.value = false;
  if (json.result !== "ok") {
    alert(`danger`, json.result, 1000);
    return ;
  }
  singleBotId.value = [ json.singleBotId0, json.singleBotId1 ];
  console.log(singleBotId.value);
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
  }
};

const receivedTellResult = json => {
  end0.value = json.result === 0 ? "WIN" : json.result === 1 ? "LOSE" : "DRAW";
  end1.value = json.result === 1 ? "WIN" : json.result === 0 ? "LOSE" : "DRAW";
  switch (json.result) {
    case 0:
      checker.value.setStatus(1, 'die');
      checker.value.setStatus(0, 'idle');
      break;
    case 1:
      checker.value.setStatus(0, 'die');
      checker.value.setStatus(1, 'idle');
      break;
    case -1:
      checker.value.setStatus(0, 'die');
      checker.value.setStatus(1, 'die');
      break;
  }
  state.value = "gameOver";
  let reason0 = json.reason0;
  let reason1 = json.reason1;
  let reason = (reason0 || '') + (reason0 != null && reason1 != null ? '\n' : '') + (reason1 || '');
  if (gameMode.value === 'multi') {
    switch (json.result) {
      case getMe():
        alert(`success`, `胜利！Rating +${json.score}\n战败原因: \n${reason}`, 5000);
        break;
      case 1 - getMe():
        alert(`danger`, `战败... Rating -${json.score}\n战败原因: \n${reason}`, 5000);
        break;
      case -1:
        alert(`warning`, `平局 原因: \n${reason}`, 5000);
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
  selectedBotId.value = -1;
};

const receivedSendTalk = async json => {
  if (json.result === "ok") {
    let message = {
      id: randomId(),
      userId: json.userId,
      username: json.username,
      content: json.content,
      time: json.time
    };
    messages.value.push(message);
    await nextTick();
    chatroomRef.value.scrollTop = chatroomRef.value.scrollHeight;
    let messageDiv = document.querySelector(`#msg-${message.id}>.message-content`);
    messageDiv.classList.add("new-talk");
    setTimeout(() => {
      messageDiv.classList.remove("new-talk");
    }, 500);
  } else {
    alert('danger', json.result);
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
      } else if (json.action === "sendTalk") {
        receivedSendTalk(json);
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
      allRecordList.value = resp.reverse();
      turnRecordPage(0);
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

.new-talk {
  animation-name: newTalk;
  animation-duration: 1s;
}

@keyframes newTalk {
  from {
    background-color: lightgreen;
    color: white;
  }
  
  to {
    background-color: none;
    color: black;
  }
}
</style>