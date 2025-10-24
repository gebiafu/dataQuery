<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="任务名称">
          <el-input v-model="searchForm.jobName" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="searchForm.jobStatus" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="运行中" value="RUNNING" />
            <el-option label="已停止" value="STOP" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增任务</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="jobName" label="任务名称" width="150" />
        <el-table-column prop="eventName" label="关联事件" width="150" />
        <el-table-column prop="cronExpression" label="Cron表达式" width="150" />
        <el-table-column prop="jobStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.jobStatus === 'RUNNING'" type="success">运行中</el-tag>
            <el-tag v-else type="info">已停止</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.jobStatus === 'STOP'"
              link
              type="success"
              @click="handleStart(row)"
            >
              启动
            </el-button>
            <el-button
              v-else
              link
              type="warning"
              @click="handleStop(row)"
            >
              停止
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="info" @click="handleViewLog(row)">日志</el-button>
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
      width="600px"
      @closed="handleDialogClosed"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="form.jobName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="关联事件" prop="eventId">
          <el-select v-model="form.eventId" placeholder="请选择事件" style="width: 100%">
            <el-option
              v-for="item in eventList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cronExpression">
          <el-input v-model="form.cronExpression" placeholder="例如: 0 0/5 * * * ?" />
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            示例：0 0/5 * * * ? (每5分钟执行一次)
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 日志对话框 -->
    <el-dialog v-model="logDialogVisible" title="执行日志" width="80%">
      <el-table :data="logData" border stripe v-loading="logLoading" max-height="500">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="eventName" label="事件名称" width="150" />
        <el-table-column prop="executeStatus" label="执行状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.executeStatus === 'SUCCESS'" type="success">成功</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeResult" label="执行结果" show-overflow-tooltip />
        <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
      </el-table>

      <el-pagination
        v-model:current-page="logPagination.pageNum"
        v-model:page-size="logPagination.pageSize"
        :total="logPagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadLogData"
        @current-change="loadLogData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { scheduleApi, eventApi, scheduleLogApi } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const logLoading = ref(false)
const dialogVisible = ref(false)
const logDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const tableData = ref([])
const eventList = ref([])
const logData = ref([])
const currentJobId = ref(null)

const searchForm = reactive({
  jobName: '',
  jobStatus: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const logPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  jobName: '',
  eventId: null,
  cronExpression: '',
  description: ''
})

const rules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  eventId: [{ required: true, message: '请选择事件', trigger: 'change' }],
  cronExpression: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
  loadEvents()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await scheduleApi.page({
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

const loadEvents = async () => {
  try {
    const res = await eventApi.list()
    eventList.value = res.data
  } catch (error) {
    console.error('加载事件失败：', error)
  }
}

const loadLogData = async () => {
  if (!currentJobId.value) return

  logLoading.value = true
  try {
    const res = await scheduleLogApi.page({
      jobId: currentJobId.value,
      pageNum: logPagination.pageNum,
      pageSize: logPagination.pageSize
    })
    logData.value = res.data.records
    logPagination.total = res.data.total
  } catch (error) {
    console.error('加载日志失败：', error)
  } finally {
    logLoading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.jobName = ''
  searchForm.jobStatus = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增调度任务'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑调度任务'
  try {
    const res = await scheduleApi.getById(row.id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败：', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await scheduleApi.delete(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败：', error)
    }
  }).catch(() => {})
}

const handleStart = async (row) => {
  try {
    await scheduleApi.start(row.id)
    ElMessage.success('启动成功')
    loadData()
  } catch (error) {
    console.error('启动失败：', error)
  }
}

const handleStop = async (row) => {
  try {
    await scheduleApi.stop(row.id)
    ElMessage.success('停止成功')
    loadData()
  } catch (error) {
    console.error('停止失败：', error)
  }
}

const handleViewLog = (row) => {
  currentJobId.value = row.id
  logPagination.pageNum = 1
  loadLogData()
  logDialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await scheduleApi.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await scheduleApi.create(form)
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
  form.jobName = ''
  form.eventId = null
  form.cronExpression = ''
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
