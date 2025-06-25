<template>
  <div class="d-flex min-vh-100">
    <SidebarLayout />
    <div class="d-flex flex-column w-100">
      <HeaderLayout />
      <div class="p-3">
        <router-view />
      </div>
    </div>
  </div>
  <FooterLayout />
</template>

<script setup>
import FooterLayout from './FooterLayout.vue';
import HeaderLayout from './HeaderLayout.vue';
import SidebarLayout from './SidebarLayout.vue';

</script>
