package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentalhealth.entity.Article;
import com.mentalhealth.service.ArticleService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章 Controller
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /** 分页获取已发布文章 */
    @GetMapping("/list")
    public ResultVO<IPage<Article>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "9") Integer pageSize,
            @RequestParam(required = false) String category) {
        return ResultVO.success(articleService.getPublishedPage(pageNum, pageSize, category));
    }

    /** 获取推荐文章 */
    @GetMapping("/recommended")
    public ResultVO<List<Article>> recommended() {
        return ResultVO.success(articleService.getRecommendedList());
    }

    /** 获取文章详情 */
    @GetMapping("/{id}")
    public ResultVO<Article> detail(@PathVariable Long id) {
        articleService.incrementViewCount(id);
        return ResultVO.success(articleService.getById(id));
    }
}
