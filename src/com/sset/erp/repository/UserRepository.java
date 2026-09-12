package com.sset.erp.repository;

import com.sset.erp.model.Role;
import com.sset.erp.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface providing data access abstraction for User entities.
 * Demonstrates Abstraction: separates business services from underlying storage engine.
 */
public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(Role role);
    User save(User user);
    boolean deleteById(String id);
    boolean existsByUsername(String username);
    long count();
    long countByRole(Role role);
}
