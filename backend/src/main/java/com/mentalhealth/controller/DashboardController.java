package com.mentalhealth.controller;

import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/summary")
    public ResultVO<Map<String, Object>> summary(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        // 管理员看到全部数据，普通用户只看自己的
        String userFilter = "admin".equals(role) ? "" : " AND user_id = " + userId;
        String userWhere = "admin".equals(role) ? "" : " WHERE user_id = " + userId;

        Map<String, Object> result = new HashMap<>();

        // 1. 日记总数
        Long diaryCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM diary" + userWhere, Long.class);
        result.put("diaryCount", diaryCount != null ? diaryCount : 0);

        // 2. 周报数量
        Long reportCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM emotion_report" + userWhere, Long.class);
        result.put("reportCount", reportCount != null ? reportCount : 0);

        // 3. AI对话次数
        Long chatCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chat_session" + userWhere, Long.class);
        result.put("chatCount", chatCount != null ? chatCount : 0);

        // 4. 预警记录数量
        Long crisisCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crisis_alert" + userWhere, Long.class);
        result.put("crisisCount", crisisCount != null ? crisisCount : 0);

        // 5. 最近7天情绪趋势
        String trendSql = "SELECT record_date, AVG(mood_score) as avgScore " +
                "FROM mood_record WHERE record_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)" +
                (userFilter.isEmpty() ? "" : " AND user_id = " + userId) +
                " GROUP BY record_date ORDER BY record_date ASC";
        List<Map<String, Object>> trendData = jdbcTemplate.queryForList(trendSql);
        result.put("recentMoodTrend", trendData);

        // 6. 最近5条日记（仅展示内容，情绪数据请查看情绪记录）
        String diarySql = "SELECT id, content, create_time " +
                "FROM diary" + userWhere +
                " ORDER BY create_time DESC LIMIT 5";
        List<Map<String, Object>> recentDiaries = jdbcTemplate.queryForList(diarySql);
        result.put("recentDiaries", recentDiaries);

        // 7. 预警总数（管理员需要待处理数）
        if ("admin".equals(role)) {
            Long pendingCrisis = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM crisis_alert WHERE handle_status = 'PENDING'", Long.class);
            result.put("pendingCrisisCount", pendingCrisis != null ? pendingCrisis : 0);
        }

        return ResultVO.success(result);
    }
}
