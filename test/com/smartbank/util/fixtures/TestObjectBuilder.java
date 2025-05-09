package com.smartbank.util.fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for test object builders.
 * This class provides a foundation for implementing the Builder pattern
 * for test objects, making it easier to create complex objects for testing.
 * 
 * @param <T> The type of object being built
 * @param <B> The builder type (for method chaining)
 */
public abstract class TestObjectBuilder<T, B extends TestObjectBuilder<T, B>> {
    
    /**
     * Build the object with the current builder state.
     * 
     * @return The built object
     */
    public abstract T build();
    
    /**
     * Get this builder cast to the correct type for method chaining.
     * 
     * @return This builder cast to the correct type
     */
    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }
    
    /**
     * Create a list of objects using this builder.
     * 
     * @param count Number of objects to create
     * @return A list of built objects
     */
    public List<T> buildList(int count) {
        List<T> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(build());
        }
        return result;
    }
}
