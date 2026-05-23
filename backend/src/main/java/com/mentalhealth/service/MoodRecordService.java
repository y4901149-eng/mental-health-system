package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.MoodRecord;

import java.util.List;
import java.util.Map;

/**
 * 情绪记录 Service 接口
 */
public interface MoodRecordService extends IService<MoodRecord> {

    /** 新增一条情绪记录 */
    void recordMood(MoodRecord moodRecord);

    /** 获取情绪趋势 */
    List<Map<String, Object>> getMoodTrend(Long userId, Integer days);

    /** 获取情绪分布 */
    List<Map<String, Object>> getMoodDistribution(Long userId, Integer days);

    /** 获取情绪统计摘要 */
    Map<String, Object> getMoodSummary(Long userId, Integer days);

    /** 获取今日情绪统计和记录列表 */
    Map<String, Object> getTodayOverview(Long userId);

    /** 获取最近一段时间的情绪记录 */
    List<MoodRecord> getMoodRecords(Long userId, Integer days);
}
