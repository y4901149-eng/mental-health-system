package com.mentalhealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mentalhealth.entity.AssessmentRecord;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 评估记录 Mapper 接口
 */
public interface AssessmentRecordMapper extends BaseMapper<AssessmentRecord> {

    /** 查询用户的历史评估记录（包含量表标题） */
    @Select("SELECT ar.*, a.title as assessmentTitle FROM assessment_record ar " +
            "LEFT JOIN assessment a ON ar.assessment_id = a.id " +
            "WHERE ar.user_id = #{userId} ORDER BY ar.submit_time DESC")
    List<Map<String, Object>> selectUserRecordsWithTitle(Long userId);
}
