import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles.css'
import './prompt-builder.css'
import router from './router'
import RootApp from './RootApp.vue'

createApp(RootApp).use(ElementPlus).use(router).mount('#app')
