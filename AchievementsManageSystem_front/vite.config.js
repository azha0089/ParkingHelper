import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  build: {
    chunkSizeWarningLimit: 1500,
  },
  server: {
    proxy: {
      '/gdex': {
        target: 'https://www.geelongdataexchange.com.au',
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^\/gdex/, ''),
      },
      '/nominatim': {
        target: 'https://nominatim.openstreetmap.org',
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^\/nominatim/, ''),
      },
      '/datavic': {
        target: 'https://discover.data.vic.gov.au',
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^\/datavic/, ''),
      },
      '/gmaps': {
        target: 'https://maps.googleapis.com',
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^\/gmaps/, ''),
      },
    },
  },
})
