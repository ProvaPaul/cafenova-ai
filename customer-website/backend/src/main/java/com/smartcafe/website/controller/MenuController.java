package com.smartcafe.website.controller;

import com.smartcafe.website.dto.response.ApiResponse;
import com.smartcafe.website.dto.response.MenuItemResponse;
import com.smartcafe.website.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<?>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getCategories()));
    }

    @GetMapping("/menu")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenu(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String q) {
        List<MenuItemResponse> result;
        if (q != null && !q.isBlank())
            result = menuService.search(q);
        else if (categoryId != null)
            result = menuService.getByCategory(categoryId);
        else
            result = menuService.getAllItems();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/menu/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getById(id)));
    }
}
