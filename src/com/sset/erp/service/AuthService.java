package com.sset.erp.service;

import com.sset.erp.dto.AuthResult;
import com.sset.erp.dto.UserRegistrationDTO;
import com.sset.erp.model.Role;
import com.sset.erp.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface defining authentication and user management contracts.
 * Demonstrates Abstraction and Interface-based design in Java OOP.
 */
public interface AuthService {
    AuthResult login(String username, String password);
    AuthResult register(UserRegistrationDTO dto);
    boolean toggleUserStatus(String userId);
    boolean updatePassword(String username, String currentPassword, String newPassword);
    List<User> getAllUsers();
    List<User> getUsersByRole(Role role);
    Optional<User> getUserById(String userId);
    long getUserCount();
    long getUserCountByRole(Role role);
    void logout();
}
