<template>
  <div class="container mt-5">
    <!-- Button for going back -->
    <div class="mb-3">
      <el-button type="primary" :icon="ArrowLeft" @click="goBack" round>
        {{ $t('action.back') }}
      </el-button>
    </div>

    <!-- Add product form -->
    <el-card shadow="hover" class="p-4">
      <template #header>
        <div class="card-header-title text-center">
          <h2 class="mb-0">{{ $t('product.add') }}</h2>
        </div>
      </template>
      <el-form :model="newProduct" :rules="rules" ref="productForm" label-position="top" @submit.prevent="openConfirmDialog">

        <!-- Category -->
        <el-form-item :label="$t('product.category')" prop="categoryIds">
          <multiselect
              v-model="newProduct.categoryIds"
              :options="categoryList"
              :multiple="true"
              :close-on-select="false"
              :placeholder="$t('product.category')"
              label="categoryName"
              track-by="id"
              class="w-100"
              :select-label="$t('product.selectLabel')"
              :deselect-label="$t('product.deselectLabel')"
              :selected-label="$t('product.selectedLabel')"
          />
          <p class="mt-2">
            {{ $t('product.relatedCategories') }}:
            <span v-if="newProduct.categoryIds.length === 0">{{ $t('product.noValue') }}</span>
            <span v-else>{{ newProduct.categoryIds.map(c => c.categoryName).join(', ') }}</span>
          </p>
        </el-form-item>

        <!-- Product Name -->
        <el-form-item :label="$t('product.productName')" prop="productName">
          <el-input v-model="newProduct.productName" :placeholder="$t('placeholder.enterName')" clearable />
        </el-form-item>

        <!-- Product Code -->
        <el-form-item :label="$t('product.productCode')" prop="productCode">
          <el-input v-model="newProduct.productCode" :placeholder="$t('placeholder.enterCode')" clearable />
        </el-form-item>

        <!-- Description -->
        <el-form-item :label="$t('product.description')" prop="description">
          <el-input type="textarea" v-model="newProduct.description" :placeholder="$t('placeholder.enterDescription')" :rows="3" />
        </el-form-item>

        <!-- Price -->
        <el-form-item :label="$t('product.price')" prop="price">
          <el-input type="number" v-model.number="newProduct.price" :placeholder="$t('placeholder.enterPrice')" :min="0" />
        </el-form-item>

        <!-- Quantity -->
        <el-form-item :label="$t('product.quantity')" prop="quantity">
          <el-input type="number" v-model.number="newProduct.quantity" :placeholder="$t('placeholder.enterQuantity')" :min="0" />
        </el-form-item>

        <!-- Image Upload -->
        <el-form-item :label="$t('product.images')" prop="productImages">
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
            <el-button type="primary">{{ $t('category.uploadImages') }}</el-button>
          </el-upload>
          <p v-if="imageError" class="text-danger mt-2">{{ $t('messages.imageRequired') }}</p>
        </el-form-item>

        <!-- Submit Button -->
        <div class="text-center">
          <el-button type="primary" native-type="submit" :loading="loading">{{ $t('product.add') }}</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.css';
import { ElNotification, ElMessageBox, ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import api from '@/utils/axios.js';

const { t } = useI18n();
const router = useRouter();
const productForm = ref(null);
const loading = ref(false);
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
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { max: 200, message: t('validation.maxLength', { max: 200 }), trigger: 'blur' }
  ],
  productCode: [
    { required: true, message: t('validation.required'), trigger: 'blur' }
  ],
  description: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { max: 200, message: t('validation.maxLength', { max: 200 }), trigger: 'blur' }
  ],
  price: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { type: 'number', min: 0, message: t('product.price') + ' >= 0', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { type: 'number', min: 0, message: t('product.quantity') + ' >= 0', trigger: 'blur' }
  ],
  categoryIds: [
    {
      validator: (rule, value, callback) => {
        if (value.length === 0) {
          callback(new Error(t('validation.required')));
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
          callback(new Error(t('messages.imageRequired')));
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
    const response = await api.get('/categories');
    categoryList.value = response.data || [];
  } catch (error) {
    ElNotification.error({ title: t('action.error'), message: t('messages.loadFailed') });
  }
};

const handleFileChange = (file, fileListNew) => {
  const maxSize = 3 * 1024 * 1024;
  if (file.size > maxSize) {
    ElNotification.error({ title: t('action.error'), message: `${file.name} > 3MB` });
    fileListNew.splice(fileListNew.indexOf(file), 1);
    return;
  }
  const isDuplicate = fileList.value.some(f => f.name === file.name && f.size === file.size);
  if (isDuplicate) {
    ElNotification.error({ title: t('action.error'), message: `${file.name} ${t('messages.duplicate')}` });
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
      ElMessageBox.confirm(
          t('messages.confirmSaveProduct'),
          t('action.confirm'),
          {
            confirmButtonText: t('action.confirm'),
            cancelButtonText: t('action.cancel'),
            type: 'warning'
          }
      ).then(() => {
        saveProduct();
      }).catch(() => {
        ElMessage.info(t('action.cancel'));
      });
    } else {
      ElNotification.error({ title: t('action.error'), message: t('validation.required') });
    }
  });
};

const saveProduct = async () => {
  loading.value = true;
  try {
    const formData = new FormData();
    formData.append('productName', newProduct.value.productName);
    formData.append('productCode', newProduct.value.productCode);
    formData.append('description', newProduct.value.description);
    formData.append('price', newProduct.value.price);
    formData.append('quantity', newProduct.value.quantity);
    newProduct.value.categoryIds.forEach((cat, index) => {
      formData.append(`categories[${index}]`, cat.id);
    });
    newProduct.value.productImages.forEach(file => {
      formData.append('productImages', file);
    });
    await api.post('/products', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    ElNotification.success({ title: t('action.success'), message: t('messages.addProductSuccess') });
    productForm.value.resetFields();
    newProduct.value = { productName: '', productCode: '', description: '', price: null, quantity: null, categoryIds: [], productImages: [] };
    fileList.value = [];
    setTimeout(() => { router.push('/product'); }, 1000);
  } catch (error) {
    console.error(error);
    const errorMessage = error.response?.data?.error || t('messages.addProductFailed');
    ElNotification.error({ title: t('action.error'), message: errorMessage });
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push('/product');
};

onMounted(fetchCategories);
</script>

<style scoped>
.container { max-width: 800px; }
.el-card { border-radius: 8px; }
.el-form-item { margin-bottom: 1.5rem; }
.w-100 { width: 100%; }
.upload-demo .el-upload { width: 100%; }
.multiselect { border-radius: 4px; }
.text-danger { color: #dc3545; }
</style>
