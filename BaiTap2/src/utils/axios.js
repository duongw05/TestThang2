import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:8080/api'
})

api.interceptors.request.use(config => {
    const lang = localStorage.getItem('lang') || 'vi'
    config.headers['Accept-Language'] = lang
    return config
})

export default api
