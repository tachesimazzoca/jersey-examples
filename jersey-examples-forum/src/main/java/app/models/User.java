package app.models;

import com.google.common.base.Optional;

public class User {
    private final Optional<Account> account;

    public User() {
        account = Optional.absent();
    }

    public User(Account account) {
        this.account = Optional.of(account);
    }

    public Optional<Account> getAccount() {
        return account;
    }
}
