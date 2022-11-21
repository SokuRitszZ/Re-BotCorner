<template>
  <Collapse button-style="width: 100%; border-radius: 0;" collapse-id="recent-match" otherStyle="height: 30vh; overflow: auto">
    <template v-slot:button>
      最近比赛
    </template>
    <template v-slot:content>
      <div style="height: 600px; width: 100%; overflow: auto">
        <table class="table table-striped">
          <thead>
          <tr>
            <td>时间</td>
            <td>蓝方</td>
            <td>红方</td>
            <td>胜者</td>
            <td>
              <button :disabled="hasInitRecordList" @click="initRecordList" class="btn btn-secondary" style="padding: 0; width: 25px; line-height: 25px; border-radius: 5px;"><i class="bi bi-arrow-repeat"></i></button>
            </td>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(record, index) in recordList">
            <td>{{ timeFormat(new Date(record.createTime), "yyyy-MM-dd HH:mm:ss") }}</td>
            <td> <span><img :src="record.infos[0].headIcon" style="width: 30px; border-radius: 50%; padding: 1px; border: 1px solid blue" alt=""></span><div style="display: inline-block; margin-left: 5px;">{{ record.infos[0].username }}</div></td>
            <td> <span><img :src="record.infos[1].headIcon" style="width: 30px; border-radius: 50%; padding: 1px; border: 1px solid red" alt=""></span><div style="display: inline-block; margin-left: 5px;">{{ record.infos[1].username }}</div></td>
            <td>{{record.result}}</td>
            <td>
              <button @click="playRecord(record)" class="btn btn-primary text-center p-0" style="width: 30px; height: 30px;">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-play-fill" viewBox="0 0 16 16">
                  <path d="m11.596 8.697-6.363 3.692c-.54.313-1.233-.066-1.233-.697V4.308c0-.63.692-1.01 1.233-.696l6.363 3.692a.802.802 0 0 1 0 1.393z"/>
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
</template>

<script setup>
import {onMounted, ref} from "vue";
import alert from "../../script/alert.js";
import {getRecordListApi} from "../../script/api.js";
import Collapse from "../../components/Collapse.vue";
import timeFormat from "../../script/timeFormat.js";

const hasInitRecordList = ref(false);
const recordList = ref([]);
const allRecordList = ref([]);
const pagePtr = ref(0);

const initRecordList = () => {
  hasInitRecordList.value = true;
  getRecordListApi(1).then(resp => {
    const list = JSON.parse(resp.data);
    allRecordList.value = list.reverse();
    turnRecordPage(0);
    hasInitRecordList.value = false;
  })
  .catch(err => {
    alert(`danger`, `获取录像失败`, 1000);
    hasInitRecordList.value = false;
  });
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

const playRecord = record => emit('play-record', record);

const addRecord = record => allRecordList.value.unshift(record);

const flush = () => {
  turnRecordPage(pagePtr.value);
}

const emit = defineEmits([
  "play-record"
]);

onMounted(() => {
  initRecordList();
});

defineExpose({
  addRecord,
  flush
});

</script>

<style scoped>

</style>