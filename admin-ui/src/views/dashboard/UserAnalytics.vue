<template>
  <div class="user-analytics-container">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="fetchData"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="metric-row">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">总用户数</div>
            <div class="stat-value">{{ metrics.totalUsers }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">本月新增</div>
            <div class="stat-value">{{ metrics.monthlyNew }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">今日新增</div>
            <div class="stat-value">{{ metrics.todayNew }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <span>用户增长趋势</span>
      </template>
      <div ref="growthChartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getUserGrowth } from '@/api/statistics'

const dateRange = ref<string[]>([])
const metrics = ref({
  totalUsers: 0,
  monthlyNew: 0,
  todayNew: 0
})
const growthChartRef = ref<HTMLDivElement>()
let growthChart: echarts.ECharts | null = null

const fetchData = async () => {
  try {
    const end = new Date()
    const start = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
    const [startDate, endDate] = dateRange.value.length ? dateRange.value : [start.toISOString().split('T')[0], end.toISOString().split('T')[0]]

    const res = await getUserGrowth(startDate, endDate)
    const data = res.data as any[]

    metrics.value = {
      totalUsers: data.reduce((sum: number, d: any) => sum + (d.users || 0), 0),
      monthlyNew: data.reduce((sum: number, d: any) => sum + (d.users || 0), 0),
      todayNew: data.length > 0 ? data[data.length - 1].users || 0 : 0
    }

    updateChart(data)
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

const updateChart = (data: any[]) => {
  if (!growthChartRef.value) return
  if (!growthChart) growthChart = echarts.init(growthChartRef.value)

  growthChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: data.map(d => d.users),
      itemStyle: { color: '#409eff' }
    }]
  })
}

onMounted(() => {
  const end = new Date()
  const start = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
  dateRange.value = [start.toISOString().split('T')[0], end.toISOString().split('T')[0]]
  fetchData()
  window.addEventListener('resize', () => growthChart?.resize())
})

onUnmounted(() => {
  growthChart?.dispose()
})
</script>

<style scoped lang="scss">
.user-analytics-container {
  padding: 20px;
}
.stat-item {
  text-align: center;
  .stat-label { color: #909399; margin-bottom: 8px; }
  .stat-value { font-size: 24px; font-weight: bold; }
}
.metric-row {
  margin-bottom: 20px;
}
.chart-container { height: 400px; }
</style>
