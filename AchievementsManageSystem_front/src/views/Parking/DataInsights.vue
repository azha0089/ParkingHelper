<template>
  <div class="data-insights">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="insight-card">
          <template #header>
            <div class="card-header">
              <h3>Data Visualization</h3>
              <el-radio-group v-model="selectedChart" size="small" class="chart-selector">
                <el-radio-button label="population">CBD Population Trend</el-radio-button>
                <el-radio-button label="vehicles">Vehicles Registration Trend</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <div ref="mainChartRef" style="width: 100%; height: 400px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="insight-card">
          <template #header>
            <div class="card-header">
              <h3>Distribution of Parking Lot Utilization Rates</h3>
            </div>
          </template>
          <div class="chart-container">
            <div ref="pieChartRef" style="width: 100%; height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="insight-card">
          <template #header>
            <div class="card-header">
              <h3>Distribution of Parking Lot Types</h3>
            </div>
          </template>
          <div class="chart-container">
            <div ref="barChartRef" style="width: 100%; height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="insight-card">
          <template #header>
            <div class="card-header">
              <h3>Data Source</h3>
            </div>
          </template>
          <div class="data-source">
            <p>Clearly Marked Data Sources：</p>
            <ul>
              <li>Population Data: Sourced from the National Bureau of Statistics（2018-2023）</li>
              <li>Vehicle Registration Data: Sourced from the Traffic Management Bureau（2018-2023）</li>
              <li>Parking Lot Utilization Rate: Based on real-time data collected by the system</li>
              <li>Distribution of Parking Lot Types: Based on publicly available data from the Urban Planning Bureau</li>
            </ul>
            <p class="note">Note: The X/Y axis labels in the charts are clearly marked.
              Data last updated: December 2023</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'

// 图表引用
const mainChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)

// 图表实例
let mainChart = null
let pieChart = null
let barChart = null

// 选择的图表类型
const selectedChart = ref('population')

// 检查是否为移动设备
const isMobile = ref(false)

// 模拟数据 - 人口趋势
const populationData = {
  years: ['2018', '2019', '2020', '2021', '2022', '2023'],
  values: [580, 620, 650, 690, 720, 780]
}

// 模拟数据 - 车辆注册量
const vehiclesData = {
  years: ['2018', '2019', '2020', '2021', '2022', '2023'],
  values: [320, 350, 390, 430, 480, 550]
}

// 模拟数据 - 停车场使用率
const usageData = [
  { value: 40, name: 'High Utilization Rate(>80%)' },
  { value: 35, name: 'Medium Utilization Rate(40%-80%)' },
  { value: 25, name: 'Low Utilization Rate(<40%)' }
]

// 模拟数据 - 停车场类型
const parkingTypeData = [
  { type: 'On-Street', count: 150 },
  { type: 'Underground', count: 85 },
  { type: 'Multi-level', count: 60 },
  { type: 'Temporary', count: 30 }
]

// 检查设备类型并调整图表
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
  
  // 调整所有图表大小
  if (mainChart) mainChart.resize()
  if (pieChart) pieChart.resize()
  if (barChart) barChart.resize()
}

// 初始化主图表
const initMainChart = () => {
  if (!mainChartRef.value) return
  
  mainChart = echarts.init(mainChartRef.value)
  updateMainChart()
}

// 更新主图表
const updateMainChart = () => {
  if (!mainChart) return
  
  const data = selectedChart.value === 'population' ? populationData : vehiclesData
  const title = selectedChart.value === 'population' ? 'CBD Population Trend (Over 5 Years)' : 'Vehicle Registration Trend (Over 5 Years)'
  const yAxisName = selectedChart.value === 'population' ? 'Population (Thousands)' : 'Registered Vehicles (Thousands)'
  
  const option = {
    title: {
      text: title,
      textStyle: {
        fontSize: isMobile.value ? 14 : 16
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.years,
      name: 'Year',
      axisLabel: {
        fontSize: isMobile.value ? 10 : 12
      }
    },
    yAxis: {
      type: 'value',
      name: yAxisName,
      axisLabel: {
        fontSize: isMobile.value ? 10 : 12
      }
    },
    series: [
      {
        name: selectedChart.value === 'population' ? 'Population' : 'Vehicles',
        type: 'line',
        data: data.values,
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
  
  mainChart.setOption(option)
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  
  pieChart = echarts.init(pieChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: isMobile.value ? 'horizontal' : 'vertical',
      right: isMobile.value ? 'center' : 10,
      top: isMobile.value ? 'bottom' : 'center',
      data: usageData.map(item => item.name),
      textStyle: {
        fontSize: isMobile.value ? 10 : 12
      }
    },
    series: [
      {
        name: 'Utilization Rate',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: isMobile.value ? 14 : 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: usageData
      }
    ]
  }
  
  pieChart.setOption(option)
}

// 初始化柱状图
const initBarChart = () => {
  if (!barChartRef.value) return
  
  barChart = echarts.init(barChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: parkingTypeData.map(item => item.type),
      axisLabel: {
        interval: 0,
        rotate: isMobile.value ? 45 : 30,
        fontSize: isMobile.value ? 10 : 12
      }
    },
    yAxis: {
      type: 'value',
      name: 'Number',
      axisLabel: {
        fontSize: isMobile.value ? 10 : 12
      }
    },
    series: [
      {
        name: 'Parking lot Number',
        type: 'bar',
        data: parkingTypeData.map(item => item.count),
        itemStyle: {
          color: function(params) {
            const colorList = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C']
            return colorList[params.dataIndex % colorList.length]
          }
        }
      }
    ]
  }
  
  barChart.setOption(option)
}

// 监听选择的图表类型变化
watch(selectedChart, () => {
  updateMainChart()
})

// 监听移动设备状态变化
watch(isMobile, () => {
  // 重新初始化所有图表以适应新的设备类型
  if (mainChart) updateMainChart()
  if (pieChart) {
    pieChart.dispose()
    initPieChart()
  }
  if (barChart) {
    barChart.dispose()
    initBarChart()
  }
})

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  initMainChart()
  initPieChart()
  initBarChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
  if (mainChart) mainChart.dispose()
  if (pieChart) pieChart.dispose()
  if (barChart) barChart.dispose()
})
</script>

<style scoped>
.data-insights {
  padding: 20px;
}

.insight-card {
  width: 100%;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  margin-top: 10px;
}

.data-source {
  padding: 10px;
}

.data-source ul {
  padding-left: 20px;
  margin: 10px 0;
}

.data-source .note {
  font-style: italic;
  color: #909399;
  margin-top: 10px;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .data-insights {
    padding: 15px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .chart-selector {
    margin-top: 10px;
    width: 100%;
    display: flex;
  }
  
  .chart-selector :deep(.el-radio-button) {
    flex: 1;
  }
  
  .chart-selector :deep(.el-radio-button__inner) {
    width: 100%;
  }
  
  .chart-container {
    height: auto !important;
  }
  
  .chart-container > div {
    height: 300px !important;
  }
  
  .data-source {
    padding: 5px;
  }
  
  .data-source ul {
    padding-left: 15px;
  }
}

@media screen and (max-width: 480px) {
  .data-insights {
    padding: 10px;
  }
  
  .card-header h3 {
    font-size: 16px;
  }
  
  .chart-container > div {
    height: 250px !important;
  }
  
  .data-source {
    font-size: 13px;
  }
  
  .data-source .note {
    font-size: 12px;
  }
}
</style> 