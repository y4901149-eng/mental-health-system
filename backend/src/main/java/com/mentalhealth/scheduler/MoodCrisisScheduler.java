package com.mentalhealth.scheduler;

import com.mentalhealth.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 情绪危机定时检测
 * 每天凌晨检查所有用户最近7天的情绪分数，持续低分自动创建预警
 */
@Component
public class MoodCrisisScheduler {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AIService aiService;

    /** 每天凌晨 2:00 执行 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkAllUsersMood() {
        System.out.println("🔍 开始定时扫描用户情绪分数...");
        try {
            // 查出最近7天有 mood_record 且平均分 <= 4 的用户
            List<Long> userIds = jdbc.queryForList(
                    "SELECT user_id FROM mood_record WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                    "GROUP BY user_id HAVING AVG(mood_score) <= 4.0",
                    Long.class);
            int count = 0;
            for (Long uid : userIds) {
                aiService.checkMoodOnly(uid);
                count++;
            }
            System.out.println("✅ 定时扫描完成, 检查了 " + count + " 位用户");
        } catch (Exception e) {
            System.err.println("❌ 定时扫描失败: " + e.getMessage());
        }
    }
}
