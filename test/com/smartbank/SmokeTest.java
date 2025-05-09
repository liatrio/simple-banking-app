package com.smartbank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple smoke test to verify that the testing framework is set up correctly.
 */
public class SmokeTest {

    @Test
    @DisplayName("Verify JUnit 5 is working")
    public void testJunitIsWorking() {
        assertTrue(true, "JUnit 5 should be working");
    }
    
    @Test
    @DisplayName("Verify assertions are working")
    public void testAssertions() {
        // Basic assertions
        assertEquals(2, 1 + 1, "1 + 1 should equal 2");
        assertNotEquals(3, 1 + 1, "1 + 1 should not equal 3");
        
        // Boolean assertions
        assertTrue(true, "true should be true");
        assertFalse(false, "false should be false");
        
        // Null assertions
        assertNull(null, "null should be null");
        assertNotNull("Hello", "String should not be null");
        
        // Same object assertions
        Object obj = new Object();
        assertSame(obj, obj, "Objects should be the same");
        assertNotSame(obj, new Object(), "Objects should not be the same");
    }
}
