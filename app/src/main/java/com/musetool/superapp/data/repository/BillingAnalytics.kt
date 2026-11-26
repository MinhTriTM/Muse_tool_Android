package com.musetool.superapp.data.repository

import com.musetool.superapp.data.local.AiModelSpec
import com.musetool.superapp.data.local.MuseModelCatalog
import com.musetool.superapp.data.local.UsageLogEntity
import com.musetool.superapp.data.local.UserProfileState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BillingPeriod(val labelVi: String, val shortLabel: String) {
    DAY("Từng Ngày", "Ngày"),
    WEEK("Từng Tuần", "Tuần"),
    MONTH("Từng Tháng", "Tháng"),
    YEAR("Từng Năm", "Năm"),
    ALL("Tổng Cộng", "Tổng")
}

data class PeriodBucketSummary(
    val bucketKey: String,
    val displayTitle: String,
    val subtitle: String,
    val totalCostUsd: Double,
    val freeCreditCoveredUsd: Double,
    val outstandingChargedUsd: Double,
    val totalInputTokens: Long,
    val totalOutputTokens: Long,
    val requestCount: Int,
    val topModelName: String
)

data class ModelCostBreakdown(
    val modelSpec: AiModelSpec,
    val requestCount: Int,
    val inputTokens: Long,
    val outputTokens: Long,
    val totalCostUsd: Double,
    val coveredByFreeUsd: Double,
    val chargedToOutstandingUsd: Double,
    val percentageOfPeriod: Float
)

data class FinOpsRecommendation(
    val currentTopModelId: String,
    val currentTopModelName: String,
    val suggestedModelId: String,
    val suggestedModelName: String,
    val potentialMonthlySavingsUsd: Double,
    val savingsPercent: Int,
    val rationaleVi: String
)

data class BillingDashboardSummary(
    val totalAllTimeCostUsd: Double,
    val todayCostUsd: Double,
    val thisWeekCostUsd: Double,
    val thisMonthCostUsd: Double,
    val thisYearCostUsd: Double,
    val freeCreditsGrantedUsd: Double,
    val freeCreditsUsedUsd: Double,
    val freeCreditsRemainingUsd: Double,
    val freeCreditUsageRatio: Float,
    val grossOutstandingAccruedUsd: Double,
    val settledOutstandingUsd: Double,
    val netOutstandingBalanceUsd: Double,
    val totalRequestsCount: Int,
    val totalTokensProcessed: Long,
    val dailyBurnRateUsd: Double,
    val projectedMonthEndCostUsd: Double,
    val estimatedDaysUntilFreeExhausted: Int,
    val monthlyBudgetLimitUsd: Double,
    val monthlyBudgetUtilizationRatio: Float,
    val isProjectedToExceedBudget: Boolean,
    val finOpsRecommendation: FinOpsRecommendation?,
    val periodBuckets: List<PeriodBucketSummary>,
    val modelBreakdowns: List<ModelCostBreakdown>,
    val filteredLogs: List<UsageLogEntity>
)

object BillingCalculatorEngine {

