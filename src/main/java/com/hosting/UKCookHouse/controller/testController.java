package com.hosting.UKCookHouse.controller;

import com.hosting.UKCookHouse.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class testController {

    @Autowired
    private DatabaseConfig dbConfig;


    @PostMapping("/login1")
    public Map<String, Object> loginUser(@RequestBody Map<String, String> userData) {
        Map<String, Object> response = new HashMap<>();

        String email = userData.get("email");
        String password = userData.get("password");

        // Validate inputs
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Email and password are required!");
            return response;
        }

        String query = "SELECT user_id, username, email, password FROM users WHERE email = ?";

        try (Connection con = DriverManager.getConnection(dbConfig.getDbUrl(), dbConfig.getDbUsername(), dbConfig.getDbPassword());
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");

                //Verify password using BCrypt
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    response.put("status", "success");
                    response.put("message", "Email and password verified successfully!");
                    response.put("user_id", rs.getInt("user_id"));
                    response.put("username", rs.getString("username"));
                    response.put("email", rs.getString("email"));
                } else {
                    response.put("status", "error");
                    response.put("message", "Invalid password.");
                }
            } else {
                response.put("status", "error");
                response.put("message", "Invalid email or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Database connection error: " + e.getMessage());
        }
        return response;
    }
}
