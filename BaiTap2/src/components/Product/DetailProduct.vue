<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>Chi tiết sản phẩm: {{ product?.productName || 'Đang tải...' }}</span>
        <el-button type="primary" @click="goBack" size="small" plain>Quay lại</el-button>
      </div>
    </template>

    <el-empty v-if="loading" description="Đang tải dữ liệu..." />
    <el-empty v-else-if="error" description="Không tìm thấy sản phẩm" />

    <template v-else-if="product">
      <!-- Thông tin sản phẩm -->
      <el-descriptions title="Thông tin sản phẩm" :column="2" border>
        <el-descriptions-item label="Mã sản phẩm">{{ product.productCode }}</el-descriptions-item>
        <el-descriptions-item label="Tên sản phẩm">{{ product.productName }}</el-descriptions-item>
        <el-descriptions-item label="Trạng thái">{{ product.status === 'ACTIVE' ? 'Hoạt động' : 'Ngừng hoạt động' }}</el-descriptions-item>
        <el-descriptions-item label="Giá">{{ formatCurrency(product.price) }}</el-descriptions-item>
        <el-descriptions-item label="Số lượng">{{ product.quantity }}</el-descriptions-item>
        <el-descriptions-item label="Người tạo">{{ product.createdBy || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Ngày tạo">{{ formatDate(product.createdDate) }}</el-descriptions-item>
        <el-descriptions-item label="Người sửa">{{ product.modifiedBy || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Ngày sửa">{{ formatDate(product.modifiedDate) }}</el-descriptions-item>
        <el-descriptions-item label="Mô tả">{{ product.description || 'Không có' }}</el-descriptions-item>
      </el-descriptions>

      <!-- Danh mục -->
      <div v-if="product.categories?.length" style="margin-top: 30px">
        <h3>Danh mục liên quan</h3>
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

      <!-- Ảnh sản phẩm -->
      <div v-if="product.productImages?.length" style="margin-top: 30px">
        <h3>Ảnh sản phẩm</h3>
        <el-carousel height="200px" trigger="click" arrow="always">
          <el-carousel-item v-for="img in product.productImages" :key="img.id">
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
import axios from 'axios';

const route = useRoute();
const router = useRouter();

const product = ref(null);
const loading = ref(false);
const error = ref(false);

const formatDate = (dateString) => {
  if (!dateString) return 'Không có';
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
    const res = await axios.get(`http://localhost:8080/api/products/${id}`);
    product.value = res.data;
  } catch (e) {
    console.error('Lỗi khi fetch sản phẩm:', e);
    error.value = true;
  } finally {
    loading.value = false;
  }
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
</style>
