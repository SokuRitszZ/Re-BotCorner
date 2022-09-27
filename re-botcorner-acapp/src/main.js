import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { createPinia } from 'pinia';

import 'bootstrap/dist/js/bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';

import 'animate.css';

const app = createApp(App);
app.use(createPinia()); /** pinia */
app.mount('#app');