package com.aiart.platform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AdminDashboardDtos {
    private AdminDashboardDtos() {
    }

    public record DashboardView(List<Metric> metrics,
                                List<StatusCount> generationStatus,
                                List<StatusCount> auditStatus,
                                List<StatusCount> taskStatus,
                                List<StatusCount> styleStatus,
                                List<DailyCount> dailyGenerations,
                                List<DailyCount> dailyArtworks,
                                List<PointFlow> pointFlows,
                                List<RecentActivity> recentActivities) {
    }

    public record Metric(String key, String label, BigDecimal value, String unit, String tone) {
    }

    public record StatusCount(String status, long count) {
    }

    public record DailyCount(LocalDate date, long count) {
    }

    public record PointFlow(String reason, BigDecimal totalIn, BigDecimal totalOut, long count) {
    }

    public record RecentActivity(String type, String title, String status, LocalDateTime createdAt) {
    }
}
