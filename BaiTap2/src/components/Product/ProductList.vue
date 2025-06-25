<template>
  <div class="container-fluid mt-4">
    <div class="mb-3 d-flex justify-content-end">
      <el-button type="primary" :icon="Plus" @click="goToAdd">
        {{ t('product.add') }}
      </el-button>
      <el-button type="success" :icon="Download" @click="exportExcel" class="me-2">
        {{ t('product.exportExcel') }}
      </el-button>
    </div>

    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header-title text-center">
          <h4 class="mb-0">{{ t('product.searchTitle') }}</h4>
        </div>
      </template>
      <el-form :model="filters" label-position="top">
        <el-row :gutter="20" align="middle">
          <el-col :span="5">
            <el-form-item :label="t('product.nameOrCode')">
              <el-input v-model="filters.keyword" :placeholder="t('product.nameOrCode')" clearable />
            </el-form-item>
          </el-col>

          <el-col :span="4">
            <el-form-item :label="t('product.fromDate')">
              <el-date-picker
                  v-model="filters.createdFrom"
                  type="datetime"
                  :placeholder="t('product.fromDate')"
                  format="DD/MM/YYYY HH:mm:ss"
                  value-format="DD/MM/YYYY HH:mm:ss"
                  class="w-100"
                  clearable
                  :disabled-date="disableFutureDates"
              />
            </el-form-item>
          </el-col>

          <el-col :span="4">
            <el-form-item :label="t('product.toDate')">
              <el-date-picker
                  v-model="filters.createdTo"
                  type="datetime"
                  :placeholder="t('product.toDate')"
                  format="DD/MM/YYYY HH:mm:ss"
                  value-format="DD/MM/YYYY HH:mm:ss"
                  class="w-100"
                  clearable
              />
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item :label="t('product.category')">
              <el-select
                  v-model="filters.categoryId"
                  :placeholder="t('product.category')"
                  class="w-100"
                  clearable
              >
                <el-option
                    v-for="category in categoryOptions"
                    :key="category.id"
                    :label="category.categoryName"
                    :value="category.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="5">
            <el-form-item label="&nbsp;">
              <div class="d-flex">
                <el-button type="primary" :icon="Search" @click="handleSearch" class="me-2">
                  {{ t('action.search') }}
                </el-button>
                <el-button :icon="Refresh" @click="resetFilters">
                  {{ t('action.reset') }}
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <el-table
          :data="products"
          v-loading="loading"
          stripe
          border
          style="width: 100%"
          :empty-text="t('product.noData')"
      >
        <el-table-column type="index" label="#" width="60" :index="getIndex" />
        <el-table-column prop="productCode" :label="t('product.productCode')" width="140" />
        <el-table-column :label="t('product.category')" min-width="180">
          <template #default="{ row }">{{ row.categoryNames?.join(', ') }}</template>
        </el-table-column>
        <el-table-column prop="productName" :label="t('product.productName')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" :label="t('product.price')" width="120" :formatter="formatCurrency" />
        <el-table-column prop="quantity" :label="t('product.quantity')" width="100" />
        <el-table-column prop="createdDate" :label="t('product.createDate')" width="160" :formatter="formatDate" />
        <el-table-column prop="createdBy" :label="t('product.createdBy')" width="120" />
        <el-table-column prop="modifiedDate" :label="t('product.updateDate')" width="160" :formatter="formatDate" />
        <el-table-column prop="modifiedBy" :label="t('product.modifiedBy')" width="120" />
        <el-table-column :label="t('product.actions')" width="150" fixed="right">
          <template #default="scope">
            <el-button type="info" :icon="View" size="small" circle @click="goToDetail(scope.row.id)" :title="t('product.detail')" />
            <el-button type="primary" :icon="Edit" size="small" circle @click="goToEdit(scope.row.id)" :title="t('product.edit')" />
            <el-button type="danger" :icon="Delete" size="small" circle @click="confirmDelete(scope.row.id)" :title="t('product.delete')" :loading="deleteLoading" />
          </template>
        </el-table-column>
      </el-table>

      <div class="d-flex justify-content-end mt-4">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 20, 50]"
            :page-size="size"
            layout="sizes, total, prev, pager, next, jumper"
            :total="totalElements"
            background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import axios from 'axios';
