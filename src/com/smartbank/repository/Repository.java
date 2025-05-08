package com.smartbank.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface that defines common CRUD operations.
 * @param <T> The entity type
 * @param <ID> The type of the entity's ID
 */
public interface Repository<T, ID> {
    /**
     * Save an entity to the database.
     * @param entity The entity to save
     * @return The saved entity
     */
    T save(T entity);
    
    /**
     * Update an existing entity.
     * @param entity The entity to update
     * @return The updated entity
     */
    T update(T entity);
    
    /**
     * Find an entity by its ID.
     * @param id The ID of the entity
     * @return An Optional containing the entity if found, or empty if not found
     */
    Optional<T> findById(ID id);
    
    /**
     * Find all entities.
     * @return A list of all entities
     */
    List<T> findAll();
    
    /**
     * Delete an entity.
     * @param entity The entity to delete
     */
    void delete(T entity);
    
    /**
     * Delete an entity by its ID.
     * @param id The ID of the entity to delete
     * @return true if the entity was deleted, false if not found
     */
    boolean deleteById(ID id);
}