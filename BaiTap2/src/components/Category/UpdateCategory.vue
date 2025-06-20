<template>
  <div class="container mt-5" v-loading="isPageLoading">
    <div class="mb-3">
      <button @click="goBack" class="btn btn-outline-secondary rounded-pill shadow-sm">
        <i class="bi bi-arrow-left-circle"></i> Quay lại
      </button>
    </div>

    <el-form ref="ruleForm" :model="form" :rules="rules" label-width="130px" class="bg-light p-4 rounded shadow-lg" @submit.prevent>
      <h2 class="text-center mb-4">Cập nhật Danh Mục</h2>

      <el-form-item label="Tên danh mục" prop="categoryName">
        <el-input v-model="form.categoryName" placeholder="Nhập tên danh mục" />
      </el-form-item>

      <el-form-item label="Mã danh mục" prop="categoryCode">
        <el-input v-model="form.categoryCode" placeholder="Nhập mã danh mục" />
      </el-form-item>

      <el-form-item label="Mô tả" prop="description">
        <el-input type="textarea" rows="3" v-model="form.description" placeholder="Nhập mô tả" />
      </el-form-item>

      <el-form-item label="Hình ảnh danh mục">
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
          <el-button type="primary">Tải lên hình ảnh</el-button>
        </el-upload>
      </el-form-item>

      <div class="text-center mt-4">
        <el-button type="success" @click="handleSubmit">Cập nhật danh mục</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElNotification, ElMessageBox } from 'element-plus'
import 'element-plus/es/components/loading/style/css'

const router = useRouter()
const route = useRoute()

const form = ref({
  categoryName: '',
  categoryCode: '',
  description: ''
})

const rules = {
  categoryName: [{ required: true, message: 'Vui lòng nhập tên danh mục', trigger: 'blur' }],
  categoryCode: [{ required: true, message: 'Vui lòng nhập mã danh mục', trigger: 'blur' }],
  description: [{ required: true, message: 'Vui lòng nhập mô tả', trigger: 'blur' }]
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
        title: 'Cảnh báo',
        message: 'Vui lòng chọn ít nhất 1 ảnh danh mục.'
      })
      return
    }

    try {
      await ElMessageBox.confirm('Bạn có chắc chắn muốn cập nhật danh mục này?', 'Xác nhận', {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
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
      await axios.put(`http://localhost:8080/api/categories/${id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      ElNotification.success({
        title: 'Thành công',
        message: 'Cập nhật danh mục thành công!'
      })

      router.push('/categories')
    } catch (error) {
      if (error !== 'cancel') {
        console.error(error)
        ElNotification.error({
          title: 'Lỗi',
          message: error.response?.data?.message || 'Cập nhật thất bại!'
        })
      }
    }
  })
}

const loadCategory = async () => {
  try {
    const id = route.params.id
    const res = await axios.get(`http://localhost:8080/api/categories/${id}`)
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
    console.error(err)
    ElNotification.error({
      title: 'Lỗi',
      message: 'Không thể tải danh mục.'
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
