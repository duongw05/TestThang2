<template>
  <div class="category-list-container">
    <el-card class="box-card">
      <div class="card-header">
        <h3 class="title">{{ t('category.listTitle') }}</h3>
        <div>
          <el-button type="success" :icon="Plus" @click="goToAddCategory">
            {{ t('category.add') }}
          </el-button>
          <el-button type="warning" :icon="Download" @click="exportToExcel">
            {{ t('category.exportExcel') }}
          </el-button>
        </div>
      </div>

      <div class="search-section">
        <el-input
            v-model="searchParams.keyword"
            :placeholder="t('placeholder.enterKeyword')"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            class="search-input"
        />
        <el-date-picker
            v-model="searchParams.createdFrom"
            type="datetime"
            :placeholder="t('filter.fromDate')"
            format="DD/MM/YYYY HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            clearable
            class="date-picker"
        />
        <el-date-picker
            v-model="searchParams.createdTo"
            type="datetime"
            :placeholder="t('filter.toDate')"
            format="DD/MM/YYYY HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            clearable
            class="date-picker"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">
          {{ t('action.search') }}
        </el-button>
        <el-button :icon="Refresh" @click="resetSearch">
          {{ t('action.reset') }}
        </el-button>
      </div>

      <el-table
          :data="categories"
          border
          stripe
          v-loading="loading"
          :element-loading-text="t('messages.loadFailed')"
          :empty-text="t('category.emptyText')"
          class="category-table"
      >
        <el-table-column type="index" :label="t('table.index')" width="60" :index="indexMethod" />
        <el-table-column prop="categoryCode" :label="t('category.code')" width="150" sortable />
        <el-table-column prop="categoryName" :label="t('category.name')" width="150" sortable />
        <el-table-column prop="description" :label="t('category.description')" />
        <el-table-column prop="createdDate" :label="t('category.createdDate')" width="180" />
        <el-table-column prop="createdBy" :label="t('category.createdBy')" width="120" />
        <el-table-column prop="modifiedDate" :label="t('category.modifiedDate')" width="180" />
        <el-table-column prop="modifiedBy" :label="t('category.modifiedBy')" width="120" />
        <el-table-column :label="t('action.actions')" width="150" fixed="right">
          <template #default="scope">
            <el-button type="info" :icon="View" size="small" circle @click="goToDetailCategory(scope.row.id)" :title="t('action.details')" />
            <el-button type="primary" :icon="Edit" size="small" circle @click="goToEditCategory(scope.row.id)" title="Edit" />
            <el-button type="danger" :icon="Delete" size="small" circle @click="confirmDeleteCategory(scope.row.id)" title="Delete" />
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 20, 50]"
            :page-size="size"
            layout="sizes, total, prev, pager, next, jumper"
            :total="totalElements"
            background
            :pager-count="7"
            :prev-text="t('pagination.prev')"
            :next-text="t('pagination.next')"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, View, Download } from '@element-plus/icons-vue'
import api from '@/utils/axios.js'

const { t } = useI18n()
const router = useRouter()

const categories = ref([])
const currentPage = ref(1)
const size = ref(10)
const totalElements = ref(0)
const loading = ref(false)

const searchParams = reactive({
  keyword: '',
  createdFrom: null,
  createdTo: null
})

const fetchCategories = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: size.value,
      keyword: searchParams.keyword?.trim() || null,
      createdFrom: searchParams.createdFrom || null,
      createdTo: searchParams.createdTo || null
    }

    const res = await api.get('/categories/search', { params })

    categories.value = res.data?.data || []
    totalElements.value = res.data?.pagination?.totalElements ?? 0
    currentPage.value = res.data?.pagination?.currentPage ?? 1
    size.value = res.data?.pagination?.pageSize ?? 10

    if (categories.value.length === 0 && totalElements.value > 0 && currentPage.value > 1) {
      currentPage.value = Math.max(1, res.data.pagination.totalPages)
      await fetchCategories()
    }
  } catch (err) {
    console.error('Error:', err)
    ElMessage.error(t('messages.loadFailed'))
    categories.value = []
    totalElements.value = 0
    currentPage.value = 1
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  size.value = val
  currentPage.value = 1
  fetchCategories()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCategories()
}

const indexMethod = (index) => (currentPage.value - 1) * size.value + index + 1

const goToAddCategory = () => {
  router.push({ name: 'AddCategory' })
}

const goToDetailCategory = (id) => {
  router.push({ name: 'DetailCategory', params: { id } })
}

const goToEditCategory = (id) => {
  router.push({ name: 'UpdateCategory', params: { id } })
}

const confirmDeleteCategory = async (id) => {
  try {
    await ElMessageBox.confirm(
        t('messages.confirmDelete'),
        t('action.confirm'),
        {
          confirmButtonText: t('action.confirm'),
          cancelButtonText: t('action.cancel'),
          type: 'warning'
        }
    )
    await api.delete(`/categories/${id}`)
    ElMessage.success(t('messages.deleteSuccess'))
    fetchCategories()
  } catch (err) {
    if (err === 'cancel' || err === 'close') {
      ElMessage.info(t('messages.deleteCancel'))
    } else {
      ElMessage.error(t('messages.deleteError'))
    }
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchCategories()
}

const resetSearch = () => {
  searchParams.keyword = ''
  searchParams.createdFrom = null
  searchParams.createdTo = null
  currentPage.value = 1
  fetchCategories()
}

const exportToExcel = async () => {
  try {
    await ElMessageBox.confirm(
        t('messages.confirmExport', { count: totalElements.value }),
        t('messages.confirmTitle'),
        {
          confirmButtonText: t('category.exportExcel'),
          cancelButtonText: t('exportCancel'),
          type: 'warning'
        }
    )

    const dto = {
      keyword: searchParams.keyword?.trim() || null,
      createdFrom: searchParams.createdFrom,
      createdTo: searchParams.createdTo
    }

    const res = await api.post('/categories/export', dto, {
      responseType: 'blob'
    })

    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'categories.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info(t('messages.exportCancelled'))
    } else {
      console.error(error)
      ElMessage.error(t('messages.loadFailed'))
    }
  }
}

onMounted(() => {
  fetchCategories()
})
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