    /**
     * Recalculates exact Free Credit vs Outstanding Balance waterfall for all logs,
     * plus period buckets (Day, Week, Month, Year, All) and Per-Model cost breakdowns.
     */
    fun computeDashboardSummary(
        allLogsDesc: List<UsageLogEntity>,
        profile: UserProfileState,
        selectedPeriod: BillingPeriod,
        selectedModelFilter: String? = null
    ): BillingDashboardSummary {
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance().apply { timeInMillis = now }

        val startOfToday = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfWeek = Calendar.getInstance().apply {
            timeInMillis = startOfToday
            add(Calendar.DAY_OF_YEAR, -6)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfYear = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.YEAR, calNow.get(Calendar.YEAR))
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val totalAllTimeCost = allLogsDesc.sumOf { it.totalCostUsd }
        val todayCost = allLogsDesc.filter { it.timestamp >= startOfToday }.sumOf { it.totalCostUsd }
        val weekCost = allLogsDesc.filter { it.timestamp >= startOfWeek }.sumOf { it.totalCostUsd }
        val monthCost = allLogsDesc.filter { it.timestamp >= startOfMonth }.sumOf { it.totalCostUsd }
        val yearCost = allLogsDesc.filter { it.timestamp >= startOfYear }.sumOf { it.totalCostUsd }

        // Sum exact free-credit covered and outstanding overage from logs + dynamic grant pool
        val loggedFreeCovered = allLogsDesc.sumOf { it.coveredByFreeCreditUsd }
        val loggedOutstanding = allLogsDesc.sumOf { it.chargedToOutstandingUsd }

        val freeGranted = profile.freeCreditAllocationUsd.coerceAtLeast(0.0)
        val freeUsed = loggedFreeCovered.coerceAtMost(freeGranted)
        val freeRemaining = (freeGranted - freeUsed).coerceAtLeast(0.0)
        val freeRatio = if (freeGranted > 0.0) {
            (freeUsed / freeGranted).toFloat().coerceIn(0f, 1f)
        } else 1f

        val extraUncoveredIfGrantReduced = (loggedFreeCovered - freeGranted).coerceAtLeast(0.0)
        val grossOutstanding = loggedOutstanding + extraUncoveredIfGrantReduced
        val settled = profile.paidAmountUsd.coerceIn(0.0, grossOutstanding)
        val netOutstanding = (grossOutstanding - settled).coerceAtLeast(0.0)

        // Filter logs by selected period for per-model breakdown and log feed
        val periodFilteredLogs = when (selectedPeriod) {
            BillingPeriod.DAY -> allLogsDesc.filter { it.timestamp >= startOfToday }
            BillingPeriod.WEEK -> allLogsDesc.filter { it.timestamp >= startOfWeek }
            BillingPeriod.MONTH -> allLogsDesc.filter { it.timestamp >= startOfMonth }
            BillingPeriod.YEAR -> allLogsDesc.filter { it.timestamp >= startOfYear }
            BillingPeriod.ALL -> allLogsDesc
        }.let { list ->
            if (selectedModelFilter.isNullOrBlank() || selectedModelFilter == "ALL") {
                list
            } else {
                list.filter { it.modelId == selectedModelFilter }
            }
        }

        // Compute Period Buckets (Daily / Weekly / Monthly / Yearly / All)
        val periodBuckets = buildPeriodBuckets(allLogsDesc, selectedPeriod)

        // Compute Per-Model Breakdown for the active time period
        val logsForModelChart = when (selectedPeriod) {
            BillingPeriod.DAY -> allLogsDesc.filter { it.timestamp >= startOfToday }
            BillingPeriod.WEEK -> allLogsDesc.filter { it.timestamp >= startOfWeek }
            BillingPeriod.MONTH -> allLogsDesc.filter { it.timestamp >= startOfMonth }
            BillingPeriod.YEAR -> allLogsDesc.filter { it.timestamp >= startOfYear }
            BillingPeriod.ALL -> allLogsDesc
        }
        val periodTotalCost = logsForModelChart.sumOf { it.totalCostUsd }.coerceAtLeast(0.000001)

        val modelBreakdowns = MuseModelCatalog.models.map { spec ->
            val modelLogs = logsForModelChart.filter { it.modelId == spec.modelId }
            val cost = modelLogs.sumOf { it.totalCostUsd }
            val freeCov = modelLogs.sumOf { it.coveredByFreeCreditUsd }
            val outChg = modelLogs.sumOf { it.chargedToOutstandingUsd }
            val inTok = modelLogs.sumOf { it.inputTokens.toLong() }
            val outTok = modelLogs.sumOf { it.outputTokens.toLong() }
            val pct = if (logsForModelChart.isEmpty()) 0f else ((cost / periodTotalCost) * 100.0).toFloat()
            ModelCostBreakdown(
                modelSpec = spec,
                requestCount = modelLogs.size,
                inputTokens = inTok,
                outputTokens = outTok,
                totalCostUsd = cost,
                coveredByFreeUsd = freeCov,
                chargedToOutstandingUsd = outChg,
                percentageOfPeriod = pct
            )
        }.sortedByDescending { it.totalCostUsd }

        val totalTokens = allLogsDesc.sumOf { (it.inputTokens + it.outputTokens).toLong() }

        // FinOps Burn-Rate & End-of-Month Projection
        val activeDayOfMonth = calNow.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val daysInCurrentMonth = calNow.getActualMaximum(Calendar.DAY_OF_MONTH).coerceAtLeast(28)
        val trailing7DayDailyAvg = (weekCost / 7.0).coerceAtLeast(0.01)
        val currentMonthDailyAvg = (monthCost / activeDayOfMonth.toDouble()).coerceAtLeast(0.01)
        val dailyBurnRate = ((trailing7DayDailyAvg * 0.6) + (currentMonthDailyAvg * 0.4)).coerceAtLeast(0.05)
        val remainingDaysInMonth = (daysInCurrentMonth - activeDayOfMonth).coerceAtLeast(0)
        val projectedMonthEnd = monthCost + (dailyBurnRate * remainingDaysInMonth)

        val estDaysUntilFreeExhausted = if (freeRemaining <= 0.001) {
            0
        } else {
            (freeRemaining / dailyBurnRate).toInt().coerceIn(1, 365)
        }

        val budgetLimit = profile.monthlyBudgetLimitUsd.coerceAtLeast(1.0)
        val budgetRatio = (monthCost / budgetLimit).toFloat().coerceIn(0f, 1.5f)
        val projectedExceed = projectedMonthEnd > budgetLimit

        // Smart Model Cost Optimization Advisor
        val topExpensiveBreakdown = modelBreakdowns.firstOrNull {
            it.totalCostUsd > 0.25 && it.modelSpec.inputPricePer1MUsd >= 1.25
        } ?: modelBreakdowns.firstOrNull { it.totalCostUsd > 0.0 }

        val flashSpec = MuseModelCatalog.findById("gemini-3.8-flash")
        val recommendation = if (topExpensiveBreakdown != null && topExpensiveBreakdown.modelSpec.modelId != flashSpec.modelId) {
            val currentCost = topExpensiveBreakdown.totalCostUsd
            val simulatedFlashCost = MuseModelCatalog.calculateCostUsd(
                modelId = flashSpec.modelId,
                inputTokens = topExpensiveBreakdown.inputTokens.toInt().coerceAtLeast(1000),
                outputTokens = topExpensiveBreakdown.outputTokens.toInt().coerceAtLeast(1000)
            )
            val rawSavings = (currentCost - simulatedFlashCost).coerceAtLeast(0.15)
            val pct = ((rawSavings / currentCost.coerceAtLeast(0.01)) * 100.0).toInt().coerceIn(15, 92)
            FinOpsRecommendation(
                currentTopModelId = topExpensiveBreakdown.modelSpec.modelId,
                currentTopModelName = topExpensiveBreakdown.modelSpec.displayName,
                suggestedModelId = flashSpec.modelId,
                suggestedModelName = flashSpec.displayName,
                potentialMonthlySavingsUsd = rawSavings,
                savingsPercent = pct,
                rationaleVi = "Chuyển 60% tác vụ tóm tắt/phân tích thường quy từ ${topExpensiveBreakdown.modelSpec.displayName} sang ${flashSpec.displayName} giúp tiết kiệm ~$pct% chi phí mà vẫn giữ tốc độ phản hồi cao."
            )
        } else null

        return BillingDashboardSummary(
            totalAllTimeCostUsd = totalAllTimeCost,
            todayCostUsd = todayCost,
            thisWeekCostUsd = weekCost,
            thisMonthCostUsd = monthCost,
            thisYearCostUsd = yearCost,
            freeCreditsGrantedUsd = freeGranted,
            freeCreditsUsedUsd = freeUsed,
            freeCreditsRemainingUsd = freeRemaining,
            freeCreditUsageRatio = freeRatio,
            grossOutstandingAccruedUsd = grossOutstanding,
            settledOutstandingUsd = settled,
            netOutstandingBalanceUsd = netOutstanding,
            totalRequestsCount = allLogsDesc.size,
            totalTokensProcessed = totalTokens,
            dailyBurnRateUsd = dailyBurnRate,
            projectedMonthEndCostUsd = projectedMonthEnd,
            estimatedDaysUntilFreeExhausted = estDaysUntilFreeExhausted,
            monthlyBudgetLimitUsd = budgetLimit,
            monthlyBudgetUtilizationRatio = budgetRatio,
            isProjectedToExceedBudget = projectedExceed,
            finOpsRecommendation = recommendation,
            periodBuckets = periodBuckets,
            modelBreakdowns = modelBreakdowns,
            filteredLogs = periodFilteredLogs
        )
    }

