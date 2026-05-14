package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Diary;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface DiaryService extends IService<Diary> {

    /** 分页查询用户日记 */
    IPage<Diary> getUserDiaryPage(Long userId, Integer pageNum, Integer pageSize, String keyword);
}
