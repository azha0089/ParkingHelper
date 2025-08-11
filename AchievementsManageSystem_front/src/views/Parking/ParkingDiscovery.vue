<template>
  <div class="parking-discovery">
    <div class="search-section">
      <el-card class="search-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <h3>停车场搜索</h3>
          </div>
        </template>
        <div class="search-form">
          <el-form :model="searchForm" label-width="100px" label-position="top" class="responsive-form">
            <el-form-item label="目的地">
              <el-input
                id="destination-input"
                v-model="searchForm.destination"
                placeholder="输入目的地（例如: Geelong Central）"
                clearable
                @keyup.enter.native="handleSearch">
                <template #prefix>
                  <el-icon><Location /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <div class="button-group">
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="resetForm">重置</el-button>
              </div>
            </el-form-item>
          </el-form>
          <div class="hint">数据来源：Geelong Data Exchange（LoRa 传感器实时状态）</div>
        </div>
      </el-card>
    </div>

    <div class="result-section">
      <el-card class="result-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <h3>搜索结果</h3>
            <div v-if="centerPoint" class="center-info">中心点：{{ centerPoint.lat.toFixed(5) }}, {{ centerPoint.lng.toFixed(5) }}（半径 {{ (radius/1000).toFixed(1) }} km）</div>
          </div>
        </template>

        <el-empty v-if="!loading && searchResults.length===0" description="暂无数据，请输入目的地并搜索" />

        <el-table v-else :data="searchResults" style="width: 100%" border stripe :max-height="tableMaxHeight" class="responsive-table">
          <el-table-column prop="name" label="名称" min-width="180" />
          <el-table-column prop="distanceText" label="距离" min-width="100" />
          <el-table-column prop="availableText" label="可用车位" min-width="110" />
          <el-table-column prop="updateTime" label="更新时间" min-width="180" />
          <el-table-column label="操作" fixed="right" width="200">
            <template #default="scope">
              <el-button size="small" @click="showDetails(scope.row)">查看详情</el-button>
              <el-button size="small" type="primary" link :href="scope.row.mapUrl" target="_blank">地图链接</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" title="停车点详情" width="90%" :fullscreen="isMobile" destroy-on-close>
      <div class="detail-container" v-if="selectedParking">
        <h2 style="margin-bottom: 10px">{{ selectedParking.name }}</h2>
        <div class="detail-info">
          <div class="info-row">
            <p><strong>坐标：</strong>{{ selectedParking.lat }}, {{ selectedParking.lng }}</p>
            <p><strong>距离：</strong>{{ selectedParking.distanceText }}</p>
          </div>
          <div class="info-row">
            <p><strong>可用车位：</strong>{{ selectedParking.availableText }}</p>
            <p><strong>更新时间：</strong>{{ selectedParking.updateTime }}</p>
          </div>
        </div>
        <div class="map-container">
          <div id="parking-map" class="leaflet-map"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import axios from 'axios'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { loadScript } from '../../utils/MyFunction'

const searchForm = reactive({ destination: '' })
const searchResults = ref([])
const dialogVisible = ref(false)
const selectedParking = ref(null)
const tableMaxHeight = ref('450px')
const isMobile = ref(false)
const loading = ref(false)
const centerPoint = ref(null)
const radius = 3000 // 3km，CKAN数据较为分散，可适度加大半径

let map = null
let marker = null
let autocomplete = null

// 使用CKAN Data API，通过vite代理
// 参考: https://discover.data.vic.gov.au/api/3/action/datastore_search?resource_id=7f381a3a-fd9e-477f-a6ae-b45e1a65bb16
const CKAN_API = '/datavic/api/3/action/datastore_search'
const RESOURCE_ID = '7f381a3a-fd9e-477f-a6ae-b45e1a65bb16'
const NOMINATIM = '/nominatim/search'
const GMAPS_API = '/gmaps/maps/api/geocode/json'
const GMAPS_KEY = 'YOUR_API_KEY' // 请替换为你的Google Maps API密钥

const resetForm = () => {
  searchForm.destination = ''
  searchResults.value = []
  centerPoint.value = null
}

