package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.entity.Assessment;
import com.mentalhealth.entity.AssessmentQuestion;
import com.mentalhealth.mapper.AssessmentMapper;
import com.mentalhealth.mapper.AssessmentQuestionMapper;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/assessments")
public class AdminAssessmentController {

    @Autowired
    private AssessmentMapper assessmentMapper;

    @Autowired
    private AssessmentQuestionMapper questionMapper;

    @GetMapping
    public ResultVO<IPage<Assessment>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "15") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Assessment> wrapper = new LambdaQueryWrapper<Assessment>()
                .orderByDesc(Assessment::getId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Assessment::getTitle, keyword);
        }
        return ResultVO.success(assessmentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @PostMapping
    public ResultVO<?> create(@RequestBody Assessment assessment) {
        if (assessment.getStatus() == null) assessment.setStatus("draft");
        if (assessment.getQuestionCount() == null) assessment.setQuestionCount(0);
        assessmentMapper.insert(assessment);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    public ResultVO<?> update(@PathVariable Long id, @RequestBody Assessment assessment) {
        Assessment existing = assessmentMapper.selectById(id);
        if (existing == null) return ResultVO.badRequest("量表不存在");
        if (assessment.getTitle() != null) existing.setTitle(assessment.getTitle());
        if (assessment.getDescription() != null) existing.setDescription(assessment.getDescription());
        if (assessment.getType() != null) existing.setType(assessment.getType());
        if (assessment.getStatus() != null) existing.setStatus(assessment.getStatus());
        assessmentMapper.updateById(existing);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    public ResultVO<?> delete(@PathVariable Long id) {
        questionMapper.delete(new LambdaQueryWrapper<AssessmentQuestion>().eq(AssessmentQuestion::getAssessmentId, id));
        assessmentMapper.deleteById(id);
        return ResultVO.success();
    }

    // ===== 题目管理 =====
    @GetMapping("/{id}/questions")
    public ResultVO<?> questions(@PathVariable Long id) {
        return ResultVO.success(questionMapper.selectList(
                new LambdaQueryWrapper<AssessmentQuestion>()
                        .eq(AssessmentQuestion::getAssessmentId, id)
                        .orderByAsc(AssessmentQuestion::getSortOrder)));
    }

    @PostMapping("/questions")
    public ResultVO<?> createQuestion(@RequestBody AssessmentQuestion question) {
        questionMapper.insert(question);
        // 更新量表题目数
        Long count = questionMapper.selectCount(
                new LambdaQueryWrapper<AssessmentQuestion>().eq(AssessmentQuestion::getAssessmentId, question.getAssessmentId()));
        Assessment a = assessmentMapper.selectById(question.getAssessmentId());
        if (a != null) { a.setQuestionCount(count != null ? count.intValue() : 0); assessmentMapper.updateById(a); }
        return ResultVO.success();
    }

    @PutMapping("/questions/{id}")
    public ResultVO<?> updateQuestion(@PathVariable Long id, @RequestBody AssessmentQuestion question) {
        AssessmentQuestion existing = questionMapper.selectById(id);
        if (existing == null) return ResultVO.badRequest("题目不存在");
        if (question.getQuestionText() != null) existing.setQuestionText(question.getQuestionText());
        if (question.getOptionA() != null) existing.setOptionA(question.getOptionA());
        if (question.getOptionB() != null) existing.setOptionB(question.getOptionB());
        if (question.getOptionC() != null) existing.setOptionC(question.getOptionC());
        if (question.getOptionD() != null) existing.setOptionD(question.getOptionD());
        if (question.getScoreA() != null) existing.setScoreA(question.getScoreA());
        if (question.getScoreB() != null) existing.setScoreB(question.getScoreB());
        if (question.getScoreC() != null) existing.setScoreC(question.getScoreC());
        if (question.getScoreD() != null) existing.setScoreD(question.getScoreD());
        if (question.getSortOrder() != null) existing.setSortOrder(question.getSortOrder());
        questionMapper.updateById(existing);
        return ResultVO.success();
    }

    @DeleteMapping("/questions/{id}")
    public ResultVO<?> deleteQuestion(@PathVariable Long id) {
        AssessmentQuestion q = questionMapper.selectById(id);
        if (q != null) {
            questionMapper.deleteById(id);
            Long count = questionMapper.selectCount(
                    new LambdaQueryWrapper<AssessmentQuestion>().eq(AssessmentQuestion::getAssessmentId, q.getAssessmentId()));
            Assessment a = assessmentMapper.selectById(q.getAssessmentId());
            if (a != null) { a.setQuestionCount(count != null ? count.intValue() : 0); assessmentMapper.updateById(a); }
        }
        return ResultVO.success();
    }
}
