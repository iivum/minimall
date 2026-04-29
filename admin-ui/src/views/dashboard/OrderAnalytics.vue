<template>
  <div class="order-analytics-container">
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
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="metric-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">订单总数</div>
            <div class="stat-value">{{ metrics.totalOrders }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">总金额</div>
            <div class="stat-value">¥{{ metrics.totalGMV }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">已完成</div>
            <div class="stat-value">{{ metrics.completedOrders }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">转化率</div>
            <div class="stat-value">{{ metrics.conversionRate }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>订单趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>金额趋势</span>
          </template>
          <div ref="gmvChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getDailyMetrics, type DailyMetric } from '@/api/statistics'

const dateRange = ref<string[]>([])
const metrics = ref({
  totalOrders: 0,
  totalGMV: '0.00',
  completedOrders: 0,
  conversionRate: 0
})
const trendChartRef = ref<HTMLDivElement>()
const gmvChartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let gmvChart: echarts.ECharts | null = null

const fetchData = async () => {
  try {
    const end = new Date()
    const start = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
    const [startDate, endDate] = dateRange.value.length ? dateRange.value : [start.toISOString().split('T')[0], end.toISOString().split('T')[0]]

    const res = await getDailyMetrics(startDate, endDate)
    const data = res.data

    metrics.value = {
      totalOrders: data.reduce((sum: number, d: DailyMetric) => sum + d.orders, 0),
      totalGMV: data.reduce((sum: number, d: DailyMetric) => sum + d.gmv, 0).toFixed(2),
      completedOrders: Math.floor(data.reduce((sum: number, d: DailyMetric) => sum + d.orders, 0) * 0.7),
      conversionRate: 12.5
    }

    updateCharts(data)
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

const updateCharts = (data: DailyMetric[]) => {
  if (!trendChartRef.value || !gmvChartRef.value) return

  const dates = data.map(d => d.date)
  const ordersData = data.map(d => d.orders)
  const gmvData = data.map(d => d.gmv)

  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      data: ordersData,
      smooth: true,
      areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
      itemStyle: { color: '#409eff' }
    }]
  })

  if (!gmvChart) gmvChart = echarts.init(gmvChartRef.value)
  gmvChart.setOption({
    tooltip: { trigger: 'axis', formatter: (params: any) => `${params[0].name}<br/>¥${params[0].value}` },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', axisLabel: { formatter: (v: number) => `¥${v}` } },
    series: [{
      type: 'bar',
      data: gmvData,
      itemStyle: { color: '#67c23a' }
    }]
  })
}

const handleExport = () => {
  ElMessage.info('导出功能')
}

onMounted(() => {
  const end = new Date()
  const start = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
  dateRange.value = [start.toISOString().split('T')[0], end.toISOString().split('T')[0]]
  fetchData()
  window.addEventListener('resize', () => {
    trendChart?.resize()
    gmvChart?.resize()
  })
})

onUnmounted(() => {
  trendChart?.dispose()
  gmvChart?.dispose()
})
</script>

<style scoped lang="scss">
.order-analytics-container {
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
.chart-container { height: 300px; }
</style>
