package com.smartbank.model;

import com.smartbank.util.fixtures.TestDataFactory;
import com.smartbank.util.fixtures.TestObjectBuilder;

/**
 * Builder for creating User instances for testing.
 * This builder allows creating User objects with default or custom values.
 */
public class UserBuilder extends TestObjectBuilder<User, UserBuilder> {
    private String username;
    private String password;
    private String role;
    private String firstName;
    private String lastName;
    private String email;
    
    /**
     * Create a new UserBuilder with default values.
     */
    public UserBuilder() {
        this.username = TestDataFactory.randomString("user");
        this.password = "password123"; // Default test password
        this.role = "customer";
        this.firstName = TestDataFactory.randomString("firstName");
        this.lastName = TestDataFactory.randomString("lastName");
        this.email = TestDataFactory.randomEmail();
    }
    
    /**
     * Create a new UserBuilder with values from an existing User.
     * 
     * @param user The User to copy values from
     * @return A new UserBuilder
     */
    public static UserBuilder from(User user) {
        UserBuilder builder = new UserBuilder();
        builder.username = user.getUsername();
        builder.password = user.getPasswordHash(); // Note: this is the hash, not the original password
        builder.role = user.getRole();
        builder.firstName = user.getFirstName();
        builder.lastName = user.getLastName();
        builder.email = user.getEmail();
        return builder;
    }
    
    /**
     * Set the username.
     * 
     * @param username The username
     * @return This builder
     */
    public UserBuilder withUsername(String username) {
        this.username = username;
        return self();
    }
    
    /**
     * Set the password.
     * 
     * @param password The password
     * @return This builder
     */
    public UserBuilder withPassword(String password) {
        this.password = password;
        return self();
    }
    
    /**
     * Set the role.
     * 
     * @param role The role
     * @return This builder
     */
    public UserBuilder withRole(String role) {
        this.role = role;
        return self();
    }
    
    /**
     * Set as admin role.
     * 
     * @return This builder
     */
    public UserBuilder asAdmin() {
        this.role = "admin";
        return self();
    }
    
    /**
     * Set as customer role.
     * 
     * @return This builder
     */
    public UserBuilder asCustomer() {
        this.role = "customer";
        return self();
    }
    
    /**
     * Set the first name.
     * 
     * @param firstName The first name
     * @return This builder
     */
    public UserBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return self();
    }
    
    /**
     * Set the last name.
     * 
     * @param lastName The last name
     * @return This builder
     */
    public UserBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return self();
    }
    
    /**
     * Set the email.
     * 
     * @param email The email
     * @return This builder
     */
    public UserBuilder withEmail(String email) {
        this.email = email;
        return self();
    }
    
    /**
     * Build a User instance with the current builder state.
     * 
     * @return A new User instance
     */
    @Override
    public User build() {
        return new User(username, password, role, firstName, lastName, email);
    }
}
