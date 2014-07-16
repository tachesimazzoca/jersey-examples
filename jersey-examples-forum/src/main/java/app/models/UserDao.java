package app.models;

import com.google.common.base.Optional;

public interface UserDao {
    Optional<User> find(long id);

    Optional<User> findByEmail(String email);

    User save(User user);
}
