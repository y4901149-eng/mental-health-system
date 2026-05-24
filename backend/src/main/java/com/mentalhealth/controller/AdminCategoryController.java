package com.mentalhealth.controller;

import com.mentalhealth.entity.AssessmentCategory;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    @Autowired
    private JdbcTemplate jdbc;

    @GetMapping
    public ResultVO<List<AssessmentCategory>> list() {
        List<AssessmentCategory> list = jdbc.query(
                "SELECT id, name, sort_order, enabled, created_at FROM assessment_category ORDER BY sort_order, id",
                (rs, row) -> {
                    AssessmentCategory c = new AssessmentCategory();
                    c.setId(rs.getLong("id"));
                    c.setName(rs.getString("name"));
                    c.setSortOrder(rs.getInt("sort_order"));
                    c.setEnabled(rs.getInt("enabled"));
                    return c;
                });
        return ResultVO.success(list);
    }

    @PostMapping
    public ResultVO<?> create(@RequestBody AssessmentCategory category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return ResultVO.badRequest("板块名称不能为空");
        }
        jdbc.update("INSERT INTO assessment_category (name, sort_order) VALUES (?, ?)",
                category.getName().trim(), category.getSortOrder() != null ? category.getSortOrder() : 0);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    public ResultVO<?> update(@PathVariable Long id, @RequestBody AssessmentCategory category) {
        if (category.getName() != null) {
            jdbc.update("UPDATE assessment_category SET name = ? WHERE id = ?", category.getName().trim(), id);
        }
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    public ResultVO<?> delete(@PathVariable Long id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM assessment WHERE category_id = ?", Integer.class, id);
        if (count != null && count > 0) {
            return ResultVO.badRequest("该板块下仍有 " + count + " 个测评，不能删除");
        }
        jdbc.update("DELETE FROM assessment_category WHERE id = ?", id);
        return ResultVO.success();
    }

    @PutMapping("/{id}/toggle")
    public ResultVO<?> toggle(@PathVariable Long id) {
        Integer enabled = jdbc.queryForObject("SELECT enabled FROM assessment_category WHERE id = ?", Integer.class, id);
        if (enabled == null) return ResultVO.badRequest("板块不存在");
        jdbc.update("UPDATE assessment_category SET enabled = ? WHERE id = ?", enabled == 1 ? 0 : 1, id);
        return ResultVO.success();
    }
}
