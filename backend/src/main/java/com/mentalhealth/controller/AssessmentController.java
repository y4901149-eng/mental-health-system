package com.mentalhealth.controller;

import com.mentalhealth.dto.AssessmentSubmitDTO;
import com.mentalhealth.entity.Assessment;
import com.mentalhealth.entity.AssessmentRecord;
import com.mentalhealth.mapper.AssessmentRecordMapper;
import com.mentalhealth.service.AssessmentService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 心理评估 Controller
 */
@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AssessmentRecordMapper assessmentRecordMapper;

    /** 获取已发布的量表列表 */
    @GetMapping("/list")
    public ResultVO<List<Assessment>> list() {
        return ResultVO.success(assessmentService.getPublishedList());
    }

    /** 获取量表详情（含题目） */
    @GetMapping("/{id}")
    public ResultVO<Assessment> detail(@PathVariable Long id) {
        return ResultVO.success(assessmentService.getDetail(id));
    }

    /** 提交评估答案 */
    @PostMapping("/submit")
    public ResultVO<AssessmentRecord> submit(@RequestBody AssessmentSubmitDTO dto,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AssessmentRecord record = assessmentService.submitAssessment(
                userId, dto.getAssessmentId(), dto.getAnswers());
        return ResultVO.success(record);
    }

    /** 获取我的评估历史 */
    @GetMapping("/records")
    public ResultVO<List<Map<String, Object>>> records(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(assessmentService.getUserRecords(userId));
    }

    /** 获取评估记录详情 */
    @GetMapping("/record/{id}")
    public ResultVO<AssessmentRecord> recordDetail(@PathVariable Long id) {
        AssessmentRecord record = assessmentRecordMapper.selectById(id);
        return ResultVO.success(record);
    }
}
