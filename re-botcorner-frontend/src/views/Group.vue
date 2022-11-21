<template>
  <CardBody height="80vh">
    <Row>
      <Col col="col-8">
        <div>
          <div style="width: 100%; height: 100%; display: flex; flex-direction: column; ">
            <div :key="group.id" ref="groupRef" @click="showGroup" class="group" v-for="(group, index) in groups" :style="{zIndex: index}">
              <h2 style="display: inline-block"> {{ group.title }} </h2>
              <h3 style="display: inline-block; color: #ccc">#{{ group.id }}</h3>
              <h5 style="display: inline-block; margin-left: 40px; float: right"> 创建者：{{ group.creatorUsername }}</h5>
              <hr>
              <div style="color: #ccc; height: 150px; overflow: auto;">
                {{ group.description }}
              </div>
              <button @click.stop="gotoGroup(group)" class="btn btn-primary" style="float: right">进去看看</button>
            </div>
          </div>
        </div>
      </Col>
      <Col col="col-4">
        <input placeholder="查找小组" @input="searchGroup" type="text" name="search-group" class="form-control mt-2" v-model="inputSearchGroup">
        <hr>
        <Window
          ref="createGroupWindowRef"
          button-class="btn btn-success"
          button-style="width: 100%; border-radius: 0"
          title="创建小组"
        >
          <template v-slot:button>创建小组</template>
          <template v-slot:body>
            <div style="padding: 10px">
              <label class="form-label" for="group-headicon">头像</label>
              <Cropper ref="cropperRef" @cut="submitCreateGroup" />
              <div class="mb-3"></div>
              <label class="form-label" for="group-title">名字</label>
              <input v-model="title" class="form-control mb-3" name="group-title" type="text">
              <label class="form-label" for="group-description">描述</label>
              <textarea v-model="description" class="form-control mb-3" name="group-title" />
              <button @click="handleSubmitCreateGroup" class="btn btn-success" style="float: right">创建小组</button>
            </div>
          </template>
        </Window>
        <hr>
        <Window
          button-class="btn btn-primary"
          button-style="width: 100%; border-radius: 0"
          title="申请"
        >
          <template v-slot:button>申请表</template>
          <template v-slot:body>
            <div class="p-3" v-if="applications.length === 0">
              <h3>目前没有申请加入你的小组。</h3>
            </div>
            <div v-else class="p-3">
              <transition-group name="app">
                <div :key="application" v-for="application in applications" style="font-size: 20px; overflow: hidden; background-color: #eee; height: fit-content; padding: 15px; box-sizing: border-box" class="shadow-lg application">
                  <img :src="application.applicantHeadIcon" style="width: 40px; height: 40px;" class="rounded-circle" alt="">
                  {{application.applicantUsername}}<span style="color: gray;">#{{application.applicantId}}</span>
                  <span style="color: green; padding: 10px;">
                    <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" fill="currentColor" class="bi bi-send-plus-fill" viewBox="0 0 16 16">
                      <path d="M15.964.686a.5.5 0 0 0-.65-.65L.767 5.855H.766l-.452.18a.5.5 0 0 0-.082.887l.41.26.001.002 4.995 3.178 1.59 2.498C8 14 8 13 8 12.5a4.5 4.5 0 0 1 5.026-4.47L15.964.686Zm-1.833 1.89L6.637 10.07l-.215-.338a.5.5 0 0 0-.154-.154l-.338-.215 7.494-7.494 1.178-.471-.47 1.178Z"/>
                      <path d="M16 12.5a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0Zm-3.5-2a.5.5 0 0 0-.5.5v1h-1a.5.5 0 0 0 0 1h1v1a.5.5 0 0 0 1 0v-1h1a.5.5 0 0 0 0-1h-1v-1a.5.5 0 0 0-.5-.5Z"/>
                    </svg>
                  </span>
                  <img :src="application.groupIcon" style="width: 40px; height: 40px;" class="rounded-circle" alt="">
                  {{application.groupTitle}}<span style="color: gray;">#{{application.groupId}}</span>
                  <div style="display: flex; flex-direction: row-reverse">
                    <button @click="reject(application)" class="btn btn-danger" style="margin: 0 5px">拒绝</button>
                    <button @click="allow(application)" class="btn btn-success">同意</button>
                  </div>
                </div>
              </transition-group>
            </div>
          </template>
        </Window>
        <hr>
        <h1 style="text-align: center">你加入的小组</h1>
        <hr>
        <div style="width: 100%; height: 100%; display: flex; flex-direction: column">
          <div @click="gotoGroup(group.id)" class="group" v-for="(group, index) in joinedGroups" :style="{zIndex: index}">
            <h2 style="text-align: center">{{group.name}}</h2>
          </div>
        </div>
        <hr>
      </Col>
    </Row>
  </CardBody>
