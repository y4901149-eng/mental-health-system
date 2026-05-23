package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.EmergencyContact;
import java.util.List;

public interface EmergencyContactService extends IService<EmergencyContact> {
    List<EmergencyContact> getUserContacts(Long userId);
    EmergencyContact getDefaultContact(Long userId);
    void saveContact(EmergencyContact contact);
    void updateContact(EmergencyContact contact);
    void deleteContact(Long userId, Long contactId);
    void setDefault(Long userId, Long contactId);
}
