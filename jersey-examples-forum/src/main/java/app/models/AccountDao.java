package app.models;

import com.google.common.base.Optional;

public interface AccountDao {
    Optional<Account> find(long id);

    Optional<Account> findByEmail(String email);

    Account save(Account account);
}
