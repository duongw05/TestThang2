<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <h2 class="title">{{ $t('category.add') }}</h2>
        <el-button @click="goBack" type="primary" plain>{{ $t('action.back') }}</el-button>
      </div>
    </template>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="130px" label-position="left">
      <el-form-item :label="$t('category.code')" prop="categoryCode">
        <el-input v-model="form.categoryCode" :placeholder="$t('placeholder.enterCode')" />
      </el-form-item>

      <el-form-item :label="$t('category.name')" prop="categoryName">
        <el-input v-model="form.categoryName" :placeholder="$t('placeholder.enterName')" />
      </el-form-item>

      <el-form-item :label="$t('category.description')" prop="description">
        <el-input
            v-model="form.description"
            type="textarea"
            rows="3"
            :placeholder="$t('placeholder.enterDescription')"
        />
      </el-form-item>

      <el-form-item :label="$t('category.images')" required>
        <el-upload
            action="#"
            list-type="picture-card"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :on-preview="handlePreview"
            :auto-upload="false"
            multiple
            accept="image/*"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div v-if="imageError" class="el-form-item__error">{{ $t('category.noImage') }}</div>
      </el-form-item>

      <el-form-item>
        <el-button type="success" @click="submitForm">{{ $t('category.submit') }}</el-button>
        <el-button @click="resetForm">{{ $t('action.reset') }}</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import api from '@/utils/axios.js'

const { t } = useI18n()
const router = useRouter()

const formRef = ref()
const form = ref({
  categoryName: '',
  categoryCode: '',
  description: ''
})
const fileList = ref([])
const imageError = ref(false)

const rules = {
  categoryCode: [
    { required: true, message: t('messages.enterCode'), trigger: 'blur' },
    { max: 50, message: t('messages.codeMaxLength'), trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: t('messages.enterName'), trigger: 'blur' }
  ],
  description: [
    { required: true, message: t('messages.enterDescription'), trigger: 'blur' }
  ]
}

const handleFileChange = (file, fileListNew) => {
  fileList.value = fileListNew
  imageError.value = fileList.value.length === 0
}

const handleFileRemove = (file, fileListNew) => {
  fileList.value = fileListNew
  imageError.value = fileList.value.length === 0
}

const handlePreview = (file) => {
  window.open(file.url || URL.createObjectURL(file.raw), '_blank')
}

const resetForm = () => {
  form.value = {
    categoryName: '',
    categoryCode: '',
    description: ''
  }
  fileList.value = []
  imageError.value = false
}

const submitForm = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    if (fileList.value.length === 0) {
      imageError.value = true
      return
    }

    try {
      await ElMessageBox.confirm(t('messages.confirmAdd'), t('messages.confirmTitle'), {
        confirmButtonText: t('action.confirm'),
        cancelButtonText: t('action.cancel'),
        type: 'warning'
      })

      const formData = new FormData()
      formData.append('categoryName', form.value.categoryName)
      formData.append('categoryCode', form.value.categoryCode)
      formData.append('description', form.value.description)

      fileList.value.forEach(file => {
        formData.append('categoryImages', file.raw)
      })

      await api.post('/categories', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      ElMessage.success(t('messages.addSuccess'))
      router.push('/categories')

    } catch (err) {
      if (err !== 'cancel') {
        const errors = err.response?.data
        if (errors?.categoryCode) {
          ElMessage.error(errors.categoryCode)
        } else {
          ElMessage.error(t('messages.generalError'))
        }
      }
    }
  })
}

const goBack = () => router.push('/categories')
</script>

<style scoped>
.box-card {
  max-width: 850px;
  margin: 30px auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 24px;
  font-weight: bold;
}
</style>