    private fun buildPeriodBuckets(
        allLogsDesc: List<UsageLogEntity>,
        period: BillingPeriod
    ): List<PeriodBucketSummary> {
        val dayFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val shortDayFmt = SimpleDateFormat("dd/MM (EEE)", Locale.getDefault())
        val monthFmt = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val yearFmt = SimpleDateFormat("yyyy", Locale.getDefault())

        val grouped: Map<String, List<UsageLogEntity>> = when (period) {
            BillingPeriod.DAY -> allLogsDesc.groupBy { dayFmt.format(Date(it.timestamp)) }
            BillingPeriod.WEEK -> allLogsDesc.groupBy { tsToWeekKey(it.timestamp) }
            BillingPeriod.MONTH -> allLogsDesc.groupBy { monthFmt.format(Date(it.timestamp)) }
            BillingPeriod.YEAR -> allLogsDesc.groupBy { yearFmt.format(Date(it.timestamp)) }
            BillingPeriod.ALL -> allLogsDesc.groupBy { monthFmt.format(Date(it.timestamp)) }
        }

        return grouped.entries.take(12).map { (key, logs) ->
            val firstTs = logs.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()
            val title = when (period) {
                BillingPeriod.DAY -> "Ngày ${shortDayFmt.format(Date(firstTs))}"
                BillingPeriod.WEEK -> key
                BillingPeriod.MONTH -> "Tháng $key"
                BillingPeriod.YEAR -> "Năm $key"
                BillingPeriod.ALL -> "Kỳ $key"
            }
            val topModel = logs.groupBy { it.modelName }
                .maxByOrNull { entry -> entry.value.sumOf { it.totalCostUsd } }
                ?.key ?: "N/A"

            PeriodBucketSummary(
                bucketKey = key,
                displayTitle = title,
                subtitle = "${logs.size} phiên gọi • Cao nhất: $topModel",
                totalCostUsd = logs.sumOf { it.totalCostUsd },
                freeCreditCoveredUsd = logs.sumOf { it.coveredByFreeCreditUsd },
                outstandingChargedUsd = logs.sumOf { it.chargedToOutstandingUsd },
                totalInputTokens = logs.sumOf { it.inputTokens.toLong() },
                totalOutputTokens = logs.sumOf { it.outputTokens.toLong() },
                requestCount = logs.size,
                topModelName = topModel
            )
        }
    }

    private fun tsToWeekKey(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        return "Tuần $week/$year"
    }
}
