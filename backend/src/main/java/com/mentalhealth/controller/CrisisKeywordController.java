package com.mentalhealth.controller;

import com.mentalhealth.service.CrisisKeywordService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 危机关键词 Controller
 * 提供 GET /api/crisis/keywords 返回启用的关键词（按风险等级分组）
 */
@RestController
@RequestMapping("/api/crisis")
public class CrisisKeywordController {

    @Autowired
    private CrisisKeywordService crisisKeywordService;

    @GetMapping("/keywords")
    public ResultVO<Map<String, List<Map<String, Object>>>> getKeywords() {
        // 返回按 riskLevel 分组的关键词列表
        Map<String, List<com.mentalhealth.entity.CrisisKeyword>> grouped = crisisKeywordService.getEnabledKeywordsGrouped();

        // 转为前端需要的格式 { high: [{keyword, riskLevel}], mid: [...], low: [...] }
        java.util.LinkedHashMap<String, List<Map<String, Object>>> result = new java.util.LinkedHashMap<>();
        for (String level : new String[]{"high", "mid", "low"}) {
            List<Map<String, Object>> list = new java.util.ArrayList<>();
            if (grouped.containsKey(level)) {
                for (com.mentalhealth.entity.CrisisKeyword kw : grouped.get(level)) {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("keyword", kw.getKeyword());
                    item.put("riskLevel", kw.getRiskLevel());
                    list.add(item);
                }
            }
            result.put(level, list);
        }
        return ResultVO.success(result);
    }
}
