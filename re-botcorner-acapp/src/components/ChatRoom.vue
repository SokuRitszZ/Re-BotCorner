<template>
  <div ref="chatroomRef" style="position: absolute; border: 1px solid #ccc; width: 100%; height: calc(100% - 180px); padding: 40px; padding: 10px; box-sizing: border-box; overflow: auto; border: none">
    <div :id="`msg-${message.id}`" v-for="(message, index) in messages" class="mt-2">
      <div v-if="isShownTime(index)"
        class="no-select" style="margin: auto; padding: 0 5px; width: fit-content; color: white; background-color: rgba(4, 4, 4, 0.2); text-align: center">
        {{ message.time }}
      </div>
      <template v-if="message.type == `msg`">
        <div class="line no-select">
          <div v-if="isLeft(message)">{{ message.username }}
            <span style="color: #aaa">
              #{{ message.userId }}
            </span>
          </div>
        </div>
        <div class="line no-select" style="display: flex; flex-direction: row-reverse;">
          <div v-if="isRight(message)">
            {{ message.username }}
            <span style="color: #aaa">
              #{{ message.userId }}
            </span>
          </div>
        </div>
        <div class="line">
          <div v-if="isLeft(message)" style="background-color: #ccc; border: 1px solid #ccc; padding: 10px; border-radius: 0 10px 10px 10px; width: fit-content" class="message-content">{{ message.content }}</div>
        </div>
        <div class="line" style="display: flex; flex-direction: row-reverse">
          <div v-if="isRight(message)" style="background-color: #55f; color: white; border: 1px solid #55f; padding: 10px; border-radius: 10px 0 10px 10px; width: fit-content;" class="message-content">{{ message.content }}</div>
        </div>
      </template>
      <template v-if="message.type === `enter`">
        <div class="mt-2 no-select"
          style="margin: auto; padding: 0 5px; width: fit-content; color: white; background-color: rgba(4, 4, 4, 0.2); text-align: center">
          {{ message.username + "#" + message.userId }}<span> 加入了</span>
        </div>
      </template>
      <template v-if="message.type === `exit`">
        <div class="mt-2 no-select"
          style="margin: auto; padding: 0 5px; width: fit-content; color: white; background-color: rgba(4, 4, 4, 0.2); text-align: center">
          {{ message.username + "#" + message.userId }}<span> 退出了</span>
        </div>
      </template>
    </div>
  </div>
  <div style="border: none; background-color: light; width: 100%; height: 140px; position: absolute; bottom: 0">
    <textarea placeholder="单条消息不超过64个字，Enter发送" @keyup.enter="sendTalk" v-model="toSendTalk" type="text" class="form-control" style="margin: auto; width: 80%; height: 100px;" />
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue';

const chatroomRef = ref(null);

const messages = ref([]);
const toSendTalk = ref('');

const isShownTime = index => {
  const msgs = messages.value;
  if (index === 0 || msgs[index].time !== msgs[index - 1].time) return true; 
  return false;
};

const addTalk = async (type, msg) => {
  msg.type = type;
  messages.value.push(msg);
  await nextTick();
  chatroomRef.value.scrollTop = chatroomRef.value.scrollHeight;
  if (type == 'msg') {
    let messageDiv = document.querySelector(`#msg-${msg.id}>.line>.message-content`);
    if (props.isLeft(msg)) {
      messageDiv.classList.add('new-talk-left');
    } else if (props.isRight(msg)) {
      messageDiv.classList.add('new-talk-right');
    }
    setTimeout(() => {
      if (props.isLeft(msg)) {
        messageDiv.classList.remove('new-talk-left');
      } else if (props.isRight(msg)) {
        messageDiv.classList.remove('new-talk-right');
      }
    }, 1000);
  }
};

const props = defineProps({
  sendTalk: {
    type: Function,
    default: content => { console.log(content); }
  },
  isLeft: {
    type: Function,
    default: message => { return false; }
  },
  isRight: {
    type: Function,
    default: message => { return false; }
  }
});

const sendTalk = () => {
  console.log(toSendTalk.value.length)
  if (toSendTalk.value.trim().length <= 1) return ;
  props.sendTalk(toSendTalk.value);
  toSendTalk.value = '';
};

defineExpose({
  addTalk
});
</script>

<style scoped>
textarea {
  resize: none;
}

.new-talk {
  animation-name: newTalk;
  animation-duration: 1s;
}

.new-talk-left {
  animation: fadeInLeft; /* referring directly to the animation's @keyframe declaration */
  animation-duration: 0.5s; /* don't forget to set a duration! */
}

.new-talk-right {
  animation: fadeInRight; /* referring directly to the animation's @keyframe declaration */
  animation-duration: 0.5s; /* don't forget to set a duration! */
}

.no-select {
  user-select: none;
}
</style>