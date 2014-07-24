package app.core;

import java.util.List;

public class Pagination<T> {
    private List<T> results;
    private int offset;
    private int limit;
    private long count;

    public Pagination(List<T> results, int offset, int limit, long count) {
        this.results = results;
        this.offset = offset;
        this.limit = limit;
        this.count = count;
    }

    public List<T> getResults() {
        return results;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public long getCount() {
        return count;
    }
}
