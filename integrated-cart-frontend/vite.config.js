import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite 설정 파일 (참고: https://vitejs.dev/config/)
export default defineConfig({
    plugins: [react()],
})
