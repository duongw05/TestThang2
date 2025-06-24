<template>
  <div class="container mt-5" v-loading="isPageLoading">
    <div class="mb-3">
      <button @click="goBack" class="btn btn-outline-secondary rounded-pill shadow-sm">
        <i class="bi bi-arrow-left-circle"></i> {{ $t('action.back') }}
      </button>
    </div>

    <el-form
        ref="ruleForm"
        :model="form"
        :rules="rules"
        label-width="130px"
        class="bg-light p-4 rounded shadow-lg"
        @submit.prevent
    >
      <h2 class="text-center mb-4">{{ $t('category.updateTitle') }}</h2>

      <el-form-item :label="$t('category.name')" prop="categoryName">
        <el-input v-model="form.categoryName" :placeholder="$t('messages.enterName')" />
      </el-form-item>

      <el-form-item :label="$t('category.code')" prop="categoryCode">
        <el-input v-model="form.categoryCode" :placeholder="$t('messages.enterCode')" disabled />
      </el-form-item>

      <el-form-item :label="$t('category.description')" prop="description">
        <el-input
            type="textarea"
            rows="3"
            v-model="form.description"
            :placeholder="$t('messages.enterDescription')"
        />
      </el-form-item>

      <el-form-item :label="$t('category.images')">
        <el-upload
            action="#"
            list-type="picture-card"
            :file-list="imageFiles"
            :on-preview="handlePreview"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :auto-upload="false"
            multiple
            accept="image/*"
        >
          <el-button type="primary">{{ $t('category.selectImages') }}</el-button>
        </el-upload>
      </el-form-item>

      <div class="text-center mt-4">
        <el-button type="success" @click="handleSubmit">{{ $t('category.submit') }}</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElNotification, ElMessageBox } from 'element-plus'
import api from '@/utils/axios.js'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()

const form = ref({
  categoryName: '',
  categoryCode: '',
  description: ''
})

const rules = {
  categoryName: [{ required: true, message: t('messages.enterName'), trigger: 'blur' }],
  categoryCode: [{ required: true, message: t('messages.enterCode'), trigger: 'blur' }],
  description: [{ required: true, message: t('messages.enterDescription'), trigger: 'blur' }]
}

const ruleForm = ref(null)
const imageFiles = ref([])
const deletedImageIds = ref([])
const isPageLoading = ref(true)

const goBack = () => router.push('/categories')

const handlePreview = (file) => {
  window.open(file.url, '_blank')
}

const handleFileChange = (file, fileList) => {
  imageFiles.value = fileList.map((item, index) => {
    const rawFile = item.raw || item.file || null
    const previewUrl = item.url || (rawFile ? URL.createObjectURL(rawFile) : '')
    return {
      name: item.name || `image-${index}`,
      url: previewUrl,
      file: rawFile,
      isOld: !!item.isOld,
      id: item.id || null
    }
  })
}

const handleFileRemove = (file, fileList) => {
  if (file.isOld && file.id) {
    deletedImageIds.value.push(file.id)
  }
  imageFiles.value = fileList.map((item, index) => {
    const rawFile = item.raw || item.file || null
    return {
      name: item.name || `image-${index}`,
      url: item.url || (rawFile ? URL.createObjectURL(rawFile) : ''),
      file: rawFile,
      isOld: !!item.isOld,
      id: item.id || null
    }
  })
}

const handleSubmit = () => {
  ruleForm.value.validate(async (valid) => {
    if (!valid) return

    const remainingImages = imageFiles.value.filter(img => !deletedImageIds.value.includes(img.id))
    if (remainingImages.length === 0) {
      ElNotification.warning({
        title: t('action.warning'),
        message: t('messages.imageRequired')
      })
      return
    }

    try {
      await ElMessageBox.confirm(t('messages.confirmUpdate'), t('action.confirm'), {
        confirmButtonText: t('action.confirm'),
        cancelButtonText: t('action.cancel'),
        type: 'warning'
      })

      const formData = new FormData()
      formData.append('categoryName', form.value.categoryName)
      formData.append('categoryCode', form.value.categoryCode)
      formData.append('description', form.value.description)

      imageFiles.value.forEach((img) => {
        if (!img.isOld && img.file) {
          formData.append('categoryImages', img.file)
        }
      })

      deletedImageIds.value.forEach((id, index) => {
        formData.append(`oldImageIds[${index}]`, id)
      })

      const id = route.params.id
      await api.put(`/categories/${id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      ElNotification.success({
        title: t('action.success'),
        message: t('messages.updateSuccess')
      })

      router.push('/categories')
    } catch (error) {
      if (error !== 'cancel') {
        ElNotification.error({
          title: t('action.error'),
          message: error.response?.data?.message || t('messages.updateFailed')
        })
      }
    }
  })
}

const loadCategory = async () => {
  try {
    const id = route.params.id
    const res = await api.get(`/categories/${id}`)
    const data = res.data

    form.value = {
      categoryName: data.categoryName,
      categoryCode: data.categoryCode,
      description: data.description
    }

    imageFiles.value = (data.categoryImages || []).map((img, i) => ({
      name: `image-${i}`,
      url: `data:image/png;base64,${img.image}`,
      isOld: true,
      id: img.id,
      file: null
    }))
  } catch (err) {
    ElNotification.error({
      title: t('action.error'),
      message: t('messages.loadFailed')
    })
  } finally {
    isPageLoading.value = false
  }
}

onMounted(() => {
  loadCategory()
})
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: auto;
}
</style>
