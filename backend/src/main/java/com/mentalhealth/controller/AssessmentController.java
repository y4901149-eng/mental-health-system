package com.mentalhealth.controller;

import com.mentalhealth.dto.AssessmentSubmitDTO;
import com.mentalhealth.entity.Assessment;
import com.mentalhealth.entity.AssessmentCategory;
import com.mentalhealth.entity.AssessmentRecord;
import com.mentalhealth.mapper.AssessmentRecordMapper;
import com.mentalhealth.service.AssessmentService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbc;

    /** 获取启用的测评板块列表 */
    @GetMapping("/categories")
    public ResultVO<List<AssessmentCategory>> categories() {
        List<AssessmentCategory> list = jdbc.query(
                "SELECT id, name, sort_order, enabled, created_at FROM assessment_category WHERE enabled = 1 ORDER BY sort_order, id",
                (rs, row) -> {
                    AssessmentCategory c = new AssessmentCategory();
                    c.setId(rs.getLong("id"));
                    c.setName(rs.getString("name"));
                    c.setSortOrder(rs.getInt("sort_order"));
                    c.setEnabled(rs.getInt("enabled"));
                    return c;
                });
        return ResultVO.success(list);
    }

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
