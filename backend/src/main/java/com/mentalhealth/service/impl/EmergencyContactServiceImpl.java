package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.EmergencyContact;
import com.mentalhealth.mapper.EmergencyContactMapper;
import com.mentalhealth.service.EmergencyContactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmergencyContactServiceImpl extends ServiceImpl<EmergencyContactMapper, EmergencyContact> implements EmergencyContactService {

    @Override
    public List<EmergencyContact> getUserContacts(Long userId) {
        return list(new LambdaQueryWrapper<EmergencyContact>()
                .eq(EmergencyContact::getUserId, userId)
                .orderByDesc(EmergencyContact::getIsDefault)
                .orderByDesc(EmergencyContact::getCreateTime));
    }

    @Override
    public EmergencyContact getDefaultContact(Long userId) {
        return getOne(new LambdaQueryWrapper<EmergencyContact>()
                .eq(EmergencyContact::getUserId, userId)
                .eq(EmergencyContact::getIsDefault, 1));
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long contactId) {
        // 清除该用户所有默认
        baseMapper.update(null,
                new LambdaUpdateWrapper<EmergencyContact>()
                        .eq(EmergencyContact::getUserId, userId)
                        .set(EmergencyContact::getIsDefault, 0));
        // 设置新的默认
        EmergencyContact contact = getById(contactId);
        if (contact != null && contact.getUserId().equals(userId)) {
            contact.setIsDefault(1);
            updateById(contact);
        }
    }
}