const fetchCkanAll = async (limit = 1000) => {
  // CKAN支持分页；此数据集total较小（~18），一次抓取足够；这里写成可分页
  const resp = await axios.get(CKAN_API, { params: { resource_id: RESOURCE_ID, limit } })
  if (!resp.data?.success) throw new Error('CKAN返回失败')
  return resp.data.result.records || []
}

const handleSearch = async () => {
  if (!searchForm.destination) {
    ElMessage.warning('请输入目的地')
    return
  }
  loading.value = true
  try {
    let geoData = null

    // 优先使用Google Maps API，失败时回退到Nominatim
    try {
      // 1) 使用Google Maps API进行地理编码
      const geoRes = await axios.get(GMAPS_API, {
        params: {
          address: searchForm.destination,
          key: GMAPS_KEY,
          language: 'zh-CN'
        }
      })

      if (geoRes.data && geoRes.data.status === 'OK' && geoRes.data.results.length > 0) {
        const location = geoRes.data.results[0].geometry.location
        geoData = { lat: location.lat, lng: location.lng }
      } else {
        throw new Error('Google地理编码失败')
      }
    } catch (geoError) {
      console.warn('Google地理编码失败，回退到Nominatim:', geoError)

      // 回退到Nominatim
      const nominatimRes = await axios.get(NOMINATIM, {
        params: { q: searchForm.destination, format: 'json', limit: 1 },
        headers: { 'Accept-Language': 'zh-CN' }
      })

      if (!nominatimRes.data || nominatimRes.data.length === 0) {
        throw new Error('无法定位该目的地')
      }

      const { lat, lon } = nominatimRes.data[0]
      geoData = { lat: parseFloat(lat), lng: parseFloat(lon) }
    }

    // 设置中心点
    centerPoint.value = geoData

    // 2) 拉取CKAN实时车位
    const records = await fetchCkanAll(1000)

    // 3) 映射 + 过滤半径
    const rows = records
      .filter(f => f.latitude && f.longitude)
      .map(f => {
        const plat = parseFloat(f.latitude)
        const plng = parseFloat(f.longitude)
        const dist = haversine(centerPoint.value.lat, centerPoint.value.lng, plat, plng)
        const available = normalizeAvailable(f.payload_fields_park_flag)
        const name = `Parking Spot ${f.deviceid || ''}`.trim()
        const mapUrl = `https://www.openstreetmap.org/?mlat=${plat}&mlon=${plng}#map=18/${plat}/${plng}`
        return {
          id: f.deviceid || `${plat},${plng}`,
          name,
          lat: plat,
          lng: plng,
          available,
          availableText: available === null ? '未知' : (available ? '1（空闲）' : '0（占用）'),
          distance: dist,
          distanceText: dist < 1000 ? `${Math.round(dist)} m` : `${(dist/1000).toFixed(2)} km`,
          updateTime: f.metadata_time || '-',
          mapUrl
        }
      })
      .filter(row => isFinite(row.distance) && row.distance <= radius)
      .sort((a, b) => a.distance - b.distance)

    searchResults.value = rows

    if (rows.length === 0) {
      ElMessage.warning('附近没有检测到传感停车点，尝试扩大半径或换个地点')
    } else {
      ElMessage.success(`共找到 ${rows.length} 个传感停车点`)
    }

    localStorage.setItem('lastParkingResults', JSON.stringify({ center: centerPoint.value, rows }))
  } catch (e) {
    console.error('获取数据失败: ', e)
    const cached = localStorage.getItem('lastParkingResults')
    if (cached) {
      const data = JSON.parse(cached)
      centerPoint.value = data.center
      searchResults.value = data.rows
      ElMessage.warning('外部API不可用，已显示上次缓存数据')
    } else {
      ElMessage.error('无法获取停车场数据（可能是跨域或网络问题）')
    }
  } finally {
    loading.value = false
  }
}

const normalizeAvailable = (parkFlag) => {
  if (parkFlag === null || typeof parkFlag === 'undefined') return null
  const v = String(parkFlag).trim()
  return v === '0' || v.toLowerCase() === 'false'
}

const showDetails = async (row) => {
  selectedParking.value = row
  dialogVisible.value = true
  await nextTick()
  initMap(row.lat, row.lng)
}

