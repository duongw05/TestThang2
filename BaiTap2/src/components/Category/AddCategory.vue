<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <h2 class="title">Thêm Danh mục sản phẩm</h2>
        <el-button @click="goBack" type="primary" plain>Quay lại</el-button>
      </div>
    </template>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="130px" label-position="left">
      <el-form-item label="Mã danh mục" prop="categoryCode">
        <el-input v-model="form.categoryCode" placeholder="Nhập mã danh mục" />
      </el-form-item>

      <el-form-item label="Tên danh mục" prop="categoryName">
        <el-input v-model="form.categoryName" placeholder="Nhập tên danh mục" />
      </el-form-item>

      <el-form-item label="Mô tả" prop="description">
        <el-input
            v-model="form.description"
            type="textarea"
            rows="3"
            placeholder="Nhập mô tả"
        />
      </el-form-item>

      <el-form-item label="Ảnh danh mục" required>
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
        <div v-if="imageError" class="el-form-item__error">Vui lòng chọn ít nhất 1 ảnh</div>
      </el-form-item>

      <el-form-item>
        <el-button type="success" @click="submitForm">Lưu</el-button>
        <el-button @click="resetForm">Reset</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

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
    { required: true, message: 'Mã danh mục không được để trống', trigger: 'blur' },
    { max: 50, message: 'Mã danh mục tối đa 50 ký tự', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: 'Tên danh mục không được để trống', trigger: 'blur' }
  ],
  description: [
    { required: true, message: 'Mô tả không được để trống', trigger: 'blur' }
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
      await ElMessageBox.confirm('Bạn có chắc chắn muốn thêm danh mục này?', 'Xác nhận', {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        type: 'warning'
      })

      const formData = new FormData()
      formData.append('categoryName', form.value.categoryName)
      formData.append('categoryCode', form.value.categoryCode)
      formData.append('description', form.value.description)

      fileList.value.forEach(file => {
        formData.append('categoryImages', file.raw)
      })

      await axios.post('http://localhost:8080/api/categories', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      ElMessage.success('Thêm danh mục thành công!')
      router.push('/categories')

    } catch (err) {
      if (err !== 'cancel') {
        const errors = err.response?.data
        if (errors?.categoryCode) {
          ElMessage.error(errors.categoryCode)
        } else {
          ElMessage.error('Đã xảy ra lỗi!')
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
