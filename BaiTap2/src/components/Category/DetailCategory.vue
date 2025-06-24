<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>{{ $t('category.detailTitle') }}: {{ category?.categoryName || $t('loading') }}</span>
        <el-button type="primary" @click="goBack" size="small" plain>{{ $t('action.back') }}</el-button>
      </div>
    </template>

    <el-empty v-if="loading" :description="$t('loading')" />
    <el-empty v-else-if="error" :description="$t('error.notFound')" />

    <template v-else-if="category">
      <el-descriptions :title="$t('category.detailTitle')" :column="2" border>
        <el-descriptions-item :label="$t('category.code')">{{ category.categoryCode }}</el-descriptions-item>
        <el-descriptions-item :label="$t('category.name')">{{ category.categoryName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('category.status')">
          {{ category.status === 'ACTIVE' ? $t('status.active') : $t('status.inactive') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.createdBy')">
          {{ category.createdBy || $t('category.unknown') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.createdDate')">
          {{ formatDate(category.createdDate) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.modifiedBy')">
          {{ category.modifiedBy || $t('category.unknown') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.modifiedDate')">
          {{ formatDate(category.modifiedDate) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.description')">
          {{ category.description || $t('category.noDescription') }}
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="category.categoryImages?.length" style="margin-top: 30px">
        <div class="image-gallery-header">
          <h3>{{ $t('category.images') }}</h3>
          <span class="image-count">
            {{ $t('category.images') }} {{ currentImageIndex + 1 }} / {{ category.categoryImages.length }}
          </span>
        </div>
        <el-carousel height="200px" trigger="click" arrow="always" @change="handleCarouselChange">
          <el-carousel-item v-for="img in category.categoryImages" :key="img.id">
            <img
                :src="`data:image/png;base64,${img.image}`"
                alt="category image"
                style="max-height: 180px; margin: auto; display: block"
            />
          </el-carousel-item>
        </el-carousel>
      </div>
    </template>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/utils/axios.js';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const route = useRoute();
const router = useRouter();

const category = ref(null);
const loading = ref(false);
const error = ref(false);
const currentImageIndex = ref(0);

const formatDate = (dateString) => {
  if (!dateString) return t('category.unknown');
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN');
};

const goBack = () => {
  router.push('/categories');
};

const fetchCategory = async (id) => {
  loading.value = true;
  error.value = false;
  try {
    const res = await api.get(`/categories/${id}`);
    category.value = res.data;
  } catch (e) {
    error.value = true;
  } finally {
    loading.value = false;
  }
};

const handleCarouselChange = (newIndex) => {
  currentImageIndex.value = newIndex;
};

onMounted(() => {
  const id = route.params.id;
  if (id) {
    fetchCategory(id);
  } else {
    error.value = true;
  }
});
</script>

<style scoped>
.box-card {
  max-width: 1100px;
  margin: 30px auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.image-gallery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.image-count {
  font-weight: bold;
  color: #606266;
}
</style>
