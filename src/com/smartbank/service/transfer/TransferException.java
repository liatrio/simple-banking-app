package com.smartbank.service.transfer;

/**
 * Exception thrown when a transfer operation fails.
 */
public class TransferException extends Exception {
    private final TransferErrorCode errorCode;
    
    /**
     * Create a new TransferException with a message.
     * 
     * @param message The error message
     */
    public TransferException(String message) {
        this(message, TransferErrorCode.GENERAL_ERROR);
    }
    
    /**
     * Create a new TransferException with a message and cause.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public TransferException(String message, Throwable cause) {
        this(message, cause, TransferErrorCode.GENERAL_ERROR);
    }
    
    /**
     * Create a new TransferException with a message and error code.
     * 
     * @param message The error message
     * @param errorCode The error code
     */
    public TransferException(String message, TransferErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Create a new TransferException with a message, cause, and error code.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     * @param errorCode The error code
     */
    public TransferException(String message, Throwable cause, TransferErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Get the error code.
     * 
     * @return The error code
     */
    public TransferErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Enumeration of transfer error codes.
     */
    public enum TransferErrorCode {
        GENERAL_ERROR,
        INSUFFICIENT_FUNDS,
        ACCOUNT_NOT_FOUND,
        INVALID_AMOUNT,
        EXCEEDS_TRANSFER_LIMIT,
        INVALID_ACCOUNT_TYPE,
        SAME_ACCOUNT,
        TRANSACTION_FAILED
    }
}