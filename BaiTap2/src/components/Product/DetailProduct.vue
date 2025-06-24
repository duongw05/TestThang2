<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>{{ $t('product.detailTitle') }}: {{ product?.productName || $t('loading') }}</span>
        <el-button type="primary" @click="goBack" size="small" plain>
          {{ $t('action.back') }}
        </el-button>
      </div>
    </template>

    <el-empty v-if="loading" :description="$t('loading')" />
    <el-empty v-else-if="error" :description="$t('error.notFound')" />

    <template v-else-if="product">
      <el-descriptions :title="$t('product.infoTitle')" :column="2" border>
        <el-descriptions-item :label="$t('product.productCode')">{{ product.productCode }}</el-descriptions-item>
        <el-descriptions-item :label="$t('product.productName')">{{ product.productName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('product.status')">
          {{ product.status === 'ACTIVE' ? $t('status.active') : $t('status.inactive') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('product.price')">
          {{ formatCurrency(product.price) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('product.quantity')">{{ product.quantity }}</el-descriptions-item>
        <el-descriptions-item :label="$t('product.createdBy')">{{ product.createdBy || $t('product.noValue') }}</el-descriptions-item>
        <el-descriptions-item :label="$t('product.createDate')">{{ formatDate(product.createdDate) }}</el-descriptions-item>
        <el-descriptions-item :label="$t('product.modifiedBy')">{{ product.modifiedBy || $t('product.noValue') }}</el-descriptions-item>
        <el-descriptions-item :label="$t('product.updateDate')">{{ formatDate(product.modifiedDate) }}</el-descriptions-item>
        <el-descriptions-item :label="$t('product.description')">{{ product.description || $t('product.noDescription') }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="product.categories?.length" class="mt-4">
        <h3>{{ $t('product.relatedCategories') }}</h3>
        <el-tag
            v-for="cat in product.categories"
            :key="cat.id"
            class="me-2"
            type="success"
            effect="plain"
        >
          {{ cat.categoryName }}
        </el-tag>
      </div>

      <div v-if="product.productImages?.length" class="mt-4">
        <div class="image-gallery-header">
          <h3>{{ $t('product.images') }}</h3>
          <span class="image-count" v-if="product.productImages.length > 0">
              {{ $t('product.imageIndex', { current: currentImageIndex + 1, total: product.productImages.length }) }}
          </span>
        </div>
        <el-carousel height="200px" trigger="click" arrow="always" @change="handleCarouselChange">
          <el-carousel-item
              v-for="(img, index) in product.productImages"
              :key="img.id"
          >
            <img
                :src="`data:image/png;base64,${img.image}`"
                alt="product image"
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

const product = ref(null);
const loading = ref(false);
const error = ref(false);
const currentImageIndex = ref(0);

const formatDate = (dateString) => {
  if (!dateString) return t('product.noValue');
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN');
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value);
};

const goBack = () => {
  router.push('/product');
};

const fetchProduct = async (id) => {
  loading.value = true;
  error.value = false;
  try {
    const res = await api.get(`/products/${id}`);
    product.value = res.data;
  } catch (e) {
    console.error('Lỗi khi fetch sản phẩm:', e);
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
    fetchProduct(id);
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
