package app.models;

import javax.ws.rs.core.NewCookie;

import com.google.common.base.Optional;

import app.core.Session;

public class UserContext {
    private static final String KEY_FLASH = "flash";
    private static final String KEY_ACCOUNT_ID = "accountId";
    private final Session session;
    private final AccountDao accountDao;

    public UserContext(Session session, AccountDao accountDao) {
        this.session = session;
        this.accountDao = accountDao;
    }

    public NewCookie toCookie() {
        return session.toCookie();
    }

    public Optional<String> getFlash() {
        return session.remove(KEY_FLASH);
    }

    public void setFlash(String value) {
        session.put(KEY_FLASH, value);
    }

    public Optional<Account> getAccount() {
        Optional<String> accountId = session.get(KEY_ACCOUNT_ID);
        if (!accountId.isPresent())
            return Optional.absent();
        return accountDao.find(Long.parseLong(accountId.get()));
    }

    public Optional<Account> authenticate(String email, String password) {
        Optional<Account> accountOpt = accountDao.findByEmail(email);
        if (accountOpt.isPresent() && accountOpt.get().isEqualPassword(password)) {
            session.put(KEY_ACCOUNT_ID, accountOpt.get().getId().toString());
        } else {
            accountOpt = Optional.absent();
        }
        return accountOpt;
    }

    public void logout() {
        session.remove(KEY_ACCOUNT_ID);
    }
}
