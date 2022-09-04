<template>
  <CardBody>
    <Row>
      <Col col="col-4">
      <Collapse button-style="width: 100%; border-radius: 0" collapse-id="info">
        <template v-slot:button>
          <template v-if="state === 'matching'">
            匹配信息
          </template>
          <template v-else>
            局面信息
          </template>
        </template>
        <template v-slot:content>
          <template v-if="state === 'matching'">
            <Row>
              <Col><button @click="isSingleMode = true" style="width: 100%; border-radius: 0;"
                class="btn btn-secondary">单人练习</button></Col>
              <Col><button @click="isSingleMode = false" style="width: 100%; border-radius: 0;"
                class="btn btn-secondary">寻找对手</button></Col>
            </Row>
            <hr>
            <template v-if="isSingleMode">
              <button @click="startSingleGaming" style="width: 100%; border-radius: 0;"
                class="btn btn-success">游戏开始</button>
            </template>
            <template v-else>
              寻找对手
            </template>
          </template>
          <template v-else>
            <template v-if="gameMode === 'single'">
              <Container>
                <Row>
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
                    <button class="btn btn-success" @click="getChoose0">
                      好
                    </button>
                  </Col>
                </Row>
              </Container>
              <hr>
              <Container>
                <Row>
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
                    <button class="btn btn-success" @click="getChoose1">
                      好
                    </button>
                  </Col>
                </Row>
              </Container>
            </template>
            <template v-else>
              multiPlaying
            </template>
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

const parentRef = ref(null);
const canvasRef = ref(null);

const game = ref(null);
const checker = ref(null);
const isSingleMode = ref(true);
const state = ref("matching");
const gameMode = ref('');
const choose0 = ref(0);
const choose1 = ref(0);

const startSingleGaming = () => {
  /** 向后端发出开始游戏的信息（单人） */
  SOCKET().sendMessage({
    action: 'startSingleGaming'
  });
};

const getChoose0 = () => {
  console.log(choose0.value);
};

const getChoose1 = () => {
  console.log(choose1.value);
}

const receivedStartSingleGaming = json => {
  const userId = USER().getUserID;
  game.value.start({
    map: json.map,
    userId0: userId,
    userId1: userId
  });
  state.value = "waitingInput";
  gameMode.value = "single";
}

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
      if (json.action === "startSingleGaming") {
        receivedStartSingleGaming(json);
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
  /* margin: auto; */
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>