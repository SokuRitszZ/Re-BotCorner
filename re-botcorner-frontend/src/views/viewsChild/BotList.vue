<template>
  <Container>
    <Row>
      <Col col="col-5">
      <CardBody height="55vh">
        <Modal ref="addBotModalRef" :title="`添加Bot`" :modalID="`addBotModal`" :btnClass="`btn btn-success`"
          :closeTitle="`关闭`" :submitTitle="`添加`" :submitAction="addBot"
          :toggleButtonStyle="`width: 100%; border-radius: 0`">
          <template v-slot:button>
            添加Bot
          </template>
          <template v-slot:body>
            <div class="mb-3">
              <label for="newbot-title" class="form-label">名字</label>
              <input type="text" class="form-control" id="newbot-title" v-model="addBotTitle" placeholder="不超过8个字">
            </div>
            <label for="newbot-game-id" class="form-label">游戏</label>
            <select id="newbot-game-id" class="form-select mb-3" v-model="addBotGame">
              <option selected :value="-1">选择游戏</option>
              <option v-for="game in GAME().list" :value="game.id">
                {{  game.id  }}.{{  game.title  }}
              </option>
            </select>
            <label for="newbot-lang" class="form-label">语言</label>
            <select @change="changeLang" id="newbot-lang" class="form-select mb-3" v-model="addBotLang">
              <option selected :value="-1">选择语言</option>
              <option v-for="lang in LANG().list" :value="lang.id">
                {{  lang.id  }}.{{  lang.lang  }}
              </option>
            </select>
            <div class="mb-3">
              <label for="newbot-description" class="form-label">描述</label>
              <textarea class="form-control" id="newbot-description" rows="3" v-model="addBotDescription" placeholder="不超过128个字"></textarea>
            </div>
            <label for="newbot-code" class="form-label">代码</label>
            <MonacoEditor ref="addBotEditorRef" height="30vh" editorId="addBotEditor" />
          </template>
          <template v-slot:footer>
            <div v-if="addBotLoading" class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </template>
        </Modal>
        <hr>
        <div style="height: 90% ; overflow: auto;">
          <button @click="chooseBot(bots.indexOf(bot))" v-for="bot in bots" class="btn btn-light mb-2" style="border: 1px solid #ccc">
            <span style="float: left">
              <img :src="`${api_url[mode]}/static/lang/lang_${LANG().langs[bot.langId].lang}.png`" alt="" width="50">
            </span>
            <span>
              <strong class="title">{{  bot.title  }}</strong>
              <span class="id" style="color: gray">#{{  bot.id  }}</span>
            </span>
          </button>
        </div>
      </CardBody>
      </Col>
      <Col col="col-7">
      <CardBody height="55vh">
        <template v-if="ptr !== -1">
          <div class="mb-3">
            <template v-if="!isToDelete">
              <button @click="toDelete" class="btn btn-danger">删除</button>
            </template>
            <template v-else>
              <button @click="notToDelete" class="btn btn-secondary mb-3">再想想</button>
              <button @click="readyToDelete" class="btn btn-danger">真要删</button>
            </template>
          </div>
          <hr>
          <div class="mb-3">
            <label for="bot-title" class="form-label">名字</label>
            <input @focus="backupTitle" @blur="updateTitle"
              type="text" class="form-control" id="bot-title" v-model="bots[ptr].title">
          </div>
          <div class="mb-3">
            <label for="bot-description" class="form-label">描述</label>
            <textarea @focus="backupDescription" @blur="updateDescription" class="form-control" id="bot-description" rows="3" v-model="bots[ptr].description"></textarea>
          </div>
          <div class="mb-3">
            <label for="bot-create-time" class="form-label">创建时间</label>
            <input disabled type="text" class="form-control" id="bot-create-time"
              :value="timeFormat(bots[ptr].createTime)">
          </div>
          <div class="mb-3">
            <label for="bot-modify-time" class="form-label">最近修改时间</label>
            <input disabled type="text" class="form-control" id="bot-modify-time"
              :value="timeFormat(bots[ptr].modifyTime)">
          </div>
          <Modal ref="modifyModalRef" :title="`${bots[ptr].title}#${bots[ptr].id}`" :modalID="`bot${bots[ptr].id}`"
            :btnClass="`btn btn-primary`" :closeTitle="`关闭`" :submitTitle="`改好了`"
            :toggleButtonStyle="`width: 100%; border-radius: 0`"
            :closeAction="recoverCode"
            :submitAction="updateCode"
          >
            <template v-slot:button>
              代码
            </template>
            <template v-slot:body>
              <MonacoEditor ref="modifyBotEditorRef" :height="`50vh`" editorId="modifyBotEditor" />
            </template>
            <template v-slot:footer>
              <div v-if="updateCodeLoading" class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
            </template>
          </Modal>
        </template>
      </CardBody>
      </Col>
    </Row>
  </Container>
</template>

<script setup>
import Container from '../../components/Container.vue';
import Row from '../../components/Row.vue';
import Col from '../../components/Col.vue';
import CardBody from '../../components/CardBody.vue';
import { nextTick, ref } from 'vue';
import Modal from '../../components/Modal.vue';
import timeFormat from '../../script/timeFormat.js';
import GAME from '../../store/GAME.js';
import LANG from '../../store/LANG.js';
import MonacoEditor from '../../components/MonacoEditor.vue';
import { addBotApi, deleteBotApi, updateBotApi } from '../../script/api';
import alert from '../../script/alert';
import { mode, api_url } from '../../config.json';

