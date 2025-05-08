package com.smartbank.service.search;

import java.util.List;

/**
 * Generic class to represent paginated search results.
 * @param <T> The type of objects in the search results.
 */
public class SearchResult<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    
    /**
     * Constructor for SearchResult.
     * @param content The content items in the current page
     * @param page The current page number (0-based)
     * @param size The page size
     * @param totalElements The total number of elements
     */
    public SearchResult(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / (double) size);
    }
    
    /**
     * Get the content items in the current page.
     * @return A list of content items
     */
    public List<T> getContent() {
        return content;
    }
    
    /**
     * Get the current page number (0-based).
     * @return The current page number
     */
    public int getPage() {
        return page;
    }
    
    /**
     * Get the page size.
     * @return The page size
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Get the total number of elements.
     * @return The total number of elements
     */
    public long getTotalElements() {
        return totalElements;
    }
    
    /**
     * Get the total number of pages.
     * @return The total number of pages
     */
    public int getTotalPages() {
        return totalPages;
    }
    
    /**
     * Check if this is the first page.
     * @return true if this is the first page, false otherwise
     */
    public boolean isFirst() {
        return page == 0;
    }
    
    /**
     * Check if this is the last page.
     * @return true if this is the last page, false otherwise
     */
    public boolean isLast() {
        return page == getTotalPages() - 1;
    }
    
    /**
     * Check if there is a previous page.
     * @return true if there is a previous page, false otherwise
     */
    public boolean hasPrevious() {
        return page > 0;
    }
    
    /**
     * Check if there is a next page.
     * @return true if there is a next page, false otherwise
     */
    public boolean hasNext() {
        return page < getTotalPages() - 1;
    }
    
    /**
     * Get the next page number.
     * @return The next page number or the current page number if there is no next page
     */
    public int getNextPage() {
        return hasNext() ? page + 1 : page;
    }
    
    /**
     * Get the previous page number.
     * @return The previous page number or the current page number if there is no previous page
     */
    public int getPreviousPage() {
        return hasPrevious() ? page - 1 : page;
    }
}