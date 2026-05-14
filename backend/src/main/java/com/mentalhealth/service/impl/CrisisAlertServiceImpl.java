package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.CrisisAlert;
import com.mentalhealth.mapper.CrisisAlertMapper;
import com.mentalhealth.service.CrisisAlertService;
import org.springframework.stereotype.Service;

@Service
public class CrisisAlertServiceImpl extends ServiceImpl<CrisisAlertMapper, CrisisAlert> implements CrisisAlertService {

    @Override
    public IPage<CrisisAlert> getAlertPage(Integer pageNum, Integer pageSize, String status, Long userId) {
        LambdaQueryWrapper<CrisisAlert> wrapper = new LambdaQueryWrapper<CrisisAlert>()
                .orderByDesc(CrisisAlert::getCreateTime);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CrisisAlert::getHandleStatus, status);
        }
        if (userId != null) {
            wrapper.eq(CrisisAlert::getUserId, userId);
        }
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void handleAlert(Long id, Long adminId, String remark) {
        CrisisAlert alert = getById(id);
        if (alert != null) {
            alert.setHandleStatus("PROCESSING");
            alert.setHandledBy(adminId);
            alert.setHandleRemark(remark);
            updateById(alert);
        }
    }

    @Override
    public void createAlert(CrisisAlert alert) {
        alert.setHandleStatus("PENDING");
        save(alert);
    }
}
