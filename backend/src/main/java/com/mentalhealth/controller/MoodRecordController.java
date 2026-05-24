package com.mentalhealth.controller;

import com.mentalhealth.entity.MoodRecord;
import com.mentalhealth.service.AIService;
import com.mentalhealth.service.MoodRecordService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 情绪记录 Controller
 */
@RestController
@RequestMapping("/api/mood")
public class MoodRecordController {

    @Autowired
    private MoodRecordService moodRecordService;

    @Autowired
    private AIService aiService;

    /** 记录一次情绪（保存后自动检测是否触发危机预警） */
    @PostMapping("/record")
    public ResultVO<?> record(@RequestBody MoodRecord moodRecord, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        moodRecord.setUserId(userId);
        moodRecordService.recordMood(moodRecord);
        // 实时检查情绪分数是否触发预警
        aiService.checkMoodOnly(userId);
        return ResultVO.success();
    }

    /** 获取今日情绪统计和记录列表 */
    @GetMapping("/today")
    public ResultVO<Map<String, Object>> today(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(moodRecordService.getTodayOverview(userId));
    }

    /** 获取情绪趋势（支持天数筛选） */
    @GetMapping("/trend")
    public ResultVO<List<Map<String, Object>>> trend(
            @RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(moodRecordService.getMoodTrend(userId, days));
    }

    /** 获取情绪分布 */
    @GetMapping("/distribution")
    public ResultVO<List<Map<String, Object>>> distribution(
            @RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(moodRecordService.getMoodDistribution(userId, days));
    }

    /** 获取情绪统计摘要 */
    @GetMapping("/summary")
    public ResultVO<Map<String, Object>> summary(
            @RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(moodRecordService.getMoodSummary(userId, days));
    }

    /** 获取最近一段时间的情绪记录 */
    @GetMapping("/records")
    public ResultVO<List<MoodRecord>> records(
            @RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(moodRecordService.getMoodRecords(userId, days));
    }
}
