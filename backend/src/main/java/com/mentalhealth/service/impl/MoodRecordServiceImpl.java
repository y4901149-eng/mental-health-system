package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.MoodRecord;
import com.mentalhealth.mapper.MoodRecordMapper;
import com.mentalhealth.service.MoodRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 情绪记录 Service 实现类
 */
@Service
public class MoodRecordServiceImpl extends ServiceImpl<MoodRecordMapper, MoodRecord> implements MoodRecordService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int DEFAULT_DAYS = 30;
    private static final int MAX_DAYS = 365;

    @Autowired
    private MoodRecordMapper moodRecordMapper;

    @Override
    public void recordMood(MoodRecord moodRecord) {
        moodRecord.setId(null);
        moodRecord.setRecordDate(LocalDate.now().format(DATE_FORMATTER));
        moodRecord.setCreateTime(LocalDateTime.now());
        save(moodRecord);
    }

    @Override
    public List<Map<String, Object>> getMoodTrend(Long userId, Integer days) {
        List<Map<String, Object>> data = moodRecordMapper.selectMoodTrend(userId, getSqlIntervalDays(days));
        return data == null ? new ArrayList<>() : data;
    }

    @Override
    public List<Map<String, Object>> getMoodDistribution(Long userId, Integer days) {
        List<Map<String, Object>> data = moodRecordMapper.selectMoodDistribution(userId, getSqlIntervalDays(days));
        return data == null ? new ArrayList<>() : data;
    }

    @Override
    public Map<String, Object> getMoodSummary(Long userId, Integer days) {
        Map<String, Object> source = moodRecordMapper.selectMoodSummary(userId, getSqlIntervalDays(days));
        Map<String, Object> summary = new LinkedHashMap<>();
        if (source != null) {
            summary.putAll(source);
        }

        summary.putIfAbsent("totalRecords", 0);
        summary.putIfAbsent("avgScore", null);
        summary.putIfAbsent("maxScore", null);
        summary.putIfAbsent("minScore", null);
        summary.putIfAbsent("activeDays", 0);

        if (summary.get("totalRecords") == null) {
            summary.put("totalRecords", 0);
        }
        if (summary.get("activeDays") == null) {
            summary.put("activeDays", 0);
        }

        Map<String, Object> topMood = moodRecordMapper.selectTopMood(userId, getSqlIntervalDays(days));
        summary.put("topMood", topMood == null ? null : topMood.get("topMood"));
        return summary;
    }

    @Override
    public Map<String, Object> getTodayOverview(Long userId) {
        List<MoodRecord> records = getTodayRecords(userId);
        MoodRecord latestRecord = records.isEmpty() ? null : records.get(0);

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalRecords", records.size());
        overview.put("avgScore", calculateAverageScore(records));
        overview.put("latestRecord", latestRecord);
        overview.put("records", records);
        return overview;
    }

    @Override
    public List<MoodRecord> getMoodRecords(Long userId, Integer days) {
        int normalizedDays = normalizeDays(days);
        LocalDate today = LocalDate.now();
        String startDate = today.minusDays(normalizedDays - 1L).format(DATE_FORMATTER);
        String endDate = today.format(DATE_FORMATTER);

        return list(new LambdaQueryWrapper<MoodRecord>()
                .eq(MoodRecord::getUserId, userId)
                .ge(MoodRecord::getRecordDate, startDate)
                .le(MoodRecord::getRecordDate, endDate)
                .orderByDesc(MoodRecord::getRecordDate)
                .orderByDesc(MoodRecord::getCreateTime)
                .orderByDesc(MoodRecord::getId));
    }

    private List<MoodRecord> getTodayRecords(Long userId) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return list(new LambdaQueryWrapper<MoodRecord>()
                .eq(MoodRecord::getUserId, userId)
                .eq(MoodRecord::getRecordDate, today)
                .orderByDesc(MoodRecord::getCreateTime)
                .orderByDesc(MoodRecord::getId));
    }

    private Double calculateAverageScore(List<MoodRecord> records) {
        int count = 0;
        int total = 0;
        for (MoodRecord record : records) {
            if (record.getMoodScore() != null) {
                total += record.getMoodScore();
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return BigDecimal.valueOf((double) total / count)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private int getSqlIntervalDays(Integer days) {
        return normalizeDays(days) - 1;
    }

    private int normalizeDays(Integer days) {
        if (days == null || days < 1) {
            return DEFAULT_DAYS;
        }
        return Math.min(days, MAX_DAYS);
    }
}
