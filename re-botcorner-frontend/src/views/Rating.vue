<template>
  <CardBody>
    <Container>
      <Row>
        <Col col="col-3">
          <CardBody>
            <button @click="selectGame(game.title)" :class="`btn btn-outline-primary mb-2 ${selectedGame === game.title ? 'active' : ''}`" v-for="game in GAME().list" style="width: 100%; height: 50px; border-radius: 0">
              {{ game.title }}
            </button>
          </CardBody>
        </Col>
        <Col col="col-9">
          <CardBody>
            <table class="table table-striped">
              <thead>
                <tr>
                  <th>排名</th>
                  <th>用户</th>
                  <th>分数</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(user, index) in shownList">
                  <td style="font-size: 35px">{{ index + 1 }}</td>
                  <td>
                    <img :src="user.headIcon" style="width: 50px">
                    {{ user.username }}
                    <span style="color: #ccc">#{{ user.userId }}</span>
                  </td>
                  <td>{{ user.rating }}</td>
                </tr>
              </tbody>
            </table>
          </CardBody>
        </Col>
      </Row>
    </Container>
  </CardBody>
</template>

<script setup>
import CardBody from '../components/CardBody.vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import Container from '../components/Container.vue';
import GAME from '../store/GAME';
import { ref } from 'vue';
import API from '../script/api';

const selectedGame = ref('');
const hasSelectedGame = ref({});
const shownList = ref([]);

const selectGame = game => {
  selectedGame.value = game;
  if (hasSelectedGame.value[game] === undefined) {
    API({
      url: `/getrating/${game}`,
      type: 'get',
      async: false,
      success: resp => {
        hasSelectedGame.value[game] = resp;
        hasSelectedGame.value[game].sort((a, b) => {
          return b.rating - a.rating;
        });
      }
    });
  }
  shownList.value = hasSelectedGame.value[game];
};

</script>

<style scoped>

tbody>tr:nth-child(1)>td>img {
  border: 5px solid gold 
}

tbody>tr:nth-child(2)>td>img {
  border: 5px solid silver;
}

tbody>tr:nth-child(3)>td>img {
  border: 5px solid #E36F26;
}

</style>