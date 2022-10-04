<template>
  <h1>西洋双陆棋</h1>
  <hr>
  <p>这是一款需要结合运气与技术的游戏。</p>
  <p>可以将其理解为飞行棋，骰子多少点数即可走几步。相应的有如下规定。</p>
  <p></p>
</template>

<script setup>
import { ref } from 'vue';
import Modal from '../../components/Modal.vue';
import MonacoEditor from '../../components/MonacoEditor.vue';
import LANG from '../../store/LANG';
import reversiDemo from '../../templateBotCode/reversiDemo';

const botTemplateModalRef = ref(null);
const botTemplateEditorRef = ref(null);
const selectedTemplateLang = ref(-1);

const botTemplateModalHide = () => {
  botTemplateModalRef.value.hide();
};

const changeTemplateLang = () => {
  const langId = selectedTemplateLang.value;
  const lang = LANG().langs[langId].lang;
  botTemplateEditorRef.value.setLang(lang);
  botTemplateEditorRef.value.setContent(reversiDemo(langId));
};
</script>