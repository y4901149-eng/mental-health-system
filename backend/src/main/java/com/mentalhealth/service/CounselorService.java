package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Counselor;
import java.util.List;

public interface CounselorService extends IService<Counselor> {
    /** 获取启用的咨询师列表 */
    List<Counselor> getEnabledCounselors();
}
