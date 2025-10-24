<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="事件名称">
          <el-input v-model="searchForm.name" placeholder="请输入事件名称" clearable />
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="searchForm.eventType" placeholder="请选择类型" clearable style="width: 100px">
            <el-option label="查询" value="query" />
            <el-option label="插入" value="insert" />
            <el-option label="更新" value="update" />
            <el-option label="删除" value="delete" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增事件</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="事件名称" width="150" />
        <el-table-column prop="eventType" label="事件类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.eventType === 'query'" type="success">查询</el-tag>
            <el-tag v-else-if="row.eventType === 'insert'" type="primary">插入</el-tag>
            <el-tag v-else-if="row.eventType === 'update'" type="warning">更新</el-tag>
            <el-tag v-else type="danger">删除</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataSourceName" label="数据源" width="150" />
        <el-table-column prop="sqlContent" label="SQL内容" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="handleTest(row)">测试</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @closed="handleDialogClosed"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="事件名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入事件名称" />
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="form.eventType" placeholder="请选择类型" style="width: 100%">
            <el-option label="查询" value="query" />
            <el-option label="插入" value="insert" />
            <el-option label="更新" value="update" />
            <el-option label="删除" value="delete" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源" prop="dataSourceId">
          <el-select v-model="form.dataSourceId" placeholder="请选择数据源" style="width: 100%">
            <el-option
              v-for="item in dataSourceList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="SQL内容" prop="sqlContent">
          <el-input
            v-model="form.sqlContent"
            type="textarea"
            :rows="10"
            placeholder="请输入SQL语句"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="info" @click="handleTestSql" :loading="testLoading">测试执行</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- SQL执行结果对话框 -->
    <el-dialog v-model="resultDialogVisible" title="执行结果" width="80%">
      <div v-if="executeResult.success" style="color: #67c23a; margin-bottom: 10px;">
        执行成功！耗时：{{ executeResult.duration }}ms
      </div>
      <div v-else style="color: #f56c6c; margin-bottom: 10px;">
        执行失败：{{ executeResult.message }}
      </div>

      <!-- 查询结果表格 -->
      <el-table
        v-if="executeResult.success && executeResult.data"
        :data="executeResult.data"
        border
        stripe
        max-height="400"
      >
        <el-table-column
          v-for="col in executeResult.columns"
          :key="col"
          :prop="col"
          :label="col"
        />
      </el-table>

      <!-- 影响行数 -->
      <div v-if="executeResult.success && executeResult.affectedRows !== undefined">
        影响行数：{{ executeResult.affectedRows }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { eventApi, dataSourceApi } from '@/api'

const loading = ref(false)
const testLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const resultDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const tableData = ref([])
const dataSourceList = ref([])
const executeResult = ref({})

const searchForm = reactive({
  name: '',
  eventType: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  name: '',
  eventType: 'query',
  dataSourceId: null,
  sqlContent: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入事件名称', trigger: 'blur' }],
  eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
  dataSourceId: [{ required: true, message: '请选择数据源', trigger: 'change' }],
  sqlContent: [{ required: true, message: '请输入SQL内容', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
  loadDataSources()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await eventApi.page({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载数据失败：', error)
  } finally {
    loading.value = false
  }
}

const loadDataSources = async () => {
  try {
    const res = await dataSourceApi.list()
    dataSourceList.value = res.data
  } catch (error) {
    console.error('加载数据源失败：', error)
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.eventType = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增事件'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑事件'
  try {
    const res = await eventApi.getById(row.id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败：', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该事件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await eventApi.delete(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败：', error)
    }
  }).catch(() => {})
}

const handleTest = async (row) => {
  testLoading.value = true
  try {
    const res = await eventApi.execute({
      dataSourceId: row.dataSourceId,
      sql: row.sqlContent,
      eventId: row.id
    })
    executeResult.value = res.data
    resultDialogVisible.value = true
  } catch (error) {
    console.error('测试失败：', error)
  } finally {
    testLoading.value = false
  }
}

const handleTestSql = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  testLoading.value = true
  try {
    const res = await eventApi.execute({
      dataSourceId: form.dataSourceId,
      sql: form.sqlContent,
      eventId: form.id
    })
    executeResult.value = res.data
    resultDialogVisible.value = true
  } catch (error) {
    console.error('测试失败：', error)
  } finally {
    testLoading.value = false
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await eventApi.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await eventApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败：', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClosed = () => {
  formRef.value.resetFields()
  form.id = null
  form.name = ''
  form.eventType = 'query'
  form.dataSourceId = null
  form.sqlContent = ''
  form.description = ''
}
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-card,
.table-card {
  background: white;
}
</style>
