<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <h2 class="title">{{ currentTitle }}</h2>
        <el-button @click="goBack" type="primary" plain>{{ $t('action.back') }}</el-button>
      </div>
    </template>

    <el-empty v-if="loading && (mode === 'view' || mode === 'edit')" :description="$t('loading')" />
    <el-empty v-else-if="error && (mode === 'view' || mode === 'edit')" :description="$t('error.notFound')" />

    <template v-else-if="mode === 'view' && category">
      <el-descriptions :title="$t('category.detailTitle')" :column="2" border>
        <el-descriptions-item :label="$t('category.code')">{{ category.categoryCode }}</el-descriptions-item>
        <el-descriptions-item :label="$t('category.name')">{{ category.categoryName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('category.status')">
          {{ category.status === 'ACTIVE' ? $t('status.active') : $t('status.inactive') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.createdBy')">
          {{ category.createdBy || $t('category.unknown') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.createdDate')">
          {{ formatDate(category.createdDate) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.modifiedBy')">
          {{ category.modifiedBy || $t('category.unknown') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.modifiedDate')">
          {{ formatDate(category.modifiedDate) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('category.description')">
          {{ category.description || $t('category.noDescription') }}
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="category.categoryImages?.length" style="margin-top: 30px">
        <div class="image-gallery-header">
          <h3>{{ $t('category.images') }}</h3>
          <span class="image-count">
            {{ $t('category.images') }} {{ currentImageIndex + 1 }} / {{ category.categoryImages.length }}
          </span>
        </div>
        <el-carousel height="200px" trigger="click" arrow="always" @change="handleCarouselChange">
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

    <el-form
        v-else
        :model="form"
        :rules="currentRules"
        ref="formRef"
        label-width="130px"
        label-position="left"
    >
      <el-form-item :label="$t('category.code')" prop="categoryCode">
        <el-input v-model="form.categoryCode" :placeholder="$t('placeholder.enterCode')" :disabled="mode === 'edit'" />
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
        <el-button v-if="mode === 'add'" type="success" @click="submitForm">{{ $t('category.submit') }}</el-button>
        <el-button v-if="mode === 'edit'" type="success" @click="submitForm">{{ $t('category.update') }}</el-button>
        <el-button v-if="mode === 'add'" @click="resetForm">{{ $t('action.reset') }}</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import api from '@/utils/axios.js'

const props = defineProps({
  mode: {
    type: String,
    required: true,
    validator: (value) => ['add', 'view', 'edit'].includes(value)
  }
})

const { t } = useI18n()
const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const form = ref({
  categoryName: '',
  categoryCode: '',
  description: ''
})
const fileList = ref([])
const imageError = ref(false)
const deletedImageIds = ref([])
const category = ref(null)
const loading = ref(false)
const error = ref(false)
const currentImageIndex = ref(0)

const currentTitle = computed(() => {
  switch (props.mode) {
    case 'add':
      return t('category.add');
    case 'view':
      return `${t('category.detailTitle')}: ${category.value?.categoryName || t('loading')}`;
    case 'edit':
      return t('category.updateTitle');
    default:
      return '';
  }
});

const currentRules = computed(() => {
  const commonRules = {
    categoryName: [{ required: true, message: t('messages.enterName'), trigger: 'blur' }],
    description: [{ required: true, message: t('messages.enterDescription'), trigger: 'blur' }]
  };

  if (props.mode === 'add') {
    return {
      ...commonRules,
      categoryCode: [
        { required: true, message: t('messages.enterCode'), trigger: 'blur' },
        { max: 50, message: t('messages.codeMaxLength'), trigger: 'blur' }
      ]
    };
  }
  return commonRules;
});

const formatDate = (dateString) => {
  if (!dateString) return t('category.unknown');
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN');
};

const goBack = () => {
  router.push('/categories');
};

const handleFileChange = (file, fileListNew) => {
  fileList.value = fileListNew.map(item => ({
    ...item,
    raw: item.raw || null,
    isOld: !!item.isOld,
    id: item.id || null
  }));
  imageError.value = fileList.value.filter(f => !f.isRemoved).length === 0;
};

const handleFileRemove = (file, fileListNew) => {
  if (file.isOld && file.id) {
    deletedImageIds.value.push(file.id);
  }
  fileList.value = fileListNew;
  imageError.value = fileList.value.filter(f => !f.isRemoved).length === 0;
};

const handlePreview = (file) => {
  window.open(file.url || URL.createObjectURL(file.raw), '_blank');
};

const handleCarouselChange = (newIndex) => {
  currentImageIndex.value = newIndex;
};

const resetForm = () => {
  form.value = {
    categoryName: '',
    categoryCode: '',
    description: ''
  };
  fileList.value = [];
  imageError.value = false;
  deletedImageIds.value = [];
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

const submitForm = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    const currentImages = fileList.value.filter(f => !f.isRemoved);
    if (currentImages.length === 0) {
      imageError.value = true;
      ElNotification.warning({
        title: t('action.warning'),
        message: t('messages.imageRequired')
      });
      return;
    }

    try {
      const confirmMessage = props.mode === 'add' ? t('messages.confirmAdd') : t('messages.confirmUpdate');
      const successMessage = props.mode === 'add' ? t('messages.addSuccess') : t('messages.updateSuccess');
      const errorMessage = props.mode === 'add' ? t('messages.generalError') : t('messages.updateFailed');

      await ElMessageBox.confirm(confirmMessage, t('messages.confirmTitle'), {
        confirmButtonText: t('action.confirm'),
        cancelButtonText: t('action.cancel'),
        type: 'warning'
      });

      const formData = new FormData();
      formData.append('categoryName', form.value.categoryName);
      formData.append('categoryCode', form.value.categoryCode);
      formData.append('description', form.value.description);

      currentImages.forEach(file => {
        if (!file.isOld && file.raw) {
          formData.append('categoryImages', file.raw);
        }
      });

      deletedImageIds.value.forEach((id, index) => {
        formData.append(`oldImageIds[${index}]`, id);
      });

      if (props.mode === 'add') {
        await api.post('/categories', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
      } else if (props.mode === 'edit') {
        const id = route.params.id;
        await api.put(`/categories/${id}`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
      }

      ElNotification.success({
        title: t('action.success'),
        message: successMessage
      });
      router.push('/categories');

    } catch (err) {
      if (err !== 'cancel') {
        const errors = err.response?.data;
        if (props.mode === 'add' && errors?.categoryCode) {
          ElMessage.error(errors.categoryCode);
        } else {
          ElMessage.error(errors?.message || errorMessage);
        }
      }
    }
  });
};

const fetchCategoryData = async (id) => {
  loading.value = true;
  error.value = false;
  try {
    const res = await api.get(`/categories/${id}`);
    const data = res.data;

    if (props.mode === 'edit') {
      form.value = {
        categoryName: data.categoryName,
        categoryCode: data.categoryCode,
        description: data.description
      };
      fileList.value = (data.categoryImages || []).map((img, i) => ({
        name: `image-${i}.png`,
        url: `data:image/png;base64,${img.image}`,
        isOld: true,
        id: img.id,
        raw: null
      }));
    }
    else if (props.mode === 'view') {
      category.value = data;
    }
  } catch (e) {
    error.value = true;
    ElNotification.error({
      title: t('action.error'),
      message: t('messages.loadFailed')
    });
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  if (props.mode === 'view' || props.mode === 'edit') {
    const id = route.params.id;
    if (id) {
      fetchCategoryData(id);
    } else {
      error.value = true;
    }
  }
});

watch(() => route.params.id, (newId, oldId) => {
  if (props.mode === 'view' || props.mode === 'edit') {
    if (newId && newId !== oldId) {
      fetchCategoryData(newId);
    } else if (!newId) {
      error.value = true;
    }
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
.title {
  font-size: 24px;
  font-weight: bold;
}
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