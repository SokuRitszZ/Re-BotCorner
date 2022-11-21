<template>
  <transition name="navbar">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <div class="container-fluid">
        <router-link class="navbar-brand ms-5" to="/">
          Bot Corner
        </router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent"
                aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            <slot name="left"></slot>
          </ul>
          <span class="navbar-text mx-xxl-5">
          <slot name="right"></slot>
        </span>
        </div>
      </div>
    </nav>
  </transition>
</template>

<script setup>
</script>

<style>
.navbar-enter-active {
  animation: fadeInDown 0.5s;
}

.navbar {
  animation: fadeInDown 0.5s;
}
</style>