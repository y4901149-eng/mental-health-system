package com.mentalhealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mentalhealth.entity.MoodRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 情绪记录 Mapper 接口
 */
public interface MoodRecordMapper extends BaseMapper<MoodRecord> {

    /** 查询情绪趋势（支持天数筛选） */
    @Select("SELECT record_date, AVG(mood_score) as avgScore " +
            "FROM mood_record WHERE user_id = #{userId} " +
            "AND record_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY record_date ORDER BY record_date ASC")
    List<Map<String, Object>> selectMoodTrend(@Param("userId") Long userId, @Param("days") Integer days);

    /** 查询情绪标签分布 */
    @Select("SELECT mood_tag as name, COUNT(*) as value " +
            "FROM mood_record WHERE user_id = #{userId} " +
            "AND mood_tag IS NOT NULL AND mood_tag != '' " +
            "AND record_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY mood_tag ORDER BY value DESC")
    List<Map<String, Object>> selectMoodDistribution(@Param("userId") Long userId, @Param("days") Integer days);

    /** 查询情绪统计摘要 */
    @Select("SELECT " +
            "  COUNT(*) as totalRecords, " +
            "  ROUND(AVG(mood_score), 1) as avgScore, " +
            "  MAX(mood_score) as maxScore, " +
            "  MIN(mood_score) as minScore, " +
            "  COUNT(DISTINCT record_date) as activeDays " +
            "FROM mood_record WHERE user_id = #{userId} " +
            "AND record_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)")
    Map<String, Object> selectMoodSummary(@Param("userId") Long userId, @Param("days") Integer days);

    /** 查询最常出现的情绪标签 */
    @Select("SELECT mood_tag as topMood, COUNT(*) as cnt " +
            "FROM mood_record WHERE user_id = #{userId} " +
            "AND mood_tag IS NOT NULL AND mood_tag != '' " +
            "AND record_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY mood_tag ORDER BY cnt DESC LIMIT 1")
    Map<String, Object> selectTopMood(@Param("userId") Long userId, @Param("days") Integer days);
}