const initMap = (lat, lng) => {
  const mapEl = document.getElementById('parking-map')
  if (!mapEl) return
  if (!map) {
    map = L.map(mapEl, { zoomControl: true })
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(map)
  }
  map.setView([lat, lng], 17)
  if (marker) {
    marker.setLatLng([lat, lng])
  } else {
    marker = L.marker([lat, lng]).addTo(map)
  }
}

function haversine(lat1, lon1, lat2, lon2) {
  const toRad = d => (d * Math.PI) / 180
  const R = 6371000
  const dLat = toRad(lat2 - lat1)
  const dLon = toRad(lon2 - lon1)
  const a = Math.sin(dLat/2)**2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon/2)**2
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
  return R * c
}

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
  tableMaxHeight.value = window.innerHeight > 800 ? '450px' : (window.innerHeight * 0.4) + 'px'
}

const initGoogleAutocomplete = () => {
  // 如果Google Maps API已加载，初始化自动完成
  if (window.google && window.google.maps && window.google.maps.places) {
    const input = document.getElementById('destination-input')
    if (input && !autocomplete) {
      autocomplete = new google.maps.places.Autocomplete(input, {
        types: ['geocode', 'establishment'],
        fields: ['formatted_address', 'geometry', 'name'],
        componentRestrictions: { country: 'au' } // 限制在澳大利亚范围内搜索，可根据需要修改
      })

      // 防止表单提交
      input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && document.querySelector('.pac-container:visible')) {
          e.preventDefault()
        }
      })

      autocomplete.addListener('place_changed', () => {
        const place = autocomplete.getPlace()
        if (place.geometry) {
          searchForm.destination = place.formatted_address || place.name
          // 可选：自动触发搜索
          // handleSearch()
        }
      })
    }
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)

  // 加载Google Maps JavaScript API
  loadScript(`https://maps.googleapis.com/maps/api/js?key=${GMAPS_KEY}&libraries=places&language=zh-CN`)
    .then(() => {
      // 初始化地址自动完成
      initGoogleAutocomplete()
    })
    .catch(err => console.error('Google Maps API加载失败，将使用基本搜索', err))
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.parking-discovery { padding: 20px; }
.search-section { margin-bottom: 20px; }
.search-card, .result-card { width: 100%; margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.center-info { color: #909399; font-size: 13px; }
.hint { color: #909399; font-size: 12px; margin-top: 8px; }
.button-group { display: flex; gap: 10px; }
.detail-container { padding: 20px; }
.detail-info { margin-bottom: 20px; }
.info-row { display: flex; flex-wrap: wrap; gap: 20px; margin-bottom: 10px; }
.map-container { height: 360px; border: 1px solid #ebeef5; border-radius: 4px; }
.leaflet-map { height: 100%; width: 100%; }
.map-placeholder { height: 100%; display: flex; justify-content: center; align-items: center; }

/* Google Places 自动完成样式修复 */
:global(.pac-container) {
  z-index: 10000 !important;
  font-family: var(--el-font-family) !important;
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  border: 1px solid var(--el-border-color-light);
  background-color: white;
}

:global(.pac-item) {
  padding: 6px 10px;
  cursor: pointer;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
}

:global(.pac-item:hover) {
  background-color: var(--el-fill-color-light);
}

:global(.pac-icon) {
  display: inline-block;
  width: 20px;
  height: 20px;
  margin-right: 10px;
  background-color: transparent !important;
}

:global(.pac-item-query) {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .parking-discovery { padding: 15px; }
  .responsive-form :deep(.el-form-item__label) { padding: 0 0 8px; }
  .button-group { width: 100%; justify-content: space-between; }
  .button-group .el-button { flex: 1; }
  .detail-container { padding: 15px; }
  .info-row { flex-direction: column; gap: 5px; }
  .map-container { height: 300px; }
}

@media screen and (max-width: 480px) {
  .parking-discovery { padding: 10px; }
  .responsive-table :deep(.el-table__header) { font-size: 14px; }
  .responsive-table :deep(.el-table__body) { font-size: 13px; }
  .map-container { height: 240px; }
}
</style>
