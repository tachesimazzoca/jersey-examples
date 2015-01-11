package app.core.util;

import java.util.List;

public class Pagination<T> {
    private final List<T> results;
    private final int offset;
    private final int limit;
    private final long count;

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
