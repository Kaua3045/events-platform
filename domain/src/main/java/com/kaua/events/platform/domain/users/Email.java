package com.kaua.events.platform.domain.users;

import com.kaua.events.platform.domain.ValueObject;

import java.util.regex.Pattern;

public record Email(String value) implements ValueObject {

    private static final String EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+){0,5}\\.[a-zA-Z]{2,5}$")
            .pattern();

    public Email {
        this.assertArgumentNotEmpty(value, "email", "should not be empty");
        this.assertArgumentPattern(value, EMAIL_PATTERN, "email", "should be a valid email address");
    }
}
