<template>
  <div class="overview-container">
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
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="快捷选项">
          <el-radio-group v-model="quickRange" @change="handleQuickRangeChange">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="week">近7天</el-radio-button>
            <el-radio-button label="month">近30天</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refreshData">刷新</el-button>
          <el-button @click="handleExport('csv')">导出CSV</el-button>
          <el-button @click="handleExport('xlsx')">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="metric-row">
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-icon orders"><el-icon><ShoppingCart /></el-icon></div>
          <div class="metric-content">
            <div class="metric-label">订单数</div>
            <div class="metric-value">{{ formatNumber(summary.totalOrders) }}</div>
            <div class="metric-change" :class="getChangeClass(summary.ordersChange)">
              {{ formatChange(summary.ordersChange) }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-icon gmv"><el-icon><Money /></el-icon></div>
          <div class="metric-content">
            <div class="metric-label">GMV</div>
            <div class="metric-value">¥{{ formatMoney(summary.totalGMV) }}</div>
            <div class="metric-change" :class="getChangeClass(summary.gmvChange)">
              {{ formatChange(summary.gmvChange) }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-icon users"><el-icon><User /></el-icon></div>
          <div class="metric-content">
            <div class="metric-label">新用户</div>
            <div class="metric-value">{{ formatNumber(summary.totalUsers) }}</div>
            <div class="metric-change" :class="getChangeClass(summary.usersChange)">
              {{ formatChange(summary.usersChange) }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-icon aov"><el-icon><TrendCharts /></el-icon></div>
          <div class="metric-content">
            <div class="metric-label">客单价</div>
            <div class="metric-value">¥{{ formatMoney(summary.averageOrderValue) }}</div>
            <div class="metric-change neutral">vs 上期</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span>交易趋势</span>
          </template>
          <div ref="gmvChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span>用户增长</span>
          </template>
          <div ref="userChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'
import { getOverviewMetrics, exportData, type StatisticsOverview } from '@/api/statistics'

const dateRange = ref<string[]>([])
const quickRange = ref('week')
const summary = ref<Partial<StatisticsOverview>>({})
const gmvChartRef = ref<HTMLDivElement>()
const userChartRef = ref<HTMLDivElement>()
let gmvChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null

const getDateRange = () => {
  const end = new Date()
  const start = new Date()
  if (quickRange.value === 'today') {
    // same day
  } else if (quickRange.value === 'week') {
    start.setDate(start.getDate() - 7)
  } else if (quickRange.value === 'month') {
    start.setDate(start.getDate() - 30)
  }
  return [start.toISOString().split('T')[0], end.toISOString().split('T')[0]]
}

const fetchData = async () => {
  try {
    const [start, end] = dateRange.value.length ? dateRange.value : getDateRange()
    const res = await getOverviewMetrics(start, end)
    summary.value = res.data
    updateCharts()
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

const handleDateChange = () => {
  quickRange.value = ''
  fetchData()
}

const handleQuickRangeChange = () => {
  dateRange.value = getDateRange()
  fetchData()
}

const refreshData = () => {
  fetchData()
}

const handleExport = async (format: 'csv' | 'xlsx') => {
  try {
    const [start, end] = dateRange.value.length ? dateRange.value : getDateRange()
    const res = await exportData(start, end, format)
    const data = (res.data as any).data

    if (format === 'csv') {
      const csv = [
        ['日期', '订单数', 'GMV', '用户数'],
        ...data.map((d: any) => [d.date, d.orders, d.gmv, d.users])
      ].map(row => row.join(',')).join('\n')
      downloadFile(csv, `statistics_${start}_${end}.csv`, 'text/csv')
    } else {
      const ws = XLSX.utils.json_to_sheet(data)
      const wb = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(wb, ws, 'Statistics')
      XLSX.writeFile(wb, `statistics_${start}_${end}.xlsx`)
    }
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const downloadFile = (content: string, filename: string, type: string) => {
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

const updateCharts = () => {
  if (!gmvChartRef.value || !userChartRef.value) return

  const metrics = summary.value.dailyMetrics || []
  const dates = metrics.map((d: any) => d.date)
  const gmvData = metrics.map((d: any) => d.gmv)
  const ordersData = metrics.map((d: any) => d.orders)
  const usersData = metrics.map((d: any) => d.users)

  if (!gmvChart) {
    gmvChart = echarts.init(gmvChartRef.value)
  }
  gmvChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['GMV', '订单数'] },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: 'GMV', axisLabel: { formatter: (v: number) => `¥${v}` } },
      { type: 'value', name: '订单', axisLabel: { formatter: (v: number) => v.toString() } }
    ],
    series: [
      { name: 'GMV', type: 'line', data: gmvData, smooth: true, itemStyle: { color: '#409eff' } },
      { name: '订单数', type: 'line', yAxisIndex: 1, data: ordersData, smooth: true, itemStyle: { color: '#67c23a' } }
    ]
  })

  if (!userChart) {
    userChart = echarts.init(userChartRef.value)
  }
  userChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '用户数', type: 'bar', data: usersData, itemStyle: { color: '#e6a23c' } }
    ]
  })
}

const formatNumber = (num: number | undefined) => num?.toLocaleString() ?? '0'
const formatMoney = (num: number | undefined) => (num ?? 0).toFixed(2)
const formatChange = (change: number | undefined) => {
  if (change === undefined) return ''
  return change >= 0 ? `+${change}%` : `${change}%`
}
const getChangeClass = (change: number | undefined) => {
  if (change === undefined) return 'neutral'
  return change >= 0 ? 'positive' : 'negative'
}

onMounted(() => {
  dateRange.value = getDateRange()
  fetchData()
  window.addEventListener('resize', () => {
    gmvChart?.resize()
    userChart?.resize()
  })
})

onUnmounted(() => {
  gmvChart?.dispose()
  userChart?.dispose()
})
</script>

<style scoped lang="scss">
.overview-container {
  padding: 20px;
}
.filter-card {
  margin-bottom: 20px;
}
.metric-row {
  margin-bottom: 20px;
}
.metric-card {
  display: flex;
  align-items: center;
  .metric-icon {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: white;
    margin-right: 16px;
    &.orders { background: linear-gradient(135deg, #409eff, #66b1ff); }
    &.gmv { background: linear-gradient(135deg, #67c23a, #85ce61); }
    &.users { background: linear-gradient(135deg, #e6a23c, #ebb563); }
    &.aov { background: linear-gradient(135deg, #f56cbc, #f59fbf); }
  }
  .metric-content {
    flex: 1;
    .metric-label { color: #909399; font-size: 14px; }
    .metric-value { font-size: 24px; font-weight: bold; margin: 4px 0; }
    .metric-change {
      font-size: 12px;
      &.positive { color: #67c23a; }
      &.negative { color: #f56c6c; }
      &.neutral { color: #909399; }
    }
  }
}
.chart-row {
  .chart-container { height: 300px; }
}
</style>
