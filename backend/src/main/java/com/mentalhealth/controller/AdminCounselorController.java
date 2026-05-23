package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mentalhealth.entity.Counselor;
import com.mentalhealth.entity.CounselorAvailableSlot;
import com.mentalhealth.service.CounselorService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/counselors")
public class AdminCounselorController {

    @Autowired
    private CounselorService counselorService;

    @Autowired
    private JdbcTemplate jdbc;

    @GetMapping
    public ResultVO<List<Counselor>> list() {
        return ResultVO.success(counselorService.list(new LambdaQueryWrapper<Counselor>()
                .orderByAsc(Counselor::getId)));
    }

    @PostMapping
    public ResultVO<?> create(@RequestBody Counselor counselor) {
        if (counselor.getName() == null || counselor.getName().trim().isEmpty()) {
            return ResultVO.badRequest("老师姓名不能为空");
        }
        counselor.setName(counselor.getName().trim());
        counselor.setEnabled(1);
        counselorService.save(counselor);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    public ResultVO<?> update(@PathVariable Long id, @RequestBody Counselor counselor) {
        Counselor existing = counselorService.getById(id);
        if (existing == null) return ResultVO.badRequest("老师不存在");
        if (counselor.getName() != null) existing.setName(counselor.getName().trim());
        if (counselor.getSpecialty() != null) existing.setSpecialty(counselor.getSpecialty().trim());
        if (counselor.getIntro() != null) existing.setIntro(counselor.getIntro().trim());
        if (counselor.getEnabled() != null) existing.setEnabled(counselor.getEnabled());
        counselorService.updateById(existing);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    public ResultVO<?> delete(@PathVariable Long id) {
        counselorService.removeById(id);
        return ResultVO.success();
    }

    @PutMapping("/{id}/toggle")
    public ResultVO<?> toggle(@PathVariable Long id) {
        Counselor existing = counselorService.getById(id);
        if (existing == null) return ResultVO.badRequest("老师不存在");
        existing.setEnabled(existing.getEnabled() == 1 ? 0 : 1);
        counselorService.updateById(existing);
        return ResultVO.success();
    }

    /** 获取老师可预约时间段 */
    @GetMapping("/{id}/slots")
    public ResultVO<List<CounselorAvailableSlot>> getSlots(@PathVariable Long id) {
        List<CounselorAvailableSlot> slots = jdbc.query(
                "SELECT id, counselor_id, week_day, time_slot, enabled, created_at FROM counselor_available_slot WHERE counselor_id = ? ORDER BY week_day, time_slot",
                (rs, row) -> {
                    CounselorAvailableSlot s = new CounselorAvailableSlot();
                    s.setId(rs.getLong("id"));
                    s.setCounselorId(rs.getLong("counselor_id"));
                    s.setWeekDay(rs.getInt("week_day"));
                    s.setTimeSlot(rs.getString("time_slot"));
                    s.setEnabled(rs.getInt("enabled"));
                    return s;
                }, id);
        return ResultVO.success(slots);
    }

    /** 保存老师可预约时间段（全量替换） */
    @PostMapping("/{id}/slots")
    public ResultVO<?> saveSlots(@PathVariable Long id, @RequestBody java.util.Map<Integer, List<String>> weekSlots) {
        jdbc.update("DELETE FROM counselor_available_slot WHERE counselor_id = ?", id);
        for (java.util.Map.Entry<Integer, List<String>> entry : weekSlots.entrySet()) {
            int weekDay = entry.getKey();
            for (String slot : entry.getValue()) {
                jdbc.update("INSERT INTO counselor_available_slot (counselor_id, week_day, time_slot) VALUES (?, ?, ?)", id, weekDay, slot);
            }
        }
        return ResultVO.success();
    }
}
