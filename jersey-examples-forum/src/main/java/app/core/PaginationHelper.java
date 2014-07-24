package app.core;

import java.util.List;
import java.util.NoSuchElementException;

public class PaginationHelper<T> {
    private final Pagination<T> pagination;
    private final String offsetUrlFormat;

    public PaginationHelper(Pagination<T> pagination, String offsetUrlFormat) {
        this.pagination = pagination;
        this.offsetUrlFormat = offsetUrlFormat;
    }

    public List<T> getResults() {
        return pagination.getResults();
    }

    public int getOffset() {
        return pagination.getOffset();
    }

    public int getLimit() {
        return pagination.getLimit();
    }

    public long getCount() {
        return pagination.getCount();
    }

    public boolean hasPreviousOffset() {
        return (getOffset() - getLimit() >= 0);
    }

    public boolean hasNextOffset() {
        return (getCount() > getOffset() + getLimit());
    }

    public String getPreviousOffsetUrl() {
        int limit = getLimit();
        int offset = getOffset() - limit;
        if (offset < 0)
            throw new NoSuchElementException();
        return String.format(offsetUrlFormat, offset, limit);
    }

    public String getNextOffsetUrl() {
        int limit = getLimit();
        int offset = getOffset() + limit;
        if (offset >= getCount())
            throw new NoSuchElementException();
        return String.format(offsetUrlFormat, offset, limit);
    }
}
