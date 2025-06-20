<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>Chi tiết danh mục: {{ category?.categoryName || 'Đang tải...' }}</span>
        <el-button type="primary" @click="goBack" size="small" plain>Quay lại</el-button>
      </div>
    </template>

    <el-empty v-if="loading" description="Đang tải dữ liệu..." />
    <el-empty v-else-if="error" description="Không tìm thấy danh mục" />

    <template v-else-if="category">
      <!-- Thông tin danh mục -->
      <el-descriptions title="Thông tin danh mục" :column="2" border>
        <el-descriptions-item label="Mã danh mục">{{ category.categoryCode }}</el-descriptions-item>
        <el-descriptions-item label="Tên danh mục">{{ category.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="Trạng thái">{{ category.status === 'ACTIVE' ? 'Hoạt động' : 'Ngừng hoạt động' }}</el-descriptions-item>
        <el-descriptions-item label="Người tạo">{{ category.createdBy || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Ngày tạo">{{ formatDate(category.createdDate) }}</el-descriptions-item>
        <el-descriptions-item label="Người sửa">{{ category.modifiedBy || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Ngày sửa">{{ formatDate(category.modifiedDate) }}</el-descriptions-item>
        <el-descriptions-item label="Mô tả">{{ category.description || 'Không có' }}</el-descriptions-item>
      </el-descriptions>

      <!-- Ảnh danh mục -->
      <div v-if="category.categoryImages?.length" style="margin-top: 30px">
        <h3>Ảnh danh mục</h3>
        <el-carousel height="200px" trigger="click" arrow="always">
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
import axios from 'axios';

const route = useRoute();
const router = useRouter();

const category = ref(null);
const loading = ref(false);
const error = ref(false);

const formatDate = (dateString) => {
  if (!dateString) return 'Không có';
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
    const res = await axios.get(`http://localhost:8080/api/categories/${id}`);
    category.value = res.data;
  } catch (e) {
    console.error('Lỗi khi fetch danh mục:', e);
    error.value = true;
  } finally {
    loading.value = false;
  }
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
</style>
