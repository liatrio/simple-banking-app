package com.smartbank;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class for all SmartBank tests.
 * Provides common setup and teardown functionality.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseTest {
    
    /**
     * Setup method called before each test.
     * Override this method in subclasses to add specific setup logic.
     */
    @BeforeEach
    public void setUp() {
        // Common setup code for all tests
    }
    
    /**
     * Teardown method called after each test.
     * Override this method in subclasses to add specific teardown logic.
     */
    @AfterEach
    public void tearDown() {
        // Common teardown code for all tests
    }
}
