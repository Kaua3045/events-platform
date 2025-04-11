package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.users.User;

public interface UserRepository {

    boolean existsByEmail(String email);

    User save(User user);
}
