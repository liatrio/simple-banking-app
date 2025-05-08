package com.smartbank.repository;

import com.smartbank.service.interest.InterestCalculationRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for interest calculation records.
 */
public interface InterestCalculationRecordRepository {
    
    /**
     * Save a new interest calculation record.
     * 
     * @param record The record to save
     * @return The saved record
     */
    InterestCalculationRecord save(InterestCalculationRecord record);
    
    /**
     * Update an existing interest calculation record.
     * 
     * @param record The record to update
     * @return The updated record
     */
    InterestCalculationRecord update(InterestCalculationRecord record);
    
    /**
     * Find a record by its ID.
     * 
     * @param recordId The record ID
     * @return An Optional containing the record if found, or empty if not found
     */
    Optional<InterestCalculationRecord> findById(long recordId);
    
    /**
     * Find all interest calculation records for a specific account.
     * 
     * @param accountNumber The account number
     * @return A list of interest calculation records
     */
    List<InterestCalculationRecord> findByAccountNumber(long accountNumber);
    
    /**
     * Find interest calculation records for a specific account within a date range.
     * 
     * @param accountNumber The account number
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return A list of interest calculation records
     */
    List<InterestCalculationRecord> findByAccountNumberAndDateRange(long accountNumber, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find interest calculation records by operation type.
     * 
     * @param operationType The operation type
     * @return A list of interest calculation records
     */
    List<InterestCalculationRecord> findByOperationType(InterestCalculationRecord.OperationType operationType);
    
    /**
     * Find interest calculation records by date.
     * 
     * @param date The date
     * @return A list of interest calculation records
     */
    List<InterestCalculationRecord> findByDate(LocalDate date);
    
    /**
     * Find interest calculation records within a date range.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return A list of interest calculation records
     */
    List<InterestCalculationRecord> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Delete a record by its ID.
     * 
     * @param recordId The record ID
     * @return true if the record was deleted, false if it did not exist
     */
    boolean deleteById(long recordId);
    
    /**
     * Delete all records for a specific account.
     * 
     * @param accountNumber The account number
     * @return The number of records deleted
     */
    int deleteByAccountNumber(long accountNumber);
    
    /**
     * Find all interest calculation records.
     * 
     * @return A list of all interest calculation records
     */
    List<InterestCalculationRecord> findAll();
}