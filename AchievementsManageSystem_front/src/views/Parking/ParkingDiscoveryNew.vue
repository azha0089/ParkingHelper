<template>
  <div class="parking-app">
    <div class="search-box">
      <input
          v-model="searchQuery"
          type="text"
          placeholder="Search location..."
          @keyup.enter="searchDestination"
      />
      <button @click="searchDestination">Search</button>
    </div>

    <div ref="mapContainer" class="map-container"></div>
    <div class="parking-list" v-if="groupedParkingLots.length > 0">
        <h3>Nearby parking ({{ groupedParkingLots.length }})</h3>
        <div class="parking-group"
             :class="{ highlighted: highlightedGroupId === group.zone_number }"
             :ref="el => setGroupRef(el, group.zone_number)"
             v-for="group in groupedParkingLots" :key="group.id">
          <h4>{{ group.name }}</h4>
          <p>Available: {{ group.availableCount }} / {{ group.locations.length }}</p>
          <p>Distance: {{ group.distance.toFixed(1) }} km</p>
          <button @click="zoomToGroup(group)">View</button>
          <button style="margin-left:20px;background: #13ce66" @click="onPredictionClick(group)">Prediction</button>
          <!-- 显示预测结果 -->
          <div v-if="selectedGroup && selectedGroup.id === group.id && predictionResult" style="margin-top:8px; color:#047857;">
            <strong>Prediction @ {{ selectedTime }}:</strong>
            <div>Available Rate: {{ (predictionResult.freeRate * 100).toFixed(1) }}%</div>
            <div>Congestion: {{ predictionResult.congestion }}</div>
          </div>
        </div>
      </div>
    <div v-else class="no-data">
      <p>No parking data found</p>
    </div>

    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>Loading...</p>
    </div>
  </div>
  <!-- 时间选择弹窗 -->
  <TimePickerModal
      :visible="timePickerVisible"
      @confirm="onTimeConfirm"
      @close="onTimePickerClose"
  />
</template>

<script>
import {ref, onMounted, computed, nextTick} from 'vue';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';
import TimePickerModal from './TimePickerModal.vue';

