package com.smartbank.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the JPAUtil class.
 */
public class JPAUtilTest {

    private EntityManagerFactory mockFactory;
    private EntityManager mockEntityManager;

    @BeforeEach
    public void setUp() throws Exception {
        // Create mocks for EntityManagerFactory and EntityManager
        mockFactory = mock(EntityManagerFactory.class);
        mockEntityManager = mock(EntityManager.class);
        
        // Reset the static factory field in JPAUtil
        Field factoryField = JPAUtil.class.getDeclaredField("factory");
        factoryField.setAccessible(true);
        factoryField.set(null, null);
        
        // Set up the mock behavior
        when(mockFactory.createEntityManager()).thenReturn(mockEntityManager);
    }

    @Test
    @DisplayName("getEntityManager should return a new EntityManager")
    public void testGetEntityManager() throws Exception {
        // Arrange - Set the factory field directly
        Field factoryField = JPAUtil.class.getDeclaredField("factory");
        factoryField.setAccessible(true);
        factoryField.set(null, mockFactory);
        
        // Act
        EntityManager result = JPAUtil.getEntityManager();
        
        // Assert
        assertNotNull(result);
        assertEquals(mockEntityManager, result);
        verify(mockFactory).createEntityManager();
    }



    @Test
    @DisplayName("closeEntityManagerFactory should close the factory if it exists and is open")
    public void testCloseEntityManagerFactory() throws Exception {
        // Arrange - Set the factory field directly
        when(mockFactory.isOpen()).thenReturn(true);
        Field factoryField = JPAUtil.class.getDeclaredField("factory");
        factoryField.setAccessible(true);
        factoryField.set(null, mockFactory);
        
        // Act
        JPAUtil.closeEntityManagerFactory();
        
        // Assert
        verify(mockFactory).isOpen();
        verify(mockFactory).close();
    }
}
