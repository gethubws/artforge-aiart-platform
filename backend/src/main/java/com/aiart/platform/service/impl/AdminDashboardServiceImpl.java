package com.aiart.platform.service.impl;

import com.aiart.platform.dto.AdminDashboardDtos;
import com.aiart.platform.entity.AiGenerationJob;
import com.aiart.platform.entity.Artwork;
import com.aiart.platform.entity.ContentAudit;
import com.aiart.platform.entity.EnterpriseTask;
import com.aiart.platform.entity.PointTransaction;
import com.aiart.platform.entity.StylePackage;
import com.aiart.platform.entity.User;
import com.aiart.platform.mapper.AiGenerationJobMapper;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.ContentAuditMapper;
import com.aiart.platform.mapper.EnterpriseTaskMapper;
import com.aiart.platform.mapper.PointTransactionMapper;
import com.aiart.platform.mapper.StylePackageMapper;
import com.aiart.platform.mapper.UserMapper;
import com.aiart.platform.service.AdminDashboardService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final UserMapper userMapper;
    private final ArtworkMapper artworkMapper;
    private final AiGenerationJobMapper generationJobMapper;
    private final ContentAuditMapper contentAuditMapper;
    private final StylePackageMapper stylePackageMapper;
    private final EnterpriseTaskMapper enterpriseTaskMapper;
    private final PointTransactionMapper pointTransactionMapper;

    @Override
    public AdminDashboardDtos.DashboardView dashboard() {
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime quarterStart = LocalDate.now().minusDays(89).atStartOfDay();
        long generationCount = generationJobMapper.selectCount(Wrappers.<AiGenerationJob>lambdaQuery());
        long completedGenerationCount = generationJobMapper.selectCount(Wrappers.<AiGenerationJob>lambdaQuery().eq(AiGenerationJob::getStatus, "COMPLETED"));
        long pendingAuditCount = contentAuditMapper.selectCount(Wrappers.<ContentAudit>lambdaQuery().eq(ContentAudit::getStatus, "PENDING"));
        long publishedStyleCount = stylePackageMapper.selectCount(Wrappers.<StylePackage>lambdaQuery().eq(StylePackage::getStatus, "PUBLISHED"));
        long publishedTaskCount = enterpriseTaskMapper.selectCount(Wrappers.<EnterpriseTask>lambdaQuery().eq(EnterpriseTask::getStatus, "PUBLISHED"));

        List<AdminDashboardDtos.Metric> metrics = List.of(
                metric("users", "用户", userMapper.selectCount(Wrappers.<User>lambdaQuery()), "人", "sage"),
                metric("artworks", "作品", artworkMapper.selectCount(Wrappers.<Artwork>lambdaQuery()), "张", "blue"),
                metric("publicArtworks", "公开作品", artworkMapper.selectCount(Wrappers.<Artwork>lambdaQuery().eq(Artwork::getVisibility, "PUBLIC")), "张", "amber"),
                metric("generations", "生成任务", generationCount, "次", "sage"),
                metric("successRate", "生成成功率", successRate(generationCount, completedGenerationCount), "%", "blue"),
                metric("pendingAudits", "待审核", pendingAuditCount, "项", pendingAuditCount > 0 ? "amber" : "sage"),
                metric("stylePackages", "已发布风格包", publishedStyleCount, "个", "sage"),
                metric("tasks", "进行中任务", publishedTaskCount, "个", "blue")
        );

        return new AdminDashboardDtos.DashboardView(
                metrics,
                statusCounts(generationJobMapper.selectList(Wrappers.<AiGenerationJob>lambdaQuery().ge(AiGenerationJob::getCreatedAt, quarterStart)), AiGenerationJob::getStatus),
                statusCounts(contentAuditMapper.selectList(Wrappers.<ContentAudit>lambdaQuery().ge(ContentAudit::getCreatedAt, quarterStart)), ContentAudit::getStatus),
                statusCounts(enterpriseTaskMapper.selectList(Wrappers.<EnterpriseTask>lambdaQuery().ge(EnterpriseTask::getUpdatedAt, quarterStart)), EnterpriseTask::getStatus),
                statusCounts(stylePackageMapper.selectList(Wrappers.<StylePackage>lambdaQuery().ge(StylePackage::getUpdatedAt, quarterStart)), StylePackage::getStatus),
                dailyCounts(generationJobMapper.selectList(Wrappers.<AiGenerationJob>lambdaQuery().ge(AiGenerationJob::getCreatedAt, weekStart)), AiGenerationJob::getCreatedAt),
                dailyCounts(artworkMapper.selectList(Wrappers.<Artwork>lambdaQuery().ge(Artwork::getCreatedAt, weekStart)), Artwork::getCreatedAt),
                pointFlows(),
                recentActivities());
    }

    private AdminDashboardDtos.Metric metric(String key, String label, long value, String unit, String tone) {
        return new AdminDashboardDtos.Metric(key, label, BigDecimal.valueOf(value), unit, tone);
    }

    private AdminDashboardDtos.Metric metric(String key, String label, BigDecimal value, String unit, String tone) {
        return new AdminDashboardDtos.Metric(key, label, value, unit, tone);
    }

    private BigDecimal successRate(long total, long completed) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private <T> List<AdminDashboardDtos.StatusCount> statusCounts(List<T> items, Function<T, String> statusGetter) {
        return items.stream()
                .collect(Collectors.groupingBy(item -> safeStatus(statusGetter.apply(item)), LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new AdminDashboardDtos.StatusCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(AdminDashboardDtos.StatusCount::status))
                .toList();
    }

    private <T> List<AdminDashboardDtos.DailyCount> dailyCounts(List<T> items, Function<T, LocalDateTime> dateGetter) {
        Map<LocalDate, Long> counts = items.stream()
                .map(dateGetter)
                .filter(date -> date != null)
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));
        List<AdminDashboardDtos.DailyCount> result = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(6);
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            result.add(new AdminDashboardDtos.DailyCount(date, counts.getOrDefault(date, 0L)));
        }
        return result;
    }

    private List<AdminDashboardDtos.PointFlow> pointFlows() {
        List<PointTransaction> transactions = pointTransactionMapper.selectList(Wrappers.<PointTransaction>lambdaQuery()
                .ge(PointTransaction::getCreatedAt, LocalDate.now().minusDays(29).atStartOfDay()));
        Map<String, List<PointTransaction>> groups = transactions.stream()
                .collect(Collectors.groupingBy(transaction -> safeStatus(transaction.getReason()), LinkedHashMap::new, Collectors.toList()));
        return groups.entrySet().stream()
                .map(entry -> {
                    BigDecimal totalIn = entry.getValue().stream()
                            .filter(transaction -> "IN".equals(transaction.getDirection()))
                            .map(PointTransaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalOut = entry.getValue().stream()
                            .filter(transaction -> "OUT".equals(transaction.getDirection()))
                            .map(PointTransaction::getAmount)
                            .map(BigDecimal::abs)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new AdminDashboardDtos.PointFlow(entry.getKey(), totalIn, totalOut, entry.getValue().size());
                })
                .sorted(Comparator.comparing(AdminDashboardDtos.PointFlow::count).reversed())
                .limit(8)
                .toList();
    }

    private List<AdminDashboardDtos.RecentActivity> recentActivities() {
        List<AdminDashboardDtos.RecentActivity> activities = new ArrayList<>();
        LocalDateTime windowStart = LocalDate.now().minusDays(30).atStartOfDay();
        generationJobMapper.selectPage(new Page<>(1, 5), Wrappers.<AiGenerationJob>lambdaQuery()
                        .ge(AiGenerationJob::getCreatedAt, windowStart)
                        .orderByDesc(AiGenerationJob::getCreatedAt))
                .getRecords()
                .forEach(job -> activities.add(new AdminDashboardDtos.RecentActivity("生成", title(job.getPromptText(), "生成任务"), job.getStatus(), job.getCreatedAt())));
        contentAuditMapper.selectPage(new Page<>(1, 5), Wrappers.<ContentAudit>lambdaQuery()
                        .ge(ContentAudit::getCreatedAt, windowStart)
                        .orderByDesc(ContentAudit::getCreatedAt))
                .getRecords()
                .forEach(audit -> activities.add(new AdminDashboardDtos.RecentActivity("审核", audit.getContentType(), audit.getStatus(), audit.getCreatedAt())));
        enterpriseTaskMapper.selectPage(new Page<>(1, 5), Wrappers.<EnterpriseTask>lambdaQuery()
                        .ge(EnterpriseTask::getUpdatedAt, windowStart)
                        .orderByDesc(EnterpriseTask::getUpdatedAt))
                .getRecords()
                .forEach(task -> activities.add(new AdminDashboardDtos.RecentActivity("任务", task.getTitle(), task.getStatus(), task.getUpdatedAt())));
        return activities.stream()
                .sorted(Comparator.comparing(AdminDashboardDtos.RecentActivity::createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(10)
                .toList();
    }

    private String title(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String clean = value.trim();
        return clean.length() > 36 ? clean.substring(0, 36) : clean;
    }

    private String safeStatus(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value;
    }
}
