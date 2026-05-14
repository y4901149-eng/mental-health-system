package com.mentalhealth.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 情绪分析工具
 * 根据日记内容关键词规则分析情绪标签和分数
 */
public class MoodAnalyzer {

    private static final Map<String, KeywordRule> RULES = new LinkedHashMap<>();
    static {
        RULES.put("happy",    new KeywordRule("happy",    "开心",    80, new String[]{"happy","开心","高兴","快乐","棒","赞","好开心","太棒了","愉悦","美好"}));
        RULES.put("calm",     new KeywordRule("calm",     "平静",    65, new String[]{"calm","平静","放松","宁静","舒缓","安稳","舒适","平和"}));
        RULES.put("sad",      new KeywordRule("sad",      "悲伤",    25, new String[]{"sad","难过","悲伤","伤心","崩溃","绝望","痛苦","泪","哭泣","哭"}));
        RULES.put("anxious",  new KeywordRule("anxious",  "焦虑",    30, new String[]{"anxious","焦虑","紧张","担心","不安","压力","恐慌","害怕"}));
        RULES.put("angry",    new KeywordRule("angry",    "愤怒",    20, new String[]{"angry","生气","愤怒","烦躁","恼火","气死"}));
        RULES.put("tired",    new KeywordRule("tired",    "疲惫",    25, new String[]{"tired","累","疲惫","困","乏力","没劲","疲劳","疲倦"}));
        RULES.put("neutral",  new KeywordRule("neutral",  "一般",    50, new String[]{}));
    }

    /** 分析结果 */
    public static class AnalysisResult {
        public String moodTag;
        public String moodTagCn;
        public Integer emotionScore;

        public AnalysisResult(String moodTag, String moodTagCn, Integer emotionScore) {
            this.moodTag = moodTag;
            this.moodTagCn = moodTagCn;
            this.emotionScore = emotionScore;
        }
    }

    /**
     * 分析文本情绪
     * @param content 日记内容
     * @return 分析结果
     */
    public static AnalysisResult analyze(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new AnalysisResult("neutral", "一般", 50);
        }
        String lower = content.toLowerCase();
        int totalScore = 0;
        String matchedTag = "neutral";
        String matchedCn = "一般";

        for (Map.Entry<String, KeywordRule> entry : RULES.entrySet()) {
            KeywordRule rule = entry.getValue();
            if (rule.keywords.length == 0) continue;
            for (String kw : rule.keywords) {
                if (lower.contains(kw)) {
                    if (rule.score > totalScore) {
                        totalScore = rule.score;
                        matchedTag = rule.tag;
                        matchedCn = rule.tagCn;
                    }
                    break;
                }
            }
        }

        // 如果没匹配到任何关键词，默认中性
        if (totalScore == 0) {
            totalScore = 50;
            matchedTag = "neutral";
            matchedCn = "一般";
        }

        return new AnalysisResult(matchedTag, matchedCn, totalScore);
    }

    private static class KeywordRule {
        String tag;
        String tagCn;
        int score;
        String[] keywords;

        KeywordRule(String tag, String tagCn, int score, String[] keywords) {
            this.tag = tag;
            this.tagCn = tagCn;
            this.score = score;
            this.keywords = keywords;
        }
    }
}
