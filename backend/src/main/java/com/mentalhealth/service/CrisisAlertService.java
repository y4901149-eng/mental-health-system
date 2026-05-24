package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.CrisisAlert;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface CrisisAlertService extends IService<CrisisAlert> {
    /** 分页查询预警（管理员） */
    IPage<CrisisAlert> getAlertPage(Integer pageNum, Integer pageSize, String status, Long userId);
    /** 处理预警（status: RESOLVED 或 PENDING） */
    void handleAlert(Long id, Long adminId, String remark, String status);
    /** 创建预警 */
    void createAlert(CrisisAlert alert);
}
