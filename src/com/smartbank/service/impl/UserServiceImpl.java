package com.smartbank.service.impl;

import com.smartbank.model.User;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.UserRepository;
import com.smartbank.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementation of the UserService interface.
 */
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    
    private final UserRepository userRepository;
    
    public UserServiceImpl() {
        this.userRepository = RepositoryFactory.getUserRepository();
    }
    
    @Override
    public User createUser(String username, String password, String role) throws IllegalArgumentException {
        return createUser(username, password, role, null, null, null);
    }
    
    @Override
    public User createUser(String username, String password, String role, 
                           String firstName, String lastName, String email) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        User user = new User(username, password, role, firstName, lastName, email);
        user = userRepository.save(user);
        
        LOGGER.info("Created user with username: " + username);
        return user;
    }
    
    @Override
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }
    
    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    @Override
    public User updateUserRole(String userId, String newRole) throws Exception {
        if (newRole == null || newRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception("User not found: " + userId);
        }
        
        User user = userOpt.get();
        user.setRole(newRole);
        user = userRepository.update(user);
        
        LOGGER.info("Updated role to '" + newRole + "' for user: " + user.getUsername());
        return user;
    }
    
    @Override
    public User changePassword(String userId, String oldPassword, String newPassword) throws Exception {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception("User not found: " + userId);
        }
        
        User user = userOpt.get();
        if (!user.checkPassword(oldPassword)) {
            throw new Exception("Incorrect old password");
        }
        
        // Create a new user with the same attributes but a new password
        User updatedUser = new User(
            user.getUsername(), 
            newPassword, 
            user.getRole(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail()
        );
        // Explicitly set the userId to maintain the same ID
        // Note: This assumes there's a way to set the userId in the User class
        // You might need to modify the User class accordingly
        
        updatedUser = userRepository.update(updatedUser);
        
        LOGGER.info("Changed password for user: " + user.getUsername());
        return updatedUser;
    }
    
    @Override
    public Optional<User> authenticate(String username, String password) {
        return userRepository.authenticate(username, password);
    }
    
    @Override
    public User updateUser(String userId, String role, String firstName, String lastName, String email) throws Exception {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception("User not found: " + userId);
        }
        
        User user = userOpt.get();
        if (role != null && !role.trim().isEmpty()) {
            user.setRole(role);
        }
        
        user.setFirstName(firstName);  // Nullable
        user.setLastName(lastName);    // Nullable
        user.setEmail(email);          // Nullable
        
        user = userRepository.update(user);
        
        LOGGER.info("Updated user profile for: " + user.getUsername());
        return user;
    }
    
    @Override
    public boolean deleteUser(String userId) {
        return userRepository.deleteById(userId);
    }
}