<template>
  <CardBody>
    <Row>
      <Col col="col-3">
        <CardBody>
          <button @click="selectGame(game.title)" :class="`btn btn-outline-dark mb-2 ${selectedGame === game.title ? 'active' : ''}`" v-for="game in GAME().list" style="width: 100%; height: 50px; border-radius: 0">
            {{ game.name }}
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
            <tr :key="user.userId" v-for="(user, index) in shownList">
              <td style="font-size: 35px">{{ user.rank }}</td>
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
  </CardBody>
</template>

<script setup>
import CardBody from '../components/CardBody.vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import Container from '../components/Container.vue';
import GAME from '../store/GAME';
import { ref } from 'vue';
import RATING from "../store/RATING.js";

const selectedGame = ref('');
const shownList = ref([]);

const selectGame = async game => {
  selectedGame.value = game;
  RATING().pullRating(game).then(list => {
    list.map((user, index) => {
      user.rank = index > 0 && list[index].rating == list[index - 1].rating ? list[index - 1].rank : index + 1;
    });
    shownList.value = list;
  });
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