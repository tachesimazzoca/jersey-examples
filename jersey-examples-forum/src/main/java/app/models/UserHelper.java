package app.models;

import app.core.session.StorageSession;
import com.google.common.base.Optional;

import javax.ws.rs.core.NewCookie;
import java.util.HashMap;
import java.util.Map;

public class UserHelper {
    private static final String KEY_ATTRIBUTES = "attributes";
    private static final String KEY_FLASH = "flash";
    private static final String KEY_ACCOUNT_ID = "accountId";
    private final StorageSession session;
    private final AccountDao accountDao;

    public UserHelper(StorageSession session, AccountDao accountDao) {
        this.session = session;
        this.accountDao = accountDao;
    }

    public NewCookie toCookie() {
        return session.toCookie();
    }

    public Optional<String> getFlash() {
        return session.remove(KEY_FLASH, String.class);
    }

    public void setFlash(String value) {
        session.put(KEY_FLASH, value);
    }

    public Optional<?> getAttribute(String key) {
        return getAttribute(key, Object.class);
    }

    public <T> Optional<T> getAttribute(String key, Class<T> type) {
        @SuppressWarnings("unchecked")
        Map<String, T> attributes = (Map<String, T>) session.get(
                KEY_ATTRIBUTES, Map.class).orNull();
        if (attributes != null && attributes.containsKey(key))
            return Optional.of((T) attributes.get(key));
        else
            return Optional.absent();
    }

    public void setAttribute(String key, Object value) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) session.get(
                KEY_ATTRIBUTES, Map.class).orNull();
        if (attributes == null)
            attributes = new HashMap<String, Object>();
        attributes.put(key, value);
        session.put(KEY_ATTRIBUTES, attributes);
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
