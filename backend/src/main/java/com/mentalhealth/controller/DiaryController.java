package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentalhealth.entity.Diary;
import com.mentalhealth.service.AIService;
import com.mentalhealth.service.DiaryService;
import com.mentalhealth.utils.MoodAnalyzer;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private AIService aiService;

    @GetMapping("/list")
    public ResultVO<IPage<Diary>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(diaryService.getUserDiaryPage(userId, pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public ResultVO<Diary> detail(@PathVariable Long id) {
        return ResultVO.success(diaryService.getById(id));
    }

    /** 新增日记（自动分析情绪） */
    @PostMapping("/create")
    public ResultVO<?> create(@RequestBody Diary diary, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        diary.setUserId(userId);
        // 自动情绪分析
        MoodAnalyzer.AnalysisResult analysis = MoodAnalyzer.analyze(diary.getContent());
        if (diary.getMoodTags() == null || diary.getMoodTags().isEmpty()) {
            diary.setMoodTags(analysis.moodTagCn);
        }
        if (diary.getEmotionScore() == null) {
            diary.setEmotionScore((double) analysis.emotionScore);
        }
        diaryService.save(diary);
        // 危机关键词检测（异步无阻塞）
        aiService.checkDiaryCrisis(diary.getContent(), userId);
        return ResultVO.success();
    }

    /** 更新日记（自动分析情绪） */
    @PutMapping("/update")
    public ResultVO<?> update(@RequestBody Diary diary, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Diary existing = diaryService.getById(diary.getId());
        if (existing != null && existing.getUserId().equals(userId)) {
            existing.setTitle(diary.getTitle());
            existing.setContent(diary.getContent());
            existing.setImageUrl(diary.getImageUrl());
            // 自动情绪分析
            MoodAnalyzer.AnalysisResult analysis = MoodAnalyzer.analyze(diary.getContent());
            existing.setMoodTags(diary.getMoodTags() != null && !diary.getMoodTags().isEmpty()
                    ? diary.getMoodTags() : analysis.moodTagCn);
            existing.setEmotionScore(diary.getEmotionScore() != null
                    ? diary.getEmotionScore() : (double) analysis.emotionScore);
            diaryService.updateById(existing);
        }
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    public ResultVO<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Diary diary = diaryService.getById(id);
        if (diary != null && diary.getUserId().equals(userId)) {
            diaryService.removeById(id);
        }
        return ResultVO.success();
    }
}
