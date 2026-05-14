package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章实体类
 * 作用：对应 article 表，存储心理健康科普文章（用于干预教育）
 */
@Data
@TableName("article")
public class Article {
    /** 文章ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章标题 */
    private String title;

    /** 文章摘要 */
    private String summary;

    /** 文章内容 - 支持富文本HTML */
    private String content;

    /** 文章分类: knowledge-心理健康知识, therapy-治疗方法, case-案例分享 */
    private String category;

    /** 封面图 */
    private String coverImage;

    /** 作者 */
    private String author;

    /** 阅读量 */
    private Integer viewCount;

    /** 状态: draft-草稿, published-发布 */
    private String status;

    /** 是否推荐: 0-否, 1-是 */
    private Integer isRecommended;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
