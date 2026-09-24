package com.example.chat.user.controller;

import com.example.chat.common.response.ApiResponse;
import com.example.chat.config.CurrentUserProvider;
import com.example.chat.user.dto.UserResponse;
import com.example.chat.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    public UserController(UserService userService, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by username or display name")
    public ResponseEntity<ApiResponse<List<UserResponse>>> search(@RequestParam(defaultValue = "") String q) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        List<UserResponse> results = userService.searchUsers(q, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
