<template>
  <CardBody>
    <Container>
      <Row>
        <Col col="col-4">
          <CardBody height="fit-content">
            <img :src="groupHeadIcon" style="width: 100%">
          </CardBody>
          <CardBody height="fit-content">
            <div style="height: fit-content; padding: 20px">
              <h2 style="display: inline-block">{{groupName}}</h2>
              <h3 style="display: inline-block; color: #ccc">#{{groupId}}</h3>
              <hr>
              <div style="height: 100px; overflow: auto;">{{groupDescription}}</div>
              <hr>
              <div style="display: inline-block">创建者：</div><div style="display: inline-block; float: right">{{groupCreater}}</div>
              <div v-show="!hasApplied">
                <Window
                  ref="submitWindowRef"
                  buttonClass="btn btn-success mt-3"
                  title="申请加入小组"
                  buttonStyle="width: 100%; border-radius: 0"
                  :opacity="1"
                >
                  <template v-slot:button>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-person-plus-fill" viewBox="0 0 16 16">
                      <path d="M1 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H1zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"/>
                      <path fill-rule="evenodd" d="M13.5 5a.5.5 0 0 1 .5.5V7h1.5a.5.5 0 0 1 0 1H14v1.5a.5.5 0 0 1-1 0V8h-1.5a.5.5 0 0 1 0-1H13V5.5a.5.5 0 0 1 .5-.5z"/>
                    </svg>
                    申请加入
                  </template>
                  <template v-slot:body>
                    <div style="padding: 10px">
                      <template v-if="!hasApplied">
                        <h5 style="text-align: center; font-weight: 800; margin-top: 10px">为什么要加入小组？填写你的验证信息</h5>
                        <hr>
                        <textarea class="form-control"></textarea>
                        <button @click="submitApply" class="btn btn-success mt-3" style="border-radius: 0; width: 100%">提交申请</button>
                      </template>
                      <template v-else>
                        <h5 style="text-align: center; font-weight: 800; margin-top: 10px">已提交申请，请等待结果</h5>
                      </template>
                    </div>
                  </template>
                </Window>
              </div>
              <div v-show="hasApplied">
                <button @click="apply" class="btn btn-success mt-3" style="width: 100%; border-radius: 0" :disabled="hasApplied">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-check-circle-fill" viewBox="0 0 16 16">
                    <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zm-3.97-3.03a.75.75 0 0 0-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-.01-1.05z"/>
                  </svg>
                  等待申请结果
                </button>
              </div>
            </div>
          </CardBody>
        </Col>
        <Col col="col-8">
          <CardBody height="80vh">
            <ul class="nav nav-tabs">
              <li class="nav-item">
                <a @click="shownView = 'contestList'" :class="`nav-link ${shownView === 'contestList' ? 'active' : ''}`" aria-current="page" href="#">比赛</a>
              </li>
              <li class="nav-item">
                <a @click="shownView = 'groupMembers'" :class="`nav-link ${shownView === 'groupMembers' ? 'active' : ''}`" href="#">组内成员</a>
              </li>
            </ul>
            <div v-show="shownView ==='contestList'" style="height: 90%; overflow: auto;">
              <table class="table table-striped">
                <thead>
                <tr>
                  <td>ID</td><td>标题</td><td>游戏</td><td>时间</td><td>状态</td><td>参赛人数</td>
                  <td v-if="isCreater()">
                    <Window
                      button-class="btn btn-success btn-sm"
                      title="添加比赛"
                    >
                      <template v-slot:button>
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-plus-fill" viewBox="0 0 16 16">
                          <path d="M4 .5a.5.5 0 0 0-1 0V1H2a2 2 0 0 0-2 2v1h16V3a2 2 0 0 0-2-2h-1V.5a.5.5 0 0 0-1 0V1H4V.5zM16 14V5H0v9a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2zM8.5 8.5V10H10a.5.5 0 0 1 0 1H8.5v1.5a.5.5 0 0 1-1 0V11H6a.5.5 0 0 1 0-1h1.5V8.5a.5.5 0 0 1 1 0z"/>
                        </svg>
                        添加比赛
                      </template>
                      <template v-slot:body>
                        <div style="padding: 10px">
                          <label class="form-label" for="contest-title">比赛标题</label>
                          <input name="contest-title" type="text" class="form-control mb-3" />
                          <label class="form-label" for="contest-game">游戏</label>
                          <select name="contest-game" class="form-control mb-3">
                            <option value="盘蛇">盘蛇</option>
                            <option value="黑白棋">黑白棋</option>
                            <option value="西洋双陆棋">西洋双陆棋</option>
                          </select>
                          <label class="form-label" for="contest-format">赛制</label>
                          <select name="contest-format" class="form-control mb-3">
                            <option value="两两对局，每对三局">两两对局，每对三局</option>
                          </select>
                          <label class="form-label" for="contest-time">比赛时间</label>
                          <input name="contest-time" type="datetime-local" class="form-control mb-3">
                          <button class="btn btn-success" style="float: right">添加比赛</button>
                        </div>
                      </template>
                    </Window>
                  </td>
                  <td v-else></td>
                </tr>
                </thead>
                <tbody>
                <tr v-for="contest in contests">
                  <td>{{contest.id}}</td><td>{{contest.title}}</td><td>{{contest.game}}</td><td>{{contest.time}}</td><td>{{contest.state}}</td><td>{{contest.people}}</td>
                  <td>
                    <button @click="gotoContest(contest.id)" class="btn btn-outline-secondary btn-sm">查看</button>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
            <div v-show="shownView === 'groupMembers'" style="display: flex; flex-flow: wrap; height: 90%; overflow: auto">
              <div v-for="member in members" style="display: block; text-align: center">
                <img :src="member.headIcon" class="img-thumbnail" style="width: 75px; height: 75px; margin: 10px">
                <div style="margin-top: -10px; color: gray">
                  {{member.username}}
                </div>
              </div>
            </div>
          </CardBody>
        </Col>
      </Row>
    </Container>
  </CardBody>
