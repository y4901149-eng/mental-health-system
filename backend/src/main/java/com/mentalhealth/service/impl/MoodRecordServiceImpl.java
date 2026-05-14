package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.MoodRecord;
import com.mentalhealth.mapper.MoodRecordMapper;
import com.mentalhealth.service.MoodRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 情绪记录 Service 实现类
 */
@Service
public class MoodRecordServiceImpl extends ServiceImpl<MoodRecordMapper, MoodRecord> implements MoodRecordService {

    @Autowired
    private MoodRecordMapper moodRecordMapper;

    @Override
    public void recordMood(MoodRecord moodRecord) {
        MoodRecord todayRecord = getTodayRecord(moodRecord.getUserId());
        if (todayRecord != null) {
            todayRecord.setMoodScore(moodRecord.getMoodScore());
            todayRecord.setMoodTag(moodRecord.getMoodTag());
            todayRecord.setNote(moodRecord.getNote());
            updateById(todayRecord);
        } else {
            moodRecord.setRecordDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            save(moodRecord);
        }
    }

    @Override
    public List<Map<String, Object>> getMoodTrend(Long userId, Integer days) {
        return moodRecordMapper.selectMoodTrend(userId, days);
    }

    @Override
    public List<Map<String, Object>> getMoodDistribution(Long userId, Integer days) {
        return moodRecordMapper.selectMoodDistribution(userId, days);
    }

    @Override
    public Map<String, Object> getMoodSummary(Long userId, Integer days) {
        Map<String, Object> summary = moodRecordMapper.selectMoodSummary(userId, days);
        // 补充最常情绪
        Map<String, Object> topMood = moodRecordMapper.selectTopMood(userId, days);
        if (topMood != null) {
            summary.put("topMood", topMood.get("topMood"));
        } else {
            summary.put("topMood", null);
        }
        return summary;
    }

    @Override
    public MoodRecord getTodayRecord(Long userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return getOne(new LambdaQueryWrapper<MoodRecord>()
                .eq(MoodRecord::getUserId, userId)
                .eq(MoodRecord::getRecordDate, today));
    }
}
