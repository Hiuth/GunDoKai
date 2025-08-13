package org.example.gundokai.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.example.gundokai.constant.PredefinedRole;
import org.example.gundokai.dto.request.UserCreationRequest;
import org.example.gundokai.dto.request.UserUpdateRequest;
import org.example.gundokai.dto.respone.EmailResponse;
import org.example.gundokai.dto.respone.UserResponse;
import org.example.gundokai.entity.Role;
import org.example.gundokai.entity.User;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.UserMapper;
import org.example.gundokai.repository.RoleRepository;
import org.example.gundokai.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    EmailVerification emailVerification;

    public void sendVerificationCode(String email) {
        String trimmedEmail = email.trim();
        log.debug("Checking email [{}] status", trimmedEmail);

        boolean emailExists = userRepository.existsByEmail(trimmedEmail);

        if (emailExists) {
            log.debug("Real account exists for email [{}]", trimmedEmail);
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTED);
        }

        emailVerification.sendCode(trimmedEmail);
    }

    public UserResponse createUser(UserCreationRequest request) {
        String code = request.getCode() == null ? "" : request.getCode().trim();
        if (!emailVerification.verifyCode(request.getEmail(), code)) {
            log.debug("Verification failed for email [{}], code [{}]", request.getEmail(), code);
            throw new AppException(ErrorCode.VERIFICATION_CODE_INVALID);
        }

        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String identifier = context.getAuthentication().getName();

        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateMyProfile(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUserEmail = context.getAuthentication().getName();
        
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        userMapper.updateUser(user, request);
        
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public void sendPasswordResetCode(String email) {
        String trimmedEmail = email.trim();
        log.debug("Checking if email [{}] exists for password reset", trimmedEmail);

        // Kiểm tra xem email có tồn tại không
        if (!userRepository.existsByEmail(trimmedEmail)) {
            log.debug("Email [{}] does not exist", trimmedEmail);
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        // Gửi mã xác nhận qua email
        emailVerification.sendCode(trimmedEmail);
    }

    public void resetPassword(String email, String code, String newPassword) {
        String trimmedEmail = email.trim();
        String trimmedCode = code == null ? "" : code.trim();

        // Xác minh mã
        if (!emailVerification.verifyCode(trimmedEmail, trimmedCode)) {
            log.debug("Password reset failed: Invalid code for email [{}]", trimmedEmail);
            throw new AppException(ErrorCode.VERIFICATION_CODE_INVALID);
        }

        // Tìm tài khoản
         User user = userRepository.findByEmail(trimmedEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Mã hóa mật khẩu mới
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(newPassword));

        // Lưu tài khoản với mật khẩu mới
        userRepository.save(user);
        log.debug("Password reset successful for email [{}]", trimmedEmail);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<EmailResponse> searchByEmail(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        return userRepository.searchByEmail(keyword.trim())
                .stream()
                .map(account -> EmailResponse.builder().email(account.getEmail()).build())
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Long countUsers() {
        return userRepository.count();
    }
}
