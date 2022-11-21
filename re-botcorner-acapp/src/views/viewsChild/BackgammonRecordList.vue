<template>
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
            <td>白方</td>
            <td>红方</td>
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
          <tr v-for="record in recordList">
            <td>{{timeFormat(new Date(record.createTime), 'yyyy-MM-dd HH:mm:ss')}}</td>
            <td> <span><img :src="record.infos[0].headIcon" style="width: 30px; border-radius: 50%; padding: 1px; border: 1px solid #ccc" alt=""></span><div style="display: inline-block;">{{ record.infos[0].username }}</div></td>
            <td> <span><img :src="record.infos[1].headIcon" style="width: 30px; border-radius: 50%; padding: 1px; border: 1px solid #900" alt=""></span><div style="display: inline-block;">{{ record.infos[1].username }}</div></td>
            <td style="line-height: 30px"> {{record.result}} </td>
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
  getRecordListApi(3).then(resp => {
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