</template>

<script setup>
import CardBody from "../components/CardBody.vue";
import Row from "../components/Row.vue";
import Col from "../components/Col.vue";
import Container from "../components/Container.vue";
import {createStaticVNode, onMounted, ref} from "vue";
import router from "../routes/index.js";
import Window from '../components/Window.vue';
import Cropper from "../components/Cropper.vue";
import {createGroupApi, getApplicationApi, getGroupListApi, handleApplicationApi} from "../script/api.js";
import alert from "../script/alert.js";

const groupRef = ref([]);

const allGroups = ref([]);
const groups = ref([]);
const joinedGroups = ref([]);
const cropperRef = ref();

const showGroup = e => {
  let dom = e.target;
  while (!dom.classList.contains("group")) dom = dom.parentNode;
  if (dom.classList.contains(`group-shown`)) dom.classList.remove(`group-shown`);
  else dom.classList.add(`group-shown`);
};

const gotoGroup = group => {
  router.push({
    name: 'groupRoom',
    params: {
      id: group.id,
    }
  });
};

const initGroups = () => {
  getGroupListApi().then(resp => {
    let data = JSON.parse(resp.data);
    for (let group of data) group.createTime = new Date(group.createTime);
    groups.value = data;
    allGroups.value = data;
  });
};

const handleSubmitCreateGroup = () => {
  cropperRef.value.cut();
};

const title = ref();
const description = ref();
const creatingGroup = ref(false);
const createGroupWindowRef = ref();

const submitCreateGroup = async file => {
  const data = new FormData();
  data.append("file", file);
  data.append("title", title.value);
  data.append("description", description.value);
  creatingGroup.value = true;
  await createGroupApi(data).then(resp => {
    if (resp.result === "success") {
      let data = JSON.parse(resp.data);
      groups.value.push(data);
      createGroupWindowRef.value.close();
      title.value = "";
      description.value = "";
      alert("success", "创建成功", 2000);
    } else {
      alert("danger", resp.message, 3000);
    }
    creatingGroup.value = false;
  }).catch(err => {
    alert("warning", "请刷新页面再进行创建", 2000)
  });
};

const applications = ref([]);

const initApplication = () => {
  getApplicationApi().then(resp => {
    if (resp.result === "success") applications.value = JSON.parse(resp.data);
    else alert("danger", resp.message, 2000);
  });
};

const reject = app => {
  handleApplicationApi(app.groupId, app.applicantId, false).then(resp => {
    const apps = applications.value;
    if (resp.result === "success") {
      apps.splice(apps.indexOf(app), 1);
    } else {
      alert("danger", resp.message, 2000);
    }
  });
};

const allow = app => {
  handleApplicationApi(app.groupId, app.applicantId, true).then(resp => {
    const apps = applications.value;
    if (resp.result === "success") {
      apps.splice(apps.indexOf(app), 1);
    } else {
      alert("danger", resp.message, 2000);
    }
  });
};

const inputSearchGroup = ref("");

const searchGroup = () => {
  const pattern = ".*" + (inputSearchGroup.value.join(".*")) + ".*";
  const regex = new RegExp(pattern);
  groups.value = allGroups.value.filter(group => regex.test(group.title));
};

onMounted(() => {
  initGroups();
  initApplication();
});

</script>

<style scoped>

.group {
  background-color: #fff;
  color: #000;
  width: 100%;
  height: 100px;
  margin-top: -40px;
  border: 1px solid #ccc;
  padding: 20px;
  box-shadow: 0 0 10px #ccc;
  transition: 0.5s;
  cursor: pointer;
  overflow: hidden;
  position: relative;
}

.group:hover {
  margin-bottom: 50px;
}

.group>*::-webkit-scrollbar {
  display: none;
}

.group-shown {
  height: 300px;
}

.group:first-child {
  margin-top: 0;
}

.group-enter-action {
  animation: flipInX 0.2s;
}

.app-enter-active {
  animation: flipInX 0.5s;
}

.app-leave-active {
  animation: flipOutX 0.5s;
}

@keyframes groupEnter {
  from {
    height: 0
  }

  to {
    height: fit-content;
  }
}
</style>