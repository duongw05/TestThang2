<template>
  <div class="container mt-5">
    <div class="mb-3">
      <el-button type="primary" :icon="ArrowLeft" @click="goBack" round>
        {{ t('action.back') }}
      </el-button>
    </div>

    <el-card shadow="hover" class="p-4">
      <template #header>
        <div class="card-header-title text-center">
          <h2 class="mb-0">{{ t('product.updateTitle') }}</h2>
        </div>
      </template>

      <div v-if="loading" class="text-center my-5">
        <el-icon size="40"><i class="el-icon-loading"></i></el-icon>
        <p>{{ t('product.loadingProduct') }}</p>
      </div>

      <el-form
          v-else
          :model="product"
          :rules="rules"
          ref="productForm"
          label-position="top"
          @submit.prevent="openConfirmDialog"
      >
        <el-form-item :label="t('product.category')" prop="categoryIds">
          <multiselect
              v-model="product.categoryIds"
              :options="categoryList"
              :multiple="true"
              :close-on-select="false"
              :placeholder="t('placeholder.selectCategory')"
              label="categoryName"
              track-by="id"
              class="w-100"
              :select-label="t('product.selectLabel')"
              :deselect-label="t('product.deselectLabel')"
              :selected-label="t('product.selectedLabel')"
          />
          <p class="mt-2">
            {{ t('product.selectedCategories') }}:
            <span v-if="product.categoryIds.length === 0">
              {{ t('product.noValue') }}
            </span>
            <span v-else>
              {{ product.categoryIds.map(c => c.categoryName).join(', ') }}
            </span>
          </p>
        </el-form-item>

        <el-form-item :label="t('product.productCode')" prop="productCode">
          <el-input v-model="product.productCode" :placeholder="t('placeholder.enterCode')" clearable disabled />
        </el-form-item>

        <el-form-item :label="t('product.productName')" prop="productName">
          <el-input v-model="product.productName" :placeholder="t('placeholder.enterName')" clearable />
        </el-form-item>

        <el-form-item :label="t('product.description')" prop="description">
          <el-input type="textarea" v-model="product.description" :placeholder="t('placeholder.enterDescription')" :rows="3" />
        </el-form-item>

        <el-form-item :label="t('product.price')" prop="price">
          <el-input type="number" v-model.number="product.price" :placeholder="t('placeholder.enterPrice')" :min="0" />
        </el-form-item>

        <el-form-item :label="t('product.quantity')" prop="quantity">
          <el-input type="number" v-model.number="product.quantity" :placeholder="t('placeholder.enterQuantity')" :min="0" />
        </el-form-item>

        <el-form-item :label="t('product.images')" prop="productImages">
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
            <el-button type="primary">{{ t('product.uploadImage') }}</el-button>
          </el-upload>
          <p v-if="imageError" class="text-danger mt-2">
            {{ t('product.imageRequired') }}
          </p>
        </el-form-item>

        <div class="text-center">
          <el-button type="primary" native-type="submit" :loading="submitLoading">
            {{ t('product.submitUpdate') }}
          </el-button>
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
      <span>{{ t('product.confirmUpdate') }}</span>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">{{ t('action.cancel') }}</el-button>
        <el-button type="primary" @click="handleConfirmUpdate">{{ t('action.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElNotification } from 'element-plus'
import Multiselect from 'vue-multiselect'
import { ArrowLeft } from '@element-plus/icons-vue'
import api from '@/utils/axios.js'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()

const productForm = ref(null)
const loading = ref(true)
const submitLoading = ref(false)
const confirmDialogVisible = ref(false)
const fileList = ref([])
const imageError = ref(false)
const categoryList = ref([])
const deletedImageIds = ref([])

const product = ref({
  id: null,
  productName: '',
  productCode: '',
  description: '',
  price: null,
  quantity: null,
  categoryIds: []
})

const rules = ref({
  productName: [
    { required: true, message: t('validation.productNameRequired'), trigger: 'blur' },
    { max: 200, message: t('validation.productNameMax'), trigger: 'blur' }
  ],
  productCode: [
    { required: true, message: t('validation.productCodeRequired'), trigger: 'blur' }
  ],
  description: [
    { required: true, message: t('validation.descriptionRequired'), trigger: 'blur' },
    { max: 200, message: t('validation.descriptionMax'), trigger: 'blur' }
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
      validator: (_, value, callback) => {
        if (!value || value.length === 0) callback(new Error(t('validation.categoryRequired')))
        else callback()
      },
      trigger: 'change'
    }
  ],
  productImages: [
    {
      validator: (_, __, callback) => {
        if (fileList.value.length === 0) {
          imageError.value = true
          callback(new Error(t('product.imageRequired')))
        } else {
          imageError.value = false
          callback()
        }
      },
      trigger: 'change'
    }
  ]
})

const fetchCategories = async () => {
  const res = await api.get('/categories')
  categoryList.value = res.data || []
}

const fetchProduct = async () => {
  const res = await api.get(`/products/${route.params.id}`)
  const data = res.data
  product.value = {
    id: data.id,
    productName: data.productName,
    productCode: data.productCode,
    description: data.description,
    price: data.price,
    quantity: data.quantity,
    categoryIds: data.categories?.map(c => ({ id: c.id, categoryName: c.categoryName })) || []
  }
  fileList.value = data.productImages?.map(img => ({
    name: img.imageName,
    url: `data:image/png;base64,${img.image}`,
    isOld: true,
    id: img.id
  })) || []
}

const handleFileChange = (file, fileListNew) => {
  const maxSize = 3 * 1024 * 1024
  if (file.raw && file.raw.size > maxSize) {
    ElNotification.error({ title: t('action.error'), message: t('messages.imageTooLarge', { fileName: file.name }) })
    fileList.value = fileListNew.filter(f => f.uid !== file.uid)
    return
  }
  fileList.value = fileListNew.map(f => ({ ...f, url: f.url || (f.raw ? URL.createObjectURL(f.raw) : '') }))
  productForm.value?.validateField('productImages')
}

const handleFileRemove = (file, fileListNew) => {
  if (file.isOld && file.id) deletedImageIds.value.push(file.id)
  fileList.value = fileListNew
  productForm.value?.validateField('productImages')
}

const handlePreview = file => file.url && window.open(file.url, '_blank')

const openConfirmDialog = () => {
  productForm.value.validate(valid => {
    if (valid) confirmDialogVisible.value = true
    else ElNotification.error({ title: t('action.error'), message: t('messages.validateError') })
  })
}

const handleConfirmUpdate = async () => {
  confirmDialogVisible.value = false
  submitLoading.value = true
  try {
    const formData = new FormData()
    Object.entries(product.value).forEach(([key, val]) => {
      if (key !== 'categoryIds') formData.append(key, val)
    })
    product.value.categoryIds.forEach(cat => formData.append('categories', cat.id))
    fileList.value.forEach(f => f.raw && formData.append('productImages', f.raw))
    deletedImageIds.value.forEach(id => formData.append('oldImageIds', id))

    await api.put(`/products/${product.value.id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    ElNotification.success({ title: t('action.success'), message: t('product.updateSuccess') })
    router.push('/product')
  } catch (err) {
    ElNotification.error({
      title: t('action.error'),
      message: err.response?.data?.error || t('product.updateFailed')
    })
  } finally {
    submitLoading.value = false
  }
}

const goBack = () => router.push('/product')

onMounted(async () => {
  try {
    await fetchCategories()
    await fetchProduct()
  } catch (err) {
    ElNotification.error({ title: t('action.error'), message: t('messages.loadProductError') })
  } finally {
    loading.value = false
  }
})
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
.upload-demo :deep(.el-upload--picture-card) {
  width: 148px;
  height: 148px;
  line-height: 148px;
}
.upload-demo :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 148px;
  height: 148px;
}
.text-danger {
  color: #dc3545;
}
</style>
