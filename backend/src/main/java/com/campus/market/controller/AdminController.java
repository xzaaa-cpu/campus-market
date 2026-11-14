package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.config.CurrentUser;
import com.campus.market.config.RequireAdmin;
import com.campus.market.dto.CategoryRequest;
import com.campus.market.dto.ItemStatusRequest;
import com.campus.market.dto.RoleUpdateRequest;
import com.campus.market.entity.Category;
import com.campus.market.service.AdminService;
import com.campus.market.vo.ItemVO;
import com.campus.market.vo.OrderVO;
import com.campus.market.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 管理后台接口，仅 ADMIN 角色可访问。
 */
@RestController
@RequestMapping("/api/admin")
@RequireAdmin
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.ok(adminService.summary());
    }

    @GetMapping("/users")
    public Result<PageResult<UserVO>> users(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String role,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.users(keyword, role, page, size));
    }

    @PutMapping("/users/{id}/role")
    public Result<UserVO> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return Result.ok(adminService.updateRole(CurrentUser.requireUserId(), id, request.getRole()));
    }

    @GetMapping("/items")
    public Result<PageResult<ItemVO>> items(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.items(keyword, status, page, size));
    }

    @PostMapping("/items/{id}/status")
    public Result<Void> changeItemStatus(@PathVariable Long id, @Valid @RequestBody ItemStatusRequest request) {
        adminService.changeItemStatus(id, request.getStatus());
        return Result.ok();
    }

    @GetMapping("/orders")
    public Result<PageResult<OrderVO>> orders(@RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.orders(status, page, size));
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(adminService.categories());
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        return Result.ok(adminService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return Result.ok(adminService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.ok();
    }
}
