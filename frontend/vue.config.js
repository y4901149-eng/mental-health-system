// Vue 配置文件
// 作用：配置开发服务器、代理转发，解决前后端联调的跨域问题
const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: [],
  parallel: false, // 禁用 thread-loader 避免缓存导致编译错误

  // 开发服务器配置
  devServer: {
    port: 3000,                // 前端开发服务器端口
    proxy: {
      // 将以 /api/ 开头的请求转发到后端
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // 将以 /uploads/ 开头的请求转发到后端（图片访问）
      '/uploads': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  },

  // 打包后的静态资源路径（相对路径，部署到子路径时有用）
  publicPath: '/'
})
