<template>
  <div class="container mt-5">
    <div class="mb-3">
      <el-button type="primary" :icon="ArrowLeft" @click="goBack" round>
        {{ $t('action.back') }}
      </el-button>
    </div>

    <el-card shadow="hover" class="p-4">
      <template #header>
        <div class="card-header-title text-center">
          <h2 class="mb-0">{{ currentTitle }}</h2>
        </div>
      </template>

      <el-empty v-if="loading && (mode === 'view' || mode === 'edit')" :description="$t('loading')" />
      <el-empty v-else-if="error && (mode === 'view' || mode === 'edit')" :description="$t('error.notFound')" />

      <template v-else-if="mode === 'view' && productData">
        <el-descriptions :title="$t('product.infoTitle')" :column="2" border>
          <el-descriptions-item :label="$t('product.productCode')">{{ productData.productCode }}</el-descriptions-item>
          <el-descriptions-item :label="$t('product.productName')">{{ productData.productName }}</el-descriptions-item>
          <el-descriptions-item :label="$t('product.status')">
            {{ productData.status === 'ACTIVE' ? $t('status.active') : $t('status.inactive') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('product.price')">
            {{ formatCurrency(productData.price) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('product.quantity')">{{ productData.quantity }}</el-descriptions-item>
          <el-descriptions-item :label="$t('product.createdBy')">{{ productData.createdBy || $t('product.noValue') }}</el-descriptions-item>
          <el-descriptions-item :label="$t('product.createDate')">{{ formatDate(productData.createdDate) }}</el-descriptions-item>
          <el-descriptions-item :label="$t('product.modifiedBy')">{{ productData.modifiedBy || $t('product.noValue') }}</el-descriptions-item>
          <el-descriptions-item :label="$t('product.updateDate')">{{ formatDate(productData.modifiedDate) }}</el-descriptions-item>
          <el-descriptions-item :label="$t('product.description')">{{ productData.description || $t('product.noDescription') }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="productData.categories?.length" class="mt-4">
          <h3>{{ $t('product.relatedCategories') }}</h3>
          <el-tag
              v-for="cat in productData.categories"
              :key="cat.id"
              class="me-2"
              type="success"
              effect="plain"
          >
            {{ cat.categoryName }}
          </el-tag>
        </div>

        <div v-if="productData.productImages?.length" class="mt-4">
          <div class="image-gallery-header">
            <h3>{{ $t('product.images') }}</h3>
            <span class="image-count" v-if="productData.productImages.length > 0">
                {{ $t('product.imageIndex', { current: currentImageIndex + 1, total: productData.productImages.length }) }}
            </span>
          </div>
          <el-carousel height="200px" trigger="click" arrow="always" @change="handleCarouselChange">
            <el-carousel-item
                v-for="(img, index) in productData.productImages"
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

      <el-form
          v-else
          :model="productFormModel"
          :rules="currentRules"
          ref="productFormRef"
          label-position="top"
          @submit.prevent="openConfirmDialog"
      >
        <el-form-item :label="$t('product.category')" prop="categoryIds">
          <multiselect
              v-model="productFormModel.categoryIds"
              :options="categoryList"
              :multiple="true"
              :close-on-select="false"
              :placeholder="$t('placeholder.selectCategory')"
              label="categoryName"
              track-by="id"
              class="w-100"
              :select-label="$t('product.selectLabel')"
              :deselect-label="$t('product.deselectLabel')"
              :selected-label="$t('product.selectedLabel')"
          />
          <p class="mt-2">
            {{ $t('product.selectedCategories') }}:
            <span v-if="productFormModel.categoryIds.length === 0">{{ $t('product.noValue') }}</span>
            <span v-else>{{ productFormModel.categoryIds.map(c => c.categoryName).join(', ') }}</span>
          </p>
        </el-form-item>

        <el-form-item :label="$t('product.productCode')" prop="productCode">
          <el-input v-model="productFormModel.productCode" :placeholder="$t('placeholder.enterCode')" clearable :disabled="mode === 'edit'" />
        </el-form-item>

        <el-form-item :label="$t('product.productName')" prop="productName">
          <el-input v-model="productFormModel.productName" :placeholder="$t('placeholder.enterName')" clearable />
        </el-form-item>

        <el-form-item :label="$t('product.description')" prop="description">
          <el-input type="textarea" v-model="productFormModel.description" :placeholder="$t('placeholder.enterDescription')" :rows="3" />
        </el-form-item>

        <el-form-item :label="$t('product.price')" prop="price">
          <el-input type="number" v-model.number="productFormModel.price" :placeholder="$t('placeholder.enterPrice')" :min="0" />
        </el-form-item>

        <el-form-item :label="$t('product.quantity')" prop="quantity">
          <el-input type="number" v-model.number="productFormModel.quantity" :placeholder="$t('placeholder.enterQuantity')" :min="0" />
        </el-form-item>

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
            <el-button type="primary">{{ $t('product.uploadImage') }}</el-button>
          </el-upload>
          <p v-if="imageError" class="text-danger mt-2">{{ $t('messages.imageRequired') }}</p>
        </el-form-item>

        <div class="text-center">
          <el-button v-if="mode === 'add'" type="primary" native-type="submit" :loading="submitLoading">{{ $t('product.add') }}</el-button>
          <el-button v-else-if="mode === 'edit'" type="primary" native-type="submit" :loading="submitLoading">{{ $t('product.submitUpdate') }}</el-button>
        </div>
      </el-form>
    </el-card>

    <el-dialog
        v-model="confirmDialogVisible"
        :title="t('action.confirm')"
        width="400px"
        :close-on-click-modal="false"
        center
    >
      <span>{{ t('messages.confirmSaveProduct') }}</span>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">{{ t('action.cancel') }}</el-button>
        <el-button type="primary" @click="handleConfirmSubmit">{{ t('action.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.css';
import { ElNotification, ElMessageBox, ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import api from '@/utils/axios.js';

const props = defineProps({
  mode: {
    type: String,
    required: true,
    validator: (value) => ['add', 'view', 'edit'].includes(value)
  }
});

const { t } = useI18n();
const router = useRouter();
const route = useRoute();

const productFormRef = ref(null);
const loading = ref(false);
const submitLoading = ref(false);
const confirmDialogVisible = ref(false);
const fileList = ref([]);
const imageError = ref(false);
const categoryList = ref([]);
const error = ref(false);

const productData = ref(null);

const productFormModel = ref({
  id: null,
  productName: '',
  productCode: '',
  description: '',
  price: null,
  quantity: null,
  categoryIds: [],
  productImages: []
});

const deletedImageIds = ref([]);
const currentImageIndex = ref(0);

const currentTitle = computed(() => {
  switch (props.mode) {
    case 'add':
      return t('product.add');
    case 'view':
      return `${t('product.detailTitle')}: ${productData.value?.productName || t('loading')}`;
    case 'edit':
      return t('product.updateTitle');
    default:
      return '';
  }
});

const currentRules = computed(() => {
  const commonRules = {
    productName: [
      { required: true, message: t('validation.productNameRequired'), trigger: 'blur' },
      { max: 200, message: t('validation.maxLength', { max: 200 }), trigger: 'blur' }
    ],
    description: [
      { required: true, message: t('validation.descriptionRequired'), trigger: 'blur' },
      { max: 200, message: t('validation.maxLength', { max: 200 }), trigger: 'blur' }
    ],
    price: [
      { required: true, message: t('validation.priceRequired'), trigger: 'blur' },
      { type: 'number', min: 0, message: t('validation.priceMin'), trigger: 'blur' }
    ],
    quantity: [
      { required: true, message: t('validation.quantityRequired'), trigger: 'blur' },
      { type: 'number', min: 0, message: t('validation.quantityMin'), trigger: 'blur' }
    ],
    categoryIds: [
      {
        validator: (rule, value, callback) => {
          if (!value || value.length === 0) {
            callback(new Error(t('validation.categoryRequired')));
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
            imageError.value = true;
            callback(new Error(t('messages.imageRequired')));
          } else {
            imageError.value = false;
            callback();
          }
        },
        trigger: 'change'
      }
    ]
  };

  if (props.mode === 'add') {
    return {
      ...commonRules,
      productCode: [
        { required: true, message: t('validation.productCodeRequired'), trigger: 'blur' }
      ]
    };
  }
  return commonRules;
});

const formatDate = (dateString) => {
  if (!dateString) return t('product.noValue');
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN');
};

const formatCurrency = (value) => {
  if (value === null || value === undefined) return t('product.noValue');
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value);
};

const goBack = () => {
  router.push('/product');
};

const handleFileChange = (file, fileListNew) => {
  const maxSize = 3 * 1024 * 1024;

  if (file.raw && file.raw.size > maxSize) {
    ElNotification.error({ title: t('action.error'), message: t('messages.imageTooLarge', { fileName: file.name }) });
    fileList.value = fileListNew.filter(f => f.uid !== file.uid);
    productFormRef.value?.validateField('productImages');
    return;
  }

  fileList.value = fileListNew.map(f => ({
    ...f,
    url: f.url || (f.raw ? URL.createObjectURL(f.raw) : ''),
    isOld: !!f.isOld,
    id: f.id || null
  }));

  imageError.value = fileList.value.length === 0;
  productFormModel.value.productImages = fileList.value.filter(f => f.raw).map(item => item.raw);
  productFormRef.value?.validateField('productImages');
};

const handleFileRemove = (file, fileListNew) => {
  if (file.isOld && file.id) {
    deletedImageIds.value.push(file.id);
  }
  fileList.value = fileListNew;
  imageError.value = fileList.value.length === 0;
  productFormModel.value.productImages = fileList.value.filter(f => f.raw).map(item => item.raw);
  productFormRef.value?.validateField('productImages');
};

const handlePreview = (file) => {
  if (file.url) {
    window.open(file.url, '_blank');
  }
};

const handleCarouselChange = (newIndex) => {
  currentImageIndex.value = newIndex;
};

const fetchCategories = async () => {
  try {
    const response = await api.get('/categories');
    categoryList.value = response.data || [];
  } catch (error) {
    console.error('Lỗi khi fetch danh mục:', error);
    ElNotification.error({ title: t('action.error'), message: t('messages.loadFailed') });
  }
};

const fetchProductDetail = async (id) => {
  loading.value = true;
  error.value = false;
  try {
    const res = await api.get(`/products/${id}`);
    const data = res.data;

    if (props.mode === 'view') {
      productData.value = data;
    } else if (props.mode === 'edit') {
      productFormModel.value = {
        id: data.id,
        productName: data.productName,
        productCode: data.productCode,
        description: data.description,
        price: data.price,
        quantity: data.quantity,
        categoryIds: data.categories?.map(c => ({ id: c.id, categoryName: c.categoryName })) || [],
        productImages: []
      };
      fileList.value = data.productImages?.map(img => ({
        name: img.imageName,
        url: `data:image/png;base64,${img.image}`,
        isOld: true,
        id: img.id
      })) || [];
      productFormRef.value?.validateField('productImages');
    }
  } catch (e) {
    console.error('Lỗi khi fetch sản phẩm:', e);
    error.value = true;
    ElNotification.error({ title: t('action.error'), message: t('messages.loadProductError') });
  } finally {
    loading.value = false;
  }
};

const openConfirmDialog = () => {
  productFormRef.value.validate((valid) => {
    if (valid) {
      confirmDialogVisible.value = true;
    } else {
      ElNotification.error({ title: t('action.error'), message: t('messages.validateError') });
    }
  });
};

const handleConfirmSubmit = async () => {
  confirmDialogVisible.value = false;
  submitLoading.value = true;

  try {
    const formData = new FormData();
    formData.append('productName', productFormModel.value.productName);t
    if (props.mode === 'add') {
      formData.append('productCode', productFormModel.value.productCode);
    } else if (props.mode === 'edit') {
    }
    formData.append('description', productFormModel.value.description);
    formData.append('price', productFormModel.value.price);
    formData.append('quantity', productFormModel.value.quantity);

    productFormModel.value.categoryIds.forEach((cat) => {
      formData.append(`categories`, cat.id);
    });

    fileList.value.forEach(file => {
      if (!file.isOld && file.raw) {
        formData.append('productImages', file.raw);
      }
    });

    deletedImageIds.value.forEach(id => {
      formData.append('oldImageIds', id);
    });

    if (props.mode === 'add') {
      await api.post('/products', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      ElNotification.success({ title: t('action.success'), message: t('messages.addProductSuccess') });
    } else if (props.mode === 'edit') {
      const productId = route.params.id;
      if (!productId) {
        throw new Error("Không tìm thấy ID sản phẩm để cập nhật.");
      }
      await api.put(`/products/${productId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      ElNotification.success({ title: t('action.success'), message: t('product.updateSuccess') });
    }

    if (productFormRef.value) {
      productFormRef.value.resetFields();
    }
    productFormModel.value = {
      id: null, productName: '', productCode: '', description: '',
      price: null, quantity: null, categoryIds: [], productImages: []
    };
    fileList.value = [];
    deletedImageIds.value = [];
    imageError.value = false;

    setTimeout(() => { router.push('/product'); }, 1000);

  } catch (error) {
    console.error("Lỗi khi gửi form:", error);
    let errorMessage = t('messages.generalError');
    if (error.response && error.response.data && error.response.data.error) {
      errorMessage = error.response.data.error;
    } else if (error.message) {
      errorMessage = error.message;
    }
    ElNotification.error({ title: t('action.error'), message: errorMessage });
  } finally {
    submitLoading.value = false;
  }
};

onMounted(async () => {
  await fetchCategories();

  if (props.mode === 'view' || props.mode === 'edit') {
    const id = route.params.id;
    if (id) {
      await fetchProductDetail(id);
    } else {
      error.value = true;
      ElNotification.error({ title: t('action.error'), message: t('messages.invalidProductId') });
    }
  }
});

watch(() => route.params.id, async (newId, oldId) => {
  if ((props.mode === 'view' || props.mode === 'edit') && newId && newId !== oldId) {
    await fetchProductDetail(newId);
  } else if (!newId && (props.mode === 'view' || props.mode === 'edit')) {
    error.value = true;
    ElNotification.error({ title: t('action.error'), message: t('messages.invalidProductId') });
  }
}, { immediate: false });

watch(() => props.mode, (newMode, oldMode) => {
  if (newMode === 'add' && (oldMode === 'edit' || oldMode === 'view')) {
    if (productFormRef.value) {
      productFormRef.value.resetFields();
    }
    productFormModel.value = {
      id: null, productName: '', productCode: '', description: '',
      price: null, quantity: null, categoryIds: [], productImages: []
    };
    fileList.value = [];
    deletedImageIds.value = [];
    imageError.value = false;
    error.value = false;
    loading.value = false;
  } else if ((newMode === 'view' || newMode === 'edit') && newMode !== oldMode) {
    const id = route.params.id;
    if (id) {
      fetchProductDetail(id);
    } else {
      error.value = true;
      ElNotification.error({ title: t('action.error'), message: t('messages.invalidProductId') });
    }
  }
});
</script>

<style scoped>
.container { max-width: 800px; }
.el-card { border-radius: 8px; }
.el-form-item { margin-bottom: 1.5rem; }
.w-100 { width: 100%; }
.upload-demo :deep(.el-upload--picture-card) {
  width: 148px;
  height: 148px;
  line-height: 148px;
}
.upload-demo :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 148px;
  height: 148px;
}
.multiselect { border-radius: 4px; }
.text-danger { color: #dc3545; }

/* Styles for view mode image gallery */
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