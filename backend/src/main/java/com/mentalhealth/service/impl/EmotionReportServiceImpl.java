package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.EmotionReport;
import com.mentalhealth.mapper.EmotionReportMapper;
import com.mentalhealth.service.EmotionReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class EmotionReportServiceImpl extends ServiceImpl<EmotionReportMapper, EmotionReport> implements EmotionReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 英文mood_tag -> 中文显示名 */
    private static final Map<String, String> TAG_CN = new HashMap<>();
    static {
        TAG_CN.put("happy", "开心"); TAG_CN.put("calm", "平静");
        TAG_CN.put("anxious", "焦虑"); TAG_CN.put("sad", "难过");
        TAG_CN.put("angry", "愤怒"); TAG_CN.put("tired", "疲惫");
    }

    @Override
    public List<EmotionReport> getUserReports(Long userId) {
        return list(new LambdaQueryWrapper<EmotionReport>()
                .eq(EmotionReport::getUserId, userId)
                .eq(EmotionReport::getReportType, "WEEKLY")
                .orderByDesc(EmotionReport::getPeriodStart));
    }

    @Override
    public EmotionReport getLatestReport(Long userId) {
        List<EmotionReport> list = getUserReports(userId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public EmotionReport generateReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        // 1. 统计 mood_record 记录数量
        Long recordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mood_record WHERE user_id = ? AND create_time >= ?",
                Long.class, userId, weekAgo.atStartOfDay());

        // 2. 平均情绪分（mood_score 1-10 -> 映射到 0-100）
        Double avgScore = jdbcTemplate.queryForObject(
                "SELECT AVG(mood_score) FROM mood_record WHERE user_id = ? AND mood_score IS NOT NULL AND create_time >= ?",
                Double.class, userId, weekAgo.atStartOfDay());

        // 3. 最常见情绪标签（mood_tag 英文->中文）
        List<Map<String, Object>> topMoods = jdbcTemplate.queryForList(
                "SELECT mood_tag, COUNT(*) as cnt FROM mood_record WHERE user_id = ? AND mood_tag IS NOT NULL AND mood_tag != '' AND create_time >= ? GROUP BY mood_tag ORDER BY cnt DESC LIMIT 1",
                userId, weekAgo.atStartOfDay());

        String topMood = "未记录";
        if (!topMoods.isEmpty() && topMoods.get(0).get("mood_tag") != null) {
            String enTag = (String) topMoods.get(0).get("mood_tag");
            topMood = TAG_CN.getOrDefault(enTag, enTag);
        }

        // 4. 每日趋势（mood_score -> ×10 映射到 0-100）
        List<Map<String, Object>> trend = jdbcTemplate.queryForList(
                "SELECT DATE(create_time) as d, ROUND(AVG(mood_score) * 10, 1) as s FROM mood_record WHERE user_id = ? AND mood_score IS NOT NULL AND create_time >= ? GROUP BY DATE(create_time) ORDER BY d",
                userId, weekAgo.atStartOfDay());

        // 5. 生成摘要
        int diaryCnt = recordCount != null ? recordCount.intValue() : 0;
        int avg = avgScore != null ? (int) Math.round(avgScore * 10) : 0;
        String summary = generateSummary(diaryCnt, avg, topMood, trend);

        // 6. 构建 statistics_json
        Map<String, Object> stats = new HashMap<>();
        stats.put("avgScore", avg);
        stats.put("topMood", topMood);
        stats.put("diaryCount", diaryCnt);

        List<String> dates = new ArrayList<>();
        List<Number> scores = new ArrayList<>();
        for (Map<String, Object> t : trend) {
            java.sql.Date d = (java.sql.Date) t.get("d");
            Number s = (Number) t.get("s");
            if (d != null) dates.add(d.toString().substring(5));
            if (s != null) scores.add(s);
        }
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("dates", dates);
        trendData.put("scores", scores);
        stats.put("trend", trendData);

        String statsJson;
        try {
            statsJson = objectMapper.writeValueAsString(stats);
        } catch (JsonProcessingException e) {
            statsJson = "{}";
        }

        // 7. 保存
        EmotionReport report = new EmotionReport();
        report.setUserId(userId);
        report.setReportType("WEEKLY");
        report.setPeriodStart(weekAgo);
        report.setPeriodEnd(today);
        report.setSummary(summary);
        report.setStatisticsJson(statsJson);
        save(report);

        return report;
    }

    private String generateSummary(int diaryCount, int avgScore, String topMood, List<Map<String, Object>> trend) {
        StringBuilder sb = new StringBuilder();
        sb.append("本周");
        if (diaryCount == 0) {
            sb.append("没有记录情绪数据，建议每天花几秒钟记录心情。");
            return sb.toString();
        }
        sb.append("共记录了 ").append(diaryCount).append(" 次情绪记录，");
        sb.append("平均情绪分为 ").append(avgScore).append(" 分，");
        sb.append("最常见的情绪是「").append(topMood).append("」。");

        if (avgScore >= 70) {
            sb.append("整体情绪状态较好，请继续保持！");
        } else if (avgScore >= 45) {
            sb.append("情绪状态总体平稳，偶有波动属于正常现象。");
        } else {
            sb.append("情绪偏低落，建议多与朋友家人沟通，必要时寻求专业帮助。");
        }

        if (trend.size() >= 2) {
            double first = ((Number) trend.get(0).get("s")).doubleValue();
            double last = ((Number) trend.get(trend.size() - 1).get("s")).doubleValue();
            if (last - first > 15) {
                sb.append("周末情绪有明显好转，继续保持！");
            } else if (first - last > 15) {
                sb.append("情绪呈下降趋势，请注意调整状态。");
            } else {
                sb.append("情绪较为稳定。");
            }
        }

        sb.append(" 建议保持规律作息，适当运动，关注自己的心理状态。");
        return sb.toString();
    }
}
