<template>
  <div :style="{ height }" class="monaco-editor" :id="`editor-${editorId}`"></div>
</template>

<script setup>
import { onMounted, ref, toRaw } from 'vue';
import * as monaco from 'monaco-editor';

/** 其中这里如果使用javascript，就会出错，因为monaco是基于WebWocker实现语法处理的
 * 解决方案参见尤雨溪的回复 https://github.com/vitejs/vite/discussions/1791 
 */

import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

self.MonacoEnvironment = {
  getWorker(_, label) {
    if (label === 'json') {
      return new jsonWorker()
    }
    if (label === 'css' || label === 'scss' || label === 'less') {
      return new cssWorker()
    }
    if (label === 'html' || label === 'handlebars' || label === 'razor') {
      return new htmlWorker()
    }
    if (label === 'typescript' || label === 'javascript') {
      return new tsWorker()
    }
    return new editorWorker()
  }
}

const props = defineProps({
  height: {
    type: String,
    required: false
  },
  editorId: {
    type: String,
    required: true
  }
});

const editorRef = ref(null);

const setLang = (lang) => {
  monaco.editor.setModelLanguage(editorRef.value.getModel(), lang);
};

const getContent = () => {
  return toRaw(editorRef.value).getValue();
};

const setContent = (content) => {
  toRaw(editorRef.value).setValue(content);
};

defineExpose({
  setLang,
  getContent,
  setContent
})

onMounted(() => {
  editorRef.value = monaco.editor.create(document.getElementById(`editor-${props.editorId}`), {
    value: "",
    language: "plain text",
    automaticLayout: true,
    theme: 'vs'
  });
});
</script>