import api from '@/utils/axios.js';
import {
  ElMessage,
  ElMessageBox
} from 'element-plus';
import {
  Plus, Search, Refresh, Edit, Delete, View, Download
} from '@element-plus/icons-vue';

const { t } = useI18n();
const router = useRouter();

const currentPage = ref(1);
const size = ref(10);
const totalElements = ref(0);
const products = ref([]);
const categoryOptions = ref([]);
const loading = ref(false);
const deleteLoading = ref(false);

const filters = ref({
  keyword: '',
  createdFrom: null,
  createdTo: null,
  categoryId: null
});

const fetchCategories = async () => {
  try {
    const res = await api.get('/categories');
    categoryOptions.value = res.data || [];
  } catch (err) {
    ElMessage.error(t('messages.loadFailed'));
  }
};

const fetchProducts = async () => {
  loading.value = true;
  const payload = {
    page: currentPage.value - 1,
    size: size.value,
    keyword: filters.value.keyword || null,
    createdFrom: filters.value.createdFrom || null,
    createdTo: filters.value.createdTo || null,
    categoryIds: filters.value.categoryId
  };

  try {
    const res = await api.get('/products/search', { params: payload });
    products.value = res.data?.data || [];
    totalElements.value = res.data?.pagination?.totalElements ?? 0;

    if (products.value.length === 0 && totalElements.value > 0 && currentPage.value > 1) {
      currentPage.value = Math.max(1, Math.ceil(totalElements.value / size.value));
      await fetchProducts();
    }
  } catch (err) {
    ElMessage.error(t('product.noData'));
    products.value = [];
    totalElements.value = 0;
    currentPage.value = 1;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchProducts();
};

const resetFilters = () => {
  filters.value = {
    keyword: '',
    createdFrom: null,
    createdTo: null,
    categoryId: null
  };
  currentPage.value = 1;
  fetchProducts();
};

const handleSizeChange = (val) => {
  size.value = val;
  currentPage.value = 1;
  fetchProducts();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchProducts();
};

const getIndex = (index) => index + 1 + (currentPage.value - 1) * size.value;

const formatDate = (row, column, value) => {
  return value ? new Date(value).toLocaleString('vi-VN') : '';
};

const formatCurrency = (row, column, value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const goToAdd = () => {
  router.push({ name: 'AddProduct' });
};

const goToEdit = (id) => {
  router.push({ name: 'UpdateProduct', params: { id } });
};

const goToDetail = (id) => {
  router.push({ name: 'DetailProduct', params: { id } });
};

const confirmDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
        t('product.confirmDelete'),
        t('action.warning'),
        {
          confirmButtonText: t('product.delete'),
          cancelButtonText: t('action.cancel'),
          type: 'warning'
        }
    );
    deleteLoading.value = true;
    await api.delete(`/products/${id}`);
    ElMessage.success(t('product.deleteSuccess'));
    fetchProducts();
  } catch (err) {
    if (error !== 'cancel') {
      ElMessage.error(t('product.deleteCancel'));
    }else{
      ElMessage.error(t('product.deleteError'));
    }

  } finally {
    deleteLoading.value = false;
  }
};

const exportExcel = async () => {
  try {
    await ElMessageBox.confirm(
        t('product.exportConfirm'),
        t('product.exportTitle'),
        {
          confirmButtonText: t('product.exportExcel'),
          cancelButtonText: t('action.cancel'),
          type: 'warning'
        }
    );

    const payload = {
      keyword: filters.value.keyword || null,
      createdFrom: filters.value.createdFrom || null,
      createdTo: filters.value.createdTo || null,
      categoryIds: filters.value.categoryId,
    };

    const res = await api.post('/products/export', payload, {
      responseType: 'blob'
    });

    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'danh_sach_san_pham.xlsx');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    ElMessage.success(t('product.exportSuccess'));
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('product.exportError'));
    }
  }
};

onMounted(() => {
  fetchCategories();
  fetchProducts();
});
</script>

<style scoped>
</style>