const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  configureWebpack: {
    devtool: 'source-map',
    resolve: {
      alias: {
        '@': '/src'
      }
    },
    devServer: {
      port: 8080,
      proxy: {
        '/api': {
          target: process.env.VUE_APP_TARGET_URL || 'http://localhost:8090',
          changeOrigin: true,
          pathRewrite: { '^/api': '' }
        }
      },
      client: {
        overlay: {
          errors: false,
          warnings: false,
          runtimeErrors: false
        }
      }
    }
  }
})
