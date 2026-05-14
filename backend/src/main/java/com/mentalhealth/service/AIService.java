package com.mentalhealth.service;

import com.mentalhealth.config.AIConfig;
import com.mentalhealth.entity.CrisisAlert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    @Autowired
    private AIConfig aiConfig;

    @Autowired(required = false)
    private CrisisAlertService crisisAlertService;

    private final RestTemplate restTemplate = new RestTemplate();

    /** 自杀/自伤检测关键词 */
    private static final String[] CRISIS_KEYWORDS = {
            "自杀", "轻生", "不想活", "活不下去", "想死", "结束生命",
            "伤害自己", "自残", "自伤", "死了算了", "没有活着的意义"
    };

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
                if (containsCrisisKeyword(userMessage)) {
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

    /** 检测危机关键词 */
    private boolean containsCrisisKeyword(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String kw : CRISIS_KEYWORDS) {
            if (lower.contains(kw)) return true;
        }
        return false;
    }

    /** 如果检测到危机关键词，自动创建预警记录 */
    private void checkCrisis(String content, Long userId) {
        if (!containsCrisisKeyword(content)) return;
        if (crisisAlertService == null || userId == null) return;

        try {
            CrisisAlert alert = new CrisisAlert();
            alert.setUserId(userId);
            alert.setAlertLevel(5);
            alert.setHandleStatus("PENDING");
            alert.setTriggerReason("AI 对话检测到高危关键词: " + content.substring(0, Math.min(content.length(), 100)));
            crisisAlertService.save(alert);
            System.out.println("⚠️ 已自动创建危机预警, userId=" + userId);
        } catch (Exception e) {
            System.err.println("创建危机预警失败: " + e.getMessage());
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
