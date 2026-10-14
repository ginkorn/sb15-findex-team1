import { resolve } from "path";
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig(() => {
  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        "@": resolve(__dirname, "./src"),
      },
    },
    server: {
      proxy: {
        "/api": {
          target: "http://localhost:8080", // 백엔드 서버 주소
          changeOrigin: true, // 백엔드 서버의 CORS 정책을 우회
          secure: false, // HTTPS가 아닌 경우 필요
          // rewrite: (path) => path.replace(/^\/api/, '') // URL 재작성 가능
        },
      },
    },
  };
});
