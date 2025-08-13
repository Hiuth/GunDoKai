package org.example.gundokai.controller;

import java.util.List;

import org.example.gundokai.dto.request.ResetPasswordRequest;
import org.example.gundokai.dto.request.UserCreationRequest;
import org.example.gundokai.dto.request.UserUpdateRequest;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.EmailResponse;
import org.example.gundokai.dto.respone.UserResponse;
import org.example.gundokai.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping("/send-code")
    public ApiResponse<String> sendCode(@RequestParam("email") String email) {
        userService.sendVerificationCode(email);
        return ApiResponse.<String>builder()
                .message("Mã xác thực đã được gửi về " + email)
                .build();
    }

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUserById(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping("/my-profile")
    ApiResponse<UserResponse> updateMyProfile(@RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateMyProfile(request))
                .build();
    }

    @PostMapping("/request-password-reset")
    public ApiResponse<String> requestPasswordReset(@RequestParam("email") String email) {
        userService.sendPasswordResetCode(email);
        return ApiResponse.<String>builder()
                .message("Mã xác nhận đổi mật khẩu đã được gửi về " + email)
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        userService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ApiResponse.<Void>builder()
                .message("Password reset successful")
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<EmailResponse>> searchByEmail(@RequestParam("keyword") String keyword) {
        List<EmailResponse> result = userService.searchByEmail(keyword);
        return ApiResponse.<List<EmailResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/count")
    ApiResponse<Long> getUserCount() {
        return ApiResponse.<Long>builder()
                .result(userService.countUsers())
                .build();
    }
}
