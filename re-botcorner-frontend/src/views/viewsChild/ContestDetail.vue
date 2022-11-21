<template>
  <div>
    <div class="text-md-center">
      <div style="display: inline-block; font-size: 30px; font-weight: 800;" class="text-center w-75">{{contest.title}}</div>
      <button @click="backContestList" class="btn btn-secondary float-end">返回</button>
    </div>
    <hr>
    <Collapse
      collapse-id="baseInfo"
      button-style="width: 100%; border-radius: 0"
    >
      <template v-slot:button>
        基本信息
      </template>
      <template v-slot:content>
        <div v-for="(value, key) in contest">
          <div class="entry" style="font-size: 1rem; display: grid; grid: auto / 30% 70%">
            <div class="text-end p-2 text-md-end" style="color: gray">
              <label class="form-control form-label">
                {{turn(key)}}:
              </label>
            </div>
            <div class="text-start p-2 text-md-start">
              <input class="form-control" :name="key" type="text" disabled :value="value">
            </div>
          </div>
        </div>
        <div>
          <div class="entry" style="font-size: 1rem; display: grid; grid: auto / 30% 70%">
            <div class="text-end p-2 text-md-end" style="color: gray">
              <label class="form-control form-label">
                选择出战的Bot
              </label>
            </div>
            <div class="text-start p-2 text-md-start">
              <select class="form-control">
                <option>

                </option>
              </select>
            </div>
          </div>
        </div>
      </template>
    </Collapse>
    <hr>
    <Collapse
      collapse-id="rank"
      button-style="width: 100%; border-radius: 0"
    >
      <template v-slot:button>
        总排名
      </template>
      <template v-slot:content>
        总排名信息
      </template>
    </Collapse>
    <hr>
    <Collapse
      collapse-id="records"
      button-style="width: 100%; border-radius: 0"
    >
      <template v-slot:button>
        录像
      </template>
      <template v-slot:content>
        录像
      </template>
    </Collapse>
  </div>
</template>

<script setup>
import {onMounted} from "vue";
import Collapse from "../../components/Collapse.vue";

const props = defineProps({
  contest: {
    required: true
  }
});

const turn = key => {
  switch (key){
    case "id": return "编号";
    case "groupId": return "小组编号";
    case "gameId": return "游戏编号";
    case "rule": return "赛制";
    case "creatorId": return "小组组长";
    case "state": return "状态";
    case "game": return "游戏";
    case "time": return "开始时间";
    case "title": return "标题";
  }
};

const emits = defineEmits(['backContestList'])

const backContestList = () => {
  emits('backContestList');
}

onMounted(() => {
});
</script>

<style scoped>
</style>