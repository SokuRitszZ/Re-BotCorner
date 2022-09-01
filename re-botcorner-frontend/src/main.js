import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './routes/index';
import { createPinia } from 'pinia';

import 'highlight.js/styles/github.css';
import 'highlight.js/lib/common';
import hljsVuePlugin from '@highlightjs/vue-plugin'

import 'bootstrap/dist/js/bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';

const app = createApp(App);
app.use(router); /** vue-router */
app.use(createPinia()); /** pinia */
app.use(hljsVuePlugin);
app.mount('#app');