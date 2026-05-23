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
import java.util.regex.Pattern;

@Service
public class EmergencyContactServiceImpl extends ServiceImpl<EmergencyContactMapper, EmergencyContact> implements EmergencyContactService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

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
                .eq(EmergencyContact::getIsDefault, 1), false);
    }

    @Override
    @Transactional
    public void saveContact(EmergencyContact contact) {
        validateContact(contact);
        contact.setName(contact.getName().trim());
        contact.setPhone(contact.getPhone().trim());
        contact.setRelation(isBlank(contact.getRelation()) ? null : contact.getRelation().trim());
        contact.setIsDefault(contact.getIsDefault() != null && contact.getIsDefault() == 1 ? 1 : 0);
        if (contact.getIsDefault() == 1 || countUserContacts(contact.getUserId()) == 0) {
            clearDefault(contact.getUserId());
            contact.setIsDefault(1);
        }
        save(contact);
    }

    @Override
    @Transactional
    public void updateContact(EmergencyContact contact) {
        validateContact(contact);
        EmergencyContact existing = getOwnedContact(contact.getUserId(), contact.getId());
        existing.setName(contact.getName().trim());
        existing.setPhone(contact.getPhone().trim());
        existing.setRelation(isBlank(contact.getRelation()) ? null : contact.getRelation().trim());
        if (contact.getIsDefault() != null && contact.getIsDefault() == 1) {
            clearDefault(contact.getUserId());
            existing.setIsDefault(1);
        }
        updateById(existing);
    }

    @Override
    @Transactional
    public void deleteContact(Long userId, Long contactId) {
        EmergencyContact existing = getOwnedContact(userId, contactId);
        removeById(contactId);
        if (existing.getIsDefault() != null && existing.getIsDefault() == 1) {
            EmergencyContact next = getOne(new LambdaQueryWrapper<EmergencyContact>()
                    .eq(EmergencyContact::getUserId, userId)
                    .orderByDesc(EmergencyContact::getCreateTime)
                    .last("limit 1"), false);
            if (next != null) {
                next.setIsDefault(1);
                updateById(next);
            }
        }
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long contactId) {
        EmergencyContact contact = getOwnedContact(userId, contactId);
        clearDefault(userId);
        contact.setIsDefault(1);
        updateById(contact);
    }

    private EmergencyContact getOwnedContact(Long userId, Long contactId) {
        EmergencyContact contact = getById(contactId);
        if (contact == null) {
            throw new RuntimeException("联系人不存在");
        }
        if (!contact.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此联系人");
        }
        return contact;
    }

    private void clearDefault(Long userId) {
        baseMapper.update(null,
                new LambdaUpdateWrapper<EmergencyContact>()
                        .eq(EmergencyContact::getUserId, userId)
                        .set(EmergencyContact::getIsDefault, 0));
    }

    private long countUserContacts(Long userId) {
        return count(new LambdaQueryWrapper<EmergencyContact>()
                .eq(EmergencyContact::getUserId, userId));
    }

    private void validateContact(EmergencyContact contact) {
        if (contact.getUserId() == null) {
            throw new RuntimeException("用户信息不能为空");
        }
        if (isBlank(contact.getName())) {
            throw new RuntimeException("联系人姓名不能为空");
        }
        if (isBlank(contact.getPhone())) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!PHONE_PATTERN.matcher(contact.getPhone().trim()).matches()) {
            throw new RuntimeException("手机号格式不正确");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
