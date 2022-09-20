<template>
  <Navbar>
    <template v-slot:left>
      <NavbarItem> 
        <router-link class="nav-link" to="/game"> 游戏 </router-link>
      </NavbarItem>
      <NavbarItem> 
        <router-link class="nav-link" to="/rating"> 排行榜 </router-link>
      </NavbarItem>
    </template>
    <template v-slot:right>
      <template v-if="USER().checkIsPulling">
        正在自动登录...
      </template>
      <template v-else-if="USER().checkIsLogined">
        <NavbarItem :classes="['dropstart']">
          <a class="nav-link dropdown-toggle" href="#" id="navbarDropdownMenuLink" role="button"
            data-bs-toggle="dropdown" aria-expanded="false">
            {{ USER().getUsername }}
          </a>
          <ul class="dropdown-menu" aria-labelledby="navbarDropdownMenuLink">
            <li> <div @click="gotoSpace" class="dropdown-item">个人空间</div> </li>
            <li> <div @click="logout" class="dropdown-item">注销</div> </li>
          </ul>
        </NavbarItem>
      </template>
      <template v-else>
        <div class="d-flex">
          <input v-model="username" class="form-control-sm form-control me-2" placeholder="用户名">
          <input v-model="password" type="password" class="form-control-sm form-control me-2" placeholder="密码">
          <button @click="handleLogin" class="me-2 btn btn-outline-success btn-sm">Login</button>
          <Modal ref="registerModalRef" :title="`Register`" :modalID="`Register`" :btnClass="`btn btn-outline-primary btn-sm`"
            :closeTitle="`关闭`" :submitTitle="`提交`" :submitAction="toRegister">
            <template v-slot:button>
              Register
            </template>
            <template v-slot:body>
              <div class="mb-3 row">
                <label for="username" class="col-sm-3 col-form-label">用户名</label>
                <div class="col-sm-9">
                  <input v-model="registerUsername" type="text" class="form-control" id="username">
                </div>
              </div>
              <div class="mb-3 row">
                <label for="password" class="col-sm-3 col-form-label">密码</label>
                <div class="col-sm-9">
                  <input v-model="registerPassword" type="password" class="form-control" id="password">
                </div>
              </div>
              <div class="mb-3 row">
                <label for="confirmedPassword" class="col-sm-3 col-form-label">确认密码</label>
                <div class="col-sm-9">
                  <input v-model="registerConfirmedPassword" type="password" class="form-control" id="confirmedPassword">
                </div>
              </div>
            </template>
          </Modal>
        </div>
      </template>
    </template>
  </Navbar>
  <Container>
    <router-view />
  </Container>
</template>

<script setup>
import Navbar from './components/Navbar.vue';
import NavbarItem from './components/NavbarItem.vue';
import Container from './components/Container.vue';
import USER from './store/USER.js';
import { onMounted, ref } from 'vue';
import Modal from './components/Modal.vue';
import API from './script/api.js';
import alert from './script/alert';
import router from './routes';
import GAME from './store/GAME';
import LANG from './store/LANG';

const username = ref(null);
const password = ref(null);

const registerModalRef = ref(null);
const registerUsername = ref(null);
const registerPassword = ref(null);
const registerConfirmedPassword = ref(null);

const handleLogin = () => {
  USER().loginByUP(username.value, password.value);
};

const logout = () => {
  USER().logout();
};

const toRegister = () => {
  const username = registerUsername.value;
  const password = registerPassword.value;
  const confirmedPassword = registerConfirmedPassword.value;
  API({
    url: '/account/register/',
    type: 'post',
    data: {
      username,
      password,
      confirmedPassword
    },
    success: resp => {
      if (resp.result === "success") {
        alert(`success`, `注册成功！两秒之后自动登录`);
        registerUsername.value = null;
        registerPassword.value = null;
        registerConfirmedPassword.value = null;
        registerModalRef.value.hide();
        setTimeout(() => {
          USER().loginByUP(username, password);
        }, 2000);
      } else {
        alert(`danger`, `注册失败：${resp.result}`, 2000);
      }
    }
  });
};

const gotoSpace = () => {
  router.push(`/space/${USER().getUserID}`);
}

onMounted(() => {
  USER().loginByToken();
  GAME().init();
  LANG().init();
});
</script>

<style>
body {
  width: 1920px;
  height: 1080px;
  background-color: rgb(228, 227, 227);
}
</style>