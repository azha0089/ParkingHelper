<template>
  <div class="common-layout">
    <el-container class="main_container">
      <el-header class="header-container" height="64px">
        <topheader></topheader>
      </el-header>
      <el-container class="content-container">
        <el-main class="main-content">
          <router-view v-slot="{Component}">
            <transition name="fade" mode="out-in">
              <component :is="Component" :key="$route.path"></component>
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import topheader from "../components/Header.vue";

export default {
  name: "layout",
  components:{
    topheader
  }
}
</script>

<style scoped>
.common-layout {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.main_container {
  height: 100%;
}

.header-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  padding: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  z-index: 999;
  background-color: #fff;
}

.content-container {
  margin-top: 64px;
  height: calc(100% - 64px);
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
  height: 100%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
