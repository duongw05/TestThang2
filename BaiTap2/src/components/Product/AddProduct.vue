<template>
  <div class="container mt-5">
    <!-- Button for going back -->
    <div class="mb-3">
      <el-button type="primary" :icon="ArrowLeft" @click="goBack" round>
        Quay lại
      </el-button>
    </div>

    <!-- Add product form -->
    <el-card shadow="hover" class="p-4">
      <template #header>
        <div class="card-header-title text-center">
          <h2 class="mb-0">Thêm Sản Phẩm Mới</h2>
        </div>
      </template>
      <el-form :model="newProduct" :rules="rules" ref="productForm" label-position="top" @submit.prevent="openConfirmDialog">
        <!-- Category -->
        <el-form-item label="Danh mục" prop="categoryIds">
          <multiselect
              v-model="newProduct.categoryIds"
              :options="categoryList"
              :multiple="true"
              :close-on-select="false"
              placeholder="Chọn danh mục"
              label="categoryName"
              track-by="id"
              class="w-100"
              select-label="Nhấn Enter để chọn"
              deselect-label="Nhấn Enter để bỏ chọn"
              selected-label="Đã chọn"
          />
          <p class="mt-2">
            Danh mục đã chọn:
            <span v-if="newProduct.categoryIds.length === 0">Chưa chọn</span>
            <span v-else>{{ newProduct.categoryIds.map(c => c.categoryName).join(', ') }}</span>
          </p>
        </el-form-item>

        <!-- Product Name -->
        <el-form-item label="Tên sản phẩm" prop="productName">
          <el-input
              v-model="newProduct.productName"
              placeholder="Nhập tên sản phẩm"
              clearable
          />
        </el-form-item>

        <!-- Product Code -->
        <el-form-item label="Mã sản phẩm" prop="productCode">
          <el-input
              v-model="newProduct.productCode"
              placeholder="Nhập mã sản phẩm"
              clearable
          />
        </el-form-item>

        <!-- Description -->
        <el-form-item label="Mô tả" prop="description">
          <el-input
              type="textarea"
              v-model="newProduct.description"
              placeholder="Nhập mô tả sản phẩm"
              :rows="3"
          />
        </el-form-item>

        <!-- Price -->
        <el-form-item label="Giá bán" prop="price">
          <el-input
              type="number"
              v-model.number="newProduct.price"
              placeholder="Nhập giá bán"
              :min="0"
          />
        </el-form-item>

        <!-- Quantity -->
        <el-form-item label="Số lượng" prop="quantity">
          <el-input
              type="number"
              v-model.number="newProduct.quantity"
              placeholder="Nhập số lượng"
              :min="0"
          />
        </el-form-item>

        <!-- Image Upload -->
        <el-form-item label="Hình ảnh sản phẩm" prop="productImages">
          <el-upload
              class="upload-demo"
              action="#"
              :on-preview="handlePreview"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              list-type="picture-card"
              :limit="10"
              :file-list="fileList"
              :auto-upload="false"
              multiple
              accept="image/*"
          >
            <el-button type="primary">Tải lên hình ảnh</el-button>
          </el-upload>
          <p v-if="imageError" class="text-danger mt-2">Vui lòng tải lên ít nhất một hình ảnh</p>
        </el-form-item>

        <!-- Submit Button -->
        <div class="text-center">
          <el-button type="primary" native-type="submit" :loading="loading">Thêm sản phẩm</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- Confirmation Dialog -->
    <el-dialog
        title="Xác nhận"
        v-model="isModalVisible"
        width="30%"
        center
    >
      <span>Bạn có chắc chắn muốn lưu sản phẩm này không?</span>
      <template #footer>
        <el-button @click="isModalVisible = false">Hủy</el-button>
        <el-button type="primary" @click="saveProduct" :loading="loading">Xác nhận</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.css';
import { ElNotification, ElForm, ElFormItem, ElInput, ElButton, ElCard, ElUpload, ElDialog } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';

const router = useRouter();
const productForm = ref(null);
const loading = ref(false);
const isModalVisible = ref(false);
const fileList = ref([]);
const imageError = ref(false);
const categoryList = ref([]);

const newProduct = ref({
  productName: '',
  productCode: '',
  description: '',
  price: null,
  quantity: null,
  categoryIds: [],
  productImages: []
});

