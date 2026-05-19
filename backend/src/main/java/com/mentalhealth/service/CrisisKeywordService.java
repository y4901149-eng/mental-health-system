package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.CrisisKeyword;

import java.util.List;
import java.util.Map;

/**
 * 危机关键词 Service 接口
 */
public interface CrisisKeywordService extends IService<CrisisKeyword> {

    /** 获取所有启用的关键词，按风险等级分组 */
    Map<String, List<CrisisKeyword>> getEnabledKeywordsGrouped();
}
