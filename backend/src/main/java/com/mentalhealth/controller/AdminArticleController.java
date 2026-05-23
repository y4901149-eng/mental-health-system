package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.entity.Article;
import com.mentalhealth.service.ArticleService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public ResultVO<IPage<Article>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "15") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getCreateTime);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Article::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                    .or().like(Article::getAuthor, keyword));
        }
        Page<Article> page = new Page<>(pageNum, pageSize);
        return ResultVO.success(articleService.page(page, wrapper));
    }

    @PostMapping
    public ResultVO<?> create(@RequestBody Article article) {
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            return ResultVO.badRequest("标题不能为空");
        }
        if (article.getStatus() == null) article.setStatus("published");
        if (article.getViewCount() == null) article.setViewCount(0);
        if (article.getIsRecommended() == null) article.setIsRecommended(0);
        articleService.save(article);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    public ResultVO<?> update(@PathVariable Long id, @RequestBody Article article) {
        Article existing = articleService.getById(id);
        if (existing == null) return ResultVO.badRequest("文章不存在");
        if (article.getTitle() != null) existing.setTitle(article.getTitle().trim());
        if (article.getSummary() != null) existing.setSummary(article.getSummary());
        if (article.getContent() != null) existing.setContent(article.getContent());
        if (article.getCategory() != null) existing.setCategory(article.getCategory());
        if (article.getCoverImage() != null) existing.setCoverImage(article.getCoverImage());
        if (article.getAuthor() != null) existing.setAuthor(article.getAuthor());
        if (article.getStatus() != null) existing.setStatus(article.getStatus());
        if (article.getIsRecommended() != null) existing.setIsRecommended(article.getIsRecommended());
        if (article.getViewCount() != null) existing.setViewCount(article.getViewCount());
        articleService.updateById(existing);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    public ResultVO<?> delete(@PathVariable Long id) {
        articleService.removeById(id);
        return ResultVO.success();
    }
}
