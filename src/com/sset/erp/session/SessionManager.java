package com.sset.erp.session;

import com.sset.erp.model.Role;
import com.sset.erp.model.User;

/**
 * Singleton class managing the global authenticated user session.
 * Demonstrates the Gang-of-Four Singleton Design Pattern in Object Oriented Programming.
 */
public class SessionManager {
    private static volatile SessionManager instance;
    private User currentUser;

    // Private constructor to prevent direct instantiation
    private SessionManager() {}

    /**
     * Thread-safe double-checked locking Singleton accessor.
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public synchronized void logout() {
        this.currentUser = null;
    }
}
