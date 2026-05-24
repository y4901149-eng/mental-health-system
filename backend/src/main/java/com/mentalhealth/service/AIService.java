package com.mentalhealth.service;

import com.mentalhealth.config.AIConfig;
import com.mentalhealth.entity.CrisisAlert;
import com.mentalhealth.entity.CrisisKeyword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIService {

    @Autowired
    private AIConfig aiConfig;

    @Autowired(required = false)
    private CrisisAlertService crisisAlertService;

    @Autowired(required = false)
    private CrisisKeywordService crisisKeywordService;

    @Autowired
    private JdbcTemplate jdbc;

    private final RestTemplate restTemplate = new RestTemplate();

    // 按等级分类的关键词缓存
    private volatile List<String> dbKeywords = new ArrayList<>();
    private volatile List<String> highKeywords = new ArrayList<>();
    private volatile List<String> midKeywords = new ArrayList<>();
    private volatile List<String> lowKeywords = new ArrayList<>();

    private static final List<String> FALLBACK_HIGH = Arrays.asList("自杀", "想死", "活不下去", "结束生命", "自残", "轻生", "绝望", "没有活下去的意义");
    private static final List<String> FALLBACK_MID  = Arrays.asList("崩溃", "撑不下去", "太痛苦", "快疯了", "受不了了", "需要帮助");
    private static final List<String> FALLBACK_LOW  = Arrays.asList("没有意义", "没意思", "身体不适");

    @PostConstruct
    public void initKeywords() {
        reloadKeywords();
    }

    /** 重新加载关键词 */
    public void reloadKeywords() {
        try {
            if (crisisKeywordService != null) {
                Map<String, List<CrisisKeyword>> grouped = crisisKeywordService.getEnabledKeywordsGrouped();
                highKeywords = grouped.getOrDefault("high", Collections.emptyList()).stream().map(CrisisKeyword::getKeyword).collect(Collectors.toList());
                midKeywords  = grouped.getOrDefault("mid",  Collections.emptyList()).stream().map(CrisisKeyword::getKeyword).collect(Collectors.toList());
                lowKeywords  = grouped.getOrDefault("low",  Collections.emptyList()).stream().map(CrisisKeyword::getKeyword).collect(Collectors.toList());
                dbKeywords = new ArrayList<>();
                dbKeywords.addAll(highKeywords); dbKeywords.addAll(midKeywords); dbKeywords.addAll(lowKeywords);
                System.out.println("已从数据库加载 " + dbKeywords.size() + " 个危机关键词");
            }
        } catch (Exception e) {
            System.err.println("加载关键词失败: " + e.getMessage());
        }
    }

    /** 检测文本中的关键词等级，返回 (matchedKeyword, level) */
    public Map.Entry<String, Integer> detectLevel(String text) {
        if (text == null) return new AbstractMap.SimpleEntry<>(null, 0);
        List<String> h = highKeywords.isEmpty() ? FALLBACK_HIGH : highKeywords;
        List<String> m = midKeywords.isEmpty()  ? FALLBACK_MID  : midKeywords;
        List<String> l = lowKeywords.isEmpty()  ? FALLBACK_LOW  : lowKeywords;
        for (String kw : h) { if (text.contains(kw)) return new AbstractMap.SimpleEntry<>(kw, 5); }
        for (String kw : m) { if (text.contains(kw)) return new AbstractMap.SimpleEntry<>(kw, 3); }
        for (String kw : l) { if (text.contains(kw)) return new AbstractMap.SimpleEntry<>(kw, 2); }
        return new AbstractMap.SimpleEntry<>(null, 0);
    }

    /** 系统提示词 */
    private static final String SYSTEM_PROMPT =
            "你是一个心理健康陪伴助手，名叫「心聆」。\n\n" +
            "你的性格特点：\n" +
            "- 温柔、耐心、有共情力\n" +
            "- 善于倾听\n" +
            "- 使用温暖鼓励的语气\n\n" +
            "回复原则：\n" +
            "1. 先共情，再引导\n" +
            "2. 不要做医学诊断\n" +
            "3. 不要说「你需要看医生」之类的话\n" +
            "4. 如果用户表示有自杀、自伤、轻生念头，在回复末尾加上【危机预警：建议拨打心理援助热线 400-161-9995】\n" +
            "5. 回复控制在 100-200 字\n" +
            "6. 使用中文回复\n" +
            "7. 每次回复后可以引导用户多说一点";

    /** 获取 AI 回复（主入口） */
    public String getReply(String userMessage, Long userId) {
        // 1. 检测危机关键词 → 自动创建预警
        checkCrisis(userMessage, userId);

        // 2. 调用 AI API
        if (aiConfig.isEnabled()) {
            try {
                String reply = callDeepSeek(userMessage);
                // 回复中也检测是否需要追加危机提示
                if (detectLevel(userMessage).getValue() > 0) {
                    reply += "\n\n【危机预警：建议拨打心理援助热线 400-161-9995】";
                }
                return reply;
            } catch (Exception e) {
                System.err.println("AI 调用失败，回退到 mock: " + e.getMessage());
            }
        }

        // 3. Fallback 到 mock
        return fallbackReply(userMessage);
    }

    /** 调用 DeepSeek API（兼容 OpenAI 格式） */
    private String callDeepSeek(String userMessage) {
        String url = aiConfig.getApiUrl() + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiConfig.getApiKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());
        requestBody.put("temperature", aiConfig.getTemperature());

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>(); sysMsg.put("role", "system"); sysMsg.put("content", SYSTEM_PROMPT); messages.add(sysMsg);
        Map<String, String> userMsg = new HashMap<>(); userMsg.put("role", "user"); userMsg.put("content", userMessage); messages.add(userMsg);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null && message.containsKey("content")) {
                    return (String) message.get("content");
                }
            }
        }

        throw new RuntimeException("AI 返回格式异常");
    }

    /** Mock fallback 回复 */
    private String fallbackReply(String content) {
        String lower = content.toLowerCase();
        for (Map.Entry<String, String[]> entry : MOCK_RESPONSES.entrySet()) {
            String[] keywords = entry.getKey().split("\\|");
            for (String kw : keywords) {
                if (lower.contains(kw)) {
                    String[] replies = entry.getValue();
                    return replies[new Random().nextInt(replies.length)];
                }
            }
        }
        return FALLBACK_REPLIES[new Random().nextInt(FALLBACK_REPLIES.length)];
    }

    /**
     * 三级流水线：关键词 → AI语义 → 情绪分数
     * 返回最终等级，并创建预警（如果 > 0）
     */
    public void checkContent(String content, Long userId, String source) {
        if (content == null || userId == null) return;

        // ── 第1级：关键词匹配 ──
        Map.Entry<String, Integer> kwResult = detectLevel(content);
        String kwWord = kwResult.getKey();
        int kwLevel = kwResult.getValue();

        // ── 第2级：AI 语义分析（仅当关键词未达5级时执行） ──
        int aiLevel = 0;
        String aiReason = "";
        if (kwLevel >= 5) {
            aiReason = "已跳过(高危关键词直接触发)";
        } else {
            boolean aiSuccess = false;
            try {
                String aiResp = callAIForRiskLevel(content);
                java.util.regex.Matcher aiMatcher = java.util.regex.Pattern.compile("\\d").matcher(aiResp);
                if (aiMatcher.find()) {
                    aiLevel = Integer.parseInt(aiMatcher.group());
                }
                aiLevel = Math.max(0, Math.min(5, aiLevel));
                aiSuccess = true;
            } catch (Exception e) {
                aiLevel = fallbackSentimentLevel(content);
                aiReason = "AI接口未启用,已使用本地规则分析";
            }
            if (aiSuccess) {
                if (aiLevel >= 4) aiReason = "AI识别为高危";
                else if (aiLevel >= 3) aiReason = "AI识别为中危";
                else if (aiLevel >= 2) aiReason = "AI识别为需关注";
                else if (aiLevel >= 1) aiReason = "AI识别为轻微";
                else aiReason = "AI识别为安全";
            }
        }

        // ── 第3级：情绪分数 ──
        int moodLevel = 0;
        double avgScore = 0;
        try {
            avgScore = jdbc.queryForObject(
                    "SELECT COALESCE(AVG(mood_score), 0) FROM mood_record WHERE user_id = ? AND create_time >= ? AND mood_score IS NOT NULL",
                    Double.class, userId, LocalDateTime.now().minusDays(7));
            if (avgScore <= 2.5 && avgScore > 0) moodLevel = 4;
            else if (avgScore <= 4.0 && avgScore > 0) moodLevel = 2;
        } catch (Exception ignored) {}

        // ── 最终等级 = 三者最高，归一化 ──
        int finalLevel = normalizeLevel(Math.max(Math.max(kwLevel, aiLevel), moodLevel));

        // ── 频率检测：24h同类触发升级 ──
        long recentCount = 0;
        if (finalLevel > 0) {
            try {
                recentCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM crisis_alert WHERE user_id = ? AND alert_level >= ? AND create_time >= ?",
                        Long.class, userId, finalLevel, LocalDateTime.now().minusHours(24));
            } catch (Exception ignored) {}
            if (recentCount > 0) {
                int upgraded = normalizeLevel(Math.min(finalLevel + 1, 5));
                if (upgraded > finalLevel) {
                    finalLevel = upgraded;
                }
            }
        }

        // ── 最终等级 > 0 才创建预警 ──
        if (finalLevel > 0) {
            try {
                CrisisAlert alert = new CrisisAlert();
                alert.setUserId(userId);
                alert.setAlertLevel(finalLevel);
                alert.setHandleStatus("PENDING");

                // 构建判定原因
                StringBuilder reason = new StringBuilder(source).append("触发预警");
                if (kwLevel > 0) reason.append(" | 关键词:").append(kwWord).append("(").append(kwLevel).append("级)");
                else reason.append(" | 关键词:未命中");
                reason.append(" | AI分析:").append(aiReason).append("(").append(aiLevel).append("级)");
                reason.append(" | 情绪分:").append(String.format("%.1f", avgScore)).append("(").append(moodLevel).append("级)");
                reason.append(" | 最终:").append(finalLevel).append("级");
                if (recentCount > 0) reason.append(" | 近24h同类触发").append(recentCount).append("次,已升级");
                reason.append(" | 原文:").append(content.substring(0, Math.min(content.length(), 60)));

                alert.setTriggerReason(reason.toString());
                crisisAlertService.save(alert);
            } catch (Exception e) {
                System.err.println("创建预警失败: " + e.getMessage());
            }
        }
    }

    /** 调用 AI 做风险等级判断（0=安全 ~ 5=极高危） */
    private String callAIForRiskLevel(String content) {
        if (!aiConfig.isEnabled()) {
            throw new RuntimeException("AI not enabled");
        }
        String url = aiConfig.getApiUrl() + "/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiConfig.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiConfig.getModel());
        body.put("max_tokens", 10);
        body.put("temperature", 0.1);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", "你是一个心理风险分析助手。请分析用户文本的心理风险等级，只返回一个数字 0-5。0=安全,1=轻微,2=关注,3=中危,4=高危,5=极高危。仅返回数字，不要任何其他文字。");
        messages.add(sysMsg);
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", content);
        messages.add(userMsg);
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (resp.getBody() != null && resp.getBody().containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.getBody().get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null && message.containsKey("content")) {
                    return ((String) message.get("content")).trim();
                }
            }
        }
        throw new RuntimeException("AI 返回异常");
    }

    /** AI 调用失败时的情绪倾向分析（仅情感词，不含正式危机词） */
    private int fallbackSentimentLevel(String content) {
        if (content == null) return 0;
        // 重度负面情绪
        String[] strong = {"恨", "不行了", "痛苦", "煎熬", "难受死了"};
        for (String s : strong) { if (content.contains(s)) return 3; }
        // 中度负面情绪
        String[] mid = {"难过", "伤心", "害怕", "担心", "焦虑", "压抑", "烦"};
        for (String s : mid) { if (content.contains(s)) return 2; }
        // 轻度负面情绪
        String[] low = {"累", "疲惫", "没劲", "无聊", "迷茫"};
        for (String s : low) { if (content.contains(s)) return 1; }
        return 0;
    }

    /** AI 对话检测 */
    private void checkCrisis(String content, Long userId) {
        checkContent(content, userId, "AI对话");
    }

    /** 日记检测 */
    public void checkDiaryCrisis(String content, Long userId) {
        checkContent(content, userId, "日记");
    }

    /** 风险等级归一化：4→5(高危), 1→2(关注), 其他不变 */
    private static int normalizeLevel(int level) {
        if (level >= 4) return 5;
        if (level >= 3) return 3;
        if (level >= 1) return 2;
        return 0;
    }

    /** 纯情绪分数检测（由 MoodRecordController 和定时任务调用） */
    public void checkMoodOnly(Long userId) {
        if (userId == null) return;
        try {
            // 1. 最近7天平均分
            Double avgScore = jdbc.queryForObject(
                    "SELECT COALESCE(AVG(mood_score), 0) FROM mood_record WHERE user_id = ? AND create_time >= ? AND mood_score IS NOT NULL",
                    Double.class, userId, java.time.LocalDateTime.now().minusDays(7));
            if (avgScore == null || avgScore <= 0) return;

            // 2. 初始等级
            int moodLevel;
            if (avgScore <= 2.5) moodLevel = 4;
            else if (avgScore <= 3.5) moodLevel = 3;
            else if (avgScore <= 4.0) moodLevel = 2;
            else return; // 正常范围，不触发

            // 3. 连续低分天数（score <= 4）
            Integer lowDays = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM (SELECT DISTINCT DATE(create_time) as d FROM mood_record WHERE user_id = ? AND mood_score <= 4 AND create_time >= ? ORDER BY d DESC) t",
                    Integer.class, userId, java.time.LocalDateTime.now().minusDays(7));
            if (lowDays == null) lowDays = 0;

            // 4. 升级：连续低分 >= 3 天 → +1（归一化后比较）
            if (lowDays >= 3) {
                int upgraded = normalizeLevel(Math.min(moodLevel + 1, 5));
                if (upgraded > moodLevel) moodLevel = upgraded;
            }

            // 5. 检查是否已有同类型未处理预警
            Long existing = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM crisis_alert WHERE user_id = ? AND alert_level >= ? AND handle_status = 'PENDING' AND trigger_reason LIKE '%情绪分触发%'",
                    Long.class, userId, moodLevel);
            if (existing > 0) return; // 已有同类预警不重复创建

            // 6. 创建预警
            CrisisAlert alert = new CrisisAlert();
            alert.setUserId(userId);
            alert.setAlertLevel(moodLevel);
            alert.setHandleStatus("PENDING");
            alert.setTriggerReason("情绪分触发预警 | 关键词:未命中 | AI分析:-(-) | 情绪分:"
                    + String.format("%.1f", avgScore) + "(" + moodLevel + "级)"
                    + " | 连续低分:" + lowDays + "天 | 最终:" + moodLevel + "级");
            crisisAlertService.save(alert);
        } catch (Exception e) {
            System.err.println("情绪分数检测失败: " + e.getMessage());
        }
    }

    // ===== Mock 回复数据（fallback 用） =====
    private static final Map<String, String[]> MOCK_RESPONSES = new LinkedHashMap<>();
    static {
        MOCK_RESPONSES.put("焦虑|紧张|害怕|担心|不安", new String[]{
                "我感受到你现在有些不安。可以告诉我具体是什么让你感到焦虑吗？",
                "焦虑是身体在提醒我们有些事需要被关注。你愿意多说说吗？",
                "这种感觉确实不好受，但我在这里陪着你。试着深呼吸一下，慢慢来。"
        });
        MOCK_RESPONSES.put("难过|悲伤|伤心|失落|哭泣|哭", new String[]{
                "我听到你的悲伤了，这种情绪是正常的，允许自己难过。",
                "难过的时候，不需要强迫自己马上好起来。我在这里倾听你。",
                "你的感受很重要。想说说发生了什么吗？说出来会舒服一些。"
        });
        MOCK_RESPONSES.put("生气|愤怒|烦躁|烦", new String[]{
                "愤怒是正常的情绪，我能感受到你现在很烦躁。",
                "先深呼吸三次，让自己冷静一下。我在这里等你。",
                "你完全有权利感到愤怒。我们可以一起想想怎么处理这种情况。"
        });
        MOCK_RESPONSES.put("累|疲惫|没劲|无力", new String[]{
                "听起来你真的很疲惫。记得照顾好自己，休息也是很重要的事。",
                "累了就休息一下吧，不需要一直那么坚强。"
        });
        MOCK_RESPONSES.put("孤独|寂寞|一个人|没人|被忽略", new String[]{
                "我能感受到你的孤独感。请记住，你并不真的是一个人。",
                "虽然现在感觉很孤单，但世界上总有人在关心着你。"
        });
        MOCK_RESPONSES.put("睡不着|失眠|做梦|熬夜", new String[]{
                "失眠真的很折磨人。试试今晚睡前听一些轻音乐？",
                "睡不着的时候，不要强迫自己。起来喝杯温水，看看书。"
        });
    }

    private static final String[] FALLBACK_REPLIES = {
            "谢谢你愿意和我分享。能再多说一些你的感受吗？",
            "我在认真听你说，每个人都会有情绪起伏的时候。",
            "你的感受值得被重视。想听听我的一些建议吗？",
            "我理解你现在的心情。有时候说出来就会好很多。",
            "感谢你的信任。你觉得现在最需要的是什么？",
            "我在这里陪伴你。有时候慢慢来也是一种前进。"
    };
}
