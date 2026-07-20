<template>
  <div ref="chartEl" class="admin-chart" :style="{ height }" role="img" :aria-label="ariaLabel"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TooltipComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '280px' },
  ariaLabel: { type: String, default: '运营数据图表' }
})

const chartEl = ref(null)
let chart = null
let resizeObserver = null

function render() {
  if (!chartEl.value) return
  if (!chart) chart = echarts.init(chartEl.value, null, { renderer: 'canvas' })
  chart.setOption(props.option, true)
}

onMounted(() => {
  render()
  resizeObserver = new ResizeObserver(() => chart?.resize())
  resizeObserver.observe(chartEl.value)
})

watch(() => props.option, render, { deep: true })

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  chart?.dispose()
})
</script>
