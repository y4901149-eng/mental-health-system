package com.mentalhealth.controller;

import com.mentalhealth.entity.MoodRecord;
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

    /** 记录今日情绪 */
    @PostMapping("/record")
    public ResultVO<?> record(@RequestBody MoodRecord moodRecord, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        moodRecord.setUserId(userId);
        moodRecordService.recordMood(moodRecord);
        return ResultVO.success();
    }

    /** 获取今日情绪记录 */
    @GetMapping("/today")
    public ResultVO<MoodRecord> today(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(moodRecordService.getTodayRecord(userId));
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
}
