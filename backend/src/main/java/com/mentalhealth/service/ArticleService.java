package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Article;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 * 文章 Service 接口
 */
public interface ArticleService extends IService<Article> {

    /** 分页查询已发布文章 */
    IPage<Article> getPublishedPage(Integer pageNum, Integer pageSize, String category);

    /** 获取推荐文章 */
    List<Article> getRecommendedList();

    /** 增加阅读量 */
    void incrementViewCount(Long id);
}
