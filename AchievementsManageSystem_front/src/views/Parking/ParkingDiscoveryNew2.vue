<template>
  <div class="parking-app">
    <div class="search-box">
      <input
          v-model="searchQuery"
          type="text"
          placeholder="输入墨尔本内目的地或停车位ID"
          @keyup.enter="searchParking"
      />
      <button @click="searchParking">搜索</button>
      <button @click="refreshData" class="refresh-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
          <path d="M3 3v5h5"></path>
          <path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16"></path>
          <path d="M16 16h5v5"></path>
        </svg>
        刷新
      </button>
      <div class="stats">
        <span class="available-count">✓ {{ availableCount }}</span>
        <span class="occupied-count">✗ {{ occupiedCount }}</span>
        <span>总数: {{ totalCount }}</span>
      </div>
    </div>

    <div ref="mapContainer" class="map-container"></div>

    <div class="parking-list">
      <div class="list-header">
        <h3>所有停车位 ({{ parkingLots.length }})</h3>
        <div class="sort-options">
          <span @click="sortBy = 'time'" :class="{ active: sortBy === 'time' }">按时间</span>
          <span @click="sortBy = 'battery'" :class="{ active: sortBy === 'battery' }">按电量</span>
        </div>
      </div>
      <div class="parking-items">
        <div class="parking-item"
             v-for="lot in sortedParkingLots"
             :key="lot._id"
             :class="{
               available: lot.payload_fields_park_flag === '0',
               occupied: lot.payload_fields_park_flag === '1'
             }"
             @click="zoomToParking(lot)">
          <div class="status-icon">
            <svg v-if="lot.payload_fields_park_flag === '0'" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="#10b981">
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="#ef4444">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/>
            </svg>
          </div>
          <div class="item-details">
            <h4>{{ lot.deviceid || '未知设备' }}</h4>
            <div class="detail-row">
              <span>状态:</span>
              <strong :class="lot.payload_fields_park_flag === '0' ? 'available' : 'occupied'">
                {{ lot.payload_fields_park_flag === '0' ? '空闲' : '占用中' }}
              </strong>
            </div>
            <div class="detail-row">
              <span>位置:</span>
              <span>{{ lot.latitude ? `${lot.latitude}, ${lot.longitude}` : '未知' }}</span>
            </div>
            <div class="detail-row">
              <span>电量:</span>
              <span :class="{ 'low-battery': lot.payload_fields_low_voltage === 'True' }">
                {{ lot.payload_fields_battery_percent }}%
              </span>
            </div>
            <div class="detail-row">
              <span>更新:</span>
              <span>{{ formatTime(lot.metadata_time) }}</span>
            </div>
          </div>
          <button class="navigate-btn" @click.stop="navigateToParking(lot)" v-if="lot.latitude">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"></polygon>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