export default {
  name: 'MelbourneParkingApp',
  components: {
    TimePickerModal
  },
  setup() {
    const timePickerVisible = ref(false);
    const selectedGroup = ref(null);
    const predictionResult = ref(null);
    const selectedTime = ref(null);
    const highlightedGroupId = ref(null);

    const groupRefs = ref({});

    const setGroupRef = (el, id) => {
      if (el) {
        groupRefs.value[id] = el;
      }
    };

    // 点击预测按钮，弹出时间选择器
    const onPredictionClick = (group) => {
      console.log(group)
      selectedGroup.value = group;
      predictionResult.value = null;
      timePickerVisible.value = true;
    };

    // 用户选完时间回调
    const onTimeConfirm = async (dateTime) => {
      timePickerVisible.value = false;
      selectedTime.value = dateTime;

      if (!selectedGroup.value) return;

      isLoading.value = true;
      try {
        // 这里取组内第一个停车点的roadsegmentid做示例
        console.log(selectedGroup)
        const roadsegmentid = selectedGroup.value.zone_number
        if (!roadsegmentid) {
          alert('No valid parking lot in this group for prediction');
          return;
        }
        console.log(roadsegmentid,dateTime)
        const result = await fetchParkingStatusSummary(roadsegmentid, dateTime);
        console.log(result)
        predictionResult.value = result;
      } catch (e) {
        alert('Prediction failed');
      } finally {
        isLoading.value = false;
      }
    };

    const onTimePickerClose = () => {
      timePickerVisible.value = false;
    };

    // Mapbox配置
    const accessToken = 'pk.eyJ1IjoiMTAxMDI5MjQ2NiIsImEiOiJjbWUzc2QyY2EwYmdvMmxwdzdsYzluYnhtIn0.rVNlhc6s2AowPgyWNsImyw';
    mapboxgl.accessToken = accessToken;

    // 墨尔本中心点(Flinders Station)
    const CBD_CENTER = [144.9671, -37.8183];
    const SEARCH_RADIUS = 100000; // 1公里半径

    // 引用和状态
    const mapContainer = ref(null);
    const map = ref(null);
    const searchQuery = ref('');
    const userLocation = ref(CBD_CENTER);
    const parkingLots = ref([]);
    const isLoading = ref(false);
    const markers = ref([]);

    // 分组停车点数据
    const groupedParkingLots = computed(() => {
      const groups = {};

      parkingLots.value.forEach(lot => {
        // 使用区域编号作为分组依据
        const zoneName = lot.zone_number ? `Zone ${lot.zone_number}` : 'Unknown Zone';

        if (!groups[zoneName]) {
          groups[zoneName] = {
            id: `group-${zoneName.replace(/\s+/g, '-')}`,
            name: zoneName,
            locations: [],
            availableCount: 0,
            center: [0, 0],
            distance: lot.distance,
            zone_number: lot.zone_number
          };
        }

        groups[zoneName].locations.push(lot.location);
        if (lot.status_description === 'Unoccupied') {
          groups[zoneName].availableCount++;
        }

        // 计算组中心点
        const sum = groups[zoneName].locations.reduce((acc, [lng, lat]) => {
          acc.lng += lng;
          acc.lat += lat;
          return acc;
        }, { lng: 0, lat: 0 });

        groups[zoneName].center = [
          sum.lng / groups[zoneName].locations.length,
          sum.lat / groups[zoneName].locations.length
        ];
      });

      return Object.values(groups).sort((a, b) => a.distance - b.distance);
    });

    async function fetchParkingStatusSummary(zoneNumber) {
      // 不带时间限制，查询指定zone所有数据
      const whereClause = `zone_number=${zoneNumber}`;
      const selectClause = `status_description, count(*) as count`;
      const groupByClause = `status_description`;

      const url = `https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/on-street-parking-bay-sensors/records?` +
          `where=${encodeURIComponent(whereClause)}` +
          `&select=${encodeURIComponent(selectClause)}` +
          `&group_by=${encodeURIComponent(groupByClause)}`;

      console.log("请求URL:", url);

      const response = await fetch(url);
      const data = await response.json();

      if (!data.results || data.results.length === 0) {
        return {
          total: 0,
          freeRate: null,
          congestion: "Unknown"
        };
      }

      // 计算总记录数和空闲数量
      let totalCount = 0;
      if(data.results[0]) {
        totalCount = data.results[0].count;
      }
      if(data.results[1]) {
        totalCount+=data.results[1].count;
      }

      let unoccupiedCount = data.results.find(item => item.status_description === "Unoccupied")?.count || 0;

      // 计算空闲率
      const freeRate = totalCount > 0 ? unoccupiedCount / totalCount : null;

      // 简单拥堵等级判定
      let congestion = "Unknown";
      if (freeRate !== null) {
        if (freeRate > 0.7) congestion = "Low";
        else if (freeRate > 0.4) congestion = "Medium";
        else congestion = "High";
      }

      return {
        total: totalCount,
        freeRate,
        congestion
      };
    }





    // 获取实时停车数据
    const fetchParkingData = async (center) => {
      isLoading.value = true;
      try {
        const lat = center[1];
        const lon = center[0];

        const url = `https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/on-street-parking-bay-sensors/records?limit=100&where=within_distance(location, geom'POINT(${lon} ${lat})', ${SEARCH_RADIUS})&order_by=lastupdated DESC`;

        const response = await fetch(url);
        if (!response.ok) throw new Error('API request failed');

        const data = await response.json();

        // 处理API返回的数据
        return (data.results || []).map(item => ({
          id: `parking-${item.kerbsideid}`,
          kerbsideid: item.kerbsideid,
          zone_number: item.zone_number,
          status_description: item.status_description,
          lastupdated: item.lastupdated,
          status_timestamp: item.status_timestamp,
          location: [item.location.lon, item.location.lat],
          distance: calculateDistance(center, [item.location.lon, item.location.lat])
        }));

      } catch (error) {
        console.error('Error fetching parking data:', error);
        return [];
      } finally {
        isLoading.value = false;
      }
    };

    // 搜索目的地
    const searchDestination = async () => {
      if (!searchQuery.value.trim()) return;

      try {
        const response = await fetch(
            `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(searchQuery.value)}.json?` +
            `proximity=${CBD_CENTER[0]},${CBD_CENTER[1]}&access_token=${accessToken}`
        );

        const data = await response.json();

        if (!data.features || data.features.length === 0) {
          alert('No matching location found');
          return;
        }

        const bestMatch = data.features[0];
        userLocation.value = bestMatch.center;
        searchQuery.value = bestMatch.place_name;

        // 获取停车数据
        parkingLots.value = await fetchParkingData(userLocation.value);

        // 更新地图标记
        updateParkingMarkers();

        // 移动地图到搜索位置
        map.value.flyTo({ center: userLocation.value, zoom: 15 });

      } catch (error) {
        console.error('Search failed:', error);
        alert('Error during search');
      }
    };

    // 计算两点之间距离(公里)
    const calculateDistance = (point1, point2) => {
      const R = 6371; // 地球半径(km)
      const dLat = (point2[1] - point1[1]) * Math.PI / 180;
      const dLon = (point2[0] - point1[0]) * Math.PI / 180;
      const a =
          Math.sin(dLat/2) * Math.sin(dLat/2) +
          Math.cos(point1[1] * Math.PI / 180) *
          Math.cos(point2[1] * Math.PI / 180) *
          Math.sin(dLon/2) * Math.sin(dLon/2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      return R * c;
    };

    // 更新停车标记
    const updateParkingMarkers = () => {
      // 清除旧标记
      markers.value.forEach(marker => marker.remove());
      markers.value = [];

      parkingLots.value.forEach(lot => {
        const el = document.createElement('div');
        el.className = `parking-marker ${lot.status_description === 'Unoccupied' ? 'available' : 'occupied'}`;
        el.innerHTML = `<span>${lot.status_description === 'Unoccupied' ? '🟢' : '🔴'}</span>`;

        const popup = new mapboxgl.Popup({ offset: 25 })
            .setHTML(`
        <div class="popup-content">
          <h4>Parking #${lot.kerbsideid}</h4>
          <div class="popup-detail"><span>Status:</span> ${lot.status_description}</div>
          <div class="popup-detail"><span>Zone:</span> ${lot.zone_number || 'Unknown'}</div>
          <div class="popup-detail"><span>Last updated:</span> ${new Date(lot.status_timestamp).toLocaleString()}</div>
          <button class="popup-button"
            onclick="window.open('https://www.google.com/maps/dir/?api=1&origin=${userLocation.value[1]},${userLocation.value[0]}&destination=${lot.location[1]},${lot.location[0]}&travelmode=driving')">
            Navigate
          </button>
        </div>
      `);

        const marker = new mapboxgl.Marker(el)
            .setLngLat(lot.location)
            .setPopup(popup)
            .addTo(map.value);

        // 点击marker时高亮对应组
        el.addEventListener('click', () => {
          highlightedGroupId.value = lot.zone_number;
          // 如果你想同时自动滚动到对应组，也可以加个滚动逻辑
          nextTick(() => {
            const el = groupRefs.value[highlightedGroupId.value];
            if (el && el.scrollIntoView) {
              el.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
          });
        });

        markers.value.push(marker);
      });
    };


    // 缩放至停车场组
    const zoomToGroup = (group) => {
      if (!map.value || !group.locations.length) return;

      // 计算边界
      const bounds = new mapboxgl.LngLatBounds();
      group.locations.forEach(loc => bounds.extend(loc));

      map.value.fitBounds(bounds, {
        padding: 50,
        maxZoom: 16
      });
    };

    // 初始化地图
    onMounted(async () => {
      map.value = new mapboxgl.Map({
        container: mapContainer.value,
        style: 'mapbox://styles/mapbox/streets-v11',
        center: CBD_CENTER,
        zoom: 15
      });

      // 添加导航控件
      map.value.addControl(new mapboxgl.NavigationControl(), 'top-right');

      // 初始加载数据
      parkingLots.value = await fetchParkingData(CBD_CENTER);
      updateParkingMarkers();

      // 添加中心点标记
      new mapboxgl.Marker({ color: '#3b82f6' })
          .setLngLat(CBD_CENTER)
          .setPopup(new mapboxgl.Popup().setHTML('<h3>Flinders Street Station</h3>'))
          .addTo(map.value);
    });

    return {
      mapContainer,
      searchQuery,
      parkingLots,
      groupedParkingLots,
      isLoading,
      searchDestination,
      zoomToGroup,
      timePickerVisible,
      selectedGroup,
      predictionResult,
      selectedTime,
      onPredictionClick,
      onTimeConfirm,
      onTimePickerClose,
      highlightedGroupId,
      setGroupRef
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
  left: 50%;
  transform: translateX(-50%);
  width: 50%;
  z-index: 10;
  display: flex;
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
}

.search-box button:hover {
  background: #2563eb;
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
  width: 300px;
  max-height: 50vh;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.parking-group {
  padding: 12px;
  margin-bottom: 8px;
  border: 2px solid #ffffff;
  background-color: #ffffff;
}

.parking-group h4 {
  margin: 0 0 8px 0;
  color: #1e3a8a;
  font-size: 16px;
}

.parking-group p {
  margin: 4px 0;
  color: #4b5563;
  font-size: 14px;
}

.parking-group button {
  margin-top: 8px;
  padding: 6px 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.parking-group button:hover {
  background: #2563eb;
}

.no-data {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 300px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  text-align: center;
  color: #666;
  z-index: 10;
}

/* 停车标记样式 */
.parking-marker {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  border: 2px solid white;
  box-shadow: 0 0 5px rgba(0,0,0,0.3);
}

.parking-marker.available {
  background: #10b981;
}

.parking-marker.occupied {
  background: #ef4444;
}

/* 弹出窗口样式 */
.mapboxgl-popup-content {
  padding: 15px;
  border-radius: 8px;
  max-width: 250px;
}

.popup-content h4 {
  margin: 0 0 10px 0;
  color: #1e3a8a;
  font-size: 16px;
}

.popup-detail {
  margin: 8px 0;
  display: flex;
  font-size: 14px;
}

.popup-detail span {
  font-weight: 600;
  margin-right: 8px;
  color: #4b5563;
  min-width: 70px;
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
  background: rgba(255, 255, 255, 0.7);
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
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.parking-group.highlighted {
  border: 2px solid #13ce66;
  background-color: #dcfce7;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

</style>
