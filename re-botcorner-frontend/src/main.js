import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './routes/index';
import { createPinia } from 'pinia';

import 'bootstrap/dist/js/bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';

import 'animate.css';

String.prototype.join = function(seperator) {
  return this.split('').join(seperator);
};

const app = createApp(App);
app.use(router); /** vue-router */
app.use(createPinia()); /** pinia */
app.mount('#app');