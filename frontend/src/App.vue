<template>
  <!-- 根组件：所有页面都在这里切换 -->
  <!-- router-view 是路由出口，显示当前路由对应的页面 -->
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
/* 全局基础样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

#app {
  height: 100%;
}
</style>
