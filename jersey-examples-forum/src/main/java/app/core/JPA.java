package app.core;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.Persistence;

import java.math.BigInteger;
import java.util.List;

import com.google.common.collect.ImmutableList;

import app.core.Pagination;

public class JPA {
    public static final EntityManagerFactory ef =
            Persistence.createEntityManagerFactory("default");

    public static EntityManagerFactory ef() {
        return ef;
    }

    @SuppressWarnings("unchecked")
    public static <T> Pagination<T> paginate(EntityManager em, int offset, int limit,
            Query countQuery, Query selectQuery, Class<T> type) {
        if (offset < 0)
            throw new IllegalArgumentException(
                    "The parameter offset must be greater than or equal to 0");
        if (limit <= 0)
            throw new IllegalArgumentException(
                    "The parameter limit must be more than 0");
        Object o = countQuery.getSingleResult();
        Long count = 0L;
        if (o instanceof BigInteger)
            count = ((BigInteger) o).longValue();
        else if (o instanceof Long)
            count = (Long) o;
        else
            throw new IllegalArgumentException("");

        int first = offset;
        if (first >= count) {
            if (count > 0)
                first = (int) ((count - 1) / limit);
            else
                first = 0;
        }
        List<T> results;
        if (count > 0) {
            results = selectQuery.setFirstResult(first).setMaxResults(limit)
                    .getResultList();
        } else {
            results = ImmutableList.<T> of();
        }
        return new Pagination<T>(results, first, limit, count);
    }
}
