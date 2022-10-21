<template>
  <h1 class="text-center">黑白棋</h1>
  <hr>
  <p>玩家可以落子在棋盘上，相应的也有规定。简明规定如下：</p>
  <p>1、落子的地方，必须满足在八个方向上，存在某方向是有自己的子，并且与这个子之间的路上全部都是敌子（不能没有敌子）。</p>
  <p>2、在合法的地方落子之后，会<strong>分别往八个方向上</strong>一路寻找，直到遇见第一个自己的子，如果中途有空位则不会在这方向上做任何操作；如果遇到了自己的子则停下，落子位与此子之间的敌子都会变为自己的子。</p>
  <p>3、如果棋盘上有空位但只有一种子，那么该子胜利。</p>
  <p>4、如果当前所执的子无处可下但棋盘上还有自己的子，则会跳过让对手下。</p>
  <p>5、如果棋盘填满了，那么数量多者胜利；数量相同则平局。</p>
  <p>6、如果看不懂，可以访问此处<a target="_blank" href="https://wiki.botzone.org.cn/index.php?title=Reversi">黑白棋规则</a></p>
  <Modal ref="botTemplateModalRef" title="Bot模板" modalID="bot-template" btnClass="btn btn-primary" closeTitle="关闭"
    submitTitle="好了" :submitAction="botTemplateModalHide" toggle-button-style="border-radius: 0">
    <template v-slot:button>
      Bot代码模板
    </template>
    <template v-slot:body>
      <select @change="changeTemplateLang" class="form-control" v-model="selectedTemplateLang">
        <option value="-1" selected>选择语言</option>
        <option v-for="lang in LANG().list" :value="lang.id">
          {{ lang.id }}.{{ lang.lang }}
        </option>
      </select>
      <hr>
      <h2>参数说明</h2>
      <code>方向：从0开始算起，12点钟方向开始，依次顺时针45度。</code>
      <br>
      <code>id: 你是什么子，0为黑子，1为白子</code>
      <br>
      <code>rows: 棋盘高度</code>
      <br>
      <code>cols: 棋盘宽度</code>
      <br>
      <code>chess: 棋盘以及棋盘上的子，0为黑子，1为白子，2为空位</code>
      <hr>
      <MonacoEditor ref="botTemplateEditorRef" height="500px" editor-id="bot-template" />
    </template>
  </Modal>
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