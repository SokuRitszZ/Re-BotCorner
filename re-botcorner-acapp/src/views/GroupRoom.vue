<template>
  <CardBody>
    <Container>
      <Row>
        <Col col="col-4">
          <CardBody height="fit-content">
            <img :src="group.icon" style="width: 100%">
          </CardBody>
          <CardBody height="fit-content">
            <div style="height: fit-content; padding: 20px">
              <h2 style="display: inline-block">{{group.title}}</h2>
              <h3 style="display: inline-block; color: #ccc">#{{group.id}}</h3>
              <hr>
              <div style="height: 100px; overflow: auto;">{{group.description}}</div>
              <hr>
              <div style="display: inline-block">创建者：</div><div style="display: inline-block; float: right">{{group.creatorUsername}}</div>
              <div v-show="!hasApplied">
                <h6 v-if="isIn" style="color: green" class="text-center">你现在是这个小组的成员</h6>
                <button v-if="isIn && !isCreator()" :disabled="toResign" @click="resignFromGroup" class="w-100 rounded-0 btn btn-danger">退出小组</button>
                <Window
                  v-if="!isCreator() && !isIn"
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
                        <h5 style="white-space: nowrap; text-align: center; font-weight: 800; margin-top: 10px">为什么要加入小组？</h5>
                        <h5 style="white-space: nowrap; text-align: center; font-weight: 800; margin-top: 10px">填写你的验证信息</h5>
                        <hr>
                        <textarea v-model="application" class="form-control"></textarea>
                        <button @click="submitApply" class="btn btn-success mt-3" style="border-radius: 0; width: 100%">提交申请</button>
                      </template>
                      <template v-else>
                        <h5 style="text-align: center; font-weight: 800; margin-top: 10px">已提交申请，请等待结果</h5>
                      </template>
                    </div>
                  </template>
                </Window>
                <button @click="toDeleteGroup" v-if="isCreator()" class="btn btn-danger w-100 rounded-0 mt-3">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-dash-circle-fill" viewBox="0 0 16 16">
                    <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM4.5 7.5a.5.5 0 0 0 0 1h7a.5.5 0 0 0 0-1h-7z"/>
                  </svg>
                  解散小组
                </button>
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
              <table v-if="showingContest === null" class="table table-striped">
                <thead>
                <tr>
                  <td>ID</td><td>标题</td><td>游戏</td><td>时间</td><td>状态</td>
                  <td v-if="true">
                    <Window
                      ref="windowContestRef"
                      v-if="isCreator()"
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
                        <form style="padding: 10px">
                          <label class="form-label" for="contest-title">比赛标题</label>
                          <input v-model="submittedContest.title" name="contest-title" type="text" class="form-control mb-3" />
                          <label class="form-label" for="contest-game">游戏</label>
                          <select v-model="submittedContest.gameId" name="contest-game" class="form-control mb-3">
                            <option v-for="game in GAME().list" :value="game.id">{{game.title}}</option>
                          </select>
                          <label class="form-label" for="contest-format">赛制</label>
                          <select v-model="submittedContest.rule" name="contest-format" class="form-control mb-3">
                            <option value="1">两两对局，每对三局</option>
                          </select>
                          <label class="form-label" for="contest-time">比赛时间</label>
                          <input v-model="submittedContest.time" name="contest-time" type="datetime-local" class="form-control mb-3">
                          <button @click.prevent="submitContest" class="btn btn-success" style="float: right;">添加比赛</button>
                        </form>
                      </template>
                    </Window>
                  </td>
                  <td v-else></td>
                </tr>
                </thead>
                <tbody>
                <tr :key="contest.id" v-for="contest in contests">
                  <td>{{contest.id}}</td><td>{{contest.title}}</td><td>{{contest.game}}</td><td>{{contest.time}}</td><td>{{contest.state}}</td>
                  <td>
                    <button @click="gotoContest(contest)" class="btn btn-outline-secondary btn-sm">查看</button>
                    <button v-if="isCreator()" @click="removeContest(contest)" class="btn btn-danger btn-sm">删除</button>
                  </td>
                </tr>
                </tbody>
              </table>
              <ContestDetail @backContestList="backContestList" class="p-3" v-else :contest="showingContest" />
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
import {useRoute, useRouter} from "vue-router";
import Container from "../components/Container.vue";
import Row from "../components/Row.vue";
import Col from "../components/Col.vue";
import {onMounted, ref} from "vue";
import Window from '../components/Window.vue';
import router from "../routes/index.js";
import {
  applyGroupApi,
  createContestApi,
  deleteGroupApi, getContestsApi,
  getGroupByIdApi,
  getMembers, removeContestApi,
  resignFromGroupApi
} from "../script/api.js";
import USER from "../store/USER.js";
import GAME from "../store/GAME.js";
import alert from "../script/alert.js";
import timeFormat from "../script/timeFormat.js";
import ContestDetail from "./viewsChild/ContestDetail.vue";

