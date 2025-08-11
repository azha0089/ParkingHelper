<template>
  <div class="top-header">
    <div class="logo-container">
      <h2 class="logo">Parking lot data insight system</h2>
      <el-button
        class="mobile-toggle"
        @click="toggleMobileMenu"
        type="primary"
        text
        :icon="mobileMenuVisible ? Close : Menu"
      />
    </div>

    <div class="nav-menu" :class="{ 'mobile-menu-active': mobileMenuVisible }">
      <el-menu
        mode="horizontal"
        :router="true"
        active-text-color="#409EFF"
        background-color="transparent"
        text-color="#606266"
        class="nav-items"
        :default-active="activeIndex"
        @select="handleSelect"
      >
<!--        <el-menu-item index="/home" class="nav-menu-item">-->
<!--          <el-icon class="nav-icon"><HomeFilled /></el-icon>-->
<!--          <span class="nav-text">Home</span>-->
<!--        </el-menu-item>-->
        <el-menu-item index="/parking-discovery-new" class="nav-menu-item">
          <el-icon class="nav-icon"><Location /></el-icon>
          <span class="nav-text">ParkingDiscovery</span>
        </el-menu-item>
<!--        <el-menu-item index="/parking-prediction" class="nav-menu-item">-->
<!--          <el-icon class="nav-icon"><DataAnalysis /></el-icon>-->
<!--          <span class="nav-text">ParkingPrediction</span>-->
<!--        </el-menu-item>-->
        <el-menu-item index="/data-insights" class="nav-menu-item">
          <el-icon class="nav-icon"><Document /></el-icon>
          <span class="nav-text">DataInsights</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="user-menu">
      <el-dropdown @command="handleCommand" trigger="click">
        <span class="user-dropdown-link">
          <img class="user-avatar" src="../assets/avatar.png">
          <span class="username">{{ username }}</span>
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logOff" :icon="Guide">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { Location, Document, DataAnalysis, Guide, ArrowDown, HomeFilled, Menu, Close } from "@element-plus/icons-vue";
import { computed, ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from "vue-router";

const router = useRouter()
const route = useRoute()
const username = sessionStorage.getItem("username") || "用户"
const activeIndex = ref(route.path)
const mobileMenuVisible = ref(false)

// 监听路由变化，更新活跃菜单项
router.afterEach((to) => {
  activeIndex.value = to.path
  // 路由变化时关闭移动端菜单
  mobileMenuVisible.value = false
})

const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value
}

const handleSelect = () => {
  // 选择菜单项后关闭移动端菜单
  if (window.innerWidth <= 768) {
    mobileMenuVisible.value = false
  }
}

const handleCommand = (command) => {
  if (command === 'logOff') {
    sessionStorage.removeItem('saToken');
    sessionStorage.removeItem('tokenName');
    sessionStorage.removeItem('role');
    sessionStorage.removeItem('username');
    ElMessage({
      showClose: true,
      message: '退出登录成功',
      type: 'success',
    })
    router.push('/loginandregister')
  }
}

// 监听窗口大小变化
const handleResize = () => {
  if (window.innerWidth > 768) {
    mobileMenuVisible.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.top-header {
  width: 100%;
  height: 64px;
  background: linear-gradient(to right, #ffffff, #f6f8fa);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: relative;
}

.logo-container {
  display: flex;
  align-items: center;
  width: 220px;
  justify-content: space-between;
}

.logo {
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-size: 22px;
  font-weight: bold;
  margin: 0;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.mobile-toggle {
  display: none;
  font-size: 24px;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-items {
  border-bottom: none;
  height: 64px;
  min-width: 600px;
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
}

/* 覆盖Element Plus的默认样式 */
/* :deep(.el-menu--horizontal .el-menu-item) {
  border-bottom: none !important;
} */

/* :deep(.el-menu--horizontal .el-menu-item.is-active) {
  border-bottom: none !important;
  background-color: rgba(64, 158, 255, 0.1) !important;
  border-radius: 4px;
} */

.nav-items .el-menu-item {
  height: 64px;
  line-height: 64px;
  padding: 0 25px;
  font-size: 16px;
  min-width: 120px;
  text-align: center;
  margin: 0 10px;
}

.nav-menu-item {
  position: relative;
}

.nav-icon {
  margin-right: 6px;
  font-size: 18px;
}

.nav-text {
  font-weight: 500;
  white-space: nowrap;
}

.user-menu {
  display: flex;
  align-items: center;
  width: 160px;
  justify-content: flex-end;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 24px;
  transition: background-color 0.3s;
}

.user-dropdown-link:hover {
  background-color: #f0f2f5;
}

.user-avatar {
  height: 36px;
  width: 36px;
  border-radius: 50%;
  margin-right: 8px;
  border: 2px solid #f0f2f5;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.username {
  font-size: 15px;
  margin-right: 4px;
  color: #606266;
  font-weight: 500;
}

.el-icon--right {
  margin-left: 4px;
  font-size: 14px;
}

/* 响应式设计 */
@media screen and (max-width: 1024px) {
  .nav-items {
    min-width: 500px;
  }

  .nav-items .el-menu-item {
    padding: 0 15px;
    min-width: 100px;
    margin: 0 5px;
  }

  .logo-container {
    width: 180px;
  }

  .user-menu {
    width: 140px;
  }
}

@media screen and (max-width: 768px) {
  .top-header {
    padding: 0 16px;
  }

  .logo-container {
    width: 100%;
    justify-content: space-between;
  }

  .mobile-toggle {
    display: block;
  }

  .nav-menu {
    position: absolute;
    top: 64px;
    left: 0;
    right: 0;
    background-color: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    padding: 10px;
    z-index: 999;
    display: none;
  }

  .mobile-menu-active {
    display: block;
  }

  .nav-items {
    flex-direction: column;
    height: auto;
    min-width: 100%;
    padding: 0;
  }

  .nav-items .el-menu-item {
    height: 50px;
    line-height: 50px;
    margin: 5px 0;
    min-width: 100%;
    justify-content: center;
  }

  .user-menu {
    width: auto;
  }

  .username {
    display: none;
  }
}
</style>
