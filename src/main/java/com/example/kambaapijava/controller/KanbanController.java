package com.example.kambaapijava.controller;

import com.example.kambaapijava.dto.Dtos.*;
import com.example.kambaapijava.model.User;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")

public class KanbanController {
    private final JdbcTemplate db;

    public KanbanController(JdbcTemplate db) {

        this.db = db;
    }

    @PostMapping("/boards")
    public Map<String, Object> board(@Valid @RequestBody BoardInput x, @AuthenticationPrincipal User u) {
        return db.queryForMap("CALL sp_board_create(?,?)", x.name(), u.id());
    }

    @GetMapping("/boards")
    public List<Map<String, Object>> boards(@AuthenticationPrincipal User u) {
        return db.queryForList("CALL sp_board_list(?)", u.id());
    }

    @GetMapping("/boards/{id}")
    public Map<String, Object> board(@PathVariable long id, @AuthenticationPrincipal User u) {
        return db.queryForMap("CALL sp_board_get(?,?)", id, u.id());
    }

    @PutMapping("/boards/{id}")
    public void updateBoard(@PathVariable long id, @Valid @RequestBody BoardInput x, @AuthenticationPrincipal User u) {
        db.update("CALL sp_board_update(?,?,?)", id, x.name(), u.id());
    }

    @DeleteMapping("/boards/{id}")
    public void deleteBoard(@PathVariable long id, @AuthenticationPrincipal User u) {
        db.update("CALL sp_board_delete(?,?)", id, u.id());
    }

    @PostMapping("/tasks")
    public Map<String, Object> create(@Valid @RequestBody TaskInput x, @AuthenticationPrincipal User u) {
        return db.queryForMap("CALL sp_task_create(?,?,?,?,?,?,?,?)",
                x.title(), x.description(), Objects.requireNonNullElse(x.status(), "TODO"),
                Objects.requireNonNullElse(x.priority(), "MEDIUM"),
                x.dueDate(), x.boardId(), x.assigneeId(), u.id());
    }

    @GetMapping("/tasks")
    public Page<Map<String, Object>> tasks(@AuthenticationPrincipal User u, @RequestParam(required = false) Long boardId, @RequestParam(required = false) String status, @RequestParam(required = false) String priority, @RequestParam(required = false) Long assigneeId, @RequestParam(required = false) String q, @RequestParam(required = false) String dueFrom, @RequestParam(required = false) String dueTo, @RequestParam(defaultValue = "created_at") String sortBy, @RequestParam(defaultValue = "DESC") String sortDir, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        size = Math.min(Math.max(size, 1), 50);
        page = Math.max(page, 0);
        var rows = db.queryForList("CALL sp_task_search(?,?,?,?,?,?,?,?,?,?,?,?)", u.id(), boardId, status, priority, assigneeId, q, dueFrom, dueTo, sortBy, sortDir, page, size);
        long total = rows.isEmpty() ? 0 : ((Number) rows.get(0).getOrDefault("total_elements", 0)).longValue();
        return new Page<>(rows, page, size, total, (int) Math.ceil((double) total / size));
    }

    @GetMapping("/tasks/{id}")
    public Map<String, Object> task(@PathVariable long id, @AuthenticationPrincipal User u) {
        return db.queryForMap("CALL sp_task_get(?,?)", id, u.role().equals("ADMIN") ? null : u.id());
    }

    @PutMapping("/tasks/{id}")
    public void update(@PathVariable long id, @Valid @RequestBody TaskInput x, @AuthenticationPrincipal User u) {
        db.update("CALL sp_task_update(?,?,?,?,?,?,?,?,?)", id, x.title(), x.description(), x.status(), x.priority(), x.dueDate(), x.boardId(), x.assigneeId(), u.id());
    }

    @PatchMapping("/tasks/{id}/status")
    public void status(@PathVariable long id, @Valid @RequestBody TaskStatus x, @AuthenticationPrincipal User u) {
        db.update("CALL sp_task_update_status(?,?,?)", id, x.status(), u.id());
    }

    @DeleteMapping("/tasks/{id}")
    public void delete(@PathVariable long id, @AuthenticationPrincipal User u) {
        db.update("CALL sp_task_delete(?,?)", id, u.id());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Map<String, Object>> users(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        size = Math.min(Math.max(size, 1), 50);
        page = Math.max(page, 0);
        var rows = db.queryForList("CALL sp_user_list(?,?)", page, size);
        long total = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_elements")).longValue();
        return new Page<>(rows, page, size, total, (int) Math.ceil((double) total / size));
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public void role(@PathVariable long id, @Valid @RequestBody RoleInput x) {
        db.update("CALL sp_user_role_update(?,?)", id, x.role());
    }

    @PatchMapping("/users/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public void active(@PathVariable long id, @RequestBody ActiveInput x) {
        db.update("CALL sp_user_active_update(?,?)", id, x.active());
    }
}
