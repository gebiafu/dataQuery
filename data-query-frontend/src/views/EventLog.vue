<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="事件类型">
          <el-select v-model="searchForm.eventType" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="查询" value="query" />
            <el-option label="插入" value="insert" />
            <el-option label="更新" value="update" />
            <el-option label="删除" value="delete" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行状态">
          <el-select v-model="searchForm.executeStatus" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="eventName" label="事件名称" width="150" />
        <el-table-column prop="eventType" label="事件类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.eventType === 'query'" type="success">查询</el-tag>
            <el-tag v-else-if="row.eventType === 'insert'" type="primary">插入</el-tag>
            <el-tag v-else-if="row.eventType === 'update'" type="warning">更新</el-tag>
            <el-tag v-else-if="row.eventType === 'delete'" type="danger">删除</el-tag>
            <el-tag v-else type="info">测试</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataSourceName" label="数据源" width="150" />
        <el-table-column prop="executeStatus" label="执行状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.executeStatus === 'SUCCESS'" type="success">成功</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeUserName" label="执行人" width="120" />
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column prop="createTime" label="执行时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="执行详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="事件名称">
          {{ detailData.eventName }}
        </el-descriptions-item>
        <el-descriptions-item label="事件类型">
          <el-tag v-if="detailData.eventType === 'query'" type="success">查询</el-tag>
          <el-tag v-else-if="detailData.eventType === 'insert'" type="primary">插入</el-tag>
          <el-tag v-else-if="detailData.eventType === 'update'" type="warning">更新</el-tag>
          <el-tag v-else type="danger">删除</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据源">
          {{ detailData.dataSourceName }}
        </el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <el-tag v-if="detailData.executeStatus === 'SUCCESS'" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行人">
          {{ detailData.executeUserName }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时">
          {{ detailData.duration }} ms
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ detailData.startTime }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ detailData.endTime }}
        </el-descriptions-item>
        <el-descriptions-item label="SQL内容" :span="2">
          <el-input
            :model-value="detailData.sqlContent"
            type="textarea"
            :rows="6"
            readonly
          />
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.executeResult" label="执行结果" :span="2">
          {{ detailData.executeResult }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.errorMessage" label="错误信息" :span="2">
          <div style="color: #f56c6c;">{{ detailData.errorMessage }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { eventLogApi } from '@/api'

const loading = ref(false)
const detailDialogVisible = ref(false)
const tableData = ref([])
const dateRange = ref([])
const detailData = ref({})

const searchForm = reactive({
  eventType: '',
  executeStatus: '',
  startTime: '',
  endTime: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await eventLogApi.page({
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

const handleSearch = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.startTime = dateRange.value[0]
    searchForm.endTime = dateRange.value[1]
  } else {
    searchForm.startTime = ''
    searchForm.endTime = ''
  }
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.eventType = ''
  searchForm.executeStatus = ''
  searchForm.startTime = ''
  searchForm.endTime = ''
  dateRange.value = []
  handleSearch()
}

const handleViewDetail = (row) => {
  detailData.value = row
  detailDialogVisible.value = true
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
