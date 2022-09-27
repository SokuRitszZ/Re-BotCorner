<template>
  <h1>盘蛇</h1>
  <hr>
  <p>玩家可以操控蛇向上、向右、向下、向左移动。双方都做好决策之后才能移动。</p>
  <p>如果撞到蛇身或者墙壁，蛇将死亡，则战败。如果同时死亡，则判为平局。</p>
  <p>蛇在10步之前每走一步增长一次、第11步开始每走三步增长一次。</p>
  <Window
    ref="botTemplateWindowRef"
    title="SnakeBot模板"
    opacity="1"
    buttonClass="btn btn-primary"
    buttonStyle="width: 100%; border-radius: 0"
  >
    <template v-slot:button>
      Bot模板
    </template>
    <template v-slot:body>
      <div style="width:100%; padding: 20px; overflow: auto;">
        <select @change="changeTemplateLang" class="form-control" v-model="selectedTemplateLang">
          <option value="-1" selected>选择语言</option>
          <option v-for="lang in LANG().list" :value="lang.id">
            {{ lang.id }}.{{ lang.lang }}
          </option>
        </select>
        <hr>
        <h2>参数说明</h2>
        <code>方向：0为上，1为右，2为下，3为左</code>
        <br>
        <code>id: 你当前是什么颜色的蛇：0为蓝色，1为红色</code>
        <br>
        <code>rows: 地图的高度</code>
        <br>
        <code>cols: 地图的宽度</code>
        <br>
        <code>step: 当前是第几步（从0开始算起）</code>
        <br>
        <code>g: 地图描述，为1时表示被占领的地方</code>
        <br>
        <code>len: 两条蛇的长度</code>
        <br>
        <code>snake: 蛇身每节所在的位置</code>
        <br>
        <hr>
        <MonacoEditor ref="botTemplateEditorRef" height="400px" width="95%" editor-id="bot-template" />
      </div>
    </template>
  </Window>
</template>

<script setup>
import { ref } from 'vue';
import MonacoEditor from '../../components/MonacoEditor.vue';
import LANG from '../../store/LANG';
import snakeDemo from '../../templateBotCode/snakeDemo';
import Window from '../../components/Window.vue';

const botTemplateEditorRef = ref(null);
const selectedTemplateLang = ref(-1);

const changeTemplateLang = () => {
  const langId = selectedTemplateLang.value;
  const lang = LANG().langs[langId].lang;
  botTemplateEditorRef.value.setLang(lang);
  botTemplateEditorRef.value.setContent(snakeDemo(langId));
};
</script>