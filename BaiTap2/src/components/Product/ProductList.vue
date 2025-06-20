<template>
  <div class="container-fluid mt-4">
    <div class="mb-3 d-flex justify-content-end">
      <el-button type="primary" :icon="Plus" @click="goToAdd">
        Thêm sản phẩm
      </el-button>
    </div>

    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header-title text-center">
          <h4 class="mb-0">Tìm kiếm sản phẩm</h4>
        </div>
      </template>
      <el-form :model="filters" label-position="top">
        <el-row :gutter="20" align="middle">
          <el-col :span="5">
            <el-form-item label="Tên hoặc Mã sản phẩm">
              <el-input v-model="filters.keyword" placeholder="Nhập tên hoặc mã sản phẩm" clearable />
            </el-form-item>
          </el-col>

          <el-col :span="4">
            <el-form-item label="Từ ngày">
              <el-date-picker
                  v-model="filters.createdFrom"
                  type="datetime"
                  placeholder="Chọn ngày bắt đầu"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  class="w-100"
                  clearable
              />
            </el-form-item>
          </el-col>

          <el-col :span="4">
            <el-form-item label="Đến ngày">
              <el-date-picker
                  v-model="filters.createdTo"
                  type="datetime"
                  placeholder="Chọn ngày kết thúc"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  class="w-100"
                  clearable
              />
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="Danh mục">
              <el-select
                  v-model="filters.categoryId"
                  placeholder="Chọn danh mục"
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

          <el-col :span="5" class="d-flex align-items-end">
            <el-form-item label=" ">
              <el-button type="primary" :icon="Search" @click="handleSearch" class="me-2">Tìm kiếm</el-button>
              <el-button :icon="Refresh" @click="resetFilters">Đặt lại</el-button>
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
          empty-text="Không có dữ liệu"
      >
        <el-table-column type="index" label="#" width="60" :index="getIndex" />
        <el-table-column prop="productCode" label="Mã" width="140" />
        <el-table-column label="Danh mục" min-width="180">
          <template #default="{ row }">{{ row.categoryNames?.join(', ') }}</template>
        </el-table-column>
        <el-table-column prop="productName" label="Tên sản phẩm" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" label="Giá" width="120" :formatter="formatCurrency" />
        <el-table-column prop="quantity" label="SL" width="80" />
        <el-table-column prop="createdDate" label="Ngày tạo" width="160" :formatter="formatDate" />
        <el-table-column prop="createdBy" label="Người tạo" width="120" />
        <el-table-column prop="modifiedDate" label="Ngày sửa" width="160" :formatter="formatDate" />
        <el-table-column prop="modifiedBy" label="Người sửa" width="120" />
        <el-table-column label="Hành động" width="150" fixed="right">
          <template #default="scope">
            <el-button type="info" :icon="View" size="small" circle @click="goToDetail(scope.row.id)" title="Chi tiết"/>
            <el-button type="primary" :icon="Edit" size="small" circle @click="goToEdit(scope.row.id)" title="Sửa"/>
            <el-button type="danger" :icon="Delete" size="small" circle @click="confirmDelete(scope.row.id)" title="Xóa"/>
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
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalElements"
            background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import qs from 'qs';
import { ElMessage, ElMessageBox, ElCard, ElForm, ElFormItem, ElInput, ElButton, ElDatePicker, ElRow, ElCol, ElTable, ElTableColumn, ElPagination, ElSelect, ElOption } from 'element-plus';
import {Plus, Search, Refresh, Edit, Delete, View} from '@element-plus/icons-vue';
import { useRouter } from "vue-router";

const router = useRouter();

const currentPage = ref(1);
const size = ref(10);
const totalElements = ref(0);
const products = ref([]);
const categoryOptions = ref([]);
const loading = ref(false);

const filters = ref({
  keyword: '',
  createdFrom: null,
  createdTo: null,
  categoryId: null
});

const fetchCategories = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/categories');
    categoryOptions.value = res.data || [];
  } catch (err) {
    console.error('Lỗi lấy danh mục:', err);
    ElMessage.error('Không thể tải danh mục.');
  }
};

const fetchProducts = async () => {
  loading.value = true;

  const payload = {
    page: currentPage.value - 1,
    size: size.value,
  };

  if (filters.value.keyword) {
    payload.keyword = filters.value.keyword;
  }
  if (filters.value.createdFrom) {
    payload.createdFrom = filters.value.createdFrom;
  }
  if (filters.value.createdTo) {
    payload.createdTo = filters.value.createdTo;
  }
  if (filters.value.categoryId) {
    payload.categoryIds = filters.value.categoryId; // Gửi categoryId dưới dạng mảng với một phần tử
  }

  console.log('Payload gửi lên:', payload);

  try {
    const res = await axios.get('http://localhost:8080/api/products/search', {
      params: payload
    });

    products.value = res.data?.data || [];
    totalElements.value = res.data?.pagination?.totalElements ?? 0;
    if (products.value.length === 0 && totalElements.value > 0 && currentPage.value > 1) {
      currentPage.value = Math.max(1, Math.ceil(totalElements.value / size.value));
      await fetchProducts();
    }

  } catch (err) {
    console.error('Lỗi tải danh sách sản phẩm:', err);
    ElMessage.error('Không thể tải dữ liệu sản phẩm. Vui lòng thử lại.');
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
  filters.value = { keyword: '', createdFrom: null, createdTo: null, categoryId: null };
  currentPage.value = 1;
  fetchProducts();
};

const handleSizeChange = (newSize) => {
  size.value = newSize;
  currentPage.value = 1;
  fetchProducts();
};

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage;
  fetchProducts();
};

const getIndex = (index) => {
  return index + 1 + (currentPage.value - 1) * size.value;
};

const formatDate = (row, column, cellValue) => {
  return cellValue ? new Date(cellValue).toLocaleString('vi-VN') : '';
};

const formatCurrency = (row, column, cellValue) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(cellValue);
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
    await ElMessageBox.confirm('Bạn có chắc chắn muốn xoá sản phẩm này?', 'Cảnh báo', {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning'
    });
    await axios.delete(`http://localhost:8080/api/products/${id}`);
    ElMessage.success('Xóa sản phẩm thành công!');
    fetchProducts();
  } catch (err) {
    if (axios.isCancel(err) || err === 'cancel') {
      console.log('Xóa sản phẩm đã bị hủy.');
    } else {
      console.error('Lỗi xoá sản phẩm:', err);
      ElMessage.error('Không thể xóa sản phẩm. Vui lòng thử lại.');
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