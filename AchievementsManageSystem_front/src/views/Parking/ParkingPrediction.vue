<template>
  <div class="parking-prediction">
    <div class="search-section">
      <el-card class="search-card">
        <template #header>
          <div class="card-header">
            <h3>停车场预测</h3>
          </div>
        </template>
        <div class="search-form">
          <el-form :model="searchForm" label-width="100px" label-position="top" class="responsive-form">
            <el-form-item label="目的地">
              <el-input v-model="searchForm.destination" placeholder="输入目的地" clearable>
                <template #prefix>
                  <el-icon><Location /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <div class="button-group">
                <el-button type="primary" @click="handleSearch">预测</el-button>
                <el-button @click="resetForm">重置</el-button>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>

    <div class="prediction-section" v-if="predictionResults.length > 0">
      <el-card class="prediction-card">
        <template #header>
          <div class="card-header">
            <h3>预测结果</h3>
            <div class="prediction-info">
              <el-tag type="info">预测基于历史数据</el-tag>
            </div>
          </div>
        </template>
        <el-table 
          :data="predictionResults" 
          style="width: 100%" 
          border 
          stripe
          :max-height="tableMaxHeight"
          class="responsive-table"
        >
          <el-table-column prop="name" label="停车场名称" min-width="150" />
          <el-table-column prop="distance" label="距离" min-width="100" />
          <el-table-column label="预测拥堵等级" min-width="140">
            <template #default="scope">
              <el-tag :type="getCongestionTagType(scope.row.congestion)">
                {{ getCongestionText(scope.row.congestion) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="predictedAvailableSpaces" label="预计可用车位" min-width="120" />
          <el-table-column label="操作" fixed="right" width="120">
            <template #default="scope">
              <el-button size="small" @click="showDetails(scope.row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="停车场预测详情"
      width="90%"
      :fullscreen="isMobile"
      destroy-on-close
    >
      <div class="detail-container" v-if="selectedParking">
        <h2>{{ selectedParking.name }}</h2>
        <div class="detail-info">
          <div class="info-row">
            <p><strong>地址：</strong>{{ selectedParking.address }}</p>
            <p><strong>距离：</strong>{{ selectedParking.distance }}</p>
          </div>
          <div class="info-row">
            <p><strong>总车位数：</strong>{{ selectedParking.totalSpaces }}</p>
            <p><strong>预计可用车位数：</strong>{{ selectedParking.predictedAvailableSpaces }}</p>
          </div>
          <p><strong>预测拥堵等级：</strong>
            <el-tag :type="getCongestionTagType(selectedParking.congestion)">
              {{ getCongestionText(selectedParking.congestion) }}
            </el-tag>
          </p>
        </div>
        <div class="chart-container">
          <h3>未来24小时预测趋势</h3>
          <div class="chart-placeholder">
            <div ref="chartRef" style="width: 100%; height: 300px;"></div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const searchForm = reactive({
  destination: ''
})

const predictionResults = ref([])
const dialogVisible = ref(false)
const selectedParking = ref(null)
const chartRef = ref(null)
const tableMaxHeight = ref('450px')
const isMobile = ref(false)
let chart = null

// 模拟数据，实际项目中应从API获取
const mockData = [
  {
    id: 1,
    name: '中央公园停车场',
    address: '市中心中央公园北门',
    distance: '500米',
    totalSpaces: 200,
    predictedAvailableSpaces: 35,
    congestion: 'high',
    hourlyPrediction: [40, 35, 30, 25, 20, 15, 10, 5, 10, 15, 20, 30, 40, 50, 60, 70, 80, 70, 60, 50, 40, 35, 30, 25]
  },
  {
    id: 2,
    name: '商业广场地下停车场',
    address: '商业广场B1层',
    distance: '800米',
    totalSpaces: 350,
    predictedAvailableSpaces: 150,
    congestion: 'medium',
    hourlyPrediction: [150, 140, 130, 120, 110, 100, 90, 80, 90, 100, 110, 120, 130, 140, 150, 160, 150, 140, 130, 120, 110, 100, 90, 80]
  },
  {
    id: 3,
    name: '市政府停车场',
    address: '市政府东侧',
    distance: '1.2公里',
    totalSpaces: 150,
    predictedAvailableSpaces: 80,
    congestion: 'low',
    hourlyPrediction: [80, 85, 90, 95, 100, 105, 110, 115, 110, 105, 100, 95, 90, 85, 80, 75, 70, 75, 80, 85, 90, 95, 100, 105]
  }
]

const getCongestionTagType = (congestion) => {
  switch (congestion) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'success'
    default: return 'info'
  }
}

const getCongestionText = (congestion) => {
  switch (congestion) {
    case 'high': return '高'
    case 'medium': return '中'
    case 'low': return '低'
    default: return '未知'
  }
}

const handleSearch = () => {
  if (!searchForm.destination) {
    ElMessage.warning('请输入目的地')
    return
  }
  
  // 模拟API调用
  setTimeout(() => {
    predictionResults.value = mockData
    ElMessage.success('预测完成')
  }, 500)
}

const resetForm = () => {
  searchForm.destination = ''
  predictionResults.value = []
}

const showDetails = (row) => {
  selectedParking.value = row
  dialogVisible.value = true
}

// 检查是否为移动设备
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
  // 调整表格高度
  tableMaxHeight.value = window.innerHeight > 800 ? '450px' : (window.innerHeight * 0.4) + 'px'
  
  // 如果图表已存在，调整大小
  if (chart) {
    chart.resize()
  }
}

