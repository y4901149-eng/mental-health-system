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
 * 心理测评 Service 实现
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
        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("请完成测评后再提交");
        }

        int totalScore = 0;
        for (Integer score : answers.values()) {
            if (score != null) {
                totalScore += score;
            }
        }

        int questionCount = answers.size();
        String level = calculateLevel(totalScore, questionCount);
        String suggestion = generateSuggestion(level, assessment.getType());

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
        if (totalScore == null || questionCount == null || questionCount <= 0) {
            return "normal";
        }

        int minScore = questionCount;
        int maxScore = questionCount * 4;
        double ratio = (double) (totalScore - minScore) / (maxScore - minScore);

        if (ratio < 0.25) return "normal";
        if (ratio < 0.50) return "mild";
        if (ratio < 0.75) return "moderate";
        return "severe";
    }

    private String generateSuggestion(String level, String type) {
        String focus = getTypeFocus(type);
        switch (level) {
            case "normal":
                return focus + "当前结果整体处于较平稳范围。建议继续保持规律作息、适当运动，并定期记录情绪变化。结果仅供自我了解和学习参考，不作为医学诊断。";
            case "mild":
                return focus + "结果提示近期可能存在一定压力或情绪波动。建议留意睡眠、学习负荷和人际支持，尝试把压力事项拆分处理。结果仅供自我了解和学习参考，不作为医学诊断。";
            case "moderate":
                return focus + "结果提示当前困扰较明显。建议主动与辅导员、家人、朋友或校心理中心沟通，必要时预约专业咨询。结果仅供自我了解和学习参考，不作为医学诊断。";
            case "severe":
                return focus + "结果提示当前状态需要重点关注。建议尽快联系校心理中心、辅导员或专业人员获得支持；如出现安全风险，请及时求助。结果仅供自我了解和学习参考，不作为医学诊断。";
            default:
                return "建议结合近期学习、生活和情绪状态进行参考，并在需要时寻求老师或专业人员帮助。结果仅供自我了解和学习参考，不作为医学诊断。";
        }
    }

    private String getTypeFocus(String type) {
        if ("anxiety".equals(type)) return "本测评主要反映焦虑相关体验。";
        if ("depression".equals(type)) return "本测评主要反映低落情绪和兴趣变化。";
        if ("mood_fluctuation".equals(type)) return "本测评主要反映情绪起伏和恢复速度。";
        if ("stress".equals(type)) return "本测评主要反映近期压力感受。";
        if ("sleep".equals(type)) return "本测评主要反映睡眠质量和作息状态。";
        if ("routine".equals(type)) return "本测评主要反映作息规律和晚睡情况。";
        if ("daytime_fatigue".equals(type)) return "本测评主要反映日间疲劳和精力恢复情况。";
        if ("academic".equals(type)) return "本测评主要反映学习压力和学业负荷。";
        if ("exam_anxiety".equals(type)) return "本测评主要反映考试相关紧张和发挥受影响情况。";
        if ("learning_motivation".equals(type)) return "本测评主要反映学习目标感和持续投入中的困难。";
        if ("time_management".equals(type)) return "本测评主要反映计划安排、拖延和任务完成效率。";
        if ("social".equals(type)) return "本测评主要反映社交场景中的紧张程度。";
        if ("relationship".equals(type)) return "本测评主要反映人际关系支持和冲突状态。";
        if ("support".equals(type)) return "本测评主要反映人际支持感和求助表达情况。";
        if ("dorm_relationship".equals(type)) return "本测评主要反映宿舍生活适应和边界沟通情况。";
        if ("communication".equals(type)) return "本测评主要反映沟通表达和处理分歧中的困难。";
        if ("emotion_regulation".equals(type)) return "本测评主要反映情绪识别和调节中的困难。";
        if ("self_esteem".equals(type)) return "本测评主要反映自我评价和自我接纳中的困扰。";
        if ("resilience".equals(type)) return "本测评主要反映面对压力和挫折时的恢复困难。";
        if ("self_efficacy".equals(type)) return "本测评主要反映任务信心和掌控感不足情况。";
        if ("life_satisfaction".equals(type)) return "本测评主要反映近期整体生活满意度相关困扰。";
        return "";
    }
}
