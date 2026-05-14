package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Diary;
import com.mentalhealth.mapper.DiaryMapper;
import com.mentalhealth.service.DiaryService;
import org.springframework.stereotype.Service;

@Service
public class DiaryServiceImpl extends ServiceImpl<DiaryMapper, Diary> implements DiaryService {

    @Override
    public IPage<Diary> getUserDiaryPage(Long userId, Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<Diary>()
                .eq(Diary::getUserId, userId)
                .orderByDesc(Diary::getCreateTime);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Diary::getContent, keyword);
        }

        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
