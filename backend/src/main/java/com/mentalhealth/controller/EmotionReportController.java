package com.mentalhealth.controller;

import com.mentalhealth.entity.EmotionReport;
import com.mentalhealth.service.EmotionReportService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/report")
public class EmotionReportController {

    @Autowired
    private EmotionReportService reportService;

    @GetMapping("/latest")
    public ResultVO<EmotionReport> latest(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(reportService.getLatestReport(userId));
    }

    @GetMapping("/list")
    public ResultVO<List<EmotionReport>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(reportService.getUserReports(userId));
    }

    @PostMapping("/generate")
    public ResultVO<EmotionReport> generate(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(reportService.generateReport(userId));
    }
}