const route = useRoute();

const group = ref({});

const contests = ref([]);
const members = ref([]);
const hasApplied = ref(false);
const shownView = ref('contestList');

const showingContest = ref(null);

const submitWindowRef = ref(null);

const gotoContest = contest => {
  showingContest.value = contest;
};

const backContestList = () => {
  showingContest.value = null;
}

const application = ref();

const submitApply = () => {
  submitWindowRef.value.close();
  applyGroupApi(group.value.id, application.value).then(resp => {
    application.value = "";
    if (resp.result === "success") {
      alert("success", resp.message);
    } else {
      alert("danger", resp.message, 2000)
    }
  });
};

const isCreator = () => {
  return USER().getUserID == group.value.creatorId;
};

const parseContest = contest => {
  contest.game = GAME().games[contest.gameId].name;
  delete contest.gameId;
  contest.time = timeFormat(new Date(contest.startTime), "yyyy-MM-dd HH:mm:ss");
  delete contest.startTime;
  contest.state = ["未进行", "正在进行", "已完成"][contest.state];
  contest.rule = ["两两对局，每对三局"][contest.rule - 1];
};

const initContests = () => {
  const id = route.params.id;
  getContestsApi(id).then(resp => {
    if (resp.result === "success") {
      const data = JSON.parse(resp.data);
      data.forEach(contest => parseContest(contest));
      data.reverse();
      contests.value = data;
    }
  });
};

const initMembers = () => {
  const id = route.params.id;
  getMembers(id).then(resp => {
    const data = JSON.parse(resp.data);
    members.value = data;
  });
};

const isIn = ref(true);

const initGroup = () => {
  const id = route.params.id;
  getGroupByIdApi(id).then(resp => {
    if (resp.result === "success") {
      let data = resp.data;
      data = JSON.parse(data);
      group.value = data;
      isIn.value = data.isIn;
    } else {
      alert("danger", resp.message, 2000);
      router.push("/group");
    }
  });
};

const toDeleteGroup = () => {
  if (confirm(`确认解散小组\n\n${group.value.title}#${group.value.id}？`)) {
    deleteGroupApi(group.value.id).then(resp => {
      if (resp.result === "success") {
        alert("success", "成功解散小组", 2000);
        router.push("/group");
      } else {
        alert("danger", resp.message, 2000);
      }
    });
  }
};

const toResign = ref(false);

const resignFromGroup = async () => {
  if (!confirm(`确认解散小组\n\n${group.value.title}#${group.value.id}？`)) return ;
  toResign.value = true;
  await resignFromGroupApi(group.value.id).then(resp => {
    if (resp.result === "success") {
      alert("success", "成功退出小组", 2000);
      router.push("/group");
    } else {
      alert("danger", resp.message, 2000);
    }
  }).catch(err => {});
  toResign.value = false;
};

const submittedContest = ref({
  title: "",
  gameId: 1,
  rule: 1,
  time: timeFormat(new Date(), "yyyy-MM-dd HH:mm:ss")
});

const windowContestRef = ref();

const initContest = () => {
  submittedContest.value = {
    title: "",
    gameId: 1,
    rule: 1,
    time: timeFormat(new Date(), "yyyy-MM-dd HH:mm:ss")
  };
};

const submitContest = () => {
  const data = submittedContest.value;
  data.time = timeFormat(new Date(data.time), "yyyy-MM-dd HH:mm:ss");
  data.groupId = route.params.id;
  windowContestRef.value.close();
  createContestApi(data).then(resp => {
    initContest();
    if (resp.result === "success") {
      const data = JSON.parse(resp.data);
      parseContest(data);
      contests.value.unshift(data);
      alert("success", "添加成功" );
    } else {
      alert("danger", resp.message, 2000);
    }
  });
};

const removeContest = contest => {
  if (!confirm(`确定要删除比赛：${contest.title}#${contest.id}？`)) return ;
  removeContestApi(contest.id).then(() => {
    alert(`success`, `删除成功`);
  }).catch(err => {
    console.log(err);
  })
}

onMounted(async () => {
  await initGroup();
  GAME().init().then(() => initContests());
  initMembers();
});

</script>

<style scoped>
div::-webkit-scrollbar {
  display: none;
}
</style>