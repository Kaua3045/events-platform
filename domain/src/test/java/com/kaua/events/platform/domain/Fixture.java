package com.kaua.events.platform.domain;

import com.kaua.events.platform.domain.users.*;
import net.datafaker.Faker;

public final class Fixture {

    private static final Faker faker = new Faker();

    private Fixture() {}


    public static final class UserFixture {
        private UserFixture() {}

        public static User newUser() {
            return User.newUser(
                    new Name(faker.name().firstName(), faker.name().lastName()),
                    new Email(faker.internet().emailAddress()),
                    Password.of("12345678Am*"),
                    UserRole.USER
            );
        }
    }
}
