package com.example.kambaapijava.repository;

import com.example.kambaapijava.model.User;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class UserRepository {
    private final JdbcTemplate db;

    public UserRepository(JdbcTemplate db) {
        this.db = db;
    }

    public User byUsername(String name) {
        try {
            return db.query("CALL sp_user_find_by_username(?)", ps -> ps.setString(1, name), (rs, n) -> new User(rs.getLong("id"), rs.getString("username"), rs.getString("email"), rs.getString("password_hash"), rs.getString("role"), rs.getBoolean("active"))).stream().findFirst().orElse(null);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void create(String u, String e, String p) {
        db.update("CALL sp_user_create(?,?,?)", u, e, p);
    }
}
