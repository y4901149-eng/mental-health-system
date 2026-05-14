package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.EmotionReport;
import java.util.List;
import java.util.Map;

public interface EmotionReportService extends IService<EmotionReport> {
    /** 获取用户周报列表 */
    List<EmotionReport> getUserReports(Long userId);
    /** 获取最新周报 */
    EmotionReport getLatestReport(Long userId);
    /** 生成新周报 */
    EmotionReport generateReport(Long userId);
}