const initChart = () => {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  
  watch(dialogVisible, (val) => {
    if (val && selectedParking.value) {
      setTimeout(() => {
        if (chart) {
          chart.resize()
          updateChart()
        }
      }, 300)
    }
  })
}

const updateChart = () => {
  if (!chart || !selectedParking.value) return
  
  const hours = Array.from({length: 24}, (_, i) => `${i}:00`)
  const option = {
    title: {
      text: '24小时车位可用预测',
      textStyle: {
        fontSize: isMobile.value ? 14 : 16
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br />{a}: {c}个车位'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: hours,
      axisLabel: {
        interval: isMobile.value ? 5 : 3,
        fontSize: isMobile.value ? 10 : 12
      }
    },
    yAxis: {
      type: 'value',
      name: '可用车位数',
      min: 0,
      axisLabel: {
        fontSize: isMobile.value ? 10 : 12
      }
    },
    series: [
      {
        name: '预计可用车位',
        type: 'line',
        data: selectedParking.value.hourlyPrediction,
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ]
          }
        }
      }
    ]
  }
  
  chart.setOption(option)
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  initChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.parking-prediction {
  padding: 20px;
}

.search-section {
  margin-bottom: 20px;
}

.search-card, .prediction-card {
  width: 100%;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.button-group {
  display: flex;
  gap: 10px;
}

.prediction-info {
  margin-left: 10px;
}

.detail-container {
  padding: 20px;
}

.detail-info {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 10px;
}

.chart-container {
  margin-top: 20px;
}

.chart-placeholder {
  height: 300px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .parking-prediction {
    padding: 15px;
  }
  
  .responsive-form :deep(.el-form-item__label) {
    padding: 0 0 8px;
  }
  
  .button-group {
    width: 100%;
    justify-content: space-between;
  }
  
  .button-group .el-button {
    flex: 1;
  }
  
  .detail-container {
    padding: 15px;
  }
  
  .info-row {
    flex-direction: column;
    gap: 5px;
  }
  
  .chart-container h3 {
    font-size: 16px;
  }
  
  .chart-placeholder {
    height: 250px;
    padding: 5px;
  }
}

@media screen and (max-width: 480px) {
  .parking-prediction {
    padding: 10px;
  }
  
  .responsive-table :deep(.el-table__header) {
    font-size: 14px;
  }
  
  .responsive-table :deep(.el-table__body) {
    font-size: 13px;
  }
  
  .chart-container h3 {
    font-size: 14px;
  }
  
  .chart-placeholder {
    height: 200px;
  }
}
</style> 