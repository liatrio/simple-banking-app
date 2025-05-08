package com.smartbank.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Entity representing a request to change a credit limit.
 * Part of the approval workflow for credit limit changes.
 */
@Entity
@Table(name = "credit_limit_change_requests")
public class CreditLimitChangeRequest {
    
    // Status values for change requests
    public enum Status {
        PENDING,
        APPROVED,
        REJECTED,
        EXPIRED,
        CANCELLED
    }
    
    // Source of the change request
    public enum Source {
        USER_REQUESTED,
        SYSTEM_AUTOMATIC,
        ADMIN_INITIATED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column(nullable = false)
    private long accountNumber;
    
    @Column(nullable = false)
    private double currentCreditLimit;
    
    @Column(nullable = false)
    private double requestedCreditLimit;
    
    @Column(nullable = false)
    private String requestDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Source source;
    
    @Column(nullable = false)
    private String requestedBy;
    
    @Column
    private String approvedBy;
    
    @Column
    private String rejectedBy;
    
    @Column
    private String decisionDate;
    
    @Column(nullable = false)
    private String reason;
    
    @Column
    private String decisionComments;
    
    @Column
    private int creditScoreAtRequest;
    
    @Column(nullable = false)
    private boolean automaticRequest;
    
    // Default constructor required by JPA
    protected CreditLimitChangeRequest() {
    }
    
    public CreditLimitChangeRequest(
            long accountNumber,
            double currentCreditLimit,
            double requestedCreditLimit,
            String requestedBy,
            Source source,
            String reason,
            int creditScoreAtRequest,
            boolean automaticRequest) {
        this.accountNumber = accountNumber;
        this.currentCreditLimit = currentCreditLimit;
        this.requestedCreditLimit = requestedCreditLimit;
        this.requestDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.status = Status.PENDING;
        this.requestedBy = requestedBy;
        this.source = source;
        this.reason = reason;
        this.creditScoreAtRequest = creditScoreAtRequest;
        this.automaticRequest = automaticRequest;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public long getAccountNumber() {
        return accountNumber;
    }
    
    public double getCurrentCreditLimit() {
        return currentCreditLimit;
    }
    
    public double getRequestedCreditLimit() {
        return requestedCreditLimit;
    }
    
    public void setRequestedCreditLimit(double requestedCreditLimit) {
        this.requestedCreditLimit = requestedCreditLimit;
    }
    
    public String getRequestDate() {
        return requestDate;
    }
    
    public LocalDateTime getRequestDateTime() {
        return LocalDateTime.parse(requestDate);
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Source getSource() {
        return source;
    }
    
    public String getRequestedBy() {
        return requestedBy;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
        this.decisionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    public String getRejectedBy() {
        return rejectedBy;
    }
    
    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
        this.decisionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    public String getDecisionDate() {
        return decisionDate;
    }
    
    public LocalDateTime getDecisionDateTime() {
        return decisionDate != null ? LocalDateTime.parse(decisionDate) : null;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getDecisionComments() {
        return decisionComments;
    }
    
    public void setDecisionComments(String decisionComments) {
        this.decisionComments = decisionComments;
    }
    
    public int getCreditScoreAtRequest() {
        return creditScoreAtRequest;
    }
    
    public boolean isAutomaticRequest() {
        return automaticRequest;
    }
    
    /**
     * Calculate the amount of change (positive for increase, negative for decrease)
     * @return The difference between requested and current credit limits
     */
    @Transient
    public double getChangeAmount() {
        return requestedCreditLimit - currentCreditLimit;
    }
    
    /**
     * Calculate the percentage change
     * @return The percentage change from current to requested credit limit
     */
    @Transient
    public double getPercentageChange() {
        return (requestedCreditLimit - currentCreditLimit) / currentCreditLimit * 100;
    }
    
    /**
     * Check if this request is for a credit limit increase
     * @return true if requested limit is higher than current limit
     */
    @Transient
    public boolean isIncreaseRequest() {
        return requestedCreditLimit > currentCreditLimit;
    }
    
    /**
     * Check if this request is for a credit limit decrease
     * @return true if requested limit is lower than current limit
     */
    @Transient
    public boolean isDecreaseRequest() {
        return requestedCreditLimit < currentCreditLimit;
    }
    
    /**
     * Get the number of days this request has been pending
     * @return The number of days since the request was created, if still pending
     */
    @Transient
    public long getDaysPending() {
        if (status != Status.PENDING) {
            return 0;
        }
        
        LocalDateTime requestDateTime = LocalDateTime.parse(requestDate);
        LocalDateTime now = LocalDateTime.now();
        return java.time.temporal.ChronoUnit.DAYS.between(requestDateTime, now);
    }
    
    /**
     * Approve this change request
     * @param approvedBy The ID of the user approving the request
     * @param comments Any comments related to the approval
     */
    public void approve(String approvedBy, String comments) {
        this.status = Status.APPROVED;
        this.approvedBy = approvedBy;
        this.decisionComments = comments;
        this.decisionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * Reject this change request
     * @param rejectedBy The ID of the user rejecting the request
     * @param comments The reason for rejection
     */
    public void reject(String rejectedBy, String comments) {
        this.status = Status.REJECTED;
        this.rejectedBy = rejectedBy;
        this.decisionComments = comments;
        this.decisionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * Cancel this change request
     */
    public void cancel() {
        this.status = Status.CANCELLED;
        this.decisionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * Mark this request as expired
     */
    public void expire() {
        this.status = Status.EXPIRED;
        this.decisionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditLimitChangeRequest that = (CreditLimitChangeRequest) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "CreditLimitChangeRequest{" +
                "id=" + id +
                ", accountNumber=" + accountNumber +
                ", currentCreditLimit=" + currentCreditLimit +
                ", requestedCreditLimit=" + requestedCreditLimit +
                ", status=" + status +
                ", source=" + source +
                ", requestDate='" + requestDate + '\'' +
                '}';
    }
}