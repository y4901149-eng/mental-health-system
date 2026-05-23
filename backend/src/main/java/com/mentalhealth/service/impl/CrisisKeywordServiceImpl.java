package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.CrisisKeyword;
import com.mentalhealth.mapper.CrisisKeywordMapper;
import com.mentalhealth.service.CrisisKeywordService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 危机关键词 Service 实现
 */
@Service
public class CrisisKeywordServiceImpl extends ServiceImpl<CrisisKeywordMapper, CrisisKeyword> implements CrisisKeywordService {

    @Override
    public Map<String, List<CrisisKeyword>> getEnabledKeywordsGrouped() {
        List<CrisisKeyword> list = list(new LambdaQueryWrapper<CrisisKeyword>()
                .eq(CrisisKeyword::getEnabled, 1)
                .orderByAsc(CrisisKeyword::getId));

        Map<String, List<CrisisKeyword>> grouped = new LinkedHashMap<>();
        grouped.put("high", new ArrayList<>());
        grouped.put("mid", new ArrayList<>());
        grouped.put("low", new ArrayList<>());

        for (CrisisKeyword kw : list) {
            String level = kw.getRiskLevel();
            if (grouped.containsKey(level)) {
                grouped.get(level).add(kw);
            }
        }
        return grouped;
    }
}
