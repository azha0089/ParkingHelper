<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1>欢迎使用停车场数据预测系统</h1>
      <p>智能分析停车场数据，提供实时信息与预测服务</p>
    </div>
    
    <el-row :gutter="30" class="card-row">
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="feature-card" shadow="hover" @click="navigateTo('/parking-discovery')">
          <div class="card-content">
            <div class="card-icon-wrapper location-bg">
              <el-icon class="card-icon"><Location /></el-icon>
            </div>
            <h2>停车场发现</h2>
            <p>查找附近停车场，获取实时车位信息和位置导航</p>
            <el-button class="card-button" type="primary" plain round>
              立即使用
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="feature-card" shadow="hover" @click="navigateTo('/parking-prediction')">
          <div class="card-content">
            <div class="card-icon-wrapper analysis-bg">
              <el-icon class="card-icon"><DataAnalysis /></el-icon>
            </div>
            <h2>停车场预测</h2>
            <p>基于历史数据预测停车场可用性，提前规划您的行程</p>
            <el-button class="card-button" type="primary" plain round>
              立即使用
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="feature-card" shadow="hover" @click="navigateTo('/data-insights')">
          <div class="card-content">
            <div class="card-icon-wrapper document-bg">
              <el-icon class="card-icon"><Document /></el-icon>
            </div>
            <h2>数据洞察</h2>
            <p>查看停车场数据分析和趋势，了解城市停车动态</p>
            <el-button class="card-button" type="primary" plain round>
              立即使用
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <div class="system-info">
      <p>系统版本: v1.0.0 | 最近更新: 2023-12-15 | <a href="#" class="info-link">使用帮助</a></p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Location, DataAnalysis, Document, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.home-container {
  padding: 30px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.welcome-section {
  text-align: center;
  margin-bottom: 50px;
  animation: fadeIn 0.8s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.welcome-section h1 {
  font-size: 32px;
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 15px;
  font-weight: bold;
}

.welcome-section p {
  font-size: 18px;
  color: #606266;
  max-width: 600px;
  margin: 0 auto;
}

.card-row {
  flex: 1;
  margin-bottom: 40px;
}

.feature-card {
  height: 320px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 30px 20px;
}

.card-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 25px;
}

.location-bg {
  background: linear-gradient(135deg, #36cfc9, #22c1c3);
}

.analysis-bg {
  background: linear-gradient(135deg, #409EFF, #4e6ef2);
}

.document-bg {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.card-icon {
  font-size: 36px;
  color: #ffffff;
}

.card-content h2 {
  font-size: 22px;
  margin-bottom: 15px;
  color: #303133;
  font-weight: 600;
}

.card-content p {
  color: #606266;
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 25px;
  flex-grow: 1;
}

.card-button {
  margin-top: auto;
}

.system-info {
  margin-top: 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.info-link {
  color: #409EFF;
  text-decoration: none;
}

.info-link:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media screen and (max-width: 1200px) {
  .feature-card {
    height: 340px;
  }
}

@media screen and (max-width: 768px) {
  .home-container {
    padding: 20px 15px;
  }
  
  .welcome-section {
    margin-bottom: 30px;
  }
  
  .welcome-section h1 {
    font-size: 26px;
  }
  
  .welcome-section p {
    font-size: 16px;
  }
  
  .feature-card {
    height: auto;
    min-height: 280px;
  }
  
  .card-content {
    padding: 20px 15px;
  }
  
  .card-icon-wrapper {
    width: 70px;
    height: 70px;
    margin-bottom: 20px;
  }
  
  .card-icon {
    font-size: 30px;
  }
  
  .card-content h2 {
    font-size: 20px;
  }
  
  .card-content p {
    font-size: 14px;
    margin-bottom: 20px;
  }
}

@media screen and (max-width: 480px) {
  .welcome-section h1 {
    font-size: 22px;
  }
  
  .welcome-section p {
    font-size: 14px;
  }
  
  .system-info {
    font-size: 12px;
  }
  
  .system-info p {
    display: flex;
    flex-direction: column;
    gap: 5px;
  }
}
</style>
