package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Counselor;
import com.mentalhealth.mapper.CounselorMapper;
import com.mentalhealth.service.CounselorService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CounselorServiceImpl extends ServiceImpl<CounselorMapper, Counselor> implements CounselorService {

    @Override
    public List<Counselor> getEnabledCounselors() {
        return list(new LambdaQueryWrapper<Counselor>()
                .eq(Counselor::getEnabled, 1)
                .orderByAsc(Counselor::getId));
    }
}
