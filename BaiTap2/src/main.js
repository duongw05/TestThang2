import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap';
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import i18n from "@/translations/i18n.js";

const app = createApp(App)
app.use(i18n)
app.use(ElementPlus)
app.use(router)

app.mount('#app')
