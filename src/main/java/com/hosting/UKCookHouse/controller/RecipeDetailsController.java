package com.hosting.UKCookHouse.controller;

import com.hosting.UKCookHouse.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.*;

@RestController
public class RecipeDetailsController {

    @Autowired
    private DatabaseConfig dbConfig;

    @CrossOrigin(origins = "*")
    @GetMapping("/recipe")
    public Map<String, Object> getRecipeDetails(@RequestParam String name) {
        Map<String, Object> recipeData = new HashMap<>();

        String recipeQuery = "SELECT * FROM recipes WHERE recipe_name = ? OR recipe_name_kn = ?";
        String stepsQuery = "SELECT step_number, step, step_number_kn, step_kn FROM recipe_steps WHERE r_s_id = ? ORDER BY step_number";
        String ingredientsQuery = "SELECT ing_name FROM recipe_ingredients WHERE r_i_id = ?";

        try (Connection con = DriverManager.getConnection(dbConfig.getDbUrl(), dbConfig.getDbUsername(), dbConfig.getDbPassword());
             PreparedStatement recipeStmt = con.prepareStatement(recipeQuery)) {

            recipeStmt.setString(1, name);
            recipeStmt.setString(2, name);

            ResultSet rs = recipeStmt.executeQuery();

            if (rs.next()) {
                int recipeId = rs.getInt("recipe_id");

                recipeData.put("recipe_id", recipeId);
                recipeData.put("recipe_name", rs.getString("recipe_name"));
                recipeData.put("recipe_name_kn", rs.getString("recipe_name_kn"));
                recipeData.put("recipe_image", rs.getString("recipe_image"));
                recipeData.put("recipe_video", rs.getString("recipe_video"));
                recipeData.put("recipe_description", rs.getString("recipe_description"));
                recipeData.put("recipe_description_kn", rs.getString("recipe_description_kn"));
                recipeData.put("calories", rs.getString("calories"));
                recipeData.put("protein", rs.getString("protein"));
                recipeData.put("fat", rs.getString("fat"));
                recipeData.put("carbohydrate", rs.getString("carbohydrate"));
                recipeData.put("sugar", rs.getString("sugar"));
                recipeData.put("cholesterol", rs.getString("cholesterol"));
                recipeData.put("cooking_time", rs.getInt("cooking_time"));
                recipeData.put("difficulty_level", rs.getString("difficulty_level"));
                recipeData.put("famous_place", rs.getString("famous_place"));

                // ✅ Fetch recipe steps
                List<Map<String, Object>> steps = new ArrayList<>();
                try (PreparedStatement stepsStmt = con.prepareStatement(stepsQuery)) {
                    stepsStmt.setInt(1, recipeId);
                    ResultSet stepsRs = stepsStmt.executeQuery();
                    while (stepsRs.next()) {
                        Map<String, Object> step = new HashMap<>();
                        step.put("step_number", stepsRs.getInt("step_number"));
                        step.put("step", stepsRs.getString("step"));
                        step.put("step_number_kn", stepsRs.getString("step_number_kn"));
                        step.put("step_kn", stepsRs.getString("step_kn"));
                        steps.add(step);
                    }
                }
                recipeData.put("steps", steps);

                // ✅ Fetch recipe ingredients
                List<String> ingredients = new ArrayList<>();
                try (PreparedStatement ingStmt = con.prepareStatement(ingredientsQuery)) {
                    ingStmt.setInt(1, recipeId);
                    ResultSet ingRs = ingStmt.executeQuery();
                    while (ingRs.next()) {
                        ingredients.add(ingRs.getString("ing_name"));
                    }
                }
                recipeData.put("ingredients", ingredients);

            } else {
                recipeData.put("error", "Recipe not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            recipeData.put("error", "Database error: " + e.getMessage());
        }

        return recipeData;
    }
}
