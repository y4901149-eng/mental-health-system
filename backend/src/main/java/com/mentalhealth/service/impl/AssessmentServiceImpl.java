package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Assessment;
import com.mentalhealth.entity.AssessmentQuestion;
import com.mentalhealth.entity.AssessmentRecord;
import com.mentalhealth.mapper.AssessmentMapper;
import com.mentalhealth.mapper.AssessmentQuestionMapper;
import com.mentalhealth.mapper.AssessmentRecordMapper;
import com.mentalhealth.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评估量表 Service 实现类
 * 作用：管理量表、处理用户提交的评估、计算得分
 */
@Service
public class AssessmentServiceImpl extends ServiceImpl<AssessmentMapper, Assessment> implements AssessmentService {

    @Autowired
    private AssessmentQuestionMapper questionMapper;

    @Autowired
    private AssessmentRecordMapper recordMapper;

    @Override
    public List<Assessment> getPublishedList() {
        return list(new LambdaQueryWrapper<Assessment>()
                .eq(Assessment::getStatus, "published")
                .orderByDesc(Assessment::getCreateTime));
    }

    @Override
    public Assessment getDetail(Long id) {
        Assessment assessment = getById(id);
        if (assessment == null) {
            throw new RuntimeException("量表不存在");
        }
        // 查询该量表的所有题目
        List<AssessmentQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<AssessmentQuestion>()
                        .eq(AssessmentQuestion::getAssessmentId, id)
                        .orderByAsc(AssessmentQuestion::getSortOrder));
        assessment.setQuestions(questions);
        return assessment;
    }

    @Override
    public AssessmentRecord submitAssessment(Long userId, Long assessmentId, Map<Long, Integer> answers) {
        Assessment assessment = getById(assessmentId);
        if (assessment == null) {
            throw new RuntimeException("量表不存在");
        }

        // 计算总分
        int totalScore = 0;
        for (Integer score : answers.values()) {
            totalScore += score;
        }

        // 计算等级
        int questionCount = answers.size();
        String level = calculateLevel(totalScore, questionCount);

        // 根据等级生成建议
        String suggestion = generateSuggestion(level, assessment.getType());

        // 保存评估记录
        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(userId);
        record.setAssessmentId(assessmentId);
        record.setTotalScore(totalScore);
        record.setLevel(level);
        record.setSuggestion(suggestion);
        record.setSubmitTime(LocalDateTime.now());
        recordMapper.insert(record);

        return record;
    }

    @Override
    public List<Map<String, Object>> getUserRecords(Long userId) {
        return recordMapper.selectUserRecordsWithTitle(userId);
    }

    @Override
    public String calculateLevel(Integer totalScore, Integer questionCount) {
        // 每个题目满分通常为3-4分，按平均每个题目3分计算百分比
        int maxScore = questionCount * 3;
        double percentage = (double) totalScore / maxScore;

        if (percentage < 0.3) return "normal";      // 正常
        if (percentage < 0.5) return "mild";        // 轻度
        if (percentage < 0.7) return "moderate";    // 中度
        return "severe";                              // 重度
    }

    /** 根据等级和类型生成建议 */
    private String generateSuggestion(String level, String type) {
        switch (level) {
            case "normal":
                return "您的心理状态良好，请继续保持健康的生活方式。建议每天保持适量运动，保证充足睡眠。";
            case "mild":
                return "您存在轻度心理困扰，建议尝试以下方法：1. 每天进行15分钟深呼吸练习；2. 保持规律作息；3. 与朋友或家人倾诉；4. 如症状持续，建议咨询心理医生。";
            case "moderate":
                return "您存在中度心理困扰，强烈建议您：1. 尽快预约专业心理咨询；2. 不要独自面对，寻求家人朋友支持；3. 避免过度自我批评；4. 必要时前往医院心理科就诊。";
            case "severe":
                return "您的评估结果提示需要立即关注！请尽快前往医院心理科或精神科就诊，或拨打心理危机干预热线。不要独自承受，专业帮助就在您身边。";
            default:
                return "请保持良好的心理状态，如有不适请及时寻求帮助。";
        }
    }
}
