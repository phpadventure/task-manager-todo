import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { VueQueryPlugin } from '@tanstack/vue-query';
import router from '@/router';
import '@/styles/globals.css';
import '@/styles/variables.css';
import App from './App.vue';

const app = createApp(App);

app.use(createPinia());
app.use(VueQueryPlugin);
app.use(router);

app.mount('#app');
