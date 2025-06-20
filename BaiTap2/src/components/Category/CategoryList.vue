<template>
  <div class="category-list-container">
    <el-card class="box-card">
      <div class="card-header">
        <h3 class="title">Danh sách Danh mục Sản phẩm</h3>
        <div>
          <el-button type="success" :icon="Plus" @click="goToAddCategory" size="default">
            Thêm Danh mục
          </el-button>
          <el-button type="warning" :icon="Download" @click="exportToExcel" size="default">
            Xuất Excel
          </el-button>
        </div>
      </div>

      <div class="search-section">
        <el-input
            v-model="searchParams.keyword"
            placeholder="Tìm kiếm theo Tên hoặc Mã danh mục"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            class="search-input"
        />
        <el-date-picker
            v-model="searchParams.createdFrom"
            type="datetime"
            placeholder="Ngày tạo từ"
            format="DD/MM/YYYY HH:mm:ss"
            value-format="DD/MM/YYYY HH:mm:ss"
            clearable
            class="date-picker"
        />
        <el-date-picker
            v-model="searchParams.createdTo"
            type="datetime"
            placeholder="Ngày tạo đến"
            format="DD/MM/YYYY HH:mm:ss"
            value-format="DD/MM/YYYY HH:mm:ss"
            clearable
            class="date-picker"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">Tìm kiếm</el-button>
        <el-button :icon="Refresh" @click="resetSearch">Reset</el-button>
      </div>

      <el-table
          :data="categories"
          border
          stripe
          v-loading="loading"
          element-loading-text="Đang tải dữ liệu..."
          empty-text="Không có dữ liệu danh mục nào."
          class="category-table"
      >
        <el-table-column type="index" label="#" width="60" :index="indexMethod" />
        <el-table-column prop="categoryCode" label="Mã danh mục" width="150" sortable />
        <el-table-column prop="categoryName" label="Tên danh mục" sortable />
        <el-table-column prop="description" label="Mô tả" />
        <el-table-column prop="createdDate" label="Ngày tạo" width="180" />
        <el-table-column prop="createdBy" label="Người tạo" width="120" />
        <el-table-column prop="modifiedDate" label="Ngày sửa" width="180" />
        <el-table-column prop="modifiedBy" label="Người sửa" width="120" />
        <el-table-column label="Hành động" width="150" fixed="right">
          <template #default="scope">
            <el-button type="info" :icon="View" size="small" circle @click="goToDetailCategory(scope.row.id)" title="Chi tiết"/>
            <el-button type="primary" :icon="Edit" size="small" circle @click="goToEditCategory(scope.row.id)" title="Sửa"/>
            <el-button type="danger" :icon="Delete" size="small" circle @click="confirmDeleteCategory(scope.row.id)" title="Xóa"/>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            v-model:current-page="currentPage"
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
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Search, Refresh,View,Download  } from '@element-plus/icons-vue';

const router = useRouter();

const categories = ref([]);
const currentPage = ref(1);
const size = ref(10);
const totalElements = ref(0);
const loading = ref(false);

const searchParams = reactive({
  keyword: '',
  createdFrom: null,
  createdTo: null
});

const fetchCategories = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value - 1,
      size: size.value
    };

    if (searchParams.keyword.trim()) {
      params.keyword = searchParams.keyword.trim();
    }

    if (searchParams.createdFrom) {
      params.createdFrom = searchParams.createdFrom;
    }

    if (searchParams.createdTo) {
      params.createdTo = searchParams.createdTo;
    }

    const res = await axios.get('http://localhost:8080/api/categories/search', { params });

    categories.value = res.data?.data || [];
    totalElements.value = res.data?.pagination?.totalElements ?? 0;
    currentPage.value = res.data?.pagination?.currentPage ?? 1;
    size.value = res.data?.pagination?.pageSize ?? 10;

    if (categories.value.length === 0 && totalElements.value > 0 && currentPage.value > 1) {
      currentPage.value = Math.max(1, res.data.pagination.totalPages);
      await fetchCategories();
    }

  } catch (err) {
    console.error('Lỗi:', err);
    ElMessage.error('Không thể tải dữ liệu danh mục.');
    categories.value = [];
    totalElements.value = 0;
    currentPage.value = 1;
  } finally {
    loading.value = false;
  }
};

const handleSizeChange = (val) => {
  size.value = val;
  currentPage.value = 1;
  fetchCategories();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchCategories();
};

const indexMethod = (index) => (currentPage.value - 1) * size.value + index + 1;

const goToAddCategory = () => {
  router.push({ name: 'AddCategory' });
};

const goToDetailCategory = (id) => {
  router.push({ name: 'DetailCategory', params: { id } });
};

const goToEditCategory = (id) => {
  router.push({ name: 'UpdateCategory', params: { id } });
};

const confirmDeleteCategory = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc chắn muốn xóa danh mục này?', 'Cảnh báo', {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning'
    });
    await axios.delete(`http://localhost:8080/api/categories/${id}`);
    ElMessage.success('Xóa danh mục thành công!');
    fetchCategories();
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('Không thể xóa danh mục.');
    }
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchCategories();
};

const resetSearch = () => {
  searchParams.keyword = '';
  searchParams.createdFrom = null;
  searchParams.createdTo = null;
  currentPage.value = 1;
  fetchCategories();
};

const exportToExcel = async () => {
  try {
    const dto = {
      keyword: searchParams.keyword.trim() || null,
      createdFrom: searchParams.createdFrom || null,
      createdTo: searchParams.createdTo || null
    };

    const res = await axios.post('http://localhost:8080/api/categories/export', dto, {
      responseType: 'blob'
    });

    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'categories.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error(error);
    ElMessage.error('Xuất Excel thất bại!');
  }
};


onMounted(() => {
  fetchCategories();
});
</script>

<style scoped>
.category-list-container {
  max-width: 1300px;
  margin: 40px auto;
  padding: 20px;
}
.box-card {
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 18px;
  border-bottom: 1px solid #ebeef5;
}
.title {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: bold;
}
.search-section {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}
.search-input {
  max-width: 300px;
}
.date-picker {
  max-width: 220px;
}
.category-table {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}
.el-table__header-wrapper th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
}
.el-table__cell {
  padding: 10px 0;
}
.pagination-container {
  margin-top: 25px;
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
}
@media (max-width: 768px) {
  .category-list-container {
    padding: 10px;
    margin: 20px auto;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .search-section {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input, .date-picker {
    max-width: 100%;
  }
}
</style>
