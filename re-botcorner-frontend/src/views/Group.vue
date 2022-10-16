<template>
  <CardBody>
    <Container>
      <Row>
        <Col col="col-8">
          <div style="width: 100%; height: 100%; display: flex; flex-direction: column">
            <div ref="groupRef" @click="showGroup(index)" class="group" v-for="(group, index) in groups" :style="{zIndex: index}">
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
        </Col>
        <Col col="col-4">
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
    </Container>
  </CardBody>
</template>

<script setup>
import CardBody from "../components/CardBody.vue";
import Row from "../components/Row.vue";
import Col from "../components/Col.vue";
import Container from "../components/Container.vue";
import {onMounted, ref} from "vue";
import router from "../routes/index.js";
import Window from '../components/Window.vue';
import Cropper from "../components/Cropper.vue";
import api, {createGroupApi, getGroupListApi} from "../script/api.js";
import alert from "../script/alert.js";

const groupRef = ref(null);

const groups = ref([]);
const joinedGroups = ref([]);
const cropperRef = ref();

const showGroup = index => {
  const dom = groupRef.value[index];
  if (dom.classList.contains(`group-shown`)) {
    dom.classList.remove(`group-shown`);
  } else {
    dom.classList.add(`group-shown`);
  }
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
  })
};

const handleSubmitCreateGroup = () => {
  cropperRef.value.cut();
};

const title = ref();
const description = ref();
const creatingGroup = ref(false);
const createGroupWindowRef = ref();

const submitCreateGroup = file => {
  const data = new FormData();
  data.append("file", file);
  data.append("title", title.value);
  data.append("description", description.value);
  creatingGroup.value = true;
  createGroupApi(data).then(resp => {
    if (resp.result === "success") {
      let data = JSON.parse(resp.data);
      console.log(data);
      groups.value.push(data);
      createGroupWindowRef.value.close();
      title.value = "";
      description.value = "";
      alert("success", "创建成功", 2000);
    } else {
      alert("danger", resp.message, 3000);
    }
    creatingGroup.value = false;
  });
};

onMounted(() => {
  initGroups();
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
</style>