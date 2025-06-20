<template>
  <nav class="d-flex flex-column bg-white p-3 min-vh-100 border-end" style="width: 250px">
    <!-- Logo -->
    <div class="d-flex align-items-center mb-4">
      <img
          :src="logoSrc"
          alt="Logo"
          class="rounded-circle me-3"
          style="width: 200px; height: 50px; object-fit: contain"
      />
    </div>

    <!-- Navigation -->
    <ul class="nav flex-column">
      <!-- Home -->
      <li class="nav-item">
        <RouterLink
            class="nav-link py-3"
            :class="{ active: selected === 'home' }"
            to="/"
            @click="selectMenu('home')"
        >
          <i class="bi bi-house-door me-2"></i> {{ $t('sidebar.home') }}
        </RouterLink>
      </li>

      <!-- Product Management -->
      <li class="nav-item">
        <div
            class="nav-link py-3 d-flex justify-content-between align-items-center"
            @click="toggleCollapse('product')"
        >
          <span><i class="bi bi-box-seam me-2"></i> {{ $t('sidebar.productManagement') }}</span>
          <i class="bi" :class="isProductOpen ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <ul v-if="isProductOpen" class="nav flex-column ms-3">
          <li class="nav-item">
            <RouterLink
                class="nav-link py-2"
                :class="{ active: selected === 'products' }"
                to="/product"
                @click="selectMenu('products')"
            >
              <i class="bi bi-box me-2"></i> {{ $t('sidebar.products') }}
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
                class="nav-link py-2"
                :class="{ active: selected === 'categories' }"
                to="/categories"
                @click="selectMenu('categories')"
            >
              <i class="bi bi-list me-2"></i> {{ $t('sidebar.categories') }}
            </RouterLink>
          </li>
        </ul>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { ref } from 'vue'

const selected = ref('home')
const selectMenu = (menu) => {
  selected.value = menu
}

const logoSrc = ref('https://nodo.vn/wp-content/uploads/2021/12/logo_13.png')

const isProductOpen = ref(true)
const toggleCollapse = (section) => {
  if (section === 'product') isProductOpen.value = !isProductOpen.value
}
</script>

<style scoped>
nav {
  background-color: #ffffff;
  color: #333;
}

.nav-link {
  color: black;
  cursor: pointer;
}

.nav-link:hover {
  background-color: #f1f1f1;
  color: black;
}

.nav-link.active {
  background-color: #007bff;
  color: white;
  box-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
}

.nav-link i {
  font-size: 1.3rem;
}

.ms-3 {
  margin-left: 1rem;
}
</style>
