package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.users.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> userOfEmail(String email);

    Optional<User> userOfId(String id);

    boolean existsByEmail(String email);

    boolean existsById(String id);

    User save(User user);
}
