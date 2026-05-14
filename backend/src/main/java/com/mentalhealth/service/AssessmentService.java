package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Assessment;
import com.mentalhealth.entity.AssessmentRecord;

import java.util.List;
import java.util.Map;

/**
 * 评估量表 Service 接口
 */
public interface AssessmentService extends IService<Assessment> {

    /** 获取已发布量表列表 */
    List<Assessment> getPublishedList();

    /** 获取量表详情（含所有题目） */
    Assessment getDetail(Long id);

    /** 提交评估答案并计算得分 */
    AssessmentRecord submitAssessment(Long userId, Long assessmentId, Map<Long, Integer> answers);

    /** 获取用户的评估历史 */
    List<Map<String, Object>> getUserRecords(Long userId);

    /** 根据分数计算等级 */
    String calculateLevel(Integer totalScore, Integer questionCount);
}
