package com.mentalhealth.controller;

import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/dashboard/summary")
    public ResultVO<Map<String, Object>> summary() {
        Map<String, Object> result = new HashMap<>();

        // 1. 总用户数
        result.put("userCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Long.class));

        // 2. 今日活跃用户（今天有日记/情绪记录/AI对话的去重用户）
        result.put("todayActive", jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT user_id) FROM ( " +
                "  SELECT user_id FROM diary WHERE DATE(create_time) = CURDATE() " +
                "  UNION SELECT user_id FROM chat_session WHERE DATE(create_time) = CURDATE() " +
                "  UNION SELECT user_id FROM mood_record WHERE DATE(create_time) = CURDATE() " +
                ") t", Long.class));

        // 3. 高风险用户（存在 alertLevel >= 4 的去重用户）
        result.put("highRiskUsers", jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT user_id) FROM crisis_alert WHERE alert_level >= 4", Long.class));

        // 4. 今日 AI 对话消息数
        result.put("todayChatCount", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chat_message WHERE DATE(create_time) = CURDATE()", Long.class));

        // 5. 今日新增日记
        result.put("todayDiaryCount", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM diary WHERE DATE(create_time) = CURDATE()", Long.class));

        // 6. 待处理预警
        result.put("pendingCrisisCount", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crisis_alert WHERE handle_status = 'PENDING'", Long.class));

        // 7. 近24小时预警触发次数
        result.put("crisis24h", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crisis_alert WHERE create_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)", Long.class));

        // 8. 情绪分布（将英文标签映射为中文）
        result.put("moodDistribution", jdbcTemplate.queryForList(
                "SELECT CASE mood_tag " +
                "  WHEN 'happy' THEN '开心' WHEN 'calm' THEN '平静' " +
                "  WHEN 'anxious' THEN '焦虑' WHEN 'sad' THEN '难过' " +
                "  WHEN 'angry' THEN '愤怒' WHEN 'tired' THEN '疲惫' " +
                "  ELSE mood_tag END as name, COUNT(*) as value " +
                "FROM mood_record " +
                "WHERE mood_tag IS NOT NULL AND mood_tag != '' " +
                "GROUP BY mood_tag ORDER BY value DESC"));

        // 9. 系统信息
        Map<String, Object> sys = new HashMap<>();
        sys.put("backend", "running");
        try { jdbcTemplate.queryForObject("SELECT 1", Integer.class); sys.put("database", "connected"); }
        catch (Exception e) { sys.put("database", "disconnected"); }
        sys.put("ai", "mock");
        result.put("systemInfo", sys);

        return ResultVO.success(result);
    }

    /** 近7天情绪趋势（折线图数据） */
    @GetMapping("/dashboard/trend")
    public ResultVO<Map<String, Object>> trend() {
        Map<String, Object> result = new HashMap<>();

        // 生成近7天日期标签
        LocalDate today = LocalDate.now();
        List<String> dates = new ArrayList<>();
        List<String> fullDates = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            dates.add(d.format(DateTimeFormatter.ofPattern("MM-dd")));
            fullDates.add(d.toString());
        }

        // 查询近7天 mood_record 平均情绪分（×10映射到0-100分）
        List<Map<String, Object>> moodRows = jdbcTemplate.queryForList(
                "SELECT record_date, ROUND(AVG(mood_score) * 10, 1) as score " +
                "FROM mood_record " +
                "WHERE record_date >= ? AND record_date <= ? " +
                "GROUP BY record_date ORDER BY record_date",
                fullDates.get(0), fullDates.get(6));

        Map<String, Object> moodMap = new HashMap<>();
        for (Map<String, Object> row : moodRows) {
            moodMap.put((String) row.get("record_date"), row.get("score"));
        }
        List<Object> avgScores = new ArrayList<>();
        for (String fd : fullDates) {
            avgScores.add(moodMap.getOrDefault(fd, 0));
        }

        // 查询近7天 crisis_alert 高风险人数（alert_level >= 4）
        List<Map<String, Object>> crisisRows = jdbcTemplate.queryForList(
                "SELECT DATE(create_time) as day, COUNT(DISTINCT user_id) as cnt " +
                "FROM crisis_alert " +
                "WHERE alert_level >= 4 " +
                "  AND create_time >= ? " +
                "  AND create_time < DATE_ADD(?, INTERVAL 1 DAY) " +
                "GROUP BY DATE(create_time) ORDER BY day",
                fullDates.get(0) + " 00:00:00",
                fullDates.get(6) + " 00:00:00");

        Map<String, Object> crisisMap = new HashMap<>();
        for (Map<String, Object> row : crisisRows) {
            String dayStr = row.get("day").toString();
            crisisMap.put(dayStr.substring(0, 10), row.get("cnt"));
        }
        List<Object> highRiskCounts = new ArrayList<>();
        for (String fd : fullDates) {
            highRiskCounts.add(crisisMap.getOrDefault(fd, 0));
        }

        result.put("dates", dates);
        result.put("avgScores", avgScores);
        result.put("highRiskCounts", highRiskCounts);
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
