package com.smartbank.model;

import javax.persistence.*;
import java.util.Date;
import java.io.Serializable;

/**
 * Entity representing a saved search criteria in the user's search history.
 */
@Entity
@Table(name = "search_history")
public class SearchHistory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String searchCriteria;
    
    @Column(nullable = false)
    private String searchType;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date lastUsedDate;
    
    @Column(nullable = false)
    private int useCount;
    
    @Column(nullable = false)
    private boolean isFavorite;
    
    // Default constructor required by JPA
    protected SearchHistory() {
    }
    
    public SearchHistory(String userId, String name, String searchCriteria, String searchType) {
        this.userId = userId;
        this.name = name;
        this.searchCriteria = searchCriteria;
        this.searchType = searchType;
        this.createdDate = new Date();
        this.lastUsedDate = new Date();
        this.useCount = 1;
        this.isFavorite = false;
    }
    
    public long getId() {
        return id;
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(String searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUsedDate() {
        return lastUsedDate;
    }

    public void setLastUsedDate(Date lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }
    
    public void incrementUseCount() {
        this.useCount++;
        this.lastUsedDate = new Date();
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    
    @Override
    public String toString() {
        return "SearchHistory{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", searchType='" + searchType + '\'' +
                ", createdDate=" + createdDate +
                ", lastUsedDate=" + lastUsedDate +
                ", useCount=" + useCount +
                ", isFavorite=" + isFavorite +
                '}';
    }
}