package com.mentalhealth.controller;

import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminManageController {

    @Autowired
    private JdbcTemplate jdbc;

    private Map<String, Object> pageResult(List<Map<String, Object>> records, int total, int page, int size) {
        Map<String, Object> r = new HashMap<>();
        r.put("records", records); r.put("total", total); r.put("page", page); r.put("size", size);
        return r;
    }

    @GetMapping("/diary")
    public ResultVO<Map<String, Object>> diaryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String moodTag,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long userId) {
        int offset = (page - 1) * size;
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (userId != null) where.append(" AND d.user_id = ").append(userId);
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.replace("'", "''");
            where.append(" AND (d.content LIKE '%").append(kw).append("%' OR d.title LIKE '%").append(kw).append("%' OR u.username LIKE '%").append(kw).append("%')");
        }
        if (moodTag != null && !moodTag.isEmpty()) {
            where.append(" AND d.mood_tags = '").append(moodTag.replace("'", "''")).append("'");
        }
        if (minScore != null) where.append(" AND d.emotion_score >= ").append(minScore);
        if (maxScore != null) where.append(" AND d.emotion_score <= ").append(maxScore);
        if (startDate != null && !startDate.isEmpty()) where.append(" AND d.create_time >= '").append(startDate).append("'");
        if (endDate != null && !endDate.isEmpty()) where.append(" AND d.create_time <= '").append(endDate).append("'");

        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT d.id, d.user_id, d.title, d.content, d.mood_tags, d.emotion_score, d.create_time, u.username " +
                "FROM diary d LEFT JOIN user u ON d.user_id = u.id" + where +
                " ORDER BY COALESCE(d.emotion_score, 100) ASC, d.create_time DESC LIMIT " + size + " OFFSET " + offset);
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM diary d LEFT JOIN user u ON d.user_id = u.id" + where, Integer.class);

        // 统计数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayCount", jdbc.queryForObject(
                "SELECT COUNT(*) FROM diary WHERE DATE(create_time) = CURDATE()", Integer.class));
        stats.put("highRiskCount", jdbc.queryForObject(
                "SELECT COUNT(*) FROM diary WHERE emotion_score < 30", Integer.class));
        stats.put("avgScore7d", jdbc.queryForObject(
                "SELECT COALESCE(ROUND(AVG(emotion_score),1),0) FROM diary WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) AND emotion_score IS NOT NULL", Double.class));
        stats.put("lowScoreCount", jdbc.queryForObject(
                "SELECT COUNT(*) FROM diary WHERE emotion_score < 20", Integer.class));

        Map<String, Object> result = pageResult(list, total, page, size);
        result.put("stats", stats);
        return ResultVO.success(result);
    }

    @DeleteMapping("/diary/{id}")
    public ResultVO<?> deleteDiary(@PathVariable Long id) {
        jdbc.update("DELETE FROM diary WHERE id = ?", id);
        return ResultVO.success();
    }

    @GetMapping("/mood")
    public ResultVO<Map<String, Object>> moodList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long userId) {
        int offset = (page - 1) * size;
        String where = "";
        if (userId != null) where += " AND m.user_id = " + userId;
        if (startDate != null && !startDate.isEmpty()) where += " AND m.record_date >= '" + startDate + "'";
        if (endDate != null && !endDate.isEmpty()) where += " AND m.record_date <= '" + endDate + "'";
        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT m.id, m.user_id, m.mood_tag, m.mood_score, m.record_date, m.note, u.username " +
                "FROM mood_record m LEFT JOIN user u ON m.user_id = u.id WHERE 1=1" + where +
                " ORDER BY m.record_date DESC LIMIT " + size + " OFFSET " + offset);
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM mood_record m WHERE 1=1" + where, Integer.class);
        return ResultVO.success(pageResult(list, total, page, size));
    }

    @DeleteMapping("/mood/{id}")
    public ResultVO<?> deleteMood(@PathVariable Long id) {
        jdbc.update("DELETE FROM mood_record WHERE id = ?", id);
        return ResultVO.success();
    }

    @GetMapping("/report")
    public ResultVO<Map<String, Object>> reportList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) Long userId) {
        int offset = (page - 1) * size;
        String where = "";
        if (userId != null) where = " WHERE r.user_id = " + userId;
        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT r.id, r.user_id, r.period_start, r.period_end, r.summary, r.statistics_json, r.create_time, u.username " +
                "FROM emotion_report r LEFT JOIN user u ON r.user_id = u.id" + where +
                " ORDER BY r.create_time DESC LIMIT " + size + " OFFSET " + offset);
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM emotion_report r" + where, Integer.class);
        return ResultVO.success(pageResult(list, total, page, size));
    }

    @DeleteMapping("/report/{id}")
    public ResultVO<?> deleteReport(@PathVariable Long id) {
        jdbc.update("DELETE FROM emotion_report WHERE id = ?", id);
        return ResultVO.success();
    }

    // ===== 对话管理 - 风险消息聚合 =====
    private static final String[] RISK_KEYWORDS = {"自杀","想死","不想活","活不下去","结束生命","崩溃","绝望","伤害自己","自残","活着的意义","没有意义","好累","撑不下去"};

    @GetMapping("/chat/risk")
    public ResultVO<Map<String, Object>> chatRiskList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String riskKeyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long userId) {
        int offset = (page - 1) * size;

        // 构建风险关键词 WHERE
        StringBuilder riskWhere = new StringBuilder();
        for (String kw : RISK_KEYWORDS) {
            if (riskWhere.length() > 0) riskWhere.append(" OR ");
            riskWhere.append("cm.content LIKE '%").append(kw).append("%'");
        }
        String riskCondition = "(" + riskWhere + ")";

        StringBuilder where = new StringBuilder(" WHERE ").append(riskCondition);
        if (userId != null) where.append(" AND s.user_id = ").append(userId);
        if (keyword != null && !keyword.isEmpty()) {
            where.append(" AND u.username LIKE '%").append(keyword.replace("'", "''")).append("%'");
        }
        if (riskKeyword != null && !riskKeyword.isEmpty()) {
            where.append(" AND cm.content LIKE '%").append(riskKeyword.replace("'", "''")).append("%'");
        }
        if (startDate != null && !startDate.isEmpty()) where.append(" AND cm.create_time >= '").append(startDate).append("'");
        if (endDate != null && !endDate.isEmpty()) where.append(" AND cm.create_time <= '").append(endDate).append("'");

        // 提取风险关键词（用于显示）
        String kwExtract = "'" + String.join("','", RISK_KEYWORDS) + "'";

        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT cm.id, cm.session_id, cm.content, cm.sender_role, cm.create_time, u.username, s.title as session_title " +
                "FROM chat_message cm " +
                "JOIN chat_session s ON cm.session_id = s.id " +
                "JOIN user u ON s.user_id = u.id " +
                where + " ORDER BY cm.create_time DESC LIMIT " + size + " OFFSET " + offset);
        int total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM chat_message cm " +
                "JOIN chat_session s ON cm.session_id = s.id " +
                "JOIN user u ON s.user_id = u.id " + where, Integer.class);

        // 统计数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", jdbc.queryForObject("SELECT COUNT(*) FROM chat_message", Integer.class));
        stats.put("riskMessageCount", total);
        stats.put("involvedUsers", jdbc.queryForObject(
                "SELECT COUNT(DISTINCT s.user_id) FROM chat_message cm JOIN chat_session s ON cm.session_id = s.id WHERE " + riskCondition, Integer.class));
        stats.put("todayRiskCount", jdbc.queryForObject(
                "SELECT COUNT(*) FROM chat_message cm WHERE DATE(cm.create_time) = CURDATE() AND " + riskCondition, Integer.class));

        Map<String, Object> result = pageResult(list, total, page, size);
        result.put("stats", stats);
        return ResultVO.success(result);
    }

    @GetMapping("/chat")
    public ResultVO<Map<String, Object>> chatList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) Long userId) {
        int offset = (page - 1) * size;
        String where = "";
        if (userId != null) where = " WHERE s.user_id = " + userId;
        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT s.id, s.user_id, s.title, s.status, s.create_time, s.update_time, u.username, " +
                "(SELECT content FROM chat_message WHERE session_id = s.id ORDER BY create_time DESC LIMIT 1) as last_message " +
                "FROM chat_session s LEFT JOIN user u ON s.user_id = u.id" + where +
                " ORDER BY s.update_time DESC LIMIT " + size + " OFFSET " + offset);
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM chat_session s" + where, Integer.class);
        return ResultVO.success(pageResult(list, total, page, size));
    }

    @DeleteMapping("/chat/message/{id}")
    public ResultVO<?> deleteChatMessage(@PathVariable Long id) {
        jdbc.update("DELETE FROM chat_message WHERE id = ?", id);
        return ResultVO.success();
    }

    @DeleteMapping("/chat/{id}")
    public ResultVO<?> deleteChat(@PathVariable Long id) {
        jdbc.update("DELETE FROM chat_message WHERE session_id = ?", id);
        jdbc.update("DELETE FROM chat_session WHERE id = ?", id);
        return ResultVO.success();
    }

    // ===== 预约管理 =====
    @GetMapping("/appointment")
    public ResultVO<Map<String, Object>> appointmentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        int offset = (page - 1) * size;
        String where = " WHERE 1=1";
        if (userId != null) where += " AND a.user_id = " + userId;
        if (status != null && !status.isEmpty()) {
            where += " AND a.status = '" + status.replace("'", "''") + "'";
        }
        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT a.id, a.user_id, a.counselor_name, a.appointment_date, a.time_slot, a.type, a.remark, a.status, a.create_time, u.username " +
                "FROM appointment a LEFT JOIN user u ON a.user_id = u.id" + where +
                " ORDER BY a.create_time DESC LIMIT " + size + " OFFSET " + offset);
        String countWhere = where.replace("a.", "");
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM appointment a" + where, Integer.class);
        return ResultVO.success(pageResult(list, total, page, size));
    }

    @PutMapping("/appointment/{id}/status")
    public ResultVO<?> updateAppointmentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null) return ResultVO.badRequest("状态不能为空");
        jdbc.update("UPDATE appointment SET status = ? WHERE id = ?", status, id);
        return ResultVO.success();
    }

    @DeleteMapping("/appointment/{id}")
    public ResultVO<?> deleteAppointment(@PathVariable Long id) {
        jdbc.update("DELETE FROM appointment WHERE id = ?", id);
        return ResultVO.success();
    }

    @GetMapping("/chat/{id}/messages")
    public ResultVO<List<Map<String, Object>>> chatMessages(@PathVariable Long id) {
        List<Map<String, Object>> msgs = jdbc.queryForList(
                "SELECT cm.id, cm.sender_role, cm.content, cm.create_time " +
                "FROM chat_message cm WHERE cm.session_id = ? ORDER BY cm.create_time ASC", id);
        return ResultVO.success(msgs);
    }
}
