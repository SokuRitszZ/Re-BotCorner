<template>
  <CardBody>
    <Container>
      <Row>
        <Col col="col-8">
          <div style="width: 100%; height: 100%; display: flex; flex-direction: column">
            <div ref="groupRef" @click="showGroup(index)" class="group" v-for="(group, index) in groups" :style="{zIndex: index}">
              <h2 style="display: inline-block"> {{ group.name }} </h2>
              <h3 style="display: inline-block; color: #ccc">#{{ group.id }}</h3>
              <h5 style="display: inline-block; margin-left: 40px; float: right"> 创建者：{{ group.creater }}</h5>
              <hr>
              <div style="color: #ccc; height: 150px; overflow: auto;">
                {{ group.description }}
              </div>
              <button @click.stop="gotoGroup(group.id)" class="btn btn-primary" style="float: right">进去看看</button>
            </div>
          </div>
        </Col>
        <Col col="col-4">
          <Window
            button-class="btn btn-success"
            button-style="width: 100%; border-radius: 0"
            title="创建小组"
          >
            <template v-slot:button>创建小组</template>
            <template v-slot:body>
              <div style="padding: 10px">
                <label class="form-label" for="group-headicon">头像</label>
                <Cropper></Cropper>
                <div class="mb-3"></div>
                <label class="form-label" for="group-title">名字</label>
                <input class="form-control mb-3" name="group-title" type="text">
                <label class="form-label" for="group-description">描述</label>
                <textarea class="form-control mb-3" name="group-title" />
                <button class="btn btn-success" style="float: right">创建小组</button>
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
import fakeMaker from "../script/fakeMaker.js";
import Window from '../components/Window.vue';
import Cropper from "../components/Cropper.vue";

const groupRef = ref(null);

const groups = ref([]);
const joinedGroups = ref([]);

const showGroup = index => {
  const dom = groupRef.value[index];
  if (dom.classList.contains(`group-shown`)) {
    dom.classList.remove(`group-shown`);
  } else {
    dom.classList.add(`group-shown`);
  }
};

const gotoGroup = id => {
  router.push({
    name: 'groupDetail',
    params: {
      id
    }
  });
};

const initGroups = () => {
  groups.value = fakeMaker({
    name: { type: 'word' },
    id: { type: 'number', max: 100000 },
    description: { type: 'paragraph' },
    creater: { type: 'word' }
  }, 100);
  joinedGroups.value = fakeMaker({
    name: { type: 'word' },
    id: { type: 'number', max: 100000 },
    description: { type: 'paragraph' },
    creater: { type: 'word' }
  }, 10);
};

onMounted(() => {
  ;;; initGroups();
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