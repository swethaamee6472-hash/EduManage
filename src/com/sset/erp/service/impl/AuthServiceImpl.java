package com.sset.erp.service.impl;

import com.sset.erp.dto.AuthResult;
import com.sset.erp.dto.UserRegistrationDTO;
import com.sset.erp.model.*;
import com.sset.erp.repository.FileUserRepository;
import com.sset.erp.repository.UserRepository;
import com.sset.erp.service.AuthService;
import com.sset.erp.session.SessionManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete implementation of AuthService.
 * Coordinates between repositories, session management, and validation logic.
 * Demonstrates Polymorphism and Dependency Injection.
 */
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthServiceImpl() {
        this(new FileUserRepository(), SessionManager.getInstance());
    }

    public AuthServiceImpl(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public AuthResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return AuthResult.failure("Please enter your username.");
        }
        if (password == null || password.isEmpty()) {
            return AuthResult.failure("Please enter your password.");
        }

        Optional<User> optionalUser = userRepository.findByUsername(username.trim());
        if (!optionalUser.isPresent()) {
            return AuthResult.failure("Invalid username or password.");
        }

        User user = optionalUser.get();

        if (!user.isActive()) {
            return AuthResult.failure("Your account has been deactivated. Please contact your college administrator.");
        }

        if (!user.validatePassword(password)) {
            return AuthResult.failure("Invalid username or password.");
        }

        // Authentication successful - set active session
        sessionManager.setCurrentUser(user);
        return AuthResult.success("Welcome back, " + user.getFullName() + "!", user);
    }

    @Override
    public AuthResult register(UserRegistrationDTO dto) {
        if (dto == null) {
            return AuthResult.failure("Registration data cannot be empty.");
        }

        List<String> validationErrors = dto.validate();
        if (!validationErrors.isEmpty()) {
            return AuthResult.failure(String.join("\n", validationErrors));
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            return AuthResult.failure("Username '" + dto.getUsername() + "' is already taken. Please choose another.");
        }

        if (dto.getEmail() != null && userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return AuthResult.failure("An account with email '" + dto.getEmail() + "' already exists.");
        }

        // Factory Pattern: Instantiate concrete subclass based on Role
        User newUser = createUserFromDTO(dto);
        userRepository.save(newUser);

        return AuthResult.success("Registration successful! You can now log in.", newUser);
    }

    /**
     * Factory method demonstrating Polymorphism to construct appropriate subclass of User.
     */
    private User createUserFromDTO(UserRegistrationDTO dto) {
        String id = "USR-" + dto.getRole().name().substring(0, 3) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        switch (dto.getRole()) {
            case ADMIN:
                return new AdminUser(
                    id, dto.getUsername(), dto.getPassword(),
                    dto.getFullName(), dto.getEmail(), dto.getAdminLevel()
                );
            case FACULTY:
                return new FacultyUser(
                    id, dto.getUsername(), dto.getPassword(),
                    dto.getFullName(), dto.getEmail(),
                    dto.getEmployeeId(), dto.getDepartment(), dto.getDesignation()
                );
            case STUDENT:
                return new StudentUser(
                    id, dto.getUsername(), dto.getPassword(),
                    dto.getFullName(), dto.getEmail(),
                    dto.getRollNumber(), dto.getDepartment(), dto.getSemester(), dto.getBatch()
                );
            case PARENT:
                return new ParentUser(
                    id, dto.getUsername(), dto.getPassword(),
                    dto.getFullName(), dto.getEmail(),
                    dto.getStudentRollNumber(), dto.getEmergencyContact()
                );
            default:
                throw new IllegalArgumentException("Unsupported role: " + dto.getRole());
        }
    }

    @Override
    public boolean toggleUserStatus(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActive(!user.isActive());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean updatePassword(String username, String currentPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (!optionalUser.isPresent()) return false;

        User user = optionalUser.get();
        if (!user.validatePassword(currentPassword)) return false;

        user.setPassword(newPassword);
        userRepository.save(user);
        return true;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null || user.getUsername() == null) {
            return false;
        }
        userRepository.save(user);
        return true;
    }

    @Override
    public long getUserCount() {
        return userRepository.count();
    }

    @Override
    public long getUserCountByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    public void logout() {
        sessionManager.logout();
    }
}
