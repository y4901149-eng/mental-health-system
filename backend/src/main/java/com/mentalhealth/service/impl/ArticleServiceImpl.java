package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Article;
import com.mentalhealth.mapper.ArticleMapper;
import com.mentalhealth.service.ArticleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章 Service 实现类
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Override
    public IPage<Article> getPublishedPage(Integer pageNum, Integer pageSize, String category) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "published")
                .orderByDesc(Article::getCreateTime);

        // 如果传了分类，按分类筛选
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Article::getCategory, category);
        }

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<Article> getRecommendedList() {
        return list(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "published")
                .eq(Article::getIsRecommended, 1)
                .orderByDesc(Article::getCreateTime)
                .last("LIMIT 6"));
    }

    @Override
    public void incrementViewCount(Long id) {
        Article article = getById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            updateById(article);
        }
    }
}
