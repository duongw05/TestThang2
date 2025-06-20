<template>
  <header class="d-flex justify-content-between align-items-center bg-white text-dark p-3 shadow-sm">
    <!-- Carousel -->
    <div id="textCarousel" class="carousel slide w-50" data-bs-ride="carousel">
      <div class="carousel-inner text-center">
        <div class="carousel-item active">
          <h5 class="fw-bold">{{ $t('carousel.text1') }}</h5>
        </div>
        <div class="carousel-item">
          <h5 class="fw-bold text-danger">{{ $t('carousel.text2') }}</h5>
        </div>
        <div class="carousel-item">
          <h5 class="fw-bold text-success">{{ $t('carousel.text3') }}</h5>
        </div>
      </div>
    </div>

    <!-- Language Switch + Logout -->
    <div class="d-flex align-items-center gap-4">
      <div class="lang-switch d-flex align-items-center gap-2">
        <span class="fw-semibold">{{ locale === 'vi' ? 'VI' : 'EN' }}</span>

        <!-- Toggle -->
        <label class="switch">
          <input type="checkbox" :checked="locale === 'en'" @change="toggleLocale" />
          <span class="slider"></span>
        </label>

        <!-- Flag -->
        <img
            :src="locale === 'vi' ? viFlag : enFlag"
            alt="Flag"
            class="rounded-circle border"
            style="width: 28px; height: 28px; object-fit: cover"
        />
      </div>

      <button class="btn btn-danger">{{ $t('logout') }}</button>
    </div>
  </header>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

const viFlag = 'https://flagcdn.com/w40/vn.png'
const enFlag = 'https://flagcdn.com/w40/gb.png'

const toggleLocale = () => {
  locale.value = locale.value === 'vi' ? 'en' : 'vi'
  localStorage.setItem('lang', locale.value)
}
</script>

<style scoped>
/* Toggle switch đẹp */
.switch {
  position: relative;
  display: inline-block;
  width: 46px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 34px;
}

.slider::before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #0d6efd;
}

input:checked + .slider::before {
  transform: translateX(22px);
}
</style>