</template>

<script setup>
import CardBody from "../components/CardBody.vue";
import {useRoute} from "vue-router";
import Container from "../components/Container.vue";
import Row from "../components/Row.vue";
import Col from "../components/Col.vue";
import {onMounted, ref} from "vue";
import {LoremIpsum} from "lorem-ipsum";
import fakeMaker from "../script/fakeMaker.js";
import Window from '../components/Window.vue';
import USER from "../store/USER.js";
import router from "../routes/index.js";

const groupName = ref(new LoremIpsum().generateWords(1));
const groupId = ref(fakeMaker({ id: { type: 'number', max: 100000 }}, 1)[0].id);
const groupDescription = ref(new LoremIpsum().generateParagraphs(1));
const groupCreater = ref(new LoremIpsum().generateWords(1));
const groupHeadIcon = ref("https://sdfsdf.dev/500x500.png");

const contests = ref([]);
const members = ref([]);
const hasApplied = ref(false);
const shownView = ref('contestList');

const submitWindowRef = ref(null);

const gotoContest = id => {
  router.push({
    name: 'contestDetail',
    params: { id }
  });
};

const isCreater = () => {
  // ;;; return true;
  console.log(USER().username, groupCreater.value);
  return USER().username === groupCreater.value;
};

const apply = () => {
  hasApplied.value = true;
};

const submitApply = () => {
  apply();
};

const initContests = () => {
  contests.value = fakeMaker({
    id: { type: 'number', max: 10000 },
    title: { type: 'word' },
    game: { type: 'word' },
    time: { type: 'time' },
    state: { type: 'custom', values: ['未开始', '进行中', '已结束'] },
    people: { type: 'number', max: 100 }
  }, 100);
};

const initMembers = () => {
  members.value = fakeMaker({
    headIcon: { type: 'image' },
    id: { type: 'number', max: 10000 },
    username: { type: 'word' }
  }, 100);
}

onMounted(() => {
  ;;; initContests();
  ;;; initMembers();
});

</script>

<style scoped>
div::-webkit-scrollbar {
  display: none;
}
</style>