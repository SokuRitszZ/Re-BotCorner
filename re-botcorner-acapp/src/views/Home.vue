<template>
  <CardBody>
    <Row>
      <Col col="col-4">
        <div style="padding: 20px; box-sizing: border-box; border-right: 1px solid #ccc">
          <h1>欢迎来到Bot Corner！</h1>
          <hr>
          <h2>Bot Corner是什么？</h2>
          <p>
            Bot Corner是一个支持人机对战的游戏博弈平台。对游戏的决策可以通过人，也可以通过Bot代码运行来作出决策。通过这个平台，学习人工智能的相关知识并编写自己的Bot代码，比拼各Bot之间的博弈角力。
          </p>
          <hr class="mb-5">
          <h2>Bot Corner怎么用呢？</h2>
          <hr>
          <p>进入游戏大厅，选择需要游玩的游戏，点击进入之后会提供游戏规则等相关说明，并且可以提供已支持的语言的Bot代码模板。单人游玩模式或是找上朋友游玩模式都可以自由选择。</p>
          <hr class="mb-5">
          <h2>开发者的一些话</h2>
          <hr>
          <p>平台成立之初，目前能够满足大部分的基本需求，也仍有更多功能还尚未实现，比如通过房间号加入游戏、修改个人信息等等。开发者后续在实现这些功能的同时也会陆续加入其他游戏和其他语言支持。未来在这个空荡荡的主页也会有更多的展示。</p>
        </div>
      </Col>
      <Col col="col-8">
        <div style="padding: 20px">
          <h1>
            待开发区域
            <hr>
            <Window>
              <template v-slot:button>
                My-Window
              </template>
              <template v-slot:body>
                <ChatRoom />
              </template>
            </Window>
          </h1>
        </div>
      </Col>
    </Row>
    <div style="text-align: center;">
      <p>作者：<strong>Andrew Leung</strong></p>
      <p>项目地址：<a target="_blank" style="text-decoration: none; color: #000" href="https://github.com/SokuRitszZ/Re-BotCorner"><i style="font-size: 20px" class="bi bi-github"></i></a></p>
    </div>
  </CardBody>
</template>

<script setup>
import CardBody from '../components/CardBody.vue';
import Row from '../components/Row.vue';
import Col from '../components/Col.vue';
import Window from '../components/Window.vue';
import ChatRoom from '../components/ChatRoom.vue';

</script>

<style scoped>
* {
  user-select: none;
}
</style>