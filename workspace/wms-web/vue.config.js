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
