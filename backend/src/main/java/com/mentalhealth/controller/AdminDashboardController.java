package com.mentalhealth.controller;

import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/dashboard/summary")
    public ResultVO<Map<String, Object>> summary() {
        Map<String, Object> result = new HashMap<>();

        // 1. 用户总数
        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Long.class);
        result.put("userCount", userCount != null ? userCount : 0);

        // 2. 日记总数
        Long diaryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM diary", Long.class);
        result.put("diaryCount", diaryCount != null ? diaryCount : 0);

        // 3. 周报总数
        Long reportCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM emotion_report", Long.class);
        result.put("reportCount", reportCount != null ? reportCount : 0);

        // 4. 对话总数
        Long chatCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chat_session", Long.class);
        result.put("chatCount", chatCount != null ? chatCount : 0);

        // 5. 预警总数
        Long crisisCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crisis_alert", Long.class);
        result.put("crisisCount", crisisCount != null ? crisisCount : 0);

        // 6. 待处理预警数
        Long pendingCrisis = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crisis_alert WHERE handle_status = 'PENDING'", Long.class);
        result.put("pendingCrisisCount", pendingCrisis != null ? pendingCrisis : 0);

        // 7. 情绪分布（所有用户的 mood_tags 统计）
        List<Map<String, Object>> moodDistribution = jdbcTemplate.queryForList(
                "SELECT mood_tag as name, COUNT(*) as value FROM mood_record " +
                "WHERE mood_tag IS NOT NULL AND mood_tag != '' " +
                "GROUP BY mood_tag ORDER BY value DESC");
        result.put("moodDistribution", moodDistribution);

        // 8. 系统信息
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("backend", "running");
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            systemInfo.put("database", "connected");
        } catch (Exception e) {
            systemInfo.put("database", "disconnected");
        }
        systemInfo.put("ai", "mock");
        result.put("systemInfo", systemInfo);

        return ResultVO.success(result);
    }

    /** 最近异常用户 TOP5（近7天平均分最低的用户） */
    @GetMapping("/dashboard/risk-users")
    public ResultVO<List<Map<String, Object>>> riskUsers() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT u.id, u.username, " +
                "  ROUND(AVG(d.emotion_score), 1) as avgScore, " +
                "  MAX(d.create_time) as lastDiaryTime " +
                "FROM diary d JOIN user u ON d.user_id = u.id " +
                "WHERE d.emotion_score IS NOT NULL " +
                "  AND d.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "GROUP BY u.id, u.username " +
                "ORDER BY avgScore ASC LIMIT 5");
        return ResultVO.success(users);
    }
}