const rules = ref({
  productName: [
    { required: true, message: 'Vui lòng nhập tên sản phẩm', trigger: 'blur' },
    { max: 200, message: 'Tên sản phẩm không được vượt quá 200 ký tự', trigger: 'blur' }
  ],
  productCode: [
    { required: true, message: 'Vui lòng nhập mã sản phẩm', trigger: 'blur' }
  ],
  description: [
    { required: true, message: 'Vui lòng nhập mô tả sản phẩm', trigger: 'blur' },
    { max: 200, message: 'Mô tả không được vượt quá 200 ký tự', trigger: 'blur' }
  ],
  price: [
    { required: true, message: 'Vui lòng nhập giá bán', trigger: 'blur' },
    { type: 'number', min: 0, message: 'Giá bán phải từ 0 trở lên', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: 'Vui lòng nhập số lượng', trigger: 'blur' },
    { type: 'number', min: 0, message: 'Số lượng phải từ 0 trở lên', trigger: 'blur' }
  ],
  categoryIds: [
    {
      validator: (rule, value, callback) => {
        if (value.length === 0) {
          callback(new Error('Vui lòng chọn ít nhất một danh mục'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  productImages: [
    {
      validator: (rule, value, callback) => {
        if (fileList.value.length === 0) {
          callback(new Error('Vui lòng tải lên ít nhất một hình ảnh'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ]
});

const fetchCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/categories');
    categoryList.value = response.data || [];
  } catch (error) {
    console.error('Lỗi lấy danh mục:', error);
    ElNotification.error({
      title: 'Lỗi',
      message: 'Không thể tải danh mục.',
      position: 'top-right'
    });
  }
};

const handleFileChange = (file, fileListNew) => {
  const maxSize = 3 * 1024 * 1024; // 3MB
  if (file.size > maxSize) {
    ElNotification.error({
      title: 'Lỗi',
      message: `Ảnh ${file.name} vượt quá kích thước tối đa (3MB)!`,
      position: 'top-right'
    });
    fileListNew.splice(fileListNew.indexOf(file), 1);
    return;
  }

  const isDuplicate = fileList.value.some(f => f.name === file.name && f.size === file.size);
  if (isDuplicate) {
    ElNotification.error({
      title: 'Lỗi',
      message: `Ảnh ${file.name} đã được chọn!`,
      position: 'top-right'
    });
    fileListNew.splice(fileListNew.indexOf(file), 1);
    return;
  }

  fileList.value = fileListNew;
  imageError.value = fileList.value.length === 0;
  newProduct.value.productImages = fileList.value.map(item => item.raw).filter(Boolean);
};

const handleFileRemove = (file, fileListNew) => {
  fileList.value = fileListNew;
  imageError.value = fileList.value.length === 0;
  newProduct.value.productImages = fileList.value.map(item => item.raw).filter(Boolean);
};

const handlePreview = (file) => {
  window.open(file.url || URL.createObjectURL(file.raw), '_blank');
};

const openConfirmDialog = () => {
  productForm.value.validate((valid) => {
    if (valid) {
      isModalVisible.value = true;
    } else {
      ElNotification.error({
        title: 'Lỗi',
        message: 'Vui lòng kiểm tra lại các trường thông tin!',
        position: 'top-right'
      });
    }
  });
};

const saveProduct = async () => {
  loading.value = true;
  try {
    const formData = new FormData();
    formData.append('productName', newProduct.value.productName || '');
    formData.append('productCode', newProduct.value.productCode || '');
    formData.append('description', newProduct.value.description || '');
    formData.append('price', newProduct.value.price || 0);
    formData.append('quantity', newProduct.value.quantity || 0);

    newProduct.value.categoryIds.forEach((cat, index) => {
      formData.append(`categories[${index}]`, cat.id);
    });

    newProduct.value.productImages.forEach((file, index) => {
      formData.append('productImages', file);
    });

    await axios.post('http://localhost:8080/api/products', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    ElNotification.success({
      title: 'Thành công',
      message: 'Thêm sản phẩm thành công!',
      position: 'top-right'
    });

    isModalVisible.value = false;
    productForm.value.resetFields();
    newProduct.value = {
      productName: '',
      productCode: '',
      productId: '',
      description: '',
      price: null,
      quantity: null,
      categoryIds: [],
      productImages: []
    };
    fileList.value = [];

    setTimeout(() => {
      router.push('/product');
    }, 1000);
  } catch (error) {
    console.error('Lỗi thêm sản phẩm:', error);
    const errorMessage = error.response?.data?.error || 'Đã xảy ra lỗi khi thêm sản phẩm.';
    ElNotification.error({
      title: 'Lỗi',
      message: errorMessage,
      position: 'top-right'
    });
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push('/product');
};

onMounted(() => {
  fetchCategories();
});
</script>

<style scoped>
.container {
  max-width: 800px;
}

.el-card {
  border-radius: 8px;
}

.el-form-item {
  margin-bottom: 1.5rem;
}

.w-100 {
  width: 100%;
}

.upload-demo .el-upload {
  width: 100%;
}

.multiselect {
  border-radius: 4px;
}

.text-danger {
  color: #dc3545;
}
</style>