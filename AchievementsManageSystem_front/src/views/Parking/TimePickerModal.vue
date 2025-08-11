<template>
  <div v-if="visible" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <h3>Select a Date & Time</h3>
      <input type="datetime-local" v-model="selectedDateTime" :min="minDate" :max="maxDate" />
      <div class="buttons">
        <button @click="confirm">Confirm</button>
        <button @click="close">Cancel</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';

export default {
  name: 'TimePickerModal',
  props: {
    visible: Boolean
  },
  emits: ['confirm', 'close'],
  setup(props, { emit }) {
    const selectedDateTime = ref('');

    // 限制只能选未来三天内的时间
    const now = new Date();
    const minDate = computed(() => now.toISOString().slice(0,16)); // 'YYYY-MM-DDTHH:mm'
    const maxDate = computed(() => {
      const d = new Date(now.getTime() + 3*24*60*60*1000);
      return d.toISOString().slice(0,16);
    });

    const confirm = () => {
      if (!selectedDateTime.value) {
        alert('Please select a date and time');
        return;
      }
      emit('confirm', selectedDateTime.value);
      selectedDateTime.value = '';
    };

    const close = () => {
      selectedDateTime.value = '';
      emit('close');
    };

    return { selectedDateTime, minDate, maxDate, confirm, close };
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 200;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  width: 320px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.3);
  text-align: center;
}

input[type="datetime-local"] {
  width: 100%;
  padding: 8px;
  margin-top: 12px;
  font-size: 16px;
  box-sizing: border-box;
}

.buttons {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
}

button {
  padding: 8px 14px;
  border: none;
  background: #3b82f6;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background: #2563eb;
}
</style>