const props = defineProps({
  bots: {
    type: Array,
  }
});

const ptr = ref(-1);
const isToDelete = ref(false);
const addBotTitle = ref(null);
const addBotLang = ref(-1);
const addBotGame = ref(-1);
const addBotDescription = ref(null);
const updateCodeLoading = ref(false);
const addBotLoading = ref(false);

const addBotModalRef = ref(null);
const addBotEditorRef = ref(null);
const modifyModalRef = ref(null);
const modifyBotEditorRef = ref(null);

const bkupTitle = ref(null);
const bkupDescription = ref(null);

/** 增 */

const addBot = () => {
  if (addBotLoading.value) return ;
  addBotLoading.value = true;
  const title = addBotTitle.value;
  const langId = addBotLang.value;
  const gameId = addBotGame.value;
  const description = addBotDescription.value;
  const code = addBotEditorRef.value.getContent();
  addBotApi(title, langId, gameId, description, code)
  .then(resp => {
    if (resp.result === 'success') {
      alert(`success`, `添加成功`);
      props.bots.push({
        title,
        langId,
        gameId,
        description: resp.description,
        code,
        rating: resp.rating,
        userId: parseInt(resp.userId),
        id: parseInt(resp.id),
        createTime: new Date(resp.createTime),
        modifyTime: new Date(resp.modifyTime)
      });
      addBotModalRef.value.hide();
      addBotTitle.value = null;
      addBotLang.value = null;
      addBotGame.value = null;
      addBotDescription.value = null;
      addBotEditorRef.value.setContent('');
    } else {
      alert(`danger`, `添加失败：${resp.result}`, 10000);
    }
    addBotLoading.value = false;
  });
};

/** 删 */

const toDelete = () => {
  isToDelete.value = true;
}

const notToDelete = () => {
  isToDelete.value = false;
}

const readyToDelete = () => {
  deleteBotApi(props.bots[ptr.value].id)
  .then(resp => {
    if (resp.result === "success") {
      alert(`success`, `删除成功`);
      props.bots.splice(ptr.value, 1);
      ptr.value = -1;
      isToDelete.value = false;
    } else {
      alert(`danger`, `删除失败：${resp.result}`, 3000);
    }
  });
}

/** 改 */

const backupTitle = () => {
  bkupTitle.value = props.bots[ptr.value].title;
};

const updateTitle = () => {
  const newTitle = props.bots[ptr.value].title;
  if (newTitle === bkupTitle.value) return ;
  updateBotApi(props.bot[ptr.value].id, { title: newTitle})
  .then(resp => {
    if (resp.result === "success") {
      props.bots[ptr.value].title = newTitle;
      props.bots[ptr.value].modifyTime = new Date(resp.modifyTime);
      alert(`success`, `修改成功`);
    } else {
      props.bots[ptr.value].title = bkupTitle.value;
      alert(`danger`, `修改失败：${resp.result}`, 3000);
    }
  });
};

const backupDescription = () => {
  bkupDescription.value = props.bots[ptr.value].description;
};

const updateDescription = () => {
  const newDescription = props.bots[ptr.value].description;
  if (newDescription === bkupDescription.value) return ;
  updateBotApi(props.bot[ptr.value].id, { description: newDescription })
  .then(resp => {
    if (resp.result === "success") {
      props.bots[ptr.value].description = newDescription;
      props.bots[ptr.value].modifyTime = new Date(resp.modifyTime);
      alert(`success`, `修改成功`);
    } else {
      props.bots[ptr.value].description = bkupDescription.value;
      alert(`danger`, `修改失败：${resp.result}`, 3000);
    }
  });
};

const recoverCode = () => {
  modifyBotEditorRef.value.setContent(props.bots[ptr.value].code);
};

const updateCode = () => {
  if (updateCodeLoading.value) return ;
  const newCode = modifyBotEditorRef.value.getContent();
  if (newCode === props.bots[ptr.value].code) {
    modifyModalRef.value.hide();
    return ;
  }
  updateCodeLoading.value = true;
  updateBotApi(props.bot[ptr.value].id, { code: newCode })
  .then(resp => {
    if (resp.result === "success") {
      props.bots[ptr.value].code = newCode;
      props.bots[ptr.value].modifyTime = new Date(resp.modifyTime);
      alert(`success`, `修改成功`);
      updateCodeLoading.value = false;
      modifyModalRef.value.hide();
    } else {
      updateCodeLoading.value = false;
      alert(`danger`, `修改失败：\n${resp.result}`, 3000);
    }
  });
};

const chooseBot = async index => {
  ptr.value = (ptr.value === index) ? -1 : index;
  if (ptr.value !== -1) {
    const lang = LANG().langs[props.bots[ptr.value].langId].lang;
    const content = props.bots[ptr.value].code;
    /** 这里需要等待editorRef加载完成之后才能设置，否则设置的时候仍是空的 */
    await nextTick();
    modifyBotEditorRef.value.setLang(lang);
    modifyBotEditorRef.value.setContent(content);
  }
};

const changeLang = () => {
  if (addBotLang.value === -1) return;
  const lang = LANG().langs[addBotLang.value].lang;
  addBotEditorRef.value.setLang(lang);
};

</script>

<style scoped>
*::-webkit-scrollbar {
  display: none;
}

* {
  scrollbar-width: none;
}

.btn {
  border-radius: 0;
  width: 100%;
}

textarea {
  height: 100px;
  resize: none;
}
</style>