export default {
  name: 'MelbourneRealtimeParkingApp',
  setup() {
    // Mapbox配置
    const accessToken = 'pk.eyJ1IjoiMTAxMDI5MjQ2NiIsImEiOiJjbWUzc2QyY2EwYmdvMmxwdzdsYzluYnhtIn0.rVNlhc6s2AowPgyWNsImyw';
    mapboxgl.accessToken = accessToken;

    // 墨尔本中心点(Flinders Station)
    const CBD_CENTER = [144.9671, -37.8183];

    // API配置
    const API_URL = 'https://discover.data.vic.gov.au/api/3/action/datastore_search';
    const RESOURCE_ID = '7f381a3a-fd9e-477f-a6ae-b45e1a65bb16';

    // 引用和状态
    const mapContainer = ref(null);
    const map = ref(null);
    const searchQuery = ref('');
    const parkingLots = ref([]);
    const isLoading = ref(false);
    const markers = ref([]);
    const sortBy = ref('time');

    // 格式化时间
    const formatTime = (timeString) => {
      if (!timeString) return '未知';
      const date = new Date(timeString);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    // 获取实时停车数据
    const fetchRealtimeParkingData = async (query = '') => {
      isLoading.value = true;
      try {
        let url = `${API_URL}?resource_id=${RESOURCE_ID}&limit=1000`;
        if (query) {
          url += `&q=${encodeURIComponent(query)}`;
        }

        const response = await fetch(url);
        if (!response.ok) throw new Error('API请求失败');

        const data = await response.json();
        return data.result.records.map(record => ({
          ...record,
          latitude: record.latitude ? parseFloat(record.latitude) : null,
          longitude: record.longitude ? parseFloat(record.longitude) : null,
          payload_fields_battery_percent: parseInt(record.payload_fields_battery_percent) || 0,
          timestamp: new Date(record.metadata_time).getTime() || 0
        }));
      } catch (error) {
        console.error('获取实时停车数据失败:', error);
        return [];
      } finally {
        isLoading.value = false;
      }
    };

    // 搜索停车位
    const searchParking = async () => {
      if (!searchQuery.value.trim()) {
        // 如果搜索框为空，刷新所有数据
        await refreshData();
        return;
      }

      try {
        parkingLots.value = await fetchRealtimeParkingData(searchQuery.value);
        updateParkingMarkers();
      } catch (error) {
        console.error('搜索失败:', error);
        alert('搜索过程中出错，请重试');
      }
    };

    // 刷新数据
    const refreshData = async () => {
      parkingLots.value = await fetchRealtimeParkingData();
      updateParkingMarkers();
    };

    // 更新停车标记
    const updateParkingMarkers = () => {
      // 清除旧标记
      markers.value.forEach(marker => marker.remove());
      markers.value = [];

      // 添加新标记
      parkingLots.value.forEach(lot => {
        if (!lot.latitude || !lot.longitude) return;

        // 创建自定义标记元素
        const el = document.createElement('div');
        el.className = `parking-marker ${lot.payload_fields_park_flag === '0' ? 'available' : 'occupied'}`;

        // 使用SVG图标
        el.innerHTML = `
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="${lot.payload_fields_park_flag === '0' ? '#10b981' : '#ef4444'}">
            <path d="${lot.payload_fields_park_flag === '0' ? 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z' : 'M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z'}"/>
          </svg>
          <div class="marker-tooltip">
            <div>${lot.deviceid || '停车位'}</div>
            <div>状态: ${lot.payload_fields_park_flag === '0' ? '空闲' : '占用中'}</div>
          </div>
        `;

        const popup = new mapboxgl.Popup({ offset: 25 })
            .setHTML(`
            <div class="popup-content">
              <h4>${lot.deviceid || '停车位'}</h4>
              <div class="popup-detail">
                <span>状态:</span> <strong class="${lot.payload_fields_park_flag === '0' ? 'available' : 'occupied'}">${lot.payload_fields_park_flag === '0' ? '空闲' : '占用中'}</strong>
              </div>
              <div class="popup-detail">
                <span>位置:</span> ${lot.latitude}, ${lot.longitude}
              </div>
              <div class="popup-detail">
                <span>电量:</span> <span class="${lot.payload_fields_low_voltage === 'True' ? 'low-battery' : ''}">${lot.payload_fields_battery_percent}%</span>
              </div>
              <div class="popup-detail">
                <span>最后更新:</span> ${formatTime(lot.metadata_time)}
              </div>
              <button class="popup-button"
                onclick="window.open('https://www.google.com/maps?q=${lot.latitude},${lot.longitude}')">
                查看地图
              </button>
            </div>
          `);

        const marker = new mapboxgl.Marker({
          element: el,
          anchor: 'bottom'
        })
            .setLngLat([lot.longitude, lot.latitude])
            .setPopup(popup)
            .addTo(map.value);

        markers.value.push(marker);
      });

      // 自动调整地图视图以显示所有标记
      if (parkingLots.value.length > 0 && parkingLots.value.some(lot => lot.latitude && lot.longitude)) {
        const bounds = new mapboxgl.LngLatBounds();
        parkingLots.value.forEach(lot => {
          if (lot.latitude && lot.longitude) {
            bounds.extend([lot.longitude, lot.latitude]);
          }
        });
        map.value.fitBounds(bounds, { padding: 100, maxZoom: 14 });
      }
    };

    // 导航到停车位
    const navigateToParking = (lot) => {
      if (lot.latitude && lot.longitude) {
        window.open(`https://www.google.com/maps?q=${lot.latitude},${lot.longitude}`, '_blank');
      }
    };

    // 缩放至特定停车位
    const zoomToParking = (lot) => {
      if (lot.latitude && lot.longitude) {
        map.value.flyTo({
          center: [lot.longitude, lot.latitude],
          zoom: 18
        });
      }
    };

    // 排序后的停车位数据
    const sortedParkingLots = computed(() => {
      const lots = [...parkingLots.value];
      switch (sortBy.value) {
        case 'battery':
          return lots.sort((a, b) => b.payload_fields_battery_percent - a.payload_fields_battery_percent);
        case 'time':
        default:
          return lots.sort((a, b) => b.timestamp - a.timestamp);
      }
    });

    // 统计数据
    const totalCount = computed(() => parkingLots.value.length);
    const availableCount = computed(() => parkingLots.value.filter(lot => lot.payload_fields_park_flag === '0').length);
    const occupiedCount = computed(() => parkingLots.value.filter(lot => lot.payload_fields_park_flag === '1').length);

    // 初始化地图
    onMounted(async () => {
      map.value = new mapboxgl.Map({
        container: mapContainer.value,
        style: 'mapbox://styles/mapbox/streets-v11',
        center: CBD_CENTER,
        zoom: 12
      });

      // 添加导航控件
      map.value.addControl(new mapboxgl.NavigationControl(), 'top-right');

      // 初始加载数据
      await refreshData();
    });

    return {
      mapContainer,
      searchQuery,
      parkingLots,
      sortedParkingLots,
      isLoading,
      totalCount,
      availableCount,
      occupiedCount,
      sortBy,
      searchParking,
      refreshData,
      zoomToParking,
      navigateToParking,
      formatTime
    };
  }
};
</script>

<style scoped>
.parking-app {
  position: relative;
  width: 100%;
  height: 100vh;
  font-family: 'Segoe UI', Arial, sans-serif;
}

.search-box {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  z-index: 10;
  display: flex;
  align-items: center;
  background: white;
  padding: 12px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.search-box input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  margin-right: 10px;
  font-size: 16px;
}

.search-box button {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  margin-right: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-box button.refresh-btn {
  background: #10b981;
}

.search-box button:hover {
  opacity: 0.9;
}

.search-box .stats {
  margin-left: auto;
  display: flex;
  gap: 10px;
  font-size: 14px;
  color: #4b5563;
}

.search-box .stats .available-count {
  color: #10b981;
  font-weight: bold;
}

.search-box .stats .occupied-count {
  color: #ef4444;
  font-weight: bold;
}

.map-container {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 100%;
}

.parking-list {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 350px;
  max-height: 60vh;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 10;
  border: 1px solid #e5e7eb;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.list-header h3 {
  margin: 0;
  color: #1e3a8a;
  font-size: 18px;
}

.sort-options {
  display: flex;
  gap: 12px;
}

.sort-options span {
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.sort-options span.active {
  color: #3b82f6;
  background: #eff6ff;
  font-weight: 500;
}

.parking-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.parking-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
  gap: 12px;
}

.parking-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.parking-item.available {
  border-left: 4px solid #10b981;
}

.parking-item.occupied {
  border-left: 4px solid #ef4444;
}

.status-icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-details {
  flex: 1;
  min-width: 0;
}

.parking-item h4 {
  margin: 0 0 6px 0;
  color: #1e3a8a;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.detail-row {
  display: flex;
  margin: 4px 0;
  font-size: 14px;
}

.detail-row span:first-child {
  color: #6b7280;
  min-width: 40px;
  margin-right: 8px;
}

.detail-row .available {
  color: #10b981;
  font-weight: 500;
}

.detail-row .occupied {
  color: #ef4444;
  font-weight: 500;
}

.detail-row .low-battery {
  color: #f59e0b;
  font-weight: 500;
}

.navigate-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #3b82f6;
  padding: 6px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.navigate-btn:hover {
  background: #eff6ff;
}

/* 停车标记样式 */
.parking-marker {
  position: relative;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.parking-marker svg {
  width: 24px;
  height: 24px;
  filter: drop-shadow(0 0 2px rgba(0, 0, 0, 0.3));
}

.marker-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 6px 12px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s;
  z-index: 10;
}

.parking-marker:hover .marker-tooltip {
  opacity: 1;
  visibility: visible;
  bottom: calc(100% + 8px);
}

/* 弹出窗口样式 */
.mapboxgl-popup-content {
  padding: 16px;
  border-radius: 8px;
  max-width: 260px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.popup-content h4 {
  margin: 0 0 12px 0;
  color: #1e3a8a;
  font-size: 16px;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 8px;
}

.popup-detail {
  margin: 8px 0;
  display: flex;
  font-size: 14px;
}

.popup-detail span {
  font-weight: 500;
  margin-right: 8px;
  color: #4b5563;
  min-width: 70px;
}

.popup-detail .available {
  color: #10b981;
}

.popup-detail .occupied {
  color: #ef4444;
}

.popup-detail .low-battery {
  color: #f59e0b;
}

.popup-button {
  width: 100%;
  padding: 8px;
  margin-top: 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.popup-button:hover {
  background: #2563eb;
}

/* 加载状态 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
