package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.CrisisAlert;
import com.mentalhealth.mapper.CrisisAlertMapper;
import com.mentalhealth.service.CrisisAlertService;
import com.mentalhealth.entity.User;
import com.mentalhealth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CrisisAlertServiceImpl extends ServiceImpl<CrisisAlertMapper, CrisisAlert> implements CrisisAlertService {

    @Autowired
    private UserMapper userMapper;

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
        long total = count(wrapper);
        Page<CrisisAlert> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        if (total > 0) {
            wrapper.last("LIMIT " + pageSize + " OFFSET " + ((pageNum - 1) * pageSize));
            List<CrisisAlert> records = list(wrapper);
            // 填充用户名（优先 nickname，其次 username）
            for (CrisisAlert alert : records) {
                if (alert.getUserId() != null) {
                    User user = userMapper.selectById(alert.getUserId());
                    if (user != null) {
                        alert.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    }
                }
            }
            page.setRecords(records);
        }
        return page;
    }

    @Override
    public void handleAlert(Long id, Long adminId, String remark) {
        CrisisAlert alert = getById(id);
        if (alert != null) {
            alert.setHandleStatus("RESOLVED");